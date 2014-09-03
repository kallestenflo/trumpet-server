package com.jayway.trumpet.server.boot;

import org.aeonbits.owner.Config;

public interface TrumpetDomainConfig extends Config {

    static final String TRUMPETEER_STALE_THRESHOLD = "trumpeteer.staleThreshold";
    static final String TRUMPETEER_PURGE_INTERVAL = "trumpeteer.purgeInterval";

    @Config.Key("trumpeteer.staleThreshold")
    @Config.DefaultValue("3600000")   //= 1000 * 60 * 60 = 1h
    long trumpeteerStaleThreshold();

    @Config.Key("trumpeteer.purgeInterval")
    @Config.DefaultValue("60000")    //= 1000 * 60 = 1min
    long trumpeteerPurgeInterval();

}
