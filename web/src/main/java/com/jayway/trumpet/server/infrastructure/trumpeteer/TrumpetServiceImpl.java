package com.jayway.trumpet.server.infrastructure.trumpeteer;

import com.jayway.trumpet.server.domain.location.DistanceUnit;
import com.jayway.trumpet.server.domain.location.Location;
import com.jayway.trumpet.server.domain.subscriber.SubscriberConfig;
import com.jayway.trumpet.server.domain.subscriber.SubscriberOutput;
import com.jayway.trumpet.server.domain.subscriber.Trumpeteer;
import com.jayway.trumpet.server.domain.subscriber.TrumpeteerRepository;
import com.jayway.trumpet.server.domain.trumpeteer.*;
import com.jayway.trumpet.server.infrastructure.event.TrumpetEvent;
import com.jayway.trumpet.server.infrastructure.event.TrumpetEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.lang.Math.min;

public class TrumpetServiceImpl implements TrumpetService, TrumpetSubscriptionService, TrumpeteerRepository {

    private static final Logger logger = LoggerFactory.getLogger(TrumpetServiceImpl.class);

    private final ConcurrentMap<String, Subscription> trumpeteers = new ConcurrentHashMap<>();

    private final TrumpetEventBus eventBus;

    private final TrumpeteerConfig trumpeteerConfig;

    public TrumpetServiceImpl(TrumpetEventBus eventBus, SubscriberConfig subscriberConfig, TrumpeteerConfig trumpeteerConfig) {
        this.trumpeteerConfig = trumpeteerConfig;

        TimerTask purgeTask = new TimerTask() {
            @Override
            public void run() {
                trumpeteers.values().stream().filter(s -> s.isStale(subscriberConfig.trumpeteerStaleThreshold())).forEach(s -> {
                    logger.debug("Purging stale subscriber {}", s.id());
                    notifyRemovingSubscriber(s);
                    s.closeChannel();
                    trumpeteers.remove(s.id());
                });
            }
        };
        if (subscriberConfig.trumpeteerPurgeEnabled()) {
            Timer purgeStaleTrumpeteersTimer = new Timer(true);
            purgeStaleTrumpeteersTimer.schedule(purgeTask, subscriberConfig.trumpeteerPurgeInterval(), subscriberConfig.trumpeteerPurgeInterval());
        }
        this.eventBus = eventBus;
        this.eventBus.subscribe(this::handleTrumpetEvent);
    }

    @Override
    public void broadcast(Trumpet trumpet) {
        int trumpetDistance = getTrumpetDistance(trumpet);

        findAll()
                .filter(t -> t.inRange(trumpet.trumpeteer, trumpetDistance))
                .map(t -> createTrumpetEvent(t, trumpet))
                .forEach(eventBus::publish);
    }

    @Override
    public void trumpetTo(Trumpeteer trumpeteer, Trumpet trumpet) {
        eventBus.publish(createTrumpetEvent(trumpeteer, trumpet));
    }

    private int getTrumpetDistance(Trumpet trumpet) {
        int configMaxDistance = trumpeteerConfig.trumpeteerMaxDistance();
        int distance = trumpet.requestedDistance.orElse(configMaxDistance);
        return min(distance, configMaxDistance);
    }

    private TrumpetEvent createTrumpetEvent(Trumpeteer trumpeteer, Trumpet trumpet) {
        Map<String, Object> trumpetPayload = new HashMap<>();
        trumpetPayload.put("id", trumpet.id);
        trumpetPayload.put("timestamp", trumpet.timestamp);
        trumpetPayload.put("message", trumpet.message);
        trumpet.topic.ifPresent(topic -> trumpetPayload.put("topic", topic));
        trumpetPayload.put("distanceFromSource", trumpet.trumpeteer.distanceTo(trumpeteer, DistanceUnit.METERS).intValue());
        trumpetPayload.put("accuracy", trumpet.trumpeteer.location().accuracy);
        trumpetPayload.put("sentByMe", trumpet.trumpeteer.id().equals(trumpeteer.id()));
        trumpetPayload.put("ext", trumpet.extParameters);

        return new TrumpetEvent(trumpeteer.id(), trumpetPayload);
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
    public void keepAlive(Trumpeteer trumpeteer) {
        Subscription subscription = trumpeteers.get(trumpeteer.id());
        if (subscription == null) {
            return;
        }
        trumpeteers.put(trumpeteer.id(), new Subscription(subscription.trumpeteer, System.currentTimeMillis()));
    }

    @Override
    public Trumpeteer create(String id, String linkId, Location location, SubscriberOutput output) {
        return new TrumpeteerImpl(id, linkId, location, output);
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
    public int countTrumpeteersInRangeOf(Trumpeteer trumpeteer, int requestedDistance) {
        Long inRange = findTrumpeteersInRangeOf(trumpeteer, requestedDistance).count();

        logger.debug("There are {} trumpeteer(s) in range of trumpeteer {}", inRange, trumpeteer.id());

        return inRange.intValue();
    }

    @Override
    public Stream<Trumpeteer> findTrumpeteersInRangeOf(Trumpeteer trumpeteer, int requestedDistance) {
        int distance = min(requestedDistance, trumpeteerConfig.trumpeteerMaxDistance());
        return trumpeteers.values().stream().filter(subscription -> !subscription.id().equals(trumpeteer.id()) && subscription.trumpeteer.inRange(trumpeteer, distance)).map(s -> s.trumpeteer);
    }

    @Override
    public void delete(String id) {
        Subscription subscription = trumpeteers.get(id);
        if (subscription == null) {
            return;
        }
        notifyRemovingSubscriber(subscription);
        subscription.closeChannel();
        trumpeteers.remove(id);
    }

    private void notifyRemovingSubscriber(Subscription subscription) {
        //TODO This method have a lot in common with TrumpeteerNotificationServiceImpl
        logger.debug("Updating trumpeteers in range for all trumpeteers in range of trumpeteer {} since it's going to be removed", subscription.id());
        int distance = trumpeteerConfig.trumpeteerMaxDistance();
        findTrumpeteersInRangeOf(subscription.trumpeteer, distance)
                .map(t -> Pair.of(t, countTrumpeteersInRangeOf(t, distance)))
                .map(p -> Pair.of(p.getKey(), p.getValue() - 1)) // Compensate for the removal of this subscriber
                .forEach(p -> {
                    String trumpetId = UUID.randomUUID().toString();
                    Map<String, String> extParameters = new HashMap<>();
                    extParameters.put("trumpeteersInRange", String.valueOf(p.getValue()));
                    Trumpeteer t = p.getKey();
                    Optional<Integer> maxDistance = Optional.of(distance);
                    trumpetTo(t, Trumpet.create(t, trumpetId, "", "", maxDistance, System.currentTimeMillis(), extParameters));
                });
    }

    @Override
    public void update(Trumpeteer trumpeteer) {
        Subscription subscription = trumpeteers.get(trumpeteer.id());
        if (subscription == null) {
            return;
        }
        trumpeteers.put(trumpeteer.id(), new Subscription(trumpeteer, System.currentTimeMillis()));
    }

    @Override
    public void clear() {
        trumpeteers.clear();
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