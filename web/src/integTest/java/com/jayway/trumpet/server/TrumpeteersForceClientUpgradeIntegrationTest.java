package com.jayway.trumpet.server;

import com.jayway.fixture.ServerRunningRule;
import com.jayway.restassured.RestAssured;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.fixture.ServerRunningRule.local;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.trumpet.server.boot.ForceClientUpgradeConfig.ANDROID_MIN_VERSION;
import static com.jayway.trumpet.server.boot.ForceClientUpgradeConfig.FORCE_CLIENT_UPGRADE_ENABLED;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class TrumpeteersForceClientUpgradeIntegrationTest {

    @SuppressWarnings("unchecked")
    @ClassRule
    public static ServerRunningRule server = local(Pair.of(FORCE_CLIENT_UPGRADE_ENABLED, Boolean.TRUE.toString()), Pair.of(ANDROID_MIN_VERSION, "1.2.7"));

    @Before public void
    clear_trumpeteer_repository_before_each_test() {
        server.trumpeteerRepository().clear();
    }

    @Before public void
    given_rest_assured_port_is_configured_correctly() {
        RestAssured.port = server.port();
        RestAssured.basePath = "/api";
    }

    @After public void
    rest_assured_is_reset_after_each_test() {
        RestAssured.reset();
    }

    @Test public void
    returns_json_error_when_version_is_lower_than_min_required() {
        // Given
        given().
                header("X-CLIENT-ID", "com.jayway.elefant").
                header("X-CLIENT-VERSION", "1.2.4").
                header("X-CLIENT-PLATFORM", "android").
        when().
                get().
        then().
                statusCode(403).
                body("minRequiredVersion", equalTo("1.2.7")).
                body("upgradeRequired", is(true));
    }

    @Test public void
    returns_json_error_neither_client_id_or_version_or_platform_is_undefined() {
        when().
                get().
        then().
                statusCode(403).
                body("minRequiredVersion", equalTo("1.2.7")).
                body("upgradeRequired", is(true));
    }

    @Test public void
    returns_json_error_when_version_is_undefined_and_min_is_required() {
        // Given
        given().
                header("X-CLIENT-ID", "com.jayway.elefant").
                header("X-CLIENT-PLATFORM", "android").
        when().
                get().
        then().
                statusCode(403).
                body("minRequiredVersion", equalTo("1.2.7")).
                body("upgradeRequired", is(true));
    }

    @Test public void
    returns_json_error_when_version_is_malformed_and_min_is_required() {
        // Given
        given().
                header("X-CLIENT-ID", "com.jayway.elefant").
                header("X-CLIENT-VERSION", "test-00203-joms").
                header("X-CLIENT-PLATFORM", "android").
        when().
                get().
        then().
                statusCode(403).
                body("minRequiredVersion", equalTo("1.2.7")).
                body("upgradeRequired", is(true));
    }

    @Test public void
    returns_json_error_when_version_is_lower_than_min_version_and_client_platform_is_undefined() {
        // Given
        given().
                header("X-CLIENT-ID", "com.jayway.elefant").
                header("X-CLIENT-VERSION", "0.4.2").
        when().
                get().
        then().
                statusCode(403).
                body("minRequiredVersion", equalTo("1.2.7")).
                body("upgradeRequired", is(true));
    }

    @Test public void
    returns_json_error_when_version_is_malformed_and_starts_with_number_and_min_is_required() {
        // Given
        given().
                header("X-CLIENT-ID", "com.jayway.elefant").
                header("X-CLIENT-VERSION", "2134").
                header("X-CLIENT-PLATFORM", "android").
        when().
                get().
        then().
                statusCode(403).
                body("minRequiredVersion", equalTo("1.2.7")).
                body("upgradeRequired", is(true));
    }

    @Test public void
    returns_ok_when_version_is_greater_than_min_required() {
        // Given
        given().
                header("X-CLIENT-ID", "com.jayway.elefant").
                header("X-CLIENT-VERSION", "1.2.9").
                header("X-CLIENT-PLATFORM", "android").
        when().
                get().
        then().
                statusCode(200);
    }

    @Test public void
    returns_ok_when_version_is_equal_to_min_required() {
        // Given
        given().
                header("X-CLIENT-ID", "com.jayway.elefant").
                header("X-CLIENT-VERSION", "1.2.7").
                header("X-CLIENT-PLATFORM", "android").
        when().
                get().
        then().
                statusCode(200);
    }
}
