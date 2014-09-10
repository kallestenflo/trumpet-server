package com.jayway.trumpet.server.domain.trumpeteer;

import com.jayway.trumpet.server.domain.location.DistanceUnit;
import com.jayway.trumpet.server.domain.location.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Trumpeteer {

    private static final Logger logger = LoggerFactory.getLogger(Trumpeteer.class);


    public final String id;
    public Location location;


    public Trumpeteer(String id,
                      Location location) {

        requireNonNull(id, "Id can not be null.");
        requireNonNull(location, "Location can not be null.");

        this.id = id;
        this.location = location;
    }


    public Trumpeteer updateLocation(Location newLocation) {
        requireNonNull(location, "Location can not be null.");

        this.location = newLocation;
        logger.debug("Trumpeteer {} updated location to latitude: {}, longitude: {}", id, this.location.latitude, this.location.longitude);

        return this;
    }

    public void trumpet(String trumpetId,
                        String message,
                        Optional<Integer> distanceInMeters,
                        Stream<Trumpeteer> candidates,
                        Consumer<Trumpet> trumpetBroadcaster) {

        requireNonNull(message, "message can not be null.");
        requireNonNull(distanceInMeters, "distanceInMeters can not be null.");
        requireNonNull(candidates, "candidates can not be null.");
        requireNonNull(trumpetBroadcaster, "trumpetBroadcaster can not be null.");

        logger.debug("Trumpeteer {} trumpeted message {}", id, trumpetId);

        broadcast(trumpetId, message, distanceInMeters, candidates, trumpetBroadcaster);
    }

    public void echo(String trumpetId,
                     String message,
                     Optional<Integer> distanceInMeters,
                     Stream<Trumpeteer> candidates,
                     Consumer<Trumpet> trumpetBroadcaster) {

        requireNonNull(message, "message can not be null.");
        requireNonNull(distanceInMeters, "distanceInMeters can not be null.");
        requireNonNull(candidates, "candidates can not be null.");
        requireNonNull(trumpetBroadcaster, "trumpetBroadcaster can not be null.");

        logger.debug("Trumpeteer {} echoed message {}", id, trumpetId);

        broadcast(trumpetId, message, distanceInMeters, candidates, trumpetBroadcaster);
    }

    public boolean inRange(Trumpeteer other, long maxDistance) {

        requireNonNull(other, "Other trumpeteer can not be null.");
        requirePositive(maxDistance, "Distance must be greater than 0.");

        Double distanceInMeters = this.location.distanceTo(other.location, DistanceUnit.METERS);

        logger.debug("Distance between trumpeteer {} and trumpeteer {} is {} meters. Max distance is {}", id, other.id, distanceInMeters.intValue(), maxDistance);

        return distanceInMeters.longValue() <= maxDistance;
    }

    public Double distanceTo(Trumpeteer other, DistanceUnit distanceUnit) {
        return this.location.distanceTo(other.location, distanceUnit);
    }

    private void broadcast(String trumpetId, String message, Optional<Integer> distanceInMeters, Stream<Trumpeteer> candidates, Consumer<Trumpet> trumpetBroadcaster) {
        long timestamp = System.currentTimeMillis();
        int distance = Math.min(distanceInMeters.orElse(200), 200);

        candidates.map(t -> createTrumpet(t, trumpetId, timestamp, message))
                .filter(t -> t.distanceFromSource <= distance)
                .forEach(trumpetBroadcaster);
    }


    private Trumpet createTrumpet(Trumpeteer receiver, String id, long timestamp, String message) {
        return Trumpet.create(this, receiver, id, message, this.distanceTo(receiver, DistanceUnit.METERS).intValue(), timestamp);
    }

    private void requirePositive(long i, String message) {
        if (i < 1) {
            throw new IllegalArgumentException(message);
        }
    }
}
