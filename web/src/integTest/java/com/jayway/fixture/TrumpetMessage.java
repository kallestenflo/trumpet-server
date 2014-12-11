package com.jayway.fixture;

import com.jayway.jsonpath.JsonPath;

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
        return JsonPath.read(payload, "message.message");
        //return (String) payload.get("message");
    }

    public String getChannel(){
        return JsonPath.read(payload, "message.channel");
        //return (String) payload.get("channel");
    }

    public String getId(){
        return JsonPath.read(payload, "message.id");
        //return (String) payload.get("id");
    }

    public Map<String, String> getExt(){
        return JsonPath.read(payload, "message.ext");
        //Object ext = payload.get("ext");
        //return ext != null ? (Map<String, String>) payload.get("ext") : Collections.emptyMap();
    }

    public URI echoUri(){
        return URI.create(read(payload, "message._links.echo.href"));
    }
}
