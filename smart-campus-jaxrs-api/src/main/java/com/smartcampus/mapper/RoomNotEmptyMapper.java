package com.smartcampus.mapper;

import java.util.Map;

import com.smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, Object> payload = Map.of(
                "status", Response.Status.CONFLICT.getStatusCode(),
                "error", "Conflict",
                "message", exception.getMessage());

        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(payload)
                .build();
    }
}



