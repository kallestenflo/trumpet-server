package com.jayway.trumpet.server.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TrumpetMessage {
    public final String message;
    public final long distanceFromSource;

    @JsonCreator
    private TrumpetMessage(@JsonProperty("message") String message,
                           @JsonProperty("distanceFromSource") long distanceFromSource) {
        this.message = message;
        this.distanceFromSource = distanceFromSource;
    }

    public static TrumpetMessage create(String message, long distanceFromSource){
        return new TrumpetMessage(message, distanceFromSource);
    }
}
