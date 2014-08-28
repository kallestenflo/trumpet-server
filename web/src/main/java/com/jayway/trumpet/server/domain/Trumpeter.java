package com.jayway.trumpet.server.domain;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Consumer;

public class Trumpeter {

    private static final Logger logger = LoggerFactory.getLogger(Trumpeter.class);

    public final String id;
    private final Consumer<Trumpeter> closeHandler;
    private Location location;
    private EventOutput output;


    public Trumpeter(String id,
                     Location location,
                     Consumer<Trumpeter> closeHandler) {
        this.id = id;
        this.location = location;
        this.closeHandler = closeHandler;
    }

    public void updateLocation(Location newLocation) {
        this.location = newLocation;
        logger.debug("Trumpeter {} updated location to latitude: {}, longitude: {}", id, this.location.latitude, this.location.longitude);
    }

    public void trumpet(String msg){
        try {
            logger.debug("Pushing trumpet to trumpeter : {}, latitude: {}, longitude: {}", id, location.latitude, location.longitude);
            output.write(createTrumpet(msg));
        } catch (IOException e) {
            if (output.isClosed()) {
                logger.debug("Trumpeter with id {} has ben closed.", id);
                closeHandler.accept(this);
            }
        }
    }

    public EventOutput subscribe(){
        this.output = new EventOutput() {
            @Override
            public void close() throws IOException {
                closeHandler.accept(Trumpeter.this);
                super.close();
            }
        };
        logger.debug("Trumpeter {} opened subscription.", id);

        return this.output;
    }


    public boolean inRangeWithOutput(Trumpeter other, int maxDistance) {
        return inRange(other, maxDistance) && hasOutput();
    }

    public boolean inRange(Trumpeter other, int maxDistance) {

        Double distanceInMeters = this.location.distance(other.location, DistanceUnit.METERS);

        logger.debug("Distance between trumpeter {} and trumpeter {} is {} meters. Max distance is {}", id, other.id, distanceInMeters.intValue(), maxDistance);

        return distanceInMeters.intValue() <= maxDistance;
    }

    private boolean hasOutput(){
        return output != null;
    }

    private OutboundEvent createTrumpet(String msg) {
        return new OutboundEvent.Builder()
                .name("trumpet")
                .data(Collections.singletonMap("msg", msg))
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }


}
