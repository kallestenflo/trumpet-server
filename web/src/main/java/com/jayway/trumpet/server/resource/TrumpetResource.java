package com.jayway.trumpet.server.resource;


import com.jayway.trumpet.server.domain.TrumpeterRepository;
import com.jayway.trumpet.server.domain.Trumpeter;
import com.jayway.trumpet.server.domain.Location;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.SseFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import java.util.function.Supplier;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class TrumpetResource {

    private static final Logger logger = LoggerFactory.getLogger(TrumpetResource.class);

    private final int maxDistance;

    private final TrumpeterRepository trumpeterRepository;

    private final Supplier<WebApplicationException> trumpeterNotFound;

    public TrumpetResource(int maxDistance) {
        this.maxDistance = maxDistance;
        this.trumpeterRepository = new TrumpeterRepository();
        this.trumpeterNotFound = () -> new WebApplicationException("Trumpeter not found!", Response.Status.NOT_FOUND);
    }

    @GET
    public Response entrypoint(@Context UriInfo uriInfo,
                               @QueryParam("latitude") Double latitude,
                               @QueryParam("longitude") Double longitude) {

        Trumpeter trumpeter = trumpeterRepository.createTrumpeter(latitude, longitude);

        HalRepresentation entrypoint = new HalRepresentation();
        entrypoint.addLink("subscribe", uriInfo.getBaseUriBuilder().path("trumpeters").path(trumpeter.id).path("subscribe").build());
        entrypoint.addLink("location", uriInfo.getBaseUriBuilder().path("trumpeters").path(trumpeter.id).path("location").build());
        entrypoint.addLink("trumpet", uriInfo.getBaseUriBuilder().path("trumpeters").path(trumpeter.id).path("trumpet").build());

        return Response.ok(entrypoint).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("trumpeters/{id}/location")
    public Response location(@PathParam("id") String id,
                             @FormParam("latitude") Double latitude,
                             @FormParam("longitude") Double longitude) {

        trumpeterRepository.getById(id)
                .orElseThrow(trumpeterNotFound)
                .updateLocation(Location.create(latitude, longitude));

        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/trumpeters/{id}/trumpet")
    public Response trumpet(@PathParam("id") String id,
                            @FormParam("msg") String msg) {

        Trumpeter trumpeter = trumpeterRepository.getById(id).orElseThrow(trumpeterNotFound);

        trumpeterRepository.findTrumpetersInRangeOf(trumpeter, maxDistance).forEach(t -> t.trumpet(msg));

        return Response.ok().build();
    }

    @GET
    @Path("/trumpeters/{id}/subscribe")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput subscribe(final @PathParam("id") String id) {

        return trumpeterRepository.getById(id)
                .orElseThrow(trumpeterNotFound)
                .subscribe();
    }
}
