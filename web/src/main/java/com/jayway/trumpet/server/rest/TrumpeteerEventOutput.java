package com.jayway.trumpet.server.rest;

import com.jayway.trumpet.server.domain.trumpeteer.Trumpet;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;

import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class TrumpeteerEventOutput extends EventOutput {

    private static final Supplier<String> trumpetIdSupplier = new Supplier<String>() {
        AtomicLong sequence = new AtomicLong();
        @Override
        public String get() {
            return String.valueOf(sequence.incrementAndGet());
        }
    };

    @Override
    public void close() {
        try {
            super.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void publish(Trumpet trumpet) {
        try {
            write(createTrumpet(trumpet.message, trumpet.distanceFromSource));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private OutboundEvent createTrumpet(String msg, long distanceFromSource) {
        return new OutboundEvent.Builder()
                .name("trumpet")
                .data(Trumpet.create(trumpetIdSupplier.get(), msg, distanceFromSource))
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}