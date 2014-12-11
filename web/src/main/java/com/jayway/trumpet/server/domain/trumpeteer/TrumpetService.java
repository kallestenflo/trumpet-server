package com.jayway.trumpet.server.domain.trumpeteer;

import com.jayway.trumpet.server.domain.subscriber.Trumpeteer;

public interface TrumpetService {
    void broadcast(Trumpet trumpet);
    void trumpetTo(Trumpeteer trumpeteer, Trumpet trumpet);
    void notifyInRangeTo(Trumpeteer trumpeteer, int inRange);
}
