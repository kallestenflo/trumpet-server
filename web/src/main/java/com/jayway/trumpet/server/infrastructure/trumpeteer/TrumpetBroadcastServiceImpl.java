package com.jayway.trumpet.server.infrastructure.trumpeteer;

import com.jayway.trumpet.server.boot.TrumpetDomainConfig;
import com.jayway.trumpet.server.domain.Tuple;
import com.jayway.trumpet.server.domain.trumpeteer.Subscriber;
import com.jayway.trumpet.server.domain.trumpeteer.Trumpet;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpetBroadcastService;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpetSubscriptionService;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;

import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TrumpetBroadcastServiceImpl implements TrumpetBroadcastService, TrumpetSubscriptionService {

    private final Map<String, Subscription> subscribers = new ConcurrentHashMap<>();

    private final Timer purgeStaleTrumpeteersTimer = new Timer(true);

    private static final Subscription NOOP_SUBSCRIPTION = new Subscription(null, new EventOutput() {
        @Override
        public void write(OutboundEvent chunk) throws IOException {
            return;
        }
    }, Integer.MIN_VALUE);

    public TrumpetBroadcastServiceImpl(TrumpetDomainConfig config) {

        TimerTask purgeTask = new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                subscribers.values().stream().filter(s -> s.isStale(now)).forEach(s -> {
                    s.closeChannel();
                    subscribers.remove(s.id);
                });
            }
        };
        purgeStaleTrumpeteersTimer.schedule(purgeTask, config.trumpeteerPurgeInterval(), config.trumpeteerPurgeInterval());
    }


    @Override
    public void broadcast(Tuple<String, Trumpet> tuple) {
        try {
            subscribers.getOrDefault(tuple.left, NOOP_SUBSCRIPTION).write(tuple.right);
        } catch (IOException e) {
            subscribers.remove(tuple.left);
        }
    }


    @Override
    public void subscribe(Subscriber subscriber) {
        subscribers.put(subscriber.id(), new Subscription(subscriber.id(), subscriber.channel(), System.currentTimeMillis()));
    }


    private static class Subscription {
        public final String id;
        public final EventOutput eventOutput;
        private final AtomicLong lastAccessed;

        private Subscription(String id, EventOutput eventOutput, long lastAccessed) {
            this.id = id;
            this.eventOutput = eventOutput;
            this.lastAccessed = new AtomicLong(lastAccessed);
        }

        public void updateLastAccessedTo(long newLastAccess) {
            lastAccessed.set(newLastAccess);
        }

        public void write(Trumpet trumpet) throws IOException {
            OutboundEvent outboundEvent = new OutboundEvent.Builder()
                    .name("trumpet")
                    .data(trumpet)
                    .mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .build();
            eventOutput.write(outboundEvent);
        }

        public boolean isStale(long staleThreshold) {
            return ((lastAccessed.get() + staleThreshold) < System.currentTimeMillis()) || eventOutput.isClosed();
        }

        public void closeChannel() {
            if (!eventOutput.isClosed()) {
                try {
                    eventOutput.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}