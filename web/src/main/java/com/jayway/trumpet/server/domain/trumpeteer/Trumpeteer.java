package com.jayway.trumpet.server.domain.trumpeteer;

import com.jayway.trumpet.server.domain.Tuple;
import com.jayway.trumpet.server.domain.location.DistanceUnit;
import com.jayway.trumpet.server.domain.location.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Trumpeteer {

    private static final Logger logger = LoggerFactory.getLogger(Trumpeteer.class);


    public final String id;
    private final Consumer<Trumpeteer> closeHandler;
    public Location location;
    private long lastAccessed;


    public Trumpeteer(String id,
                      Location location,
                      Consumer<Trumpeteer> closeHandler) {

        requireNonNull(id, "Id can not be null.");
        requireNonNull(location, "Location can not be null.");
        requireNonNull(closeHandler, "Close handler can not be null.");

        this.closeHandler = closeHandler;
        this.id = id;
        this.location = location;
        updateLastAccessed();
    }


    void updateLastAccessed() {
        this.lastAccessed = System.currentTimeMillis();
    }

    public Trumpeteer updateLocation(Location newLocation) {
        requireNonNull(location, "Location can not be null.");

        this.location = newLocation;
        logger.debug("Trumpeteer {} updated location to latitude: {}, longitude: {}", id, this.location.latitude, this.location.longitude);

        return this;
    }

    public void trumpet(String message, Optional<Integer> distanceInMeters, Stream<Trumpeteer> candidates, Consumer<Tuple<String, Trumpet>> trumpetBroadcaster) {
        int distance = Math.min(distanceInMeters.orElse(200), 200);
        candidates.filter(t -> !t.id.equals(id))
                .map(t -> Tuple.tuple(t.id, Trumpet.create(UUID.randomUUID().toString(), message, t.distanceTo(this, DistanceUnit.METERS).longValue())))
                .filter(t -> t.right.distanceFromSource <= distance)
                .forEach(trumpetBroadcaster);
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

    private void requirePositive(long i, String message) {
        if (i < 1) {
            throw new IllegalArgumentException(message);
        }
    }
}
