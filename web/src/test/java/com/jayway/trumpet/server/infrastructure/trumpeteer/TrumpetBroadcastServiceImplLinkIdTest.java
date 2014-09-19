package com.jayway.trumpet.server.infrastructure.trumpeteer;

import com.jayway.trumpet.server.boot.TrumpetDomainConfig;
import com.jayway.trumpet.server.domain.subscriber.Subscriber;
import com.jayway.trumpet.server.infrastructure.event.GuavaTrumpetEventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class TrumpetBroadcastServiceImplLinkIdTest {

    @Mock
    TrumpetDomainConfig trumpetDomainConfig;
    private TrumpetBroadcastServiceImpl tested;

    @Before public void
    given_trumpet_broadcast_service_is_created() {
        given(trumpetDomainConfig.trumpeteerStaleThreshold()).willReturn(600000L);
        given(trumpetDomainConfig.trumpeteerPurgeInterval()).willReturn(600000L);

        tested = new TrumpetBroadcastServiceImpl(new GuavaTrumpetEventBus(), trumpetDomainConfig);
    }

    @Test public void
    subscribing_with_unique_link_id_will_the_register_the_subscriber() {
        // Given
        Subscriber subscriber1 = givenSubscriber("id1", "linkId1");
        Subscriber subscriber2 = givenSubscriber("id2", "linkId2");
        tested.subscribe(subscriber1);

        // When
        tested.subscribe(subscriber2);

        // Then
        assertThat(tested.numberOfSubscribers()).isEqualTo(2);
    }

    @Test public void
    subscribing_with_link_id_already_used_by_another_subscriber_will_unsubscribe_the_other_subscriber() {
        // Given
        Subscriber subscriber1 = givenSubscriber("id1", "linkId1");
        Subscriber subscriber2 = givenSubscriber("id2", "linkId1");
        tested.subscribe(subscriber1);

        // When
        tested.subscribe(subscriber2);

        // Then
        assertThat(tested.numberOfSubscribers()).isEqualTo(1);
        assertThat(tested.findById("id1").isPresent()).isFalse();
        assertThat(tested.findById("id2").isPresent()).isTrue();
    }

    private Subscriber givenSubscriber(String id, String linkId) {
        return tested.create(id, linkId, null);
    }


}