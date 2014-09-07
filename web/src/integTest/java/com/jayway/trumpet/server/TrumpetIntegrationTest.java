package com.jayway.trumpet.server;

import com.jayway.fixture.ServerRunningRule;
import com.jayway.fixture.TrumpetClient;
import com.jayway.fixture.TrumpetClientException;
import com.jayway.jsonpath.JsonPath;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.fixture.ThrowableExpecter.expect;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

public class TrumpetIntegrationTest {

    @ClassRule
    public static ServerRunningRule server = new ServerRunningRule();
    //public static ServerRunningRule server = ServerRunningRule.remote("elefant-server.herokuapp.com", 80);

    private static final String MESSAGE = "Ho ho";


    @Test
    public void a_trumpeteer_receives_messages_when_in_range() {

        System.out.println(server.host());

        TrumpetClient sender = TrumpetClient.create(server.host(), server.port()).connect(55.583985D, 12.957578D);
        TrumpetClient inRange1 = TrumpetClient.create(server.host(), server.port()).connect(55.584126D, 12.957406D);
        TrumpetClient inRange2 = TrumpetClient.create(server.host(), server.port()).connect(55.584126D, 12.957406D);
        TrumpetClient outOfRange1 = TrumpetClient.create(server.host(), server.port()).connect(55.581212D, 12.959208D);

        sender.trumpet(MESSAGE);

        await().until(() -> !inRange1.messages().isEmpty());
        await().until(() -> !inRange2.messages().isEmpty());

        assertThat(JsonPath.<List<String>>read(inRange1.messages(), "[*].message")).containsExactly(MESSAGE);
        assertThat(JsonPath.<List<String>>read(inRange2.messages(), "[*].message")).containsExactly(MESSAGE);
        assertThat(outOfRange1.messages().isEmpty()).isTrue();
    }

    @Test
    public void a_trumpet_message_can_not_be_null_or_empty() {
        TrumpetClient sender = TrumpetClient.create(server.host(), server.port()).connect(55.583985D, 12.957578D);

        TrumpetClientException exception = expect(TrumpetClientException.class).when(() -> sender.trumpet(""));

        assertThat(exception.response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void number_of_trumpeteers_in_range_is_returned_when_location_is_updated() {
        TrumpetClient trumpeteer = TrumpetClient.create(server.host(), server.port()).connect(55.583985D, 12.957578D);

        long inRangeBefore = trumpeteer.updateLocation(55.583985D, 12.957578D);

        TrumpetClient.create(server.host(), server.port()).connect(55.583985D, 12.957578D);

        long inRangeAfter = trumpeteer.updateLocation(55.583985D, 12.957578D);

        assertThat(inRangeAfter - inRangeBefore).isEqualTo(1);
    }

}
