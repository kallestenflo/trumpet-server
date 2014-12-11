package com.jayway.trumpet.server;

import com.jayway.fixture.ServerRunningRule;
import com.jayway.fixture.TrumpetClient;
import com.jayway.fixture.TrumpetMessage;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Awaitility.with;
import static com.jayway.fixture.ServerRunningRule.local;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class TrumpeteersNotificationIntegrationTest {

    @ClassRule
    public static ServerRunningRule server = local();


    @Before public void
    clear_trumpeteer_repository_before_each_test() {
        server.trumpeteerRepository().clear();
    }

    @Test
    public void a_trumpeteer_receives_notification_of_how_many_trumpeteers_are_nearby_when_subscribing() {
        // Given
        TrumpetClient inRange1 = createClient().connect(55.584126D, 12.957406D);
        TrumpetClient inRange2 = createClient().connect(55.584125D, 12.957405D);
        TrumpetClient inRange3 = createClient().connect(55.584127D, 12.957407D);

        // When
        TrumpetClient sender = createClient().connect(55.583985D, 12.957578D);

        // Then
        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(sender)).isEqualTo(3));
        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(inRange1)).isEqualTo(3));
        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(inRange2)).isEqualTo(3));
        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(inRange3)).isEqualTo(3));
    }

    @Test
    public void a_trumpeteer_receives_notification_of_how_many_trumpeteers_are_nearby_when_updating_location() {
        // Given
        TrumpetClient inRange1 = createClient().connect(55.584126D, 12.957406D);
        TrumpetClient inRange2 = createClient().connect(55.584125D, 12.957405D);
        TrumpetClient inRange3 = createClient().connect(55.584127D, 12.957407D);
        TrumpetClient sender = createClient().connect(68.583985D, 26.957578D);

        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(sender)).isEqualTo(0));
        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(inRange1)).isEqualTo(2));
        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(inRange2)).isEqualTo(2));
        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(inRange3)).isEqualTo(2));

        // When
        sender.updateLocation(55.583985D, 12.957578D);

        // Then
        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(sender)).isEqualTo(3));
        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(inRange1)).isEqualTo(3));
        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(inRange2)).isEqualTo(3));
        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(inRange3)).isEqualTo(3));
    }

    @Test
    public void no_notifications_are_sent_to_any_trumpeteer_on_update_location_when_no_new_trumpeteers_are_found() {
        // Given
        TrumpetClient inRange1 = createClient().connect(55.584126D, 12.957406D);
        TrumpetClient inRange2 = createClient().connect(55.584125D, 12.957405D);
        TrumpetClient inRange3 = createClient().connect(55.584127D, 12.957407D);
        TrumpetClient sender = createClient().connect(55.583985D, 12.957578D);

        await().until(() -> assertThat(sender.messages()).hasSize(1));
        await().until(() -> assertThat(inRange1.messages()).hasSize(4));
        await().until(() -> assertThat(inRange2.messages()).hasSize(3));
        await().until(() -> assertThat(inRange3.messages()).hasSize(2));

        // When
        sender.updateLocation(55.583986D, 12.957579D); // Update to a location that is

        // Then
        with().pollDelay(500, MILLISECONDS). await().until(() -> assertThat(sender.messages()).hasSize(1)); // We wait 500 ms and make sure that no notification has been sent during this interval
        await().until(() -> assertThat(inRange1.messages()).hasSize(4));
        await().until(() -> assertThat(inRange2.messages()).hasSize(3));
        await().until(() -> assertThat(inRange3.messages()).hasSize(2));
    }

    @Test
    public void notification_is_sent_to_trumpeteer_when_subscribing_even_if_no_other_trumpeteers_are_around() {
        // When
        TrumpetClient sender = createClient().connect(55.583985D, 12.957578D);

        // Then
        await().until(() -> assertThat(trumpeteersDiscoveredInLastMessageTo(sender)).isEqualTo(0));
    }

    @Test
    public void notifications_is_sent_on_empty_channel() {
        // When
        TrumpetClient sender = createClient().connect(55.583985D, 12.957578D);

        // Then
        await().until(() -> assertThat(sender.messages()).extracting("channel").containsNull().hasSize(1));
    }

    private int trumpeteersDiscoveredInLastMessageTo(TrumpetClient client) {
        List<TrumpetMessage> messages = client.messages();
        if (messages.isEmpty()) {
            return -1;
        }
        return messages.get(messages.size() - 1).getTrumpeteersInRange();
    }


    private TrumpetClient createClient() {
        return TrumpetClient.create(server.host(), server.port());
    }
}
