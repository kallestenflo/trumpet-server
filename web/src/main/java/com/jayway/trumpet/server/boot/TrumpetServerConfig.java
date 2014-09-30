package com.jayway.trumpet.server.boot;

import com.jayway.trumpet.server.domain.subscriber.SubscriberConfig;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpeteerConfig;
import org.aeonbits.owner.Reloadable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.aeonbits.owner.Config.*;
import static org.aeonbits.owner.Config.HotReloadType.ASYNC;
import static org.aeonbits.owner.Config.LoadType.MERGE;

@HotReload(value=10, unit = SECONDS, type = ASYNC)
@LoadPolicy(MERGE)
@Sources({"file:trumpet-server.config",
        "file:~/.trumpet-server.config",
        "file:/etc/trumpet-server.config"})
public interface TrumpetServerConfig extends Reloadable, TrumpeteerConfig, SubscriberConfig, GcmConfig {

    static final String SERVER_HTTP_PORT = "server.http.port";
    static final String SERVER_HOST_NAME = "server.host.name";
    static final String SERVER_RESOURCE_BASE = "server.resourceBase";

    @Key(SERVER_HTTP_PORT)
    @DefaultValue("9191")
    int port();

    @Key(SERVER_HOST_NAME)
    @DefaultValue("0.0.0.0")
    String hostname();

    @Key(SERVER_RESOURCE_BASE)
    @DefaultValue("classpath")
    String resourceBase();



}