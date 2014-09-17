package com.jayway.trumpet.server.domain.subscriber;

import java.io.IOException;
import java.util.Map;

public interface SubscriberOutput {

    void write(Map<String, Object> message) throws IOException;

    boolean isClosed();

    void close();

    <T> T channel();
}
