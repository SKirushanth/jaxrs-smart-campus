package com.smartcampus.resource;

import java.net.URI;

import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/rooms")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {
    private final DataStore dataStore = DataStore.getInstance();

    @GET
    public Response listRooms() {
        return Response.ok(dataStore.getAllRooms()).build();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        Room created = dataStore.createRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
        return Response.created(location).entity(created).build();
    }

    @GET
    @Path("/{id}")
    public Response getRoomById(@PathParam("id") String id) {
        return Response.ok(dataStore.getRoomOrThrow(id)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        dataStore.deleteRoom(id);
        return Response.noContent().build();
    }
}



