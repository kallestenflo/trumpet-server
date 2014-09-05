package com.jayway.trumpet.server.domain.trumpeteer;

public class Subscriber  {

    private final String id;
    private final Object channel;

    public Subscriber(String id, Object channel) {
        this.id = id;
        this.channel = channel;
    }

    public String id() {
        return id;
    }

    public <T> T channel() {
        return (T) channel;
    }
}
