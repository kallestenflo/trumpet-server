package com.jayway.trumpet.server.infrastructure.event;

import java.util.function.Consumer;

public interface TrumpetEventBus {
    void subscribe(Consumer<TrumpetEvent> consumer);

    void publish(TrumpetEvent trumpetEvent);
}
