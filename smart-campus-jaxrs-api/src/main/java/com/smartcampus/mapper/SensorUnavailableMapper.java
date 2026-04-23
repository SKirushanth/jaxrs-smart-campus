package com.smartcampus.mapper;

import java.util.Map;

import com.smartcampus.exception.SensorUnavailableException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, Object> payload = Map.of(
                "status", Response.Status.FORBIDDEN.getStatusCode(),
                "error", "Forbidden",
                "message", exception.getMessage());

        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(payload)
                .build();
    }
}



