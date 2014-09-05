package com.jayway.trumpet.server.domain.model.trumpeteer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Trumpet {
    public final String id;
    public final long timestamp;

    public final String message;
    public final long distanceFromSource;

    @JsonCreator
    private Trumpet(@JsonProperty("id") String id,
                    @JsonProperty("timestamp") long timestamp,
                    @JsonProperty("message") String message,
                    @JsonProperty("distanceFromSource") long distanceFromSource) {
        this.id = id;
        this.timestamp = timestamp;
        this.message = message;
        this.distanceFromSource = distanceFromSource;
    }

    public static Trumpet create(String id, String message, long distanceFromSource){
        return new Trumpet(id, System.currentTimeMillis(), message, distanceFromSource);
    }

    @Override
    public String toString() {
        return "Trumpet{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", distanceFromSource=" + distanceFromSource +
                '}';
    }
}
