package com.jayway.trumpet.server.infrastructure.trumpeteer;

import com.jayway.trumpet.server.domain.location.Location;
import com.jayway.trumpet.server.domain.subscriber.SubscriberConfig;
import com.jayway.trumpet.server.domain.subscriber.SubscriberOutput;
import com.jayway.trumpet.server.domain.subscriber.Trumpeteer;
import com.jayway.trumpet.server.domain.subscriber.TrumpeteerRepository;
import com.jayway.trumpet.server.domain.trumpeteer.Trumpet;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpetBroadcastService;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpetSubscriptionService;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpeteerConfig;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpeteerImpl;
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
import java.util.stream.Stream;

import static java.lang.Math.min;

public class TrumpetBroadcastServiceImpl implements TrumpetBroadcastService, TrumpetSubscriptionService, TrumpeteerRepository {

    private static final Logger logger = LoggerFactory.getLogger(TrumpetBroadcastServiceImpl.class);

    private final ConcurrentMap<String, Subscription> trumpeteers = new ConcurrentHashMap<>();

    private final TrumpetEventBus eventBus;

    private final SubscriberConfig subscriberConfig;
    private final TrumpeteerConfig trumpeteerConfig;

    public TrumpetBroadcastServiceImpl(TrumpetEventBus eventBus, SubscriberConfig subscriberConfig, TrumpeteerConfig trumpeteerConfig) {
        this.subscriberConfig = subscriberConfig;
        this.trumpeteerConfig = trumpeteerConfig;

        TimerTask purgeTask = new TimerTask() {
            @Override
            public void run() {
                trumpeteers.values().stream().filter(s -> s.isStale(subscriberConfig.trumpeteerStaleThreshold())).forEach(s -> {
                    logger.debug("Purging stale subscriber {}", s.id());
                    s.closeChannel();
                    trumpeteers.remove(s.id());
                });
            }
        };
        Timer purgeStaleTrumpeteersTimer = new Timer(true);
        purgeStaleTrumpeteersTimer.schedule(purgeTask, 0, subscriberConfig.trumpeteerPurgeInterval());
        this.eventBus = eventBus;
        this.eventBus.subscribe(this::handleTrumpetEvent);
    }

    @Override
    public void broadcast(Trumpet trumpet, Map<String, Object> trumpetPayload) {
        eventBus.publish(new TrumpetEvent(trumpet.receiver.id(), trumpetPayload));
    }

    private void handleTrumpetEvent(TrumpetEvent trumpetEvent) {
        try {
            logger.debug("Broadcasting trumpet");

            Subscription subscription = trumpeteers.get(trumpetEvent.receiverId);
            if (subscription != null) {
                subscription.write(trumpetEvent.payload);
            }
        } catch (IOException e) {
            trumpeteers.remove(trumpetEvent.receiverId);
        }
    }

    @Override
    public void subscribe(Trumpeteer trumpeteer) {
        findExistingSubscriberWithSameLinkIdAs(trumpeteer).map(Trumpeteer::id).ifPresent(trumpeteers::remove);
        trumpeteers.put(trumpeteer.id(), new Subscription(trumpeteer, System.currentTimeMillis()));
    }

    @Override
    public Trumpeteer create(String id, String linkId, Location location, SubscriberOutput output) {
        return new TrumpeteerImpl(id, linkId, location, output, trumpeteerConfig);
    }

    @Override
    public Optional<Trumpeteer> findById(String id) {
        Subscription subscription = trumpeteers.get(id);
        if (subscription == null) {
            return Optional.empty();
        } else {
            return Optional.of(subscription.trumpeteer);
        }
    }

    @Override
    public Stream<Trumpeteer> findAll() {
        return trumpeteers.values().stream().map(s -> s.trumpeteer);
    }

    @Override
    public int countTrumpeteersInRangeOf(Trumpeteer trumpeteer, int maxDistance) {
        int distance = min(maxDistance, trumpeteer.maxTrumpetDistance());

        Long inRange = trumpeteers.values().stream().filter(subscription -> !subscription.id().equals(trumpeteer.id()) && subscription.trumpeteer.inRange(trumpeteer, distance)).count();

        logger.debug("There are {} trumpeteer(s) in range of trumpeteer {}", inRange, trumpeteer.id());

        return inRange.intValue();
    }

    @Override
    public void delete(String id) {
        Subscription subscription = trumpeteers.get(id);
        if(subscription == null) {
            return;
        }
        subscription.closeChannel();
        trumpeteers.remove(id);
    }


    public int numberOfSubscribers() {
        return trumpeteers.size();
    }

    private static class Subscription {
        public final Trumpeteer trumpeteer;
        private final AtomicLong lastAccessed;

        private Subscription(Trumpeteer trumpeteer, long lastAccessed) {
            this.trumpeteer = trumpeteer;
            this.lastAccessed = new AtomicLong(lastAccessed);
        }

        private void updateLastAccessedTo(long newLastAccess) {
            lastAccessed.set(newLastAccess);
        }

        public void write(Map<String, Object> trumpetPayload) throws IOException {
            trumpeteer.output().write(trumpetPayload);
            updateLastAccessedTo(System.currentTimeMillis());
        }

        public boolean isStale(long staleThreshold) {
            return ((lastAccessed.get() + staleThreshold) < System.currentTimeMillis()) || trumpeteer.output().isClosed();
        }

        public void closeChannel() {
            trumpeteer.output().close();
        }

        public String id() {
            return trumpeteer.id();
        }
    }

    private Optional<Trumpeteer> findExistingSubscriberWithSameLinkIdAs(Trumpeteer trumpeteer) {
        return trumpeteers.entrySet().stream().
                filter(keyValue -> keyValue.getValue().trumpeteer.linkId().equals(trumpeteer.linkId())).
                map(entry -> entry.getValue().trumpeteer).findFirst();
    }
}