package com.smartcampus.mapper;

import java.util.Map;

import com.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LinkedResourceMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, Object> payload = Map.of(
                "status", 422,
                "error", "Unprocessable Entity",
                "message", exception.getMessage());

        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(payload)
                .build();
    }
}



