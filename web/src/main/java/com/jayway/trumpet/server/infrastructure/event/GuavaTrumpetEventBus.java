package com.jayway.trumpet.server.infrastructure.event;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class GuavaTrumpetEventBus implements TrumpetEventBus {
    private final EventBus eventBus;

    private final List<Consumer<TrumpetEvent>> subscribers = new CopyOnWriteArrayList<>();

    public GuavaTrumpetEventBus() {
        eventBus = new AsyncEventBus(Executors.newFixedThreadPool(10));
        eventBus.register(this);
    }

    @Override
    public void subscribe(Consumer<TrumpetEvent> subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void publish(TrumpetEvent trumpetEvent) {
        eventBus.post(trumpetEvent);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void handleTrumpetEvent(TrumpetEvent trumpetEvent) {
        subscribers.parallelStream().forEach(subscriber -> subscriber.accept(trumpetEvent));
    }
}
