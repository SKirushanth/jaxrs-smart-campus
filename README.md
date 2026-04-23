# Smart Campus Sensor and Room Management API

This repository hosts a Java REST API for managing rooms, sensors, and sensor readings in a smart campus scenario.

## API Design Overview

The API is designed around REST principles:

- Resource-oriented endpoints: `/api/v1/rooms`, `/api/v1/sensors`, `/api/v1/sensors/{id}/readings`
- Standard HTTP methods: `GET`, `POST`, `DELETE`
- JSON request/response payloads
- Stateless interactions
- Clear status code usage (for example `200`, `201`, `204`, `403`, `409`, `422`, `500`)

Core modules:

- `smart-campus-jaxrs-api/src/main/java/com/smartcampus/model`: Domain models (`Room`, `Sensor`, `SensorReading`)
- `smart-campus-jaxrs-api/src/main/java/com/smartcampus/resource`: REST resources
- `smart-campus-jaxrs-api/src/main/java/com/smartcampus/store`: In-memory data store and business rules
- `smart-campus-jaxrs-api/src/main/java/com/smartcampus/mapper`: Exception-to-HTTP response mapping
- `smart-campus-jaxrs-api/src/main/java/com/smartcampus/filter`: Request/response logging filter

## Step-by-Step Build and Run

### 1) Prerequisites

Install:

- Java 17
- Maven

Check versions:

```powershell
java -version
mvn -version
```

### 2) Open the project folder

```powershell
cd smart-campus-jaxrs-api
```

### 3) Build the project

```powershell
mvn clean compile
```

### 4) Launch the server

```powershell
mvn exec:java
```

Server base URL:

```text
http://localhost:8080/api/v1/
```

### 5) Stop the server

Press `Enter` in the terminal that is running the app.

## Sample curl Commands (Successful API Interactions)

### 1) API discovery

```bash
curl -X GET http://localhost:8080/api/v1
```

### 2) Create a room

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

### 3) List rooms

```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 4) Create a sensor linked to a room

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

### 5) Filter sensors by type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 6) Add a sensor reading

```bash
curl -X POST http://localhost:8080/api/v1/sensors/S-CO2-01/readings \
	-H "Content-Type: application/json" \
	-d '{
		"id": "READ-001",
		"timestamp": 1713787200000,
		"value": 615.7
	}'
```

### 7) Get readings for a sensor

```bash
curl -X GET http://localhost:8080/api/v1/sensors/S-CO2-01/readings
```

## GitHub Hosting Requirement

Publish this repository as a public GitHub repository and ensure this `README.md` remains in the repository root.
