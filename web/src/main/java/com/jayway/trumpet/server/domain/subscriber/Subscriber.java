package com.jayway.trumpet.server.domain.subscriber;

public interface Subscriber  {

    String id();

    String linkId();

    SubscriberOutput output();
}
