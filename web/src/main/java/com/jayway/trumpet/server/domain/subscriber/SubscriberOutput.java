package com.jayway.trumpet.server.domain.subscriber;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public interface SubscriberOutput {

    void write(Map<String, Object> message) throws IOException;

    boolean isClosed();

    void close();

    <T> Optional<T> channel();
}
