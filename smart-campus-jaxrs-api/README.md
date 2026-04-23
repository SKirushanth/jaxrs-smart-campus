# Smart Campus Sensor & Room Management REST API

A Java REST API for managing rooms, sensors, and sensor readings in a smart campus environment.

This project is built for the module **Client-Server Architectures (5COSC022W)** at the University of Westminster.

## Technology Stack

- Java 17
- Javax JAX-RS (`javax.ws.rs`)
- Jersey (JAX-RS implementation)
- Embedded Grizzly HTTP server
- JSON via Jackson
- In-memory data storage (no database)

## REST Architecture Overview

The API follows REST principles:

- Resource-oriented endpoints (`/rooms`, `/sensors`, `/sensors/{id}/readings`)
- Proper HTTP methods (GET, POST, DELETE)
- Proper status codes (200, 201, 204, 403, 409, 422, 500)
- JSON request/response payloads
- Stateless interactions (no server-side session state per client)

### Package Structure

- `com.smartcampus.config`: API configuration and component registration
- `com.smartcampus.model`: Domain entities (`Room`, `Sensor`, `SensorReading`)
- `com.smartcampus.store`: In-memory data store and business validation
- `com.smartcampus.resource`: REST resources and sub-resource locator
- `com.smartcampus.exception`: Custom exception types
- `com.smartcampus.mapper`: Exception mappers to HTTP JSON errors
- `com.smartcampus.filter`: Request/response logging filter

## Endpoints

### Discovery

- `GET /api/v1`

### Rooms

- `GET /api/v1/rooms`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms/{id}`
- `DELETE /api/v1/rooms/{id}`

### Sensors

- `GET /api/v1/sensors`
- `GET /api/v1/sensors?type=CO2`
- `POST /api/v1/sensors`

### Sensor Readings (Sub-resource)

- `GET /api/v1/sensors/{id}/readings`
- `POST /api/v1/sensors/{id}/readings`

## Setup Instructions

### 0) Install Required Tools (Windows)

Check installed versions:

```powershell
java -version
mvn -version
```

If Maven is missing, install it with Winget:

```powershell
winget install -e --id Apache.Maven
```

Then restart the terminal and verify:

```powershell
mvn -version
```

### 1) Build

```bash
mvn clean compile
```

### 2) Run

```bash
mvn exec:java
```

Server starts at:

```text
http://localhost:8080/api/v1/
```

### 3) Stop

Press `ENTER` in the terminal running the server.

## Example cURL Commands (More than 5)

### 1) Discovery

```bash
curl -X GET http://localhost:8080/api/v1
```

### 2) Create Room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "R101",
    "name": "Computer Lab A",
    "capacity": 40,
    "sensorIds": []
  }'
```

### 3) List Rooms

```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 4) Create Sensor (valid room)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "S-CO2-01",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 0,
    "roomId": "R101"
  }'
```

### 5) Filter Sensors by Type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 6) Add Sensor Reading

```bash
curl -X POST http://localhost:8080/api/v1/sensors/S-CO2-01/readings \
  -H "Content-Type: application/json" \
  -d '{
    "id": "READ-001",
    "timestamp": 1713787200000,
    "value": 615.7
  }'
```

### 7) Get Sensor Readings

```bash
curl -X GET http://localhost:8080/api/v1/sensors/S-CO2-01/readings
```

### 8) Create Sensor with Invalid Room (422)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "S-TEMP-404",
    "type": "TEMPERATURE",
    "status": "ACTIVE",
    "currentValue": 20.1,
    "roomId": "UNKNOWN_ROOM"
  }'
```

### 9) Delete Room with Linked Sensors (409)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/R101
```

### 10) Trigger 403 by Adding Reading to MAINTENANCE Sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "S-TEMP-M1",
    "type": "TEMPERATURE",
    "status": "MAINTENANCE",
    "currentValue": 0,
    "roomId": "R101"
  }'

curl -X POST http://localhost:8080/api/v1/sensors/S-TEMP-M1/readings \
  -H "Content-Type: application/json" \
  -d '{
    "id": "READ-M-001",
    "timestamp": 1713787200000,
    "value": 22.4
  }'
```

## Error Handling Strategy

Custom exceptions are translated by mappers into structured JSON responses:

- `RoomNotEmptyException` -> `409 Conflict`
- `LinkedResourceNotFoundException` -> `422 Unprocessable Entity`
- `SensorUnavailableException` -> `403 Forbidden`
- Catch-all mapper -> `500 Internal Server Error`

Example error payload:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room 'R101' contains ACTIVE sensors and cannot be deleted."
}
```

## Logging

The logging filter prints:

- Request method + URL
- Response status code

This provides transparent request/response tracing for debugging and demonstrations.

## Viva and Report Q&A (Suggested Answers)

### Q1) Why is this API considered RESTful?

Because resources are exposed as URIs, standard HTTP methods are used for actions, status codes describe outcomes, payloads are JSON representations, and interactions are stateless.

### Q2) Why use a sub-resource locator for readings?

Readings are logically owned by sensors. Using `/sensors/{id}/readings` models hierarchy clearly and keeps API semantics close to the domain.

### Q3) Why use 409 vs 422 vs 403?

- `409` means request conflicts with current resource state (room cannot be deleted while active sensors exist).
- `422` means request entity is syntactically valid but semantically invalid (sensor references a room that does not exist).
- `403` means action is understood but forbidden due to current policy/state (sensor in maintenance cannot accept readings).

### Q4) Why implement ExceptionMapper classes?

They centralize error translation, keep resource methods clean, ensure consistent JSON error format, and avoid leaking stack traces to clients.

### Q5) Why in-memory storage for this coursework?

It satisfies the no-database constraint, keeps architecture simple and focused on client-server and REST concepts, and enables fast testing during demos.

### Q6) What are the limitations of this approach?

Data is lost on restart, no persistence or transactions, and concurrency guarantees are limited compared to production-grade databases.

## Video Demonstration Checklist (Postman)

- Show `GET /api/v1` discovery output
- Create a room and sensor
- Add sensor reading and verify `currentValue` changes
- Trigger 422 (invalid room for sensor)
- Trigger 409 (delete room with linked sensors)
- Trigger 403 (create a sensor with `MAINTENANCE` status and try adding reading)

## Public Repository Checklist

- Push source code to GitHub
- Include this README
- Add screenshots or exported Postman collection if required by lecturer
- Tag a stable submission version (optional but recommended)
