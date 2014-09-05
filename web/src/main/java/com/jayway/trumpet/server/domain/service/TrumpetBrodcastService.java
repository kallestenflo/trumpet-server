package com.jayway.trumpet.server.domain.service;

import com.jayway.trumpet.server.domain.model.trumpeteer.Trumpet;
import org.glassfish.jersey.media.sse.EventOutput;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrumpetBrodcastService {

    private final Map<String, EventOutput> sessions = new ConcurrentHashMap<>();

    public void brodcast(Trumpet trumpet){

    }



}
