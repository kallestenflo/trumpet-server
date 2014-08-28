package com.jayway.trumpet.server;

import com.jayway.fixture.ServerRunningRule;
import com.jayway.fixture.Trumpet;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

public class TrumpetIntegrationTest {

    @ClassRule
    public static ServerRunningRule server = new ServerRunningRule();

    @Test
    public void a_trumpeter_receives_messages_when_in_range() {

        Trumpet sender = new Trumpet(server.port(), 55.583985D, 12.957578D);
        Trumpet receiver1 = new Trumpet(server.port(), 55.584126D, 12.957406D);
        Trumpet receiver2 = new Trumpet(server.port(), 55.584126D, 12.957406D);
        Trumpet receiver3 = new Trumpet(server.port(), 55.581212D, 12.959208D);

        sender.trumpet("how how");

        await().until(() -> !receiver1.messages().isEmpty());
        await().until(() -> !receiver2.messages().isEmpty());


        assertThat(receiver1.messages()).containsExactly("how how");
        assertThat(receiver2.messages()).containsExactly("how how");
        assertThat(receiver3.messages().isEmpty()).isTrue();
    }

}
