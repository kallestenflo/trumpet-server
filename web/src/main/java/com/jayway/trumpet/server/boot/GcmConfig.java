package com.jayway.trumpet.server.boot;

import org.aeonbits.owner.Config;

public interface GcmConfig extends Config {
    static final String GCM_API_KEY = "gcm.apiKey";

    @Config.Key(GCM_API_KEY)
    @DefaultValue("changeme")
    String gcmApiKey();
}
