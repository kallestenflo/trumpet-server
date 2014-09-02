package com.jayway.trumpet.server.domain;

import com.jayway.trumpet.server.boot.TrumpetServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TrumpeterRepository {

    private static final long SECOND = 1000;

    private static final Logger logger = LoggerFactory.getLogger(TrumpeterRepository.class);

    private final Map<String, Trumpeter> trumpeters = new ConcurrentHashMap<>();

    private final Supplier<String> idSupplier = new Supplier<String>() {
        AtomicLong al = new AtomicLong();
        @Override
        public String get() {
            return String.valueOf(al.incrementAndGet());
        }
    };

    private final Timer purgeStaleTrumpetersTimer = new Timer(true);

    public TrumpeterRepository(TrumpetServerConfig config) {

        TimerTask purgeTask = new TimerTask() {
            @Override
            public void run() {
                logger.debug("Purging stale trumpeters...");
                trumpeters.forEach((key, trumpeter) -> {
                    if(trumpeter.isStale(config.trumpeterStaleThreshold())){
                        logger.debug("Found stale trumpeter to purge {}", trumpeter.id);
                        trumpeter.close();
                    }
                });
            }
        };

        purgeStaleTrumpetersTimer.schedule (purgeTask, config.trumpeterPurgeInterval(), config.trumpeterPurgeInterval());
    }


    public Trumpeter createTrumpeter(Double latitude,
                                     Double longitude){

        Trumpeter trumpeter = new Trumpeter(idSupplier.get(), Location.create(latitude, longitude), t -> trumpeters.remove(t.id));
        trumpeters.put(trumpeter.id, trumpeter);
        logger.debug("Trumpeter with id {} created.", trumpeter.id);
        return trumpeter;
    }

    public Optional<Trumpeter> findById(String id){
        Optional<Trumpeter> trumpeter = Optional.ofNullable(trumpeters.get(id));

        trumpeter.ifPresent(t -> t.updateLastAccessed());

        return trumpeter;
    }

    public Stream<Trumpeter> findTrumpetersInRangeOf(Trumpeter trumpeter, long maxDistance){

        return trumpeters.values().stream().filter(t -> t.inRange(trumpeter, maxDistance));
    }

    public Stream<Tuple<Trumpeter, Long>> findTrumpetersWithDistanceInRangeOf(Trumpeter trumpeter, long maxDistance){

        return trumpeters.values().stream().filter(t -> t.inRange(trumpeter, maxDistance)).map(t -> {

            long distance = trumpeter.distanceTo(t, DistanceUnit.METERS).longValue();

            return Tuple.create(t, distance);
        });
    }


}
