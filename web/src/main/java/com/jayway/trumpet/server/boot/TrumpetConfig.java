package com.jayway.trumpet.server.boot;

import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"file:trumpet.config",
        "file:~/.trumpet.config",
        "file:/etc/trumpet.config"})
public interface TrumpetConfig extends Config, TrumpetServerConfig, TrumpetDomainConfig, GcmConfig {
}
