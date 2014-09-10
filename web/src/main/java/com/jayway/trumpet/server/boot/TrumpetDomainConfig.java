package com.jayway.trumpet.server.boot;

import org.aeonbits.owner.Config;

public interface TrumpetDomainConfig extends Config {

    static final String TRUMPETEER_STALE_THRESHOLD = "trumpeteer.staleThreshold";
    static final String TRUMPETEER_PURGE_INTERVAL = "trumpeteer.purgeInterval";
    static final String MAX_TRUMPET_DISTANCE = "trumpeteer.maxDistance";

    @Config.Key(TRUMPETEER_STALE_THRESHOLD)
    @Config.DefaultValue("300000")   //= 1000 * 60 * 5 = 3 minutes
    long trumpeteerStaleThreshold();

    @Config.Key(TRUMPETEER_PURGE_INTERVAL)
    @Config.DefaultValue("30000")    //= 1000 * 30 = 30 seconds
    long trumpeteerPurgeInterval();

    @Config.Key(MAX_TRUMPET_DISTANCE)
    @Config.DefaultValue("200")
    int trumpeteerMaxDistance();

}
