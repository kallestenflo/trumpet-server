package com.jayway.trumpet.server.boot;

import org.aeonbits.owner.Config;

import static org.aeonbits.owner.Config.DefaultValue;
import static org.aeonbits.owner.Config.Key;


public interface TrumpetServerConfig extends Config {

    static final String SERVER_HTTP_PORT = "server.http.port";
    static final String SERVER_HOST_NAME = "server.host.name";

    @Key(SERVER_HTTP_PORT)
    @DefaultValue("9191")
    int port();

    @Key(SERVER_HOST_NAME)
    @DefaultValue("0.0.0.0")
    String hostname();



}