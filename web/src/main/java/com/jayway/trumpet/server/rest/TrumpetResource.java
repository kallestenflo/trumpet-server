package com.jayway.trumpet.server.rest;


import com.jayway.trumpet.server.boot.TrumpetDomainConfig;
import com.jayway.trumpet.server.domain.trumpeteer.Subscriber;
import com.jayway.trumpet.server.domain.trumpeteer.Trumpet;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpetBroadcastService;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpetSubscriptionService;
import com.jayway.trumpet.server.domain.trumpeteer.Trumpeteer;
import com.jayway.trumpet.server.domain.trumpeteer.TrumpeteerRepository;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.SseFeature;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.jayway.trumpet.server.domain.location.Location.location;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;

@Path("/")
@Produces({MediaType.APPLICATION_JSON, "application/HAL+json"})
public class TrumpetResource {

    private static final Logger logger = LoggerFactory.getLogger(TrumpetResource.class);

    private static final String TYPE_COUNT = "count";

    private final TrumpeteerRepository trumpeteerRepository;

    private final Supplier<WebApplicationException> trumpeteerNotFound;

    private final TrumpetDomainConfig config;
    private final TrumpetBroadcastService trumpetBroadcastService;
    private final TrumpetSubscriptionService trumpetSubscriptionService;

    public TrumpetResource(TrumpetDomainConfig config, TrumpetBroadcastService trumpetBroadcastService,
                           TrumpetSubscriptionService trumpetSubscriptionService) {
        this.config = config;
        this.trumpetBroadcastService = trumpetBroadcastService;
        this.trumpetSubscriptionService = trumpetSubscriptionService;
        this.trumpeteerRepository = new TrumpeteerRepository(config);
        this.trumpeteerNotFound = () -> new WebApplicationException("Trumpeteer not found!", Response.Status.NOT_FOUND);
    }

    @GET
    public Response entryPoint(@Context UriInfo uriInfo,
                               @QueryParam("latitude")  @NotNull Double latitude,
                               @QueryParam("longitude") @NotNull Double longitude,
                               @QueryParam("accuracy")  Integer accuracy) {

        accuracy = Optional.ofNullable(accuracy).orElse(config.trumpeteerMaxDistance());

        Trumpeteer trumpeteer = trumpeteerRepository.createTrumpeteer(latitude, longitude, accuracy);

        HalRepresentation entryPoint = new HalRepresentation();
        entryPoint.put("trumpeteerId", trumpeteer.id);
        entryPoint.addLink("subscribe", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteer.id).path("subscription").build());
        entryPoint.addLink("location", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteer.id).path("location").build());
        entryPoint.addLink("trumpet", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteer.id).path("trumpets").build());
        entryPoint.addLink("in-range", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteer.id).path("adjacent").build());
        entryPoint.addLink("self", uriInfo.getRequestUri());

        return Response.ok(entryPoint).build();
    }

    @GET
    @Path("trumpeteers/{id}/adjacent")
    public Response adjacent(@PathParam("id") String id,
                             @QueryParam("type") @DefaultValue(TYPE_COUNT) String type,
                             @QueryParam("distance") Integer distance) {

        if(!TYPE_COUNT.equals(type)){
            throw new IllegalArgumentException(format("Type '%s' not supported", type));
        }

        distance = Optional.ofNullable(distance).orElse(config.trumpeteerMaxDistance());

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id)
                .orElseThrow(trumpeteerNotFound);

        int count = trumpeteerRepository.countTrumpeteersInRangeOf(trumpeteer, distance);

        HalRepresentation representation = new HalRepresentation();
        representation.put("count", count);

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

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id)
                .orElseThrow(trumpeteerNotFound)
                .updateLocation(location(latitude, longitude, accuracy));

        int trumpeteersInRange = trumpeteerRepository.countTrumpeteersInRangeOf(trumpeteer, config.trumpeteerMaxDistance());

        return Response.ok(singletonMap("trumpeteersInRange", trumpeteersInRange)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/trumpeteers/{id}/trumpets")
    public Response trumpet(@Context UriInfo uriInfo,
                            @PathParam("id") String id,
                            @FormParam("message") @NotBlank String message,
                            @FormParam("distance") @Min(1) Integer distance) {

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id).orElseThrow(trumpeteerNotFound);

        Consumer<Trumpet> broadcaster = t -> {
            HalRepresentation trumpetPayload = createTrumpetPayload(uriInfo, message, trumpeteer, t);
            trumpetBroadcastService.broadcast(t, trumpetPayload);
        };

        String trumpetId = UUID.randomUUID().toString();

        trumpeteer.trumpet(trumpetId, message, Optional.ofNullable(distance), trumpeteerRepository.findAll(), broadcaster);

        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/trumpeteers/{id}/echoes")
    public Response echoes(@Context UriInfo uriInfo,
                            @PathParam("id") String id,
                            @FormParam("trumpedId") @NotBlank String trumpedId,
                            @FormParam("message") @NotBlank String message,
                            @FormParam("distance") @Min(1) Integer distance) {

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id).orElseThrow(trumpeteerNotFound);

        Consumer<Trumpet> broadcaster = t -> {
            HalRepresentation trumpetPayload = createTrumpetPayload(uriInfo, message, trumpeteer, t);
            trumpetBroadcastService.broadcast(t, trumpetPayload);
        };

        trumpeteer.echo(trumpedId, message, Optional.ofNullable(distance), trumpeteerRepository.findAll(), broadcaster);

        return Response.ok().build();
    }

    @GET
    @Path("/trumpeteers/{id}/subscription")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput subscription(final @PathParam("id") String id) {

        EventOutput channel = new EventOutput();
        Subscriber subscriber = trumpeteerRepository.findById(id)
                .map(t -> new Subscriber(t.id, channel))
                .orElseThrow(trumpeteerNotFound);

        trumpetSubscriptionService.subscribe(subscriber);
        return channel;
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

    private HalRepresentation createTrumpetPayload(UriInfo uriInfo, String message, Trumpeteer trumpeteer, Trumpet t) {
        HalRepresentation trumpetPayload = new HalRepresentation();
        trumpetPayload.put("id", t.id);
        trumpetPayload.put("timestamp", t.timestamp);
        trumpetPayload.put("message", t.message);
        trumpetPayload.put("distanceFromSource", t.distanceFromSource);
        trumpetPayload.put("accuracy", trumpeteer.location.accuracy);
        trumpetPayload.addLink("echo", uriInfo.getBaseUriBuilder()
                .path("trumpeteers")
                .path(trumpeteer.id)
                .path("echoes")
                .queryParam("message", message)
                .queryParam("trumpedId", t.id)
                .build());
        return trumpetPayload;
    }
}
