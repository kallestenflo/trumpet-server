package com.jayway.trumpet.server.domain.trumpeteer;

import com.jayway.trumpet.server.boot.TrumpetDomainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.jayway.trumpet.server.domain.location.Location.location;
import static java.lang.Math.min;

public class TrumpeteerRepository {

    private static final Logger logger = LoggerFactory.getLogger(TrumpeteerRepository.class);

    private final Map<String, Trumpeteer> trumpeteers = new ConcurrentHashMap<>();
    private final TrumpetDomainConfig config;

    private final Supplier<String> trumpeteerIdSupplier = new Supplier<String>() {
        AtomicLong sequence = new AtomicLong();
        @Override
        public String get() {
            return String.valueOf(sequence.incrementAndGet());
        }
    };

    public TrumpeteerRepository(TrumpetDomainConfig config) {
        this.config = config;
    }


    public Trumpeteer createTrumpeteer(Double latitude,
                                       Double longitude,
                                       Integer accuracy){

        Trumpeteer trumpeteer = new Trumpeteer(trumpeteerIdSupplier.get(), location(latitude, longitude, accuracy));
        trumpeteers.put(trumpeteer.id, trumpeteer);
        logger.debug("Trumpeteer {} created.", trumpeteer.id);
        return trumpeteer;
    }

    public Optional<Trumpeteer> findById(String id){
        return Optional.ofNullable(trumpeteers.get(id));
    }

    public int countTrumpeteersInRangeOf(Trumpeteer trumpeteer, Integer maxDistance){

        Integer distance = min(maxDistance, config.trumpeteerMaxDistance());

        Long inRange = trumpeteers.values().stream().filter(t -> t.inRange(trumpeteer, distance)).count() - 1;

        logger.debug("There are {} trumpeteer(s) in range of trumpeteer {}", inRange, trumpeteer.id);

        return inRange.intValue();
    }



    public Stream<Trumpeteer> findAll(){
        return trumpeteers.values().stream();
    }


}
