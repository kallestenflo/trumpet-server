package com.jayway.trumpet.server.domain;

import com.jayway.trumpet.server.boot.TrumpetDomainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.jayway.trumpet.server.domain.Tuple.tuple;

public class TrumpeteerRepository {

    private static final Logger logger = LoggerFactory.getLogger(TrumpeteerRepository.class);

    private final Map<String, Trumpeteer> trumpeteers = new ConcurrentHashMap<>();

    private final Supplier<String> trumpeteerIdSupplier = new Supplier<String>() {
        AtomicLong sequence = new AtomicLong();
        @Override
        public String get() {
            return String.valueOf(sequence.incrementAndGet());
        }
    };

    private final Timer purgeStaleTrumpeteersTimer = new Timer(true);

    public TrumpeteerRepository(TrumpetDomainConfig config) {

        TimerTask purgeTask = new TimerTask() {
            @Override
            public void run() {
                logger.debug("Purging stale trumpeteers...");
                trumpeteers.forEach((key, trumpeteer) -> {
                    if (trumpeteer.isStale(config.trumpeteerStaleThreshold())) {
                        logger.debug("Found stale trumpeteer to purge {}", trumpeteer.id);
                        trumpeteer.close();
                    }
                });
            }
        };

        purgeStaleTrumpeteersTimer.schedule(purgeTask, config.trumpeteerPurgeInterval(), config.trumpeteerPurgeInterval());
    }


    public Trumpeteer createTrumpeteer(Double latitude,
                                       Double longitude){

        Trumpeteer trumpeteer = new Trumpeteer(trumpeteerIdSupplier.get(), Location.create(latitude, longitude), t -> trumpeteers.remove(t.id));
        trumpeteers.put(trumpeteer.id, trumpeteer);
        logger.debug("Trumpeteer with id {} created.", trumpeteer.id);
        return trumpeteer;
    }

    public Optional<Trumpeteer> findById(String id){
        Optional<Trumpeteer> trumpeteer = Optional.ofNullable(trumpeteers.get(id));

        trumpeteer.ifPresent(t -> t.updateLastAccessed());

        return trumpeteer;
    }

    public Stream<Trumpeteer> findTrumpeteersInRangeOf(Trumpeteer trumpeteer, long maxDistance){

        return trumpeteers.values().stream().filter(t -> t.inRange(trumpeteer, maxDistance));
    }

    public Stream<Tuple<Trumpeteer, Long>> findTrumpeteersWithDistanceInRangeOf(Trumpeteer trumpeteer, long maxDistance){
        /*
        return trumpeteers.values().stream().filter(t -> t.inRange(trumpeteer, maxDistance)).map(t -> {

            long distance = trumpeteer.distanceTo(t, DistanceUnit.METERS).longValue();

            return Tuple.tuple(t, distance);
        });
        */


        return trumpeteers.values().stream()
                .map(t -> tuple(t, trumpeteer.distanceTo(t, DistanceUnit.METERS).longValue()))
                .filter(tuple -> tuple.right.longValue() <= maxDistance);
    }


}
