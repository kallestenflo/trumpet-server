package com.jayway.trumpet.server.infrastructure.trumpeteer;

import com.google.common.collect.Sets;
import com.jayway.trumpet.server.domain.subscriber.Trumpeteer;
import com.jayway.trumpet.server.domain.subscriber.TrumpeteerRepository;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpetService;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpeteerConfig;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpeteerNotificationService;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrumpeteerNotificationServiceImpl implements TrumpeteerNotificationService {

    private final TrumpeteerRepository trumpeteerRepository;
    private final TrumpetService trumpetService;
    private final TrumpeteerConfig config;

    public TrumpeteerNotificationServiceImpl(TrumpeteerRepository trumpeteerRepository, TrumpetService trumpetService, TrumpeteerConfig config) {
        this.trumpeteerRepository = trumpeteerRepository;
        this.trumpetService = trumpetService;
        this.config = config;
    }

    @Override
    public void notifyTrumpeteersInRange(Trumpeteer trumpeteer, Stream<Trumpeteer> trumpeteersInRangeOfBeforeUpdate,
                                         Stream<Trumpeteer> trumpeteersInRangeOfAfterUpdate,
                                         boolean sendToOriginatingTrumpeteerEvenIfNoDiff) {
        Set<Trumpeteer> diff = diff(trumpeteersInRangeOfBeforeUpdate, trumpeteersInRangeOfAfterUpdate);
        final Stream<Trumpeteer> stream;
        if (diff.isEmpty() && !sendToOriginatingTrumpeteerEvenIfNoDiff) {
            stream = diff.stream();
        } else {
            stream = Stream.concat(Stream.of(trumpeteer), diff.stream());
        }
        stream.map(t -> Pair.of(t, trumpeteerRepository.countTrumpeteersInRangeOf(t, config.trumpeteerMaxDistance())))
                .forEach(p -> {
                    Trumpeteer t = p.getKey();
                    // Don't use requestedDistance since the receiving trumpeteer may have another requestedDistance than the trumpeteer that updates its location
                    trumpetService.notifyInRangeTo(t, p.getValue());
                });
    }

    static <T> Set<T> diff(final Stream<T> s1, final Stream<T> s2) {
        // TODO This is inefficient, see http://stackoverflow.com/questions/26547286/how-to-get-the-symmetric-difference-between-two-streams-in-java-8
        Set<T> set1 = s1.collect(Collectors.toSet());
        Set<T> set2 = s2.collect(Collectors.toSet());
        return Sets.symmetricDifference(set1, set2);
    }
}
