package com.jayway.trumpet.server.domain.subscriber;

import com.jayway.trumpet.server.domain.location.Location;

import java.util.Optional;
import java.util.stream.Stream;

public interface TrumpeteerRepository {

    Trumpeteer create(String id, String linkId, Location location, SubscriberOutput output);

    Optional<Trumpeteer> findById(String id);

    Stream<Trumpeteer> findAll();

    int countTrumpeteersInRangeOf(Trumpeteer trumpeteer, int requestedDistance);

    void delete(String id);
}
