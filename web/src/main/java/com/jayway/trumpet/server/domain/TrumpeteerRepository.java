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
                trumpeteers.forEach((key, trumpeteer) -> {
                    if (trumpeteer.isStale(config.trumpeteerStaleThreshold())) {
                        logger.debug("Purge trumpeteer {}", trumpeteer.id);
                        trumpeteer.close();
                    }
                });
                logger.debug("Purging stale trumpeteers...");
            }
        };
        purgeStaleTrumpeteersTimer.schedule(purgeTask, config.trumpeteerPurgeInterval(), config.trumpeteerPurgeInterval());
    }


    public Trumpeteer createTrumpeteer(Double latitude,
                                       Double longitude){

        Trumpeteer trumpeteer = new Trumpeteer(trumpeteerIdSupplier.get(), Location.create(latitude, longitude), t -> trumpeteers.remove(t.id));
        trumpeteers.put(trumpeteer.id, trumpeteer);
        logger.debug("Trumpeteer {} created.", trumpeteer.id);
        return trumpeteer;
    }

    public Optional<Trumpeteer> findById(String id){
        Optional<Trumpeteer> trumpeteer = Optional.ofNullable(trumpeteers.get(id));

        trumpeteer.ifPresent(t -> t.updateLastAccessed());

        return trumpeteer;
    }

    public long countTrumpeteersInRangeOf(Trumpeteer trumpeteer, long maxDistance){
        long inRange = trumpeteers.values().stream().filter(t -> t.inRange(trumpeteer, maxDistance)).count() - 1;

        logger.debug("There are {} trumpeteer(s) in range of trumpeteer {}", inRange, trumpeteer.id);

        return inRange;
    }

    public Stream<Tuple<Trumpeteer, Long>> findTrumpeteersInRangeOf(Trumpeteer trumpeteer, long maxDistance){
        return trumpeteers.values().stream()
                .map(t -> tuple(t, trumpeteer.distanceTo(t, DistanceUnit.METERS).longValue()))
                .filter(tuple -> tuple.right.longValue() <= maxDistance);
    }

    public Stream<Trumpeteer> findAll(){
        return trumpeteers.values().stream();
    }


}
