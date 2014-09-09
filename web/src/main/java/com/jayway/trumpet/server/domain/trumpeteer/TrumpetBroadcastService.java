package com.jayway.trumpet.server.domain.trumpeteer;

import java.util.Map;

public interface TrumpetBroadcastService {
    void broadcast(Trumpet trumpet, Map<String, Object> trumpetPayload );
}
