package com.jayway.fixture;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
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

    private EventSource eventSource;

    private final String host;
    private final int port;

    private String updateLocationUri;
    private String trumpetUri;
    private String selfUri;
    private Map<String, Object> trumpeteer;

    private final List<TrumpetMessage> trumpetMessages = new CopyOnWriteArrayList<>();
    private final List<InRangeMessage> inRangeMessages = new CopyOnWriteArrayList<>();


    public static TrumpetClient create(int port) {
        return create("localhost", port);
    }

    public static TrumpetClient create(String host, int port) {
        return new TrumpetClient(host, port);
    }

    private TrumpetClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Map<String, Object> trumpeteer() {
        return trumpeteer;
    }

    public boolean hasReceived(int numberOfMessages) {
        return trumpetMessages.size() == numberOfMessages;
    }

    public void diconnect() {
        if (eventSource != null && eventSource.isOpen()) {
            eventSource.close();
            eventSource = null;
        }
    }

    public boolean isConnected() {
        return eventSource != null && eventSource.isOpen();
    }

    public TrumpetClient connect(Double latitude, Double longitude) {
        WebTarget target = client.target("http://" + host + ":" + port + "/api");

        Map<String, Object> ep = target.request().get(Map.class);
        String createTrumpeteerUri = read(ep, "_links.create-trumpeteer.href");

        Form form = new Form();
        form.param("type", "sse");
        form.param("latitude", Double.toString(latitude));
        form.param("longitude", Double.toString(longitude));

        trumpeteer = client.target(createTrumpeteerUri).request(MediaType.APPLICATION_JSON).post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Map.class);
        String subscriptionUri = read(trumpeteer, "_links.sse-subscribe.href");
        updateLocationUri = read(trumpeteer, "_links.update-location.href");
        trumpetUri = read(trumpeteer, "_links.trumpet.href");
        selfUri = read(trumpeteer, "_links.self.href");

        eventSource = EventSource.target(client.target(subscriptionUri)).usePersistentConnections().build();
        EventListener listener = (e) -> {
            Map<String, Object> payload = e.readData(Map.class);
            if("trumpet".equals(payload.get("messageType"))){
                trumpetMessages.add(new TrumpetMessage(e.readData(Map.class)));
            }
            else if("trumpeteersInRange".equals(payload.get("messageType"))){
                inRangeMessages.add(new InRangeMessage(e.readData(Map.class)));
            }

        };
        eventSource.register(listener, "trumpet");
        eventSource.open();

        return this;
    }

    public List<TrumpetMessage> trumpetMessages() {
        return Collections.unmodifiableList(trumpetMessages);
    }

    public List<InRangeMessage> inRangeMessages() {
        return Collections.unmodifiableList(inRangeMessages);
    }

    public TrumpetMessage lastMessage() {
        return trumpetMessages.isEmpty() ? null : trumpetMessages.get(trumpetMessages.size() - 1);
    }

    public TrumpetMessage message(int index) {
        return trumpetMessages.get(index);
    }

    public int countTrumpeteersInRange() {
        WebTarget target = client.target(selfUri);

        Response response = target.request().get();

        if (response.getStatus() != 200) {
            throw new TrumpetClientException(response);
        }
        Map<String, Object> entity = response.readEntity(Map.class);

        return (Integer) entity.get("trumpeteersInRange");
    }

    public Map<String, Object> echo(TrumpetMessage message) {

        WebTarget target = client.target(message.echoUri());

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
                .header("content-type", MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(null);

        if (response.getStatus() != 200) {
            throw new TrumpetClientException(response);
        }


        return response.readEntity(Map.class);
    }

    public Map<String, Object> trumpet(String message, Map<String, String> extParameters) {
        WebTarget target = client.target(trumpetUri);

        Form form = new Form();
        form.param("message", message);

        for (Map.Entry<String, String> extParam : extParameters.entrySet()) {
            form.param(extParam.getKey(), extParam.getValue());
        }

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        if (response.getStatus() != 200) {
            throw new TrumpetClientException(response);
        }

        return response.readEntity(Map.class);
    }

    public void trumpet(String message) {
        this.trumpet(message, Collections.emptyMap());
    }

    public void updateLocation(Double latitude, Double longitude) {
        WebTarget target = client.target(updateLocationUri);

        Form form = new Form();
        form.param("latitude", latitude.toString());
        form.param("longitude", longitude.toString());

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed to update location!");
        }
    }

    public void delete() {
        client.target(selfUri).request().delete().getStatus();
    }


}
