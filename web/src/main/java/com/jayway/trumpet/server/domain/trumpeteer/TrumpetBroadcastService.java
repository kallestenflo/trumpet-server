package com.jayway.trumpet.server.domain.trumpeteer;

import com.jayway.trumpet.server.domain.Tuple;

public interface TrumpetBroadcastService {
    void broadcast(Tuple<String, Trumpet> trumpet);
}
