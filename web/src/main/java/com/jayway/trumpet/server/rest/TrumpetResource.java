package com.jayway.trumpet.server.rest;


import com.jayway.trumpet.server.boot.TrumpetServerConfig;
import com.jayway.trumpet.server.domain.Location;
import com.jayway.trumpet.server.domain.Trumpeter;
import com.jayway.trumpet.server.domain.TrumpeterRepository;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.SseFeature;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
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

import java.util.LongSummaryStatistics;
import java.util.function.Supplier;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class TrumpetResource {

    private static final Logger logger = LoggerFactory.getLogger(TrumpetResource.class);

    private static final String DEFAULT_DISTANCE = "200";

    private final TrumpeterRepository trumpeterRepository;

    private final Supplier<WebApplicationException> trumpeterNotFound;

    public TrumpetResource(TrumpetServerConfig config) {
        this.trumpeterRepository = new TrumpeterRepository(config);
        this.trumpeterNotFound = () -> new WebApplicationException("Trumpeter not found!", Response.Status.NOT_FOUND);
    }

    @GET
    public Response entryPoint(@Context UriInfo uriInfo,
                               @NotNull @QueryParam("latitude") Double latitude,
                               @NotNull @QueryParam("longitude") Double longitude) {

        Trumpeter trumpeter = trumpeterRepository.createTrumpeter(latitude, longitude);

        HalRepresentation entryPoint = new HalRepresentation();
        entryPoint.put("trumpeterId", trumpeter.id);
        entryPoint.addLink("subscribe", uriInfo.getBaseUriBuilder().path("trumpeters").path(trumpeter.id).path("subscribe").build());
        entryPoint.addLink("location", uriInfo.getBaseUriBuilder().path("trumpeters").path(trumpeter.id).path("location").build());
        entryPoint.addLink("trumpet", uriInfo.getBaseUriBuilder().path("trumpeters").path(trumpeter.id).path("trumpet").build());

        return Response.ok(entryPoint).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("trumpeters/{id}/location")
    public Response location(@PathParam("id") String id,
                             @NotNull @FormParam("latitude") Double latitude,
                             @NotNull @FormParam("longitude") Double longitude) {

        trumpeterRepository.findById(id)
                .orElseThrow(trumpeterNotFound)
                .updateLocation(Location.create(latitude, longitude));

        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/trumpeters/{id}/trumpet")
    public Response trumpet(@PathParam("id") String id,
                            @NotBlank @FormParam("msg") String msg,
                            @FormParam("distance") @DefaultValue(DEFAULT_DISTANCE) Long distance) {

        Trumpeter trumpeter = trumpeterRepository.findById(id).orElseThrow(trumpeterNotFound);

        //trumpeterRepository.findTrumpetersInRangeOf(trumpeter, distance).forEach(t -> t.trumpet(msg, Long.MIN_VALUE));


        trumpeterRepository.findTrumpetersWithDistanceInRangeOf(trumpeter, distance)
                .forEach(tuple -> tuple.left.trumpet(msg, tuple.right));

        return Response.ok().build();
    }

    @GET
    @Path("/trumpeters/{id}/subscribe")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput subscribe(final @PathParam("id") String id) {

        return trumpeterRepository.findById(id)
                .orElseThrow(trumpeterNotFound)
                .subscribe();
    }
}
