package com.jayway.trumpet.server.infrastructure.trumpeteer;

import com.jayway.trumpet.server.domain.subscriber.SubscriberConfig;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpeteerConfig;
import com.jayway.trumpet.server.domain.location.Location;
import com.jayway.trumpet.server.domain.subscriber.SubscriberOutput;
import com.jayway.trumpet.server.domain.subscriber.Trumpeteer;
import com.jayway.trumpet.server.infrastructure.event.GuavaTrumpetEventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class TrumpetBroadcastServiceImplLinkIdTest {

    @Mock
    TrumpeteerConfig trumpeteerConfig;

    @Mock
    SubscriberConfig subscriberConfig;


    private TrumpetBroadcastServiceImpl tested;

    @Before public void
    given_trumpet_broadcast_service_is_created() {
        given(subscriberConfig.trumpeteerStaleThreshold()).willReturn(600000L);
        given(subscriberConfig.trumpeteerPurgeInterval()).willReturn(600000L);

        tested = new TrumpetBroadcastServiceImpl(new GuavaTrumpetEventBus(), subscriberConfig, trumpeteerConfig);
    }

    @Test public void
    subscribing_with_unique_link_id_will_the_register_the_subscriber() {
        // Given
        Trumpeteer trumpeteer1 = givenSubscriber("id1", "linkId1");
        Trumpeteer trumpeteer2 = givenSubscriber("id2", "linkId2");
        tested.subscribe(trumpeteer1);

        // When
        tested.subscribe(trumpeteer2);

        // Then
        assertThat(tested.numberOfSubscribers()).isEqualTo(2);
    }

    @Test public void
    subscribing_with_link_id_already_used_by_another_subscriber_will_unsubscribe_the_other_subscriber() {
        // Given
        Trumpeteer trumpeteer1 = givenSubscriber("id1", "linkId1");
        Trumpeteer trumpeteer2 = givenSubscriber("id2", "linkId1");
        tested.subscribe(trumpeteer1);

        // When
        tested.subscribe(trumpeteer2);

        // Then
        assertThat(tested.numberOfSubscribers()).isEqualTo(1);
        assertThat(tested.findById("id1").isPresent()).isFalse();
        assertThat(tested.findById("id2").isPresent()).isTrue();
    }

    private Trumpeteer givenSubscriber(String id, String linkId) {
        return tested.create(id, linkId, Location.location(22.2d, 22.1d, 10), mock(SubscriberOutput.class));
    }


}