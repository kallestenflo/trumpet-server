package com.jayway.trumpet.server;

import com.jayway.fixture.ServerRunningRule;
import com.jayway.fixture.TrumpetTestClient;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

public class TrumpetIntegrationTest {

    @ClassRule
    public static ServerRunningRule server = new ServerRunningRule();

    private static final String MESSAGE = "Ho ho";

    @Test
    public void a_trumpeter_receives_messages_when_in_range() {

        TrumpetTestClient sender = new TrumpetTestClient(server.port(), 55.583985D, 12.957578D);
        TrumpetTestClient inRange1 = new TrumpetTestClient(server.port(), 55.584126D, 12.957406D);
        TrumpetTestClient inRange2 = new TrumpetTestClient(server.port(), 55.584126D, 12.957406D);
        TrumpetTestClient outOfRange1 = new TrumpetTestClient(server.port(), 55.581212D, 12.959208D);

        sender.trumpet(MESSAGE);

        await().until(() -> !inRange1.messages().isEmpty());
        await().until(() -> !inRange2.messages().isEmpty());

        assertThat(inRange1.messages()).containsExactly(MESSAGE);
        assertThat(inRange2.messages()).containsExactly(MESSAGE);
        assertThat(outOfRange1.messages().isEmpty()).isTrue();
    }

}
