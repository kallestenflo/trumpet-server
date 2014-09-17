package com.jayway.trumpet.server.infrastructure.subscription.gcm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.jayway.trumpet.server.boot.GcmConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

public class GCMBroadcaster {

    private static final int RETRIES = 5;
    private final ObjectMapper objectMapper;
    private final Sender sender;

    public GCMBroadcaster(GcmConfig gcmConfig) {
        this.sender = new Sender(gcmConfig.gcmApiKey());
        objectMapper = new ObjectMapper();
    }

    public void publish(String registrationId, Map<String, Object> trumpet) {
        Message message = new Message.Builder().addData("trumpet", serialize(trumpet)).build();
        // TODO Implement batch sending of trumpet
        try {
            sender.send(message, registrationId, RETRIES);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
