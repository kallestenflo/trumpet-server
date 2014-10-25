package com.jayway.trumpet.server.domain.trumpeteer;

import com.jayway.trumpet.server.domain.subscriber.Trumpeteer;

import java.util.stream.Stream;

public interface TrumpeteerNotificationService {

    void notifyTrumpeteersInRange(Trumpeteer trumpeteer, Stream<Trumpeteer> trumpeteersInRangeOfBeforeUpdate,
                                  Stream<Trumpeteer> trumpeteersInRangeOfAfterUpdate,
                                  boolean sendToOriginatingTrumpeteerEvenIfNoDiff);
}
