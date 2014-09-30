package com.jayway.trumpet.server.domain.subscriber;

import com.jayway.trumpet.server.domain.location.Location;
import com.jayway.trumpet.server.domain.trumpeteer.Trumpet;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Trumpeteer {

    String id();

    String linkId();

    void trumpet(String trumpetId,
                 String message,
                 String topic,
                 Optional<Integer> distanceInMeters,
                 Stream<Trumpeteer> trumpeteerSupplier,
                 Consumer<Trumpet> trumpetBroadcaster);

    Trumpeteer updateLocation(Location newLocation);

    boolean inRange(Trumpeteer other, long maxDistance);

    SubscriberOutput output();

    Location location();

    int maxTrumpetDistance();
}
