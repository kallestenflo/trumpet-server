package com.jayway.trumpet.server.rest;


import com.jayway.trumpet.server.boot.TrumpetDomainConfig;
import com.jayway.trumpet.server.domain.model.shared.Location;
import com.jayway.trumpet.server.domain.model.trumpeteer.Trumpeteer;
import com.jayway.trumpet.server.domain.model.trumpeteer.TrumpeteerRepository;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.SseFeature;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static java.util.Collections.singletonMap;
import static java.util.Objects.*;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class TrumpetResource {

    private static final Logger logger = LoggerFactory.getLogger(TrumpetResource.class);

    private final TrumpeteerRepository trumpeteerRepository;

    private final Supplier<WebApplicationException> trumpeteerNotFound;

    private final TrumpetDomainConfig config;

    public TrumpetResource(TrumpetDomainConfig config) {
        this.config = config;
        this.trumpeteerRepository = new TrumpeteerRepository(config);
        this.trumpeteerNotFound = () -> new WebApplicationException("Trumpeteer not found!", Response.Status.NOT_FOUND);
    }

    @GET
    public Response entryPoint(@Context UriInfo uriInfo,
                               @QueryParam("latitude")  @NotNull Double latitude,
                               @QueryParam("longitude") @NotNull Double longitude) {

        Trumpeteer trumpeteer = trumpeteerRepository.createTrumpeteer(latitude, longitude);

        HalRepresentation entryPoint = new HalRepresentation();
        entryPoint.put("trumpeteerId", trumpeteer.id);
        entryPoint.addLink("subscribe", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteer.id).path("subscribe").build());
        entryPoint.addLink("location", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteer.id).path("location").build());
        entryPoint.addLink("trumpet", uriInfo.getBaseUriBuilder().path("trumpeteers").path(trumpeteer.id).path("trumpet").build());

        return Response.ok(entryPoint).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("trumpeteers/{id}/location")
    public Response location(@PathParam("id") String id,
                             @FormParam("latitude")  @NotNull Double latitude,
                             @FormParam("longitude") @NotNull Double longitude) {

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id)
                .orElseThrow(trumpeteerNotFound)
                .updateLocation(Location.create(latitude, longitude));

        long trumpeteersInRange = trumpeteerRepository.countTrumpeteersInRangeOf(trumpeteer, config.trumpeteerMaxDistance());

        return Response.ok(singletonMap("trumpeteersInRange", trumpeteersInRange)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/trumpeteers/{id}/trumpet")
    public Response trumpet(@PathParam("id") String id,
                            @FormParam("message")  @NotBlank String message,
                            @FormParam("distance") @Min(1) Long distance) {

        distance = isNull(distance) ? config.trumpeteerMaxDistance() : min(distance, config.trumpeteerMaxDistance());

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id).orElseThrow(trumpeteerNotFound);

        trumpeteerRepository.findTrumpeteersInRangeOf(trumpeteer, distance)
                .forEach(tuple -> tuple.left.trumpet(message, tuple.right));

        return Response.ok().build();
    }

    @GET
    @Path("/trumpeteers/{id}/subscribe")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput subscribe(final @PathParam("id") String id) {

        return trumpeteerRepository.findById(id)
                .orElseThrow(trumpeteerNotFound)
                .subscribe();
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
}
