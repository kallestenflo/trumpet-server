package com.jayway.trumpet.server.domain.subscriber;

import org.aeonbits.owner.Config;

public interface SubscriberConfig extends Config {
    String TRUMPETEER_STALE_THRESHOLD = "trumpeteer.staleThreshold";
    String TRUMPETEER_PURGE_INTERVAL = "trumpeteer.purgeInterval";

    @Config.Key(SubscriberConfig.TRUMPETEER_STALE_THRESHOLD)
    @Config.DefaultValue("300000")   //= 1000 * 60 * 5 = 3 minutes
    long trumpeteerStaleThreshold();

    @Config.Key(SubscriberConfig.TRUMPETEER_PURGE_INTERVAL)
    @Config.DefaultValue("30000")    //= 1000 * 30 = 30 seconds
    long trumpeteerPurgeInterval();
}
