package com.jayway.trumpet.server.boot;

import org.aeonbits.owner.Config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static org.aeonbits.owner.Config.*;

@LoadPolicy(LoadType.MERGE)
@Sources({"file:trumpetServer.config",
          "file:~/.trumpetServer.config",
          "file:/etc/trumpetServer.config"})
public interface TrumpetServerConfig extends Config {

    static final String SERVER_HTTP_PORT = "server.http.port";
    static final String SERVER_HOST_NAME= "server.host.name";
    static final String TRUMPETER_STALE_THRESHOLD= "trumpeter.staleThreshold";
    static final String TRUMPETER_PURGE_INTERVAL= "trumpeter.purgeInterval";

    @Key(SERVER_HTTP_PORT)
    @DefaultValue("9191")
    int port();

    @Key(SERVER_HOST_NAME)
    @DefaultValue("0.0.0.0")
    String hostname();

    @Key("trumpeter.staleThreshold")
    @DefaultValue("3600000")   //= 1000 * 60 * 60 = 1h
    long trumpeterStaleThreshold();

    @Key("trumpeter.purgeInterval")
    @DefaultValue("60000")    //= 1000 * 60 = 1min
    long trumpeterPurgeInterval();

}