package com.jayway.trumpet.server.boot;

import org.aeonbits.owner.Config;

public interface ForceClientUpgradeConfig extends Config {
    static final String FORCE_CLIENT_UPGRADE_ENABLED = "client.forceUpgrade.enabled";
    static final String ANDROID_MIN_VERSION = "client.android.minVersion";

    @Key(ANDROID_MIN_VERSION)
    @DefaultValue("0.0.0")
    String androidMinVersion();

    @Key(FORCE_CLIENT_UPGRADE_ENABLED)
    @DefaultValue("false")
    boolean isForceUpgradingOfOldClientsEnabled();
}
