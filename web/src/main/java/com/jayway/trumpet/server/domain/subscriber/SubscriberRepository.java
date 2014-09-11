package com.jayway.trumpet.server.domain.subscriber;

import java.util.Optional;

public interface SubscriberRepository {

    Subscriber create(String id, SubscriberOutput output);

    Optional<Subscriber> findById(String id);
}
