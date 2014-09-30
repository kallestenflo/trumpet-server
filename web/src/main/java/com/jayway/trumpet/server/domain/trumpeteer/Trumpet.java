package com.jayway.trumpet.server.domain.trumpeteer;

import com.jayway.trumpet.server.domain.subscriber.Trumpeteer;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class Trumpet {

    public final Trumpeteer trumpeteer;
    public final String id;
    public final long timestamp;
    public final String message;
    public final Optional<Integer> requestedDistance;
    public final Optional<String> topic;
    public final Map<String, String> extParameters;

    private Trumpet(Trumpeteer trumpeteer,
                    String id,
                    long timestamp,
                    String message,
                    String topic,
                    Optional<Integer> requestedDistance,
                    Map<String, String> extParameters) {
        this.trumpeteer = trumpeteer;
        this.id = id;
        this.timestamp = timestamp;
        this.message = message;
        this.requestedDistance = requestedDistance;
        this.topic = Optional.ofNullable(topic);
        this.extParameters = Collections.unmodifiableMap(extParameters);
    }

    public static Trumpet create(Trumpeteer trumpeteer, String id, String message, String topic, Optional<Integer> requestedDistance, long timestamp, Map<String, String> extParameters) {
        return new Trumpet(trumpeteer, id, timestamp, message, topic, requestedDistance, extParameters);
    }
}
