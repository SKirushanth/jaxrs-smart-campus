package com.smartcampus.resource;

import java.net.URI;

import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/sensors")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {
    private final DataStore dataStore = DataStore.getInstance();

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        Sensor created = dataStore.createSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
        return Response.created(location).entity(created).build();
    }

    @GET
    public Response listSensors(@QueryParam("type") String type) {
        return Response.ok(dataStore.getAllSensors(type)).build();
    }

    @Path("/{id}/readings")
    public SensorReadingResource sensorReadings(@PathParam("id") String sensorId) {
        dataStore.getSensorOrThrow(sensorId);
        return new SensorReadingResource(sensorId, dataStore);
    }
}



