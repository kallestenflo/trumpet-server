package com.jayway.fixture;

import com.jayway.trumpet.server.domain.Trumpet;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.jayway.jsonpath.JsonPath.read;

public class TrumpetClient {

    private Client client = ClientBuilder.newBuilder()
            .register(JacksonFeature.class)
            .register(SseFeature.class)
            .build();

    private final int port;

    private String locationUri;
    private String subscribeUri;
    private String trumpetUri;


    private final List<Trumpet> messages = new CopyOnWriteArrayList<>();


    public static TrumpetClient create(int port){
        return new TrumpetClient(port);
    }

    private TrumpetClient(int port) {
        this.port = port;
    }

    public TrumpetClient connect(Double latitude, Double longitude){
        WebTarget target = client.target("http://localhost:" + port + "/api")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude);

        Map<String, Object> ep = target.request().get(Map.class);

        locationUri = read(ep, "_links.location");
        subscribeUri = read(ep, "_links.subscribe");
        trumpetUri = read(ep, "_links.trumpet");

        Thread thread = new Thread(){
            @Override
            public void run() {
                WebTarget target = client.target(subscribeUri);

                EventInput eventInput = target.request().get(EventInput.class);
                while (!eventInput.isClosed()) {
                    final InboundEvent inboundEvent = eventInput.read();
                    if (inboundEvent == null) {
                        break;
                    }
                    Trumpet trumpet = inboundEvent.readData(Trumpet.class);

                    messages.add(trumpet);
                }
            }
        };
        thread.start();

        return this;
    }

    public List<Trumpet> messages(){
        return Collections.unmodifiableList(messages);
    }

    public void trumpet(String message){
        WebTarget target = client.target(trumpetUri);

        Form form = new Form();
        form.param("message", message);

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        if(response.getStatus() != 200){
            throw new TrumpetClientException(response);
        }
    }

    public void updateLocation(Double latitude, Double longitude){
        WebTarget target = client.target(locationUri);

        Form form = new Form();
        form.param("latitude", latitude.toString());
        form.param("longitude", longitude.toString());

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        if(response.getStatus() != 200){
            throw new RuntimeException("Failed to update location!");
        }
    }
}
