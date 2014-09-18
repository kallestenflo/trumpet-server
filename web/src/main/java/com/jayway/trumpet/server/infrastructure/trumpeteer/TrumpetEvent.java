package com.jayway.trumpet.server.infrastructure.trumpeteer;

import java.util.Collections;
import java.util.Map;

public class TrumpetEvent {
    public final String receiverId;
    public final Map<String, Object> payload;

    public TrumpetEvent(String receiverId, Map<String, Object> payload) {
        this.receiverId = receiverId;
        this.payload = Collections.unmodifiableMap(payload);
    }
}
