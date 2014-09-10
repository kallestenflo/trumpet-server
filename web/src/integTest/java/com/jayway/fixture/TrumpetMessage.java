package com.jayway.fixture;

import java.net.URI;
import java.util.Map;

import static com.jayway.jsonpath.JsonPath.read;
import static java.util.Collections.unmodifiableMap;

public class TrumpetMessage {

    private final Map<String, Object> payload;

    public TrumpetMessage(Map<String, Object> payload) {
        this.payload = unmodifiableMap(payload);
    }

    public String getMessage(){
        return (String) payload.get("message");
    }

    public String getId(){
        return (String) payload.get("id");
    }


    public URI echoUri(){
        return URI.create(read(payload, "_links.echo.href"));
    }
}