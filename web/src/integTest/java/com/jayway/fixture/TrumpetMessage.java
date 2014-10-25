package com.jayway.fixture;

import java.net.URI;
import java.util.Collections;
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

    public String getChannel(){
        return (String) payload.get("channel");
    }

    public String getId(){
        return (String) payload.get("id");
    }

    public Map<String, String> getExt(){
        Object ext = payload.get("ext");
        return ext != null ? (Map<String, String>) payload.get("ext") : Collections.emptyMap();
    }


    public URI echoUri(){
        return URI.create(read(payload, "_links.echo.href"));
    }
}
