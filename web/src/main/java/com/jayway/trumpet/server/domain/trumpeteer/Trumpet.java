package com.jayway.trumpet.server.domain.trumpeteer;

import java.util.Optional;

public class Trumpet {

    public final Trumpeteer trumpeteer;
    public final Trumpeteer receiver;
    public final String id;
    public final long timestamp;
    public final String message;
    public final Optional<String> topic;
    public final int distanceFromSource;

    private Trumpet(Trumpeteer trumpeteer,
                    Trumpeteer receiver,
                    String id,
                    long timestamp,
                    String message,
                    String topic,
                    int distanceFromSource) {
        this.trumpeteer = trumpeteer;
        this.receiver = receiver;
        this.id = id;
        this.timestamp = timestamp;
        this.message = message;
        this.topic = Optional.ofNullable(topic);
        this.distanceFromSource = distanceFromSource;
    }

    public static Trumpet create(Trumpeteer trumpeteer, Trumpeteer receiver, String id, String message, String topic, int distanceFromSource, long timestamp) {
        return new Trumpet(trumpeteer, receiver, id, timestamp, message, topic, distanceFromSource);
    }
}
