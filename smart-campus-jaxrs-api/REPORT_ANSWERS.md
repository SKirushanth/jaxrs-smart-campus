# Report and Viva Answers

## 1) Explain the architecture used in this API

The API follows a layered JAX-RS architecture:

- Resource layer: receives HTTP requests and returns HTTP responses.
- Store layer: contains in-memory data and business rules.
- Model layer: defines JSON-serializable domain entities.
- Exception layer: custom domain exceptions.
- Mapper layer: converts exceptions into consistent HTTP JSON error payloads.
- Filter layer: cross-cutting request/response logging.

This separation improves maintainability and testability by keeping concerns isolated.

## 2) Why use JAX-RS and not Spring Boot?

The coursework explicitly requires JAX-RS and prohibits Spring Boot. JAX-RS also provides a standards-based way to build REST APIs using annotations (`@Path`, `@GET`, `@POST`, `@DELETE`) while keeping dependencies lightweight.

## 3) How is REST implemented correctly here?

REST principles are addressed by:

- Noun-based URIs (`/rooms`, `/sensors`, `/sensors/{id}/readings`)
- Correct HTTP methods for resource actions
- Stateless request processing
- Standard status codes for outcome semantics
- JSON representation for resource transfer

## 4) Why is `/sensors/{id}/readings` a sub-resource?

A reading has no meaning without its parent sensor, so modeling readings as a nested sub-resource reflects domain ownership and improves URI clarity.

## 5) Why are specific status codes used?

- `409 Conflict`: deleting a room that still has linked sensors.
- `422 Unprocessable Entity`: creating a sensor with non-existing room reference.
- `403 Forbidden`: adding readings while sensor is in `MAINTENANCE`.
- `500 Internal Server Error`: unhandled server exceptions.

## 6) What is the role of ExceptionMapper?

Exception mappers centralize error handling and ensure all failures return consistent JSON payloads. This avoids duplicated try/catch blocks in resources and makes client behavior predictable.

## 7) Why in-memory store instead of a database?

The module constraints explicitly disallow database usage. `ConcurrentHashMap` and in-memory lists satisfy this while demonstrating core client-server and REST design.

## 8) What are limitations of in-memory approach?

- Data loss on application restart
- No durable transactions
- Limited horizontal scalability
- Not suitable for production persistence requirements

## 9) How is API logging handled?

A JAX-RS filter intercepts every request and response:

- Logs request method and full URL
- Logs response status code

This helps debugging and evidence collection for demonstrations.

## 10) How can this project evolve to production?

- Replace in-memory store with persistent database
- Add authentication and authorization
- Add validation framework and DTOs
- Add unit/integration tests
- Add OpenAPI documentation and CI/CD pipeline
