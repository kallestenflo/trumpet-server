package com.jayway.trumpet.server.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Trumpet {
    public final String message;
    public final long distanceFromSource;

    @JsonCreator
    private Trumpet(@JsonProperty("message") String message,
                    @JsonProperty("distanceFromSource") long distanceFromSource) {
        this.message = message;
        this.distanceFromSource = distanceFromSource;
    }

    public static Trumpet create(String message, long distanceFromSource){
        return new Trumpet(message, distanceFromSource);
    }
}
