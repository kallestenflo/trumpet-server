package com.jayway.trumpet.server.domain.trumpeteer;

import com.jayway.trumpet.server.domain.location.Location;
import com.jayway.trumpet.server.domain.subscriber.SubscriberOutput;
import com.jayway.trumpet.server.domain.subscriber.Trumpeteer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class TrumpetTest {

    @Test public void
    sent_by_me_is_false_when_sender_and_receiver_is_not_the_same() {
        // Given
        Trumpeteer sender = new TrumpeteerImpl("id1", "linkId", Location.location(22.3d, 23.2d, 10), mock(SubscriberOutput.class));
        Trumpeteer receiver = new TrumpeteerImpl("id2", "linkId", Location.location(22.3d, 23.5d, 10), mock(SubscriberOutput.class));

        // When
        Trumpet trumpet = Trumpet.create(sender, receiver, "trumpetId", "message", "topic", 22, 213);

        // Then
        assertThat(trumpet.sentByMe).isFalse();
    }

    @Test public void
    sent_by_me_is_true_when_sender_and_receiver_is_same() {
        // Given
        Trumpeteer sender = new TrumpeteerImpl("id1", "linkId", Location.location(22.3d, 23.2d, 10), mock(SubscriberOutput.class));
        Trumpeteer receiver = new TrumpeteerImpl("id1", "linkId", Location.location(22.3d, 23.5d, 10), mock(SubscriberOutput.class));

        // When
        Trumpet trumpet = Trumpet.create(sender, receiver, "trumpetId", "message", "topic", 22, 213);

        // Then
        assertThat(trumpet.sentByMe).isTrue();
    }
}