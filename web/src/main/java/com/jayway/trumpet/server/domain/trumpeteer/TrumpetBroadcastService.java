package com.jayway.trumpet.server.domain.trumpeteer;

import java.net.URI;
import java.util.Map;

public interface TrumpetBroadcastService {
    void broadcast(Trumpet trumpet, Map<String, URI> links );
}
