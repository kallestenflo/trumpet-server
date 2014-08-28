package com.jayway.trumpet.server.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class TrumpeterRepository {

    private static final Logger logger = LoggerFactory.getLogger(TrumpeterRepository.class);

    private Map<String, Trumpeter> trumpeters = new ConcurrentHashMap<>();

    private AtomicLong idGenerator = new AtomicLong();

    public Trumpeter createTrumpeter(Double latitude,
                                     Double longitude){

        Trumpeter trumpeter = new Trumpeter(Long.toString(idGenerator.incrementAndGet()), Location.create(latitude, longitude), this::delete);
        trumpeters.put(trumpeter.id, trumpeter);
        logger.debug("Trumpeter with id {} created.", trumpeter.id);
        return trumpeter;
    }

    public Optional<Trumpeter> getById(String id){
        return Optional.ofNullable(trumpeters.get(id));
    }

    public void delete(Trumpeter trumpeter){
        if(trumpeters.containsKey(trumpeter.id)){
            trumpeters.remove(trumpeter.id);
        }
    }

    public Stream<Trumpeter> findTrumpetersInRangeOf(Trumpeter trumpeter, int maxDistance){

        return trumpeters.values().stream().filter(c -> c.inRangeWithOutput(trumpeter, maxDistance));
    }

}
