package com.jayway.trumpet.server.rest;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class HalRepresentation extends HashMap<String, Object> implements Serializable {

    private static final long serialVersionUID = -2112878680072211787L;

    public HalRepresentation addLink(String rel, URI uri){
        Map<String, Object> links = (Map<String, Object>) get("_links");
        if(links == null){
            links = new HashMap<>();
            put("_links", links);
        }
        links.put(rel, uri);
        return this;
    }
}
