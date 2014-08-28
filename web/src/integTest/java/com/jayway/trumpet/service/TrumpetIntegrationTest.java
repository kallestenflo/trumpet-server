package com.jayway.trumpet.service;


import com.jayway.fixture.ServerRunningRule;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;

public class TrumpetIntegrationTest {


    @ClassRule
    public static ServerRunningRule server = new ServerRunningRule();

    private Client client = ClientBuilder.newBuilder()
            .register(JacksonFeature.class)
            .register(SseFeature.class)
            .build();

    @Test
    public void trumpets_are_delivered() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        WebTarget target = client.target("http://localhost:" + server.port() + "/api")
                .queryParam("latitude", 1)
                .queryParam("longitude", 2);

        Map<String, Object> ep = target.request().get(Map.class);

        final String location = read(ep, "_links.location");
        final String subscribe = read(ep, "_links.subscribe");
        final String trumpet = read(ep, "_links.trumpet");


        target = client.target(location);

        Form form = new Form();
        form.param("latitude", "55.583985");
        form.param("longitude", "12.957578");

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
                        .put(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);


        Thread thread = new Thread(){
            @Override
            public void run() {
                WebTarget target = client.target(subscribe);

                EventInput eventInput = target.request().get(EventInput.class);
                while (!eventInput.isClosed()) {
                    final InboundEvent inboundEvent = eventInput.read();
                    if (inboundEvent == null) {
                        break;
                    }
                    Map<String, Object> in = inboundEvent.readData(Map.class);

                    assertThat(in.get("msg")).isEqualTo("hello");
                    latch.countDown();
                }
            }
        };
        thread.start();


        target = client.target(trumpet);

        form = new Form();
        form.param("msg", "hello");

        response = target.request(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));


        assertThat(response.getStatus()).isEqualTo(200);

        latch.await(1, TimeUnit.SECONDS);


    }
}
