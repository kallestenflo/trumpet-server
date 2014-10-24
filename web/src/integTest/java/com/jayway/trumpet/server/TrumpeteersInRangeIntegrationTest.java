package com.jayway.trumpet.server;

import com.jayway.fixture.ServerRunningRule;
import com.jayway.fixture.TrumpetClient;
import com.jayway.fixture.TrumpetMessage;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.fixture.ServerRunningRule.local;
import static org.assertj.core.api.Assertions.assertThat;

public class TrumpeteersInRangeIntegrationTest {

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

    private int trumpeteersDiscoveredInLastMessageTo(TrumpetClient client) {
        List<TrumpetMessage> messages = client.messages();
        if (messages.isEmpty()) {
            return -1;
        }
        return Integer.parseInt(messages.get(messages.size() - 1).getExt().get("trumpeteersInRange"));
    }


    private TrumpetClient createClient() {
        return TrumpetClient.create(server.host(), server.port());
    }
}
