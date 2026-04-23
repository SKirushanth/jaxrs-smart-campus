package com.smartcampus.resource;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    @GET
    public Response discover(@Context UriInfo uriInfo) {
        String baseUrl = uriInfo.getBaseUri().toString();

        Map<String, Object> payload = Map.of(
                "api", Map.of(
                        "name", "Smart Campus Sensor & Room Management REST API",
                        "module", "Client-Server Architectures (5COSC022W)",
                        "version", "v1",
                        "baseUrl", baseUrl),
                "adminContact", Map.of(
                        "owner", "Smart Campus API Team",
                        "email", "smartcampus-api@westminster.ac.uk",
                        "supportHours", "Mon-Fri 09:00-17:00"),
                "resources", Map.of(
                        "rooms", "/api/v1/rooms",
                        "sensors", "/api/v1/sensors",
                        "readings", "/api/v1/sensors/{sensorId}/readings"));

        return Response.ok(payload).build();
    }
}


