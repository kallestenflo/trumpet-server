package com.jayway.trumpet.server.boot;

import org.aeonbits.owner.Config;

public interface TrumpetDomainConfig extends Config {

    static final String TRUMPETER_STALE_THRESHOLD = "trumpeter.staleThreshold";
    static final String TRUMPETER_PURGE_INTERVAL  = "trumpeter.purgeInterval";

    @Config.Key("trumpeter.staleThreshold")
    @Config.DefaultValue("3600000")   //= 1000 * 60 * 60 = 1h
    long trumpeterStaleThreshold();

    @Config.Key("trumpeter.purgeInterval")
    @Config.DefaultValue("60000")    //= 1000 * 60 = 1min
    long trumpeterPurgeInterval();

}
