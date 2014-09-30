package com.jayway.trumpet.server.domain.subscriber;

import com.jayway.trumpet.server.domain.location.DistanceUnit;
import com.jayway.trumpet.server.domain.location.Location;
import com.jayway.trumpet.server.domain.trumpeteer.Trumpet;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface Trumpeteer {

    String id();

    String linkId();

    void trumpet(String trumpetId,
                 String message,
                 String topic,
                 Map<String, String> extParameters,
                 Optional<Integer> requestedDistance,
                 Consumer<Trumpet> trumpetBroadcaster);

    Trumpeteer updateLocation(Location newLocation);

    boolean inRange(Trumpeteer other, long maxDistance);

    SubscriberOutput output();

    Location location();

    Double distanceTo(Trumpeteer other, DistanceUnit distanceUnit);
}
