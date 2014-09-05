package com.jayway.trumpet.server.domain;

public interface TrumpetPublisher {

    void publish(Trumpet trumpet);

    boolean isClosed();
}
