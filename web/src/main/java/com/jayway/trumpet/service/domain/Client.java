package com.jayway.trumpet.service.domain;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Consumer;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public final String id;
    private final Consumer<Client> closeHandler;
    private Location location;
    private EventOutput output;


    public Client(String id,
                  Location location,
                  Consumer<Client> closeHandler) {
        this.id = id;
        this.location = location;
        this.closeHandler = closeHandler;
    }

    public void updateLocation(Location newLocation) {
        this.location = newLocation;
        logger.debug("Client updateLocation updated : {}, latitude: {}, longitude: {}", id, this.location.latitude, this.location.longitude);
    }

    public void postTrumpet(String msg){
        try {
            logger.debug("Pushing trumpet to client : {}, latitude: {}, longitude: {}", id, location.latitude, location.longitude);
            output.write(createTrumpet(msg));
        } catch (IOException e) {
            if (output.isClosed()) {
                logger.debug("Client with id {} has ben closed", id);
                closeHandler.accept(this);
            }
        }
    }

    public EventOutput subscribe(){
        this.output = new EventOutput() {
            @Override
            public void close() throws IOException {
                closeHandler.accept(Client.this);
                super.close();
            }
        };
        logger.debug("Client : {} subscribed", id);

        return this.output;
    }


    public boolean inRangeWithOutput(Client other, int maxDistance) {
        return inRange(other, maxDistance) && hasOutput();
    }

    public boolean inRange(Client other, int maxDistance) {

        Double distanceInMeters = this.location.distance(other.location, DistanceUnit.METERS);

        logger.debug("Distance between client {} and client {} is {} meters.", id, other.id, distanceInMeters.intValue());

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
