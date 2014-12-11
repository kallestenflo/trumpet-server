package com.jayway.fixture;

import com.jayway.jsonpath.JsonPath;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public class InRangeMessage {
    private final Map<String, Object> payload;

    public InRangeMessage(Map<String, Object> payload) {
        this.payload = unmodifiableMap(payload);
    }

    public int getTrumpeteersInRange(){
        return JsonPath.read(payload, "message.trumpeteersInRange");
        //return (String) payload.get("id");
    }
}
