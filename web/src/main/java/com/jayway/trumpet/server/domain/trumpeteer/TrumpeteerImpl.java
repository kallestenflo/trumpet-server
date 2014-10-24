package com.jayway.trumpet.server.domain.trumpeteer;

import com.jayway.trumpet.server.domain.location.DistanceUnit;
import com.jayway.trumpet.server.domain.location.Location;
import com.jayway.trumpet.server.domain.subscriber.SubscriberOutput;
import com.jayway.trumpet.server.domain.subscriber.Trumpeteer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class TrumpeteerImpl implements Trumpeteer {

    private static final Logger logger = LoggerFactory.getLogger(TrumpeteerImpl.class);
    private static final String DEFAULT_CHANNEL = "*";

    public final String id;
    public final String linkId;
    private final SubscriberOutput output;
    public Location location;

    public TrumpeteerImpl(String id, String linkId, Location location, SubscriberOutput output) {
        requireNonNull(linkId, "LinkId can not be null.");
        requireNonNull(location, "Location can not be null.");
        requireNonNull(output, "SubscriberOutput can not be null.");
        requireNonNull(id, "Id can not be null.");
        this.id = id;
        this.location = location;
        this.output = output;
        this.linkId = linkId;
    }


    @Override
    public String id() {
        return id;
    }

    @Override
    public String linkId() {
        return linkId;
    }

    @Override
    public Trumpeteer updateLocation(Location newLocation) {
        requireNonNull(location, "Location can not be null.");

        logger.debug("Trumpeteer {} updated location to latitude: {}, longitude: {}", id, this.location.latitude, this.location.longitude);

        return new TrumpeteerImpl(id, linkId, newLocation, output);
    }

    @Override
    public SubscriberOutput output() {
        return output;
    }

    @Override
    public Location location() {
        return location;
    }


    @Override
    public void trumpet(String trumpetId,
                        String message,
                        String topic,
                        Map<String, String> extParameters,
                        Optional<Integer> requestedDistance,
                        Consumer<Trumpet> trumpetBroadcaster) {

        requireNonNull(message, "message can not be null.");
        requireNonNull(topic, "topic can not be null.");
        requireNonNull(requestedDistance, "distanceInMeters can not be null.");
        requireNonNull(trumpetBroadcaster, "trumpetBroadcaster can not be null.");
        requireNonNull(extParameters, "extParameters can not be null.");

        logger.debug("Trumpeteer {} trumpeted message {}", id, trumpetId);

        broadcast(trumpetId, message, topic, extParameters, requestedDistance, trumpetBroadcaster);
    }

    @Override
    public boolean inRange(Trumpeteer other, long maxDistance) {
        requireNonNull(other, "Other trumpeteer can not be null.");
        requirePositive(maxDistance, "Distance must be greater than 0.");

        Double distanceInMeters = this.location.distanceTo(other.location(), DistanceUnit.METERS);

        logger.debug("Distance between trumpeteer {} and trumpeteer {} is {} meters. Max distance is {}", id, other.id(), distanceInMeters.intValue(), maxDistance);

        return distanceInMeters.longValue() <= maxDistance;
    }

    public Double distanceTo(Trumpeteer other, DistanceUnit distanceUnit) {
        return this.location.distanceTo(other.location(), distanceUnit);
    }

    private void broadcast(String trumpetId, String message, String topic, Map<String, String> extParameters, Optional<Integer> requestedDistance, Consumer<Trumpet> trumpetBroadcaster) {
        Trumpet trumpet = createTrumpet(trumpetId, System.currentTimeMillis(), message, topic, requestedDistance, extParameters);

        trumpetBroadcaster.accept(trumpet);
    }


    private Trumpet createTrumpet(String id, long timestamp, String message, String topic, Optional<Integer> distanceInMeters, Map<String, String> extParameters) {
        if (DEFAULT_CHANNEL.equals(topic)) {
            topic = null;
        }
        return Trumpet.create(this, id, message, topic, distanceInMeters , timestamp, extParameters);
    }

    private void requirePositive(long i, String message) {
        if (i < 1) {
            throw new IllegalArgumentException(message);
        }
    }
}
