package com.jayway.trumpet.server.domain;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class Trumpeteer {

    private static final Logger logger = LoggerFactory.getLogger(Trumpeteer.class);

    private static final Supplier<String> trumpetIdSupplier = new Supplier<String>() {
        AtomicLong sequence = new AtomicLong();
        @Override
        public String get() {
            return String.valueOf(sequence.incrementAndGet());
        }
    };

    public final String id;
    private final Consumer<Trumpeteer> closeHandler;
    private Location location;
    private TrumpeteerEventOutput output;
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
        this.output = createEventOutput();
        updateLastAccessed();
    }

    boolean isStale(long staleThreshold) {
        return ((lastAccessed + staleThreshold) < System.currentTimeMillis()) || output.isClosed();
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

    public void trumpet(String message, long distanceFromSource) {
        requireNonNull(message, "Message can not be null.");

        try {
            logger.debug("Pushing trumpet to trumpeteer : {}, latitude: {}, longitude: {}. Distance from source: {} meters", id, location.latitude, location.longitude, distanceFromSource);
            output.write(createTrumpet(message, distanceFromSource));
        } catch (IOException e) {
            close();
        }
    }

    public EventOutput subscribe() {
        if (output.isClosed()) {
            output = createEventOutput();
        }
        logger.debug("Trumpeteer {} subscription created.", id);
        return this.output;
    }

    private TrumpeteerEventOutput createEventOutput() {
        return new TrumpeteerEventOutput();
    }

    void close() {
        output.close();
    }


    public boolean inRange(Trumpeteer other, long maxDistance) {

        requireNonNull(other, "Other trumpeteer can not be null.");
        requirePositive(maxDistance, "Distance must be greater than 0.");

        Double distanceInMeters = this.location.distanceTo(other.location, DistanceUnit.METERS);

        logger.debug("Distance between trumpeteer {} and trumpeteer {} is {} meters. Max distance is {}", id, other.id, distanceInMeters.intValue(), maxDistance);

        return distanceInMeters.longValue() <= maxDistance;
    }

    public Double distanceTo(Trumpeteer other, DistanceUnit distanceUnit){
        return this.location.distanceTo(other.location, distanceUnit);
    }


    private OutboundEvent createTrumpet(String msg, long distanceFromSource) {
        return new OutboundEvent.Builder()
                .name("trumpet")
                .data(Trumpet.create(trumpetIdSupplier.get(), msg, distanceFromSource))
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    private void requirePositive(long i, String message) {
        if (i < 1) {
            throw new IllegalArgumentException(message);
        }
    }

    private class TrumpeteerEventOutput extends EventOutput {

        boolean closed = false;

        @Override
        public void close() {
            try {
                if(!closed){
                    closeHandler.accept(Trumpeteer.this);
                }
                super.close();
                closed = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
