package com.jayway.trumpet.server.domain.trumpeteer;

public class Trumpet {

    public final Trumpeteer trumpeteer;
    public final Trumpeteer receiver;
    public final String id;
    public final long timestamp;
    public final String message;
    public final int distanceFromSource;

    private Trumpet(Trumpeteer trumpeteer,
                    Trumpeteer receiver,
                    String id,
                    long timestamp,
                    String message,
                    int distanceFromSource) {
        this.trumpeteer = trumpeteer;
        this.receiver = receiver;
        this.id = id;
        this.timestamp = timestamp;
        this.message = message;
        this.distanceFromSource = distanceFromSource;
    }

    public static Trumpet create(Trumpeteer trumpeteer, Trumpeteer receiver, String id, String message, int distanceFromSource, long timestamp){
        return new Trumpet(trumpeteer, receiver, id, timestamp , message, distanceFromSource);
    }
}
