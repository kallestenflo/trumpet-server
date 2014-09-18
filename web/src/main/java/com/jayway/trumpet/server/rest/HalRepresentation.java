package com.jayway.trumpet.server.rest;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class HalRepresentation extends LinkedHashMap<String, Object> implements Serializable {

    private static final long serialVersionUID = -2112878680072211787L;

    public static HalRepresentation hal(){
        return new HalRepresentation();
    }

    public HalRepresentation withLinks(){
        Map<String, Object> links = (Map<String, Object>) get("_links");
        if (links == null) {
            links = new LinkedHashMap<>();
            put("_links", links);
        }
        return this;
    }

    public HalRepresentation addLink(String rel, URI uri) {
        Map<String, Object> links = (Map<String, Object>) get("_links");
        if (links == null) {
            links = new LinkedHashMap<>();
            put("_links", links);
        }
        links.put(rel, Collections.singletonMap("href", uri));
        return this;
    }
}
