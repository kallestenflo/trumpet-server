package com.jayway.trumpet.server.rest;


import com.jayway.trumpet.server.boot.TrumpetDomainConfig;
import com.jayway.trumpet.server.domain.Location;
import com.jayway.trumpet.server.domain.Trumpeteer;
import com.jayway.trumpet.server.domain.TrumpeteerRepository;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.SseFeature;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import java.util.Collections;
import java.util.function.Supplier;

import static java.util.Collections.singletonMap;

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
                               @NotNull @QueryParam("latitude") Double latitude,
                               @NotNull @QueryParam("longitude") Double longitude) {

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
                             @NotNull @FormParam("latitude") Double latitude,
                             @NotNull @FormParam("longitude") Double longitude) {

        Trumpeteer trumpeteer = trumpeteerRepository.findById(id)
                .orElseThrow(trumpeteerNotFound)
                .updateLocation(Location.create(latitude, longitude));

        long trumpeteersInRange = trumpeteerRepository.countTrumpeteersInRangeOf(trumpeteer, config.trumpeteerMaxDistancd());

        return Response.ok(singletonMap("trumpeteersInRange", trumpeteersInRange)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/trumpeteers/{id}/trumpet")
    public Response trumpet(@PathParam("id") String id,
                            @NotBlank @FormParam("message") String message,
                            @FormParam("distance") Long distance) {

        if(distance == null){
            distance = config.trumpeteerMaxDistancd();
        }

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
}
