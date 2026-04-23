package com.smartcampus.mapper;

import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException webApplicationException) {
            int status = webApplicationException.getResponse().getStatus();
            Map<String, Object> payload = Map.of(
                    "status", status,
                    "error", Response.Status.fromStatusCode(status) == null
                            ? "HTTP Error"
                            : Response.Status.fromStatusCode(status).getReasonPhrase(),
                    "message", webApplicationException.getMessage());

            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(payload)
                    .build();
        }

        Map<String, Object> payload = Map.of(
                "status", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "error", "Internal Server Error",
                "message", "An unexpected server error occurred.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(payload)
                .build();
    }
}



