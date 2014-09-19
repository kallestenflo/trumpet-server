package com.jayway.trumpet.server.infrastructure.trumpeteer;

import com.jayway.trumpet.server.boot.TrumpetDomainConfig;
import com.jayway.trumpet.server.domain.subscriber.Subscriber;
import com.jayway.trumpet.server.domain.subscriber.SubscriberOutput;
import com.jayway.trumpet.server.domain.subscriber.SubscriberRepository;
import com.jayway.trumpet.server.domain.trumpeteer.Trumpet;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpetBroadcastService;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpetSubscriptionService;
import com.jayway.trumpet.server.infrastructure.event.TrumpetEvent;
import com.jayway.trumpet.server.infrastructure.event.TrumpetEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class TrumpetBroadcastServiceImpl implements TrumpetBroadcastService, TrumpetSubscriptionService, SubscriberRepository {

    private static final Logger logger = LoggerFactory.getLogger(TrumpetBroadcastServiceImpl.class);

    private final ConcurrentMap<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    private final Timer purgeStaleTrumpeteersTimer = new Timer(true);

    private final TrumpetEventBus eventBus;

    private static final Subscriber NOOP_SUBSCRIBER = new Subscriber() {
        @Override
        public String id() {
            return null;
        }

        @Override
        public String linkId() {
            return null;
        }

        @Override
        public SubscriberOutput output() {
            return NOOP_SUBSCRIBER_OUTPUT;
        }
    };

    private static final SubscriberOutput NOOP_SUBSCRIBER_OUTPUT = new SubscriberOutput() {

        @Override
        public void write(Map<String, Object> message) throws IOException {

        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public void close() {

        }

        @Override
        public <T> Optional<T> channel() {
            return null;
        }
    };

    private static final Subscription NOOP_SUBSCRIPTION = new Subscription(NOOP_SUBSCRIBER, Long.MIN_VALUE);

    public TrumpetBroadcastServiceImpl(TrumpetEventBus eventBus, TrumpetDomainConfig config) {

        TimerTask purgeTask = new TimerTask() {
            @Override
            public void run() {
                subscriptions.values().stream().filter(s -> s.isStale(config.trumpeteerStaleThreshold())).forEach(s -> {
                    logger.debug("Purging stale subscriber {}", s.id());
                    s.closeChannel();
                    subscriptions.remove(s.id());
                });
            }
        };
        purgeStaleTrumpeteersTimer.schedule(purgeTask, 0, config.trumpeteerPurgeInterval());
        this.eventBus = eventBus;
        this.eventBus.subscribe(this::handleTrumpetEvent);
    }

    @Override
    public void broadcast(Trumpet trumpet, Map<String, Object> trumpetPayload) {
        eventBus.publish(new TrumpetEvent(trumpet.receiver.id, trumpetPayload));
    }

    private void handleTrumpetEvent(TrumpetEvent trumpetEvent) {
        try {
            logger.debug("Broadcasting trumpet");

            subscriptions.getOrDefault(trumpetEvent.receiverId, NOOP_SUBSCRIPTION).write(trumpetEvent.payload);
        } catch (IOException e) {
            subscriptions.remove(trumpetEvent.receiverId);
        }
    }

    @Override
    public void subscribe(Subscriber subscriber) {
        findExistingSubscriberWithSameLinkIdAs(subscriber).map(Subscriber::id).ifPresent(subscriptions::remove);
        subscriptions.put(subscriber.id(), new Subscription(subscriber, System.currentTimeMillis()));
    }

    @Override
    public Subscriber create(String id, String linkId, SubscriberOutput output) {
        return new Subscriber() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String linkId() {
                return linkId;
            }

            @Override
            public SubscriberOutput output() {
                return output;
            }
        };
    }

    @Override
    public Optional<Subscriber> findById(String id) {
        Subscription subscription = subscriptions.get(id);
        if (subscription == null) {
            return Optional.empty();
        } else {
            return Optional.of(subscription.subscriber);
        }
    }


    public int numberOfSubscribers() {
        return subscriptions.size();
    }

    private static class Subscription {
        public final Subscriber subscriber;
        private final AtomicLong lastAccessed;

        private Subscription(Subscriber subscriber, long lastAccessed) {
            this.subscriber = subscriber;
            this.lastAccessed = new AtomicLong(lastAccessed);
        }

        private void updateLastAccessedTo(long newLastAccess) {
            lastAccessed.set(newLastAccess);
        }

        public void write(Map<String, Object> trumpetPayload) throws IOException {
            subscriber.output().write(trumpetPayload);
            updateLastAccessedTo(System.currentTimeMillis());
        }

        public boolean isStale(long staleThreshold) {
            return ((lastAccessed.get() + staleThreshold) < System.currentTimeMillis()) || subscriber.output().isClosed();
        }

        public void closeChannel() {
            subscriber.output().close();
        }

        public String id() {
            return subscriber.id();
        }
    }

    private Optional<Subscriber> findExistingSubscriberWithSameLinkIdAs(Subscriber subscriber) {
        return subscriptions.entrySet().stream().
                filter(keyValue -> keyValue.getValue().subscriber.linkId().equals(subscriber.linkId())).
                map(entry -> entry.getValue().subscriber).findFirst();
    }
}