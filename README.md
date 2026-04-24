# Smart Campus Sensor & Room Management REST API

A RESTful API for managing campus rooms, IoT sensors, and sensor readings, built for the module **Client-Server Architectures (5COSC022W)** at the University of Westminster.

**Technology Stack:** Java 17 · JAX-RS (`javax.ws.rs`) · Jersey 2.41 · Embedded Grizzly HTTP Server · Jackson JSON · In-memory storage (`ConcurrentHashMap`, `CopyOnWriteArrayList`) — no database.

---

## Project Structure

```
smart-campus-jaxrs-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── Main.java                              # Entry point — starts Grizzly server on port 8080
    ├── config/
    │   └── AppConfig.java                    # ResourceConfig — registers Jackson + scans packages
    ├── model/
    │   ├── Room.java                          # POJO: id, name, capacity, sensorIds
    │   ├── Sensor.java                        # POJO: id, type, status, currentValue, roomId
    │   └── SensorReading.java                 # POJO: id, timestamp, value
    ├── store/
    │   └── DataStore.java                    # Singleton in-memory store with validation logic
    ├── resource/
    │   ├── DiscoveryResource.java             # GET /api/v1 — metadata + HATEOAS links
    │   ├── RoomResource.java                  # GET|POST /rooms, GET|DELETE /rooms/{id}
    │   ├── SensorResource.java                # GET|POST /sensors, sub-resource locator
    │   └── SensorReadingResource.java         # GET|POST /sensors/{id}/readings (sub-resource)
    ├── exception/
    │   ├── RoomNotEmptyException.java         # Thrown when deleting room with ACTIVE sensors
    │   ├── LinkedResourceNotFoundException.java # Thrown when sensor references unknown roomId
    │   └── SensorUnavailableException.java    # Thrown when posting to MAINTENANCE sensor
    ├── mapper/
    │   ├── RoomNotEmptyMapper.java            # → HTTP 409 Conflict
    │   ├── LinkedResourceMapper.java          # → HTTP 422 Unprocessable Entity
    │   ├── SensorUnavailableMapper.java       # → HTTP 403 Forbidden
    │   └── GlobalExceptionMapper.java         # → HTTP 500 (catch-all Throwable)
    └── filter/
        └── LoggingFilter.java                 # Logs every request method+URI and response status
```

---

## Prerequisites

| Tool     | Version       |
| -------- | ------------- |
| Java JDK | 17 or higher  |
| Maven    | 3.6 or higher |

---

## Build & Run Instructions

### Step 1 — Verify tools are installed

```bash
java -version
mvn -version
```

On Windows, if Maven is missing:

```powershell
winget install -e --id Apache.Maven
```

Restart the terminal after installation.

### Step 2 — Clone the repository

```bash
git clone <your-repo-url>
cd smart-campus-jaxrs-api
```

### Step 3 — Build the project

```bash
mvn clean compile
```

### Step 4 — Start the server

```bash
mvn exec:java
```

The embedded Grizzly HTTP server starts at:

```
http://localhost:8080/api/v1/
```

### Step 5 — Stop the server

Press `ENTER` in the terminal where the server is running.

---

## API Endpoints

| Method | Path                            | Description                                          | Success        | Error         |
| ------ | ------------------------------- | ---------------------------------------------------- | -------------- | ------------- |
| GET    | `/api/v1`                       | Discovery metadata + resource links                  | 200            | —             |
| GET    | `/api/v1/rooms`                 | List all rooms                                       | 200            | —             |
| POST   | `/api/v1/rooms`                 | Create a new room                                    | 201 + Location | 400, 409      |
| GET    | `/api/v1/rooms/{id}`            | Get room by ID                                       | 200            | 404           |
| DELETE | `/api/v1/rooms/{id}`            | Delete room (blocked if ACTIVE sensors exist)        | 204            | 404, 409      |
| GET    | `/api/v1/sensors`               | List all sensors                                     | 200            | —             |
| GET    | `/api/v1/sensors?type={type}`   | Filter sensors by type (case-insensitive)            | 200            | —             |
| POST   | `/api/v1/sensors`               | Register new sensor (validates roomId)               | 201 + Location | 400, 409, 422 |
| GET    | `/api/v1/sensors/{id}/readings` | Get reading history for a sensor                     | 200            | 404           |
| POST   | `/api/v1/sensors/{id}/readings` | Add a reading (updates parent sensor's currentValue) | 201 + Location | 400, 403, 404 |

**Base URL:** `http://localhost:8080/api/v1`

---

## Sample curl Commands

```bash
BASE=http://localhost:8080/api/v1
```

### 1 — Discovery endpoint

```bash
curl -X GET $BASE
```

Expected: `200 OK` with API name, version, contact info, and resource links.

### 2 — Create a room

```bash
curl -i -X POST $BASE/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "R101",
    "name": "Computer Lab A",
    "capacity": 40
  }'
```

Expected: `201 Created` with `Location: .../rooms/R101` header and room JSON body.

### 3 — List all rooms

```bash
curl -X GET $BASE/rooms
```

Expected: `200 OK` with JSON array of all rooms.

### 4 — Get a specific room

```bash
curl -X GET $BASE/rooms/R101
```

Expected: `200 OK` with full room object.

### 5 — Create a sensor with a valid roomId

```bash
curl -i -X POST $BASE/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "S-CO2-01",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 0,
    "roomId": "R101"
  }'
```

Expected: `201 Created` with `Location` header and sensor JSON body.

### 6 — Filter sensors by type

```bash
curl -X GET "$BASE/sensors?type=CO2"
```

Expected: `200 OK` with JSON array containing only CO2 sensors.

### 7 — Add a reading to an ACTIVE sensor

```bash
curl -i -X POST $BASE/sensors/S-CO2-01/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 615.7
  }'
```

Expected: `201 Created`. The parent sensor's `currentValue` is updated to `615.7`.

### 8 — Get sensor reading history

```bash
curl -X GET $BASE/sensors/S-CO2-01/readings
```

Expected: `200 OK` with JSON array of readings including the one just posted.

### 9 — Create a sensor with an invalid roomId → 422

```bash
curl -X POST $BASE/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "S-TEMP-404",
    "type": "TEMPERATURE",
    "status": "ACTIVE",
    "currentValue": 20.1,
    "roomId": "UNKNOWN_ROOM"
  }'
```

Expected: `422 Unprocessable Entity` with structured JSON error body.

### 10 — Attempt to delete a room with ACTIVE sensors → 409

```bash
curl -X DELETE $BASE/rooms/R101
```

Expected: `409 Conflict` with JSON error explaining that ACTIVE sensors are still assigned.

### 11 — Post a reading to a MAINTENANCE sensor → 403

```bash
# First create a MAINTENANCE sensor
curl -X POST $BASE/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "S-TEMP-M1",
    "type": "TEMPERATURE",
    "status": "MAINTENANCE",
    "currentValue": 0,
    "roomId": "R101"
  }'

# Then attempt to post a reading — should be rejected
curl -X POST $BASE/sensors/S-TEMP-M1/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 22.4
  }'
```

Expected: `403 Forbidden` with JSON error explaining the sensor is in MAINTENANCE status.

---

## Error Response Format

All errors are returned as structured JSON — never raw stack traces:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room 'R101' contains ACTIVE sensors and cannot be deleted."
}
```

| Exception                         | HTTP Status               | Scenario                                  |
| --------------------------------- | ------------------------- | ----------------------------------------- |
| `RoomNotEmptyException`           | 409 Conflict              | DELETE room that still has ACTIVE sensors |
| `LinkedResourceNotFoundException` | 422 Unprocessable Entity  | POST sensor with non-existent roomId      |
| `SensorUnavailableException`      | 403 Forbidden             | POST reading to MAINTENANCE sensor        |
| `Throwable` (catch-all)           | 500 Internal Server Error | Any unexpected runtime error              |

---

## Report & Viva Answers

### 1) Explain the architecture used in this API

The API uses a layered JAX-RS architecture:

- Resource layer handles HTTP requests and responses.
- Store layer manages in-memory data and business rules.
- Model layer defines JSON-serializable entities.
- Exception layer defines domain-specific errors.
- Mapper layer converts exceptions into consistent JSON HTTP responses.
- Filter layer handles request/response logging.

This separation improves maintainability and testability.

### 2) Why use JAX-RS and not Spring Boot?

The coursework requires JAX-RS and prohibits Spring Boot. JAX-RS provides a lightweight, standards-based REST API framework using annotations like `@Path`, `@GET`, `@POST`, and `@DELETE`.

### 3) How is REST implemented correctly here?

The implementation follows REST by using:

- noun-based URIs (`/rooms`, `/sensors`, `/sensors/{id}/readings`)
- correct HTTP methods for actions
- stateless request handling
- meaningful status codes
- JSON payloads for resource transfer

### 4) Why is `/sensors/{id}/readings` a sub-resource?

A reading belongs to a specific sensor, so modeling it as `/sensors/{id}/readings` reflects that ownership and keeps the API structure clear.

### 5) Why are specific status codes used?

- `409 Conflict`: deleting a room with active linked sensors.
- `422 Unprocessable Entity`: creating a sensor with a non-existent room reference.
- `403 Forbidden`: adding a reading to a sensor in `MAINTENANCE`.
- `500 Internal Server Error`: unhandled server errors.

### 6) What is the role of ExceptionMapper?

Exception mappers centralize error handling and ensure all failures return consistent JSON payloads. This avoids duplicate error handling in resource methods.

### 7) Why use an in-memory store instead of a database?

The module explicitly disallows a database. The in-memory store demonstrates core client-server and REST design while keeping the project lightweight.

### 8) What are limitations of the in-memory approach?

- data is lost on restart
- no durable transactions
- limited scalability
- not suitable for production persistence

### 9) How is API logging handled?

A JAX-RS filter logs every request method and URL, plus the response status. This provides consistent request/response tracing without per-endpoint logging.

### 10) How can this project evolve for production?

- add a persistent database
- add authentication and authorization
- add validation and DTO layers
- add unit/integration tests
- add OpenAPI docs and CI/CD

---

## Notes

- JAX-RS resource classes are request-scoped, so state is stored in the singleton `DataStore`.
- `DataStore` uses `ConcurrentHashMap` and `CopyOnWriteArrayList` for thread-safe in-memory storage.
- `GET /api/v1` is a discovery endpoint that exposes API metadata and resource links.
- `/sensors?type=CO2` uses query filtering; path-based filters would be less flexible.
- DELETE is idempotent by state: first valid delete returns `204`, a later delete returns `404`.
- `@Consumes(MediaType.APPLICATION_JSON)` ensures request bodies are only accepted as JSON.
- `ExceptionMapper` classes keep errors consistent and prevent raw stack traces from leaking.
- `LoggingFilter` centralizes request/response logging without adding boilerplate to resources.

### Implementation Notes

- Only `MAINTENANCE` sensors are blocked from receiving readings in the current code.
- `OFFLINE` sensors will still accept readings.
- Data is lost on restart; there is no persistence layer.

## Demo Checklist

- GET `/api/v1`
- Create room and sensor
- Add a reading and verify `currentValue`
- Trigger `422` for invalid roomId
- Trigger `409` by deleting a room with active sensors
- Trigger `403` by posting to a MAINTENANCE sensor

## Submission Notes

- Push source code to GitHub
- Include this README
- Add screenshots or Postman export if required
- Tag a stable submission version if needed
