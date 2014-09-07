package com.jayway.trumpet.server.domain.trumpeteer;

public class Trumpet {

    public Trumpeteer trumpeteer;
    public Trumpeteer receiver;
    public final String id;
    public final long timestamp;
    public final String message;
    public final long distanceFromSource;

    private Trumpet(Trumpeteer trumpeteer,
                    Trumpeteer receiver,
                    String id,
                    long timestamp,
                    String message,
                    long distanceFromSource) {
        this.trumpeteer = trumpeteer;
        this.receiver = receiver;
        this.id = id;
        this.timestamp = timestamp;
        this.message = message;
        this.distanceFromSource = distanceFromSource;
    }

    public static Trumpet create(Trumpeteer trumpeteer, Trumpeteer receiver, String id, String message, long distanceFromSource){
        return new Trumpet(trumpeteer, receiver, id, System.currentTimeMillis(), message, distanceFromSource);
    }
}
