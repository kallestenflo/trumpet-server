package com.jayway.trumpet.server.infrastructure.trumpeteer;

import com.jayway.jsonpath.JsonPath;
import com.jayway.trumpet.server.domain.location.Location;
import com.jayway.trumpet.server.domain.subscriber.SubscriberConfig;
import com.jayway.trumpet.server.domain.subscriber.SubscriberOutput;
import com.jayway.trumpet.server.domain.subscriber.Trumpeteer;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpeteerConfig;
import com.jayway.trumpet.server.infrastructure.event.GuavaTrumpetEventBus;
import com.jayway.trumpet.server.infrastructure.event.TrumpetEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TrumpetServiceImplNotificationTest {

    @Mock
    TrumpeteerConfig trumpeteerConfig;

    @Mock
    SubscriberConfig subscriberConfig;

    @Captor
    ArgumentCaptor<TrumpetEvent> trumpetEvent;

    @Mock
    GuavaTrumpetEventBus guavaTrumpetEventBus;

    private TrumpetServiceImpl tested;

    @Before public void
    given_trumpet_broadcast_service_is_created() {
        given(subscriberConfig.trumpeteerStaleThreshold()).willReturn(600000L);
        given(subscriberConfig.trumpeteerPurgeInterval()).willReturn(600000L);

        tested = new TrumpetServiceImpl(guavaTrumpetEventBus, subscriberConfig, trumpeteerConfig);
    }

    @Test public void
    subscribing_with_unique_link_id_will_the_register_the_subscriber() {
        // Given
        given(trumpeteerConfig.trumpeteerMaxDistance()).willReturn(200);

        Trumpeteer trumpeteer1 = tested.create("id1", "linkId1", Location.location(55.584126d, 12.957406d, 10), mock(SubscriberOutput.class));
        Trumpeteer trumpeteer2 = tested.create("id2", "linkId2", Location.location(55.584125d, 12.957405d, 10), mock(SubscriberOutput.class));
        Trumpeteer trumpeteer3 = tested.create("id3", "linkId3", Location.location(55.584127d, 12.957407d, 10), mock(SubscriberOutput.class));
        tested.subscribe(trumpeteer1);
        tested.subscribe(trumpeteer2);
        tested.subscribe(trumpeteer3);

        // When
        tested.delete(trumpeteer1.id());

        // Then
        verify(guavaTrumpetEventBus, atLeastOnce()).publish(trumpetEvent.capture());
        assertThat(tested.numberOfSubscribers()).isEqualTo(2);
        List<Object> numberOfTrumpeteersInRangeForEachTrumpeteer = trumpetEvent.getAllValues().stream().map(e -> JsonPath.read(e.payload, "$.message.ext.trumpeteersInRange")).collect(Collectors.toList());
        assertThat(numberOfTrumpeteersInRangeForEachTrumpeteer).containsOnly("1").hasSize(2);
    }
}