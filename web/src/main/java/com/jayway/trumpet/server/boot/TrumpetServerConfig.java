package com.jayway.trumpet.server.boot;

import org.aeonbits.owner.Config;

import static org.aeonbits.owner.Config.*;

@LoadPolicy(LoadType.MERGE)
@Sources({"file:trumpetServer.config",
          "file:~/.trumpetServer.config",
          "file:/etc/trumpetServer.config"})
public interface TrumpetServerConfig extends Config {

    @Key("server.http.port")
    @DefaultValue("9191")
    int port();

    @Key("server.host.name")
    @DefaultValue("0.0.0.0")
    String hostname();

}