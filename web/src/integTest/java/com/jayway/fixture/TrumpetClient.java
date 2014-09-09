package com.jayway.fixture;

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

    private final String host;
    private final int port;

    private String locationUri;
    private String subscribeUri;
    private String trumpetUri;
    private String inRangeUri;


    private final List<TrumpetMessage> messages = new CopyOnWriteArrayList<>();


    public static TrumpetClient create(int port){
        return create("localhost", port);
    }

    public static TrumpetClient create(String host, int port){
        return new TrumpetClient(host, port);
    }

    private TrumpetClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean hasReceived(int numberOfMessages){
        return messages.size() == numberOfMessages;
    }

    public TrumpetClient connect(Double latitude, Double longitude){
        WebTarget target = client.target("http://" + host + ":" + port + "/api")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude);

        Map<String, Object> ep = target.request().get(Map.class);

        locationUri = read(ep, "_links.location.href");
        subscribeUri = read(ep, "_links.subscribe.href");
        trumpetUri = read(ep, "_links.trumpet.href");
        inRangeUri = read(ep, "_links.in-range.href");

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
                    Map<String, Object> trumpet = inboundEvent.readData(Map.class);

                    messages.add(new TrumpetMessage(trumpet));
                }
            }
        };
        thread.start();

        return this;
    }

    public List<TrumpetMessage> messages(){
        return Collections.unmodifiableList(messages);
    }

    public TrumpetMessage message(int index){
        return messages.get(index);
    }

    public int countAdjacentTrumpeteers(){
        WebTarget target = client.target(inRangeUri);

        Response response = target.request().get();

        if(response.getStatus() != 200){
            throw new TrumpetClientException(response);
        }
        Map<String, Object> entity = response.readEntity(Map.class);

        return (Integer) entity.get("count");
    }

    public Map<String, Object> echo(TrumpetMessage message){

        WebTarget target = client.target(message.echoUri());

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
                .header("content-type", MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(null);

        if(response.getStatus() != 200){
            throw new TrumpetClientException(response);
        }

        return response.readEntity(Map.class);
    }

    public Map<String, Object> trumpet(String message){
        WebTarget target = client.target(trumpetUri);

        Form form = new Form();
        form.param("message", message);

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        if(response.getStatus() != 200){
            throw new TrumpetClientException(response);
        }

        return response.readEntity(Map.class);
    }

    public long updateLocation(Double latitude, Double longitude){
        WebTarget target = client.target(locationUri);

        Form form = new Form();
        form.param("latitude", latitude.toString());
        form.param("longitude", longitude.toString());

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        Map<String, Object> entity = response.readEntity(Map.class);

        Integer trumpeteersInRange = read(entity, "trumpeteersInRange");

        if(response.getStatus() != 200){
            throw new RuntimeException("Failed to update location!");
        }

        return trumpeteersInRange.longValue();
    }
}
