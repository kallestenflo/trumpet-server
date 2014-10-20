package com.jayway.trumpet.server.rest;


import com.jayway.trumpet.server.domain.location.Location;
import com.jayway.trumpet.server.domain.subscriber.SubscriberOutput;
import com.jayway.trumpet.server.domain.subscriber.Trumpeteer;
import com.jayway.trumpet.server.domain.subscriber.TrumpeteerRepository;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpetBroadcastService;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpetSubscriptionService;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpeteerConfig;
import com.jayway.trumpet.server.infrastructure.subscription.gcm.GCMBroadcaster;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.hibernate.validator.constraints.NotBlank;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.jayway.trumpet.server.domain.location.Location.location;
import static com.jayway.trumpet.server.rest.HalRepresentation.hal;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;

@Path("/")
@Produces({MediaType.APPLICATION_JSON, "application/HAL+json"})
public class TrumpetResource {

    public static final String EXT = "ext";
    private final Supplier<WebApplicationException> trumpeteerNotFound;

    private final TrumpeteerConfig config;

    private final TrumpeteerRepository trumpeteerRepository;
    private final GCMBroadcaster gcmBroadcaster;

    private final TrumpetBroadcastService trumpetBroadcastService;
    private final TrumpetSubscriptionService trumpetSubscriptionService;

    public TrumpetResource(TrumpeteerConfig config,
                           TrumpeteerRepository trumpeteerRepository,
                           GCMBroadcaster gcmBroadcaster,
                           TrumpetBroadcastService trumpetBroadcastService,
                           TrumpetSubscriptionService trumpetSubscriptionService) {
        this.config = config;
        this.trumpeteerRepository = trumpeteerRepository;
        this.gcmBroadcaster = gcmBroadcaster;
        this.trumpetBroadcastService = trumpetBroadcastService;
        this.trumpetSubscriptionService = trumpetSubscriptionService;
        this.trumpeteerNotFound = () -> new WebApplicationException("Trumpeteer not found!", Response.Status.NOT_FOUND);
    }

    @GET
    public Response entryPoint(@Context UriInfo uriInfo) {
        HalRepresentation entryPoint = hal();
        entryPoint.addLink("create-trumpeteer", uriInfo.getBaseUriBuilder().path("trumpeteers").build());
        return Response.ok(entryPoint).build();
    }

    @GET
    @Path("trumpeteers/{id}")
    public Response me(@PathParam("id") String id,
                       @QueryParam("distance") Integer requestedDistance) {

        requestedDistance = Optional.ofNullable(requestedDistance).orElse(config.trumpeteerMaxDistance());

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id)
                .orElseThrow(trumpeteerNotFound);

        int count = trumpeteerRepository.countTrumpeteersInRangeOf(trumpeteer, requestedDistance);

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

        int requestedDistance = config.trumpeteerMaxDistance();
        accuracy = Optional.ofNullable(accuracy).orElse(requestedDistance);

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id).orElseThrow(trumpeteerNotFound);

        int numberOfTrumpeteersInRangeBeforeUpdate = trumpeteerRepository.countTrumpeteersInRangeOf(trumpeteer, requestedDistance);

        trumpeteer.updateLocation(location(latitude, longitude, accuracy));

        if (numberOfTrumpeteersInRangeBeforeUpdate == 0) {
            int numberOfTrumpeteersInRangeAfterUpdate = trumpeteerRepository.countTrumpeteersInRangeOf(trumpeteer, requestedDistance);
            if (numberOfTrumpeteersInRangeAfterUpdate > 0) {
                String trumpetId = UUID.randomUUID().toString();
                int numberOfNewTrumpeteers = numberOfTrumpeteersInRangeAfterUpdate - numberOfTrumpeteersInRangeBeforeUpdate;

                Map<String, String> extParameters = new HashMap<>();
                extParameters.put("trumpeteersDiscovered", String.valueOf(numberOfNewTrumpeteers));

                trumpeteer.trumpet(trumpetId, "", "*", extParameters, Optional.empty(), trumpetBroadcastService::broadcast);
            }
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/trumpeteers/{id}/trumpets")
    public Response trumpet(@Context UriInfo uriInfo,
                            @Context HttpServletRequest request,
                            @PathParam("id") String id,
                            @FormParam("message") @NotBlank String message,
                            @FormParam("topic") @NotBlank @DefaultValue("*") String topic,
                            @FormParam("distance") @Min(1) Integer distance) {

        if (message.length() > config.maxMessageLength()) {
            throw createWebApplicationException(format("Message to long! Max length is %s", config.maxMessageLength()), Response.Status.BAD_REQUEST);
        }

        Map<String, String> extParameters = FormParameters.getPrefixedParameters(request, EXT);

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id).orElseThrow(trumpeteerNotFound);

        String trumpetId = UUID.randomUUID().toString();

        trumpeteer.trumpet(trumpetId, message, topic, extParameters, Optional.ofNullable(distance), trumpetBroadcastService::broadcast);

        return Response.ok(singletonMap("trumpetId", trumpetId)).build();
    }

    private WebApplicationException createWebApplicationException(String message, Response.Status status) {
        return new WebApplicationException(message, status);
    }

    @POST
    @Path("/trumpeteers")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createTrumpeteer(@Context UriInfo uriInfo,
                                     final @FormParam("latitude") @NotNull Double latitude,
                                     final @FormParam("longitude") @NotNull Double longitude,
                                     @FormParam("accuracy") Integer accuracy,
                                     final @FormParam("type") @DefaultValue("sse") String type,
                                     final @FormParam("registrationID") String registrationId) {
        accuracy = Optional.ofNullable(accuracy).orElse(config.trumpeteerMaxDistance());
        final String trumpeteerId = UUID.randomUUID().toString();
        final Location location = Location.location(latitude, longitude, accuracy);

        HalRepresentation entity = hal().withLinks();
        entity.put("type", type);
        entity.put("trumpeteerId", trumpeteerId);
        entity.put("maxMessageLength", config.maxMessageLength());
        entity.put("maxDistance", config.trumpeteerMaxDistance());

        final Trumpeteer trumpeteer;
        switch (type) {
            case "sse":
                trumpeteer = createSSETrumpeteer(trumpeteerId, location);
                entity.addLink("sse-subscribe", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteerId).path("subscriptions/sse").build());
                break;
            case "gcm":
                trumpeteer = createGCMTrumpeteer(trumpeteerId, registrationId, location);
                break;
            default:
                throw new IllegalArgumentException(format("Invalid subscription type: %s. Valid types are: %s.", type, String.join(",", "sse", "gcm")));
        }

        trumpetSubscriptionService.subscribe(trumpeteer);

        entity.addLink("update-location", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteerId).path("location").build());
        entity.addLink("trumpet", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteerId).path("trumpets").build());
        entity.addLink("self", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteerId).build());

        return Response.ok(entity).build();
    }

    @GET
    @Path("/trumpeteers/{id}/subscriptions/sse")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public Response subscribeSSE(final @PathParam("id") String id) {
        EventOutput channel = (EventOutput) trumpeteerRepository.findById(id).orElseThrow(trumpeteerNotFound).output().channel().get();
        return Response.ok(channel).build();
    }

    @DELETE
    @Path("/trumpeteers/{id}")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public Response deleteTrumpeteer(final @PathParam("id") String id) {
        trumpeteerRepository.delete(id);
        return Response.ok().build();
    }

    @GET
    @Path("/trumpeteers")
    public Response trumpeteers() {
        List<Map<String, Object>> trumpeteers = trumpeteerRepository.findAll().map(t -> {
            Map<String, Object> mapped = new HashMap<>();
            mapped.put("id", t.id());
            mapped.put("latitude", t.location().latitude);
            mapped.put("longitude", t.location().longitude);
            return mapped;
        }).collect(Collectors.toList());

        return Response.ok(trumpeteers).build();
    }

    private Trumpeteer createSSETrumpeteer(String id, Location location) {
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
        return trumpeteerRepository.create(id, UUID.randomUUID().toString(), location, subscriberOutput);
    }

    private Trumpeteer createGCMTrumpeteer(String trumpeteerId, String registrationId, Location location) {
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
        return trumpeteerRepository.create(trumpeteerId, registrationId, location, subscriberOutput);
    }


}
