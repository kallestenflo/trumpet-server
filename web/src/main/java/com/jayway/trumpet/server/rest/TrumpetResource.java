package com.jayway.trumpet.server.rest;


import com.jayway.trumpet.server.boot.TrumpetDomainConfig;
import com.jayway.trumpet.server.domain.subscriber.Subscriber;
import com.jayway.trumpet.server.domain.subscriber.SubscriberOutput;
import com.jayway.trumpet.server.domain.subscriber.SubscriberRepository;
import com.jayway.trumpet.server.domain.trumpeteer.*;
import com.jayway.trumpet.server.infrastructure.subscription.gcm.GCMBroadcaster;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jayway.trumpet.server.domain.location.Location.location;
import static com.jayway.trumpet.server.rest.HalRepresentation.hal;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;

@Path("/")
@Produces({MediaType.APPLICATION_JSON, "application/HAL+json"})
public class TrumpetResource {

    private final Supplier<WebApplicationException> trumpeteerNotFound;

    private final TrumpetDomainConfig config;

    private final SubscriberRepository subscriberRepository;
    private final GCMBroadcaster gcmBroadcaster;

    private final TrumpeteerRepository trumpeteerRepository;
    private final TrumpetBroadcastService trumpetBroadcastService;
    private final TrumpetSubscriptionService trumpetSubscriptionService;

    public TrumpetResource(TrumpetDomainConfig config,
                           TrumpeteerRepository trumpeteerRepository,
                           SubscriberRepository subscriberRepository,
                           GCMBroadcaster gcmBroadcaster,
                           TrumpetBroadcastService trumpetBroadcastService,
                           TrumpetSubscriptionService trumpetSubscriptionService) {
        this.config = config;
        this.trumpeteerRepository = trumpeteerRepository;
        this.subscriberRepository = subscriberRepository;
        this.gcmBroadcaster = gcmBroadcaster;
        this.trumpetBroadcastService = trumpetBroadcastService;
        this.trumpetSubscriptionService = trumpetSubscriptionService;
        this.trumpeteerNotFound = () -> new WebApplicationException("Trumpeteer not found!", Response.Status.NOT_FOUND);
    }

    @GET
    public Response entryPoint(@Context UriInfo uriInfo,
                               @QueryParam("latitude") @NotNull Double latitude,
                               @QueryParam("longitude") @NotNull Double longitude,
                               @QueryParam("accuracy") Integer accuracy) {

        accuracy = Optional.ofNullable(accuracy).orElse(config.trumpeteerMaxDistance());

        Trumpeteer trumpeteer = trumpeteerRepository.createTrumpeteer(latitude, longitude, accuracy);

        HalRepresentation entryPoint = hal();
        entryPoint.put("trumpeteerId", trumpeteer.id);
        entryPoint.addLink("subscriptions", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteer.id).path("subscriptions").build());
        entryPoint.addLink("location", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteer.id).path("location").build());
        entryPoint.addLink("trumpet", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteer.id).path("trumpets").build());
        entryPoint.addLink("me", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteer.id).build());

        return Response.ok(entryPoint).build();
    }

    @GET
    @Path("trumpeteers/{id}")
    public Response me(@PathParam("id") String id,
                       @QueryParam("distance") Integer distance) {

        distance = Optional.ofNullable(distance).orElse(config.trumpeteerMaxDistance());

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id)
                .orElseThrow(trumpeteerNotFound);

        int count = trumpeteerRepository.countTrumpeteersInRangeOf(trumpeteer, distance);

        HalRepresentation representation = hal();
        representation.put("trumpeteersInRange", count);

        return Response.ok(representation).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("trumpeteers/{id}/location")
    public Response updateLocation(@PathParam("id") String id,
                                   @FormParam("latitude") @NotNull Double latitude,
                                   @FormParam("longitude") @NotNull Double longitude,
                                   @FormParam("accuracy") Integer accuracy) {

        accuracy = Optional.ofNullable(accuracy).orElse(config.trumpeteerMaxDistance());

        trumpeteerRepository.findById(id)
                .orElseThrow(trumpeteerNotFound)
                .updateLocation(location(latitude, longitude, accuracy));

        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/trumpeteers/{id}/trumpets")
    public Response trumpet(@Context UriInfo uriInfo,
                            @PathParam("id") String id,
                            @FormParam("message") @NotBlank String message,
                            @FormParam("topic") @NotBlank @DefaultValue("*") String topic,
                            @FormParam("distance") @Min(1) Integer distance) {

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id).orElseThrow(trumpeteerNotFound);

        Consumer<Trumpet> broadcaster = t -> {
            HalRepresentation trumpetPayload = createTrumpetPayload(uriInfo, t);
            trumpetBroadcastService.broadcast(t, trumpetPayload);
        };

        String trumpetId = UUID.randomUUID().toString();

        trumpeteer.trumpet(trumpetId, message, topic, Optional.ofNullable(distance), trumpeteersWithSubscription(), broadcaster);

        return Response.ok(singletonMap("trumpetId", trumpetId)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/trumpeteers/{id}/echoes")
    public Response echoes(@Context UriInfo uriInfo,
                           @PathParam("id") String id,
                           @FormParam("trumpetId") @NotBlank String trumpetId,
                           @FormParam("message") @NotBlank String message,
                           @FormParam("topic") @NotBlank @DefaultValue("*") String topic,
                           @FormParam("distance") @Min(1) Integer distance) {

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id).orElseThrow(trumpeteerNotFound);

        Consumer<Trumpet> broadcaster = t -> {
            HalRepresentation trumpetPayload = createTrumpetPayload(uriInfo, t);
            trumpetBroadcastService.broadcast(t, trumpetPayload);
        };

        trumpeteer.echo(trumpetId, message, topic, Optional.ofNullable(distance), trumpeteersWithSubscription(), broadcaster);

        return Response.ok(singletonMap("trumpetId", trumpetId)).build();
    }

    @POST
    @Path("/trumpeteers/{id}/subscriptions")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response subscriptions(@Context UriInfo uriInfo,
                                  final @PathParam("id") String id,
                                  final @FormParam("type") @DefaultValue("sse") String type,
                                  final @FormParam("registrationID") String registrationId) {

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id).orElseThrow(trumpeteerNotFound);

        HalRepresentation entity = hal().withLinks();
        entity.put("type", type);

        final Subscriber subscriber;
        switch (type) {
            case "sse":
                subscriber = createSSESubscriber(trumpeteer.id);
                entity.addLink("subscription", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteer.id).path("subscriptions/sse").build());
                break;
            case "gcm":
                subscriber = createGCMSubscriber(trumpeteer.id, registrationId);
                break;
            default:
                throw new IllegalArgumentException(format("Invalid subscription type: %s. Valid types are: %s.", type, String.join(",", "sse", "gcm")));
        }

        trumpetSubscriptionService.subscribe(subscriber);

        return Response.ok(entity).build();
    }

    @GET
    @Path("/trumpeteers/{id}/subscriptions/sse")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public Response subscribeSSE(final @PathParam("id") String id) {
        EventOutput channel = (EventOutput) subscriberRepository.findById(id).orElseThrow(trumpeteerNotFound).output().channel().get();
        return Response.ok(channel).build();
    }

    @GET
    @Path("/trumpeteers")
    public Response trumpeteers() {
        List<Map<String, Object>> trumpeteers = trumpeteerRepository.findAll().map(t -> {
            Map<String, Object> mapped = new HashMap<>();
            mapped.put("id", t.id);
            mapped.put("latitude", t.location.latitude);
            mapped.put("longitude", t.location.longitude);
            return mapped;
        }).collect(Collectors.toList());

        return Response.ok(trumpeteers).build();
    }

    private Subscriber createSSESubscriber(String id) {
        final EventOutput output = new EventOutput();

        SubscriberOutput subscriberOutput = new SubscriberOutput() {
            @Override
            public void write(Map<String, Object> message) throws IOException {
                OutboundEvent outboundEvent = new OutboundEvent.Builder()
                        .name("trumpet")
                        .data(message)
                        .mediaType(MediaType.APPLICATION_JSON_TYPE)
                        .build();
                output.write(outboundEvent);
            }

            @Override
            public boolean isClosed() {
                return output.isClosed();
            }

            @Override
            public void close() {
                try {
                    output.close();
                } catch (IOException ignore) {
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> Optional<T> channel() {
                return Optional.of((T) output);
            }
        };
        return subscriberRepository.create(id, UUID.randomUUID().toString(), subscriberOutput);
    }

    private Subscriber createGCMSubscriber(String trumpeteerId, String registrationId) {
        SubscriberOutput subscriberOutput = new SubscriberOutput() {
            @Override
            public void write(Map<String, Object> trumpet) throws IOException {
                gcmBroadcaster.publish(registrationId, trumpet);
            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void close() {

            }

            @Override
            public <T> Optional<T> channel() {
                return Optional.empty();
            }
        };
        return subscriberRepository.create(trumpeteerId, registrationId, subscriberOutput);
    }

    private HalRepresentation createTrumpetPayload(UriInfo uriInfo, Trumpet t) {
        HalRepresentation trumpetPayload = new HalRepresentation();
        trumpetPayload.put("id", t.id);
        trumpetPayload.put("timestamp", t.timestamp);
        trumpetPayload.put("message", t.message);
        t.topic.ifPresent(topic -> trumpetPayload.put("topic", topic));
        trumpetPayload.put("distanceFromSource", t.distanceFromSource);
        trumpetPayload.put("accuracy", t.trumpeteer.location.accuracy);
        trumpetPayload.put("sentByMe", t.sentByMe);
        trumpetPayload.addLink("echo", uriInfo.getBaseUriBuilder()
                .path("trumpeteers")
                .path(t.trumpeteer.id)
                .path("echoes")
                .queryParam("trumpetId", t.id)
                .queryParam("message", t.message)
                .queryParam("topic", t.topic)
                .build());
        return trumpetPayload;
    }

    private Stream<Trumpeteer> trumpeteersWithSubscription() {
        return trumpeteerRepository.findAll().filter(t -> subscriberRepository.findById(t.id).isPresent());
    }
}
