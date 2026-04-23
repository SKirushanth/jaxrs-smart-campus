Smart Campus Sensor and Room Management API
This repository hosts a Java REST API for managing rooms, sensors, and sensor readings in a smart campus scenario. This project was developed as part of the 5COSC022W Client-Server Architectures module at the University of Westminster.

API Design Overview
The API is designed around RESTful principles:

Resource-oriented endpoints: /api/v1/rooms, /api/v1/sensors, /api/v1/sensors/{id}/readings

Standard HTTP methods: GET, POST, DELETE

JSON request/response payloads

Stateless interactions

Clear status code usage: (e.g., 200 OK, 201 Created, 204 No Content, 403 Forbidden, 409 Conflict, 422 Unprocessable Entity, 500 Internal Server Error)

Core Modules:
com.smartcampus.model: Domain models (Room, Sensor, SensorReading)

com.smartcampus.resource: JAX-RS resources and sub-resource locators

com.smartcampus.store: Thread-safe in-memory data store (ConcurrentHashMap)

com.smartcampus.mapper: Custom ExceptionMappers for semantic error handling

com.smartcampus.filter: LoggingFilter for API observability

Step-by-Step Build and Run

1. Prerequisites
   Install:

Java 17 or higher

Maven

NetBeans IDE (Optional, but recommended)

2. Building in NetBeans
   Open NetBeans and select File > Open Project.

Navigate to the smart-campus-jaxrs-api folder and open it.

Right-click the project in the Projects window and select Clean and Build.

3. Launching the Server
   Option A: Via Terminal (Maven)

Bash
mvn clean compile
mvn exec:java
Option B: Via NetBeans

Locate Main.java (the class containing the Grizzly server startup logic).

Right-click the file and select Run File.

Server base URL: http://localhost:8080/api/v1/

Sample curl Commands (Successful API Interactions)

1. API Discovery
   Bash
   curl -X GET http://localhost:8080/api/v1
2. Create a Room
   Bash
   curl -X POST http://localhost:8080/api/v1/rooms \
    -H "Content-Type: application/json" \
    -d '{
   "id": "R101",
   "name": "Computer Lab A",
   "capacity": 40,
   "sensorIds": []
   }'
3. Create a Sensor linked to a Room
   Bash
   curl -X POST http://localhost:8080/api/v1/sensors \
    -H "Content-Type: application/json" \
    -d '{
   "id": "S-CO2-01",
   "type": "CO2",
   "status": "ACTIVE",
   "currentValue": 0,
   "roomId": "R101"
   }'
4. Filter Sensors by Type
   Bash
   curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
5. Add a Sensor Reading (Using Valid UUID)
   Bash
   curl -X POST http://localhost:8080/api/v1/sensors/S-CO2-01/readings \
    -H "Content-Type: application/json" \
    -d '{
   "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
   "timestamp": 1713787200000,
   "value": 615.7
   }'
   📄 Technical Report (Conceptual Questions)
   Part 1: Service Architecture & Setup
   Q1.1: Lifecycle and Concurrency By default, JAX-RS resources are Request-Scoped, meaning a new instance is created per request. Since our data is stored in a singleton DataStore, we use ConcurrentHashMap and CopyOnWriteArrayList to ensure thread-safety, preventing race conditions when multiple request-threads access the same in-memory data.

Q1.2: HATEOAS & Discovery Hypermedia (HATEOAS) makes an API self-descriptive. It provides links in responses so clients can navigate resources dynamically. This reduces hardcoding of URLs in the client and allows the server to evolve its structure without breaking client integrations.

Part 2: Room Management
Q2.1: Data Granularity Returning only IDs saves bandwidth but causes the "N+1 problem" where clients must make multiple calls for details. Returning full objects provides immediate data but increases payload size. This API prioritizes resource linking via IDs to maintain a lightweight core.

Q2.2: Idempotency in Deletion Yes, DELETE is idempotent. The first call removes the room (204). Subsequent calls return 404 because the room is gone, but the server's end-state remains unchanged (the room is still deleted).

Part 3: Sensor Operations & Linking
Q3.1: Content-Type Negotiation By using @Consumes(MediaType.APPLICATION_JSON), JAX-RS automatically rejects formats like text/plain or application/xml with a 415 Unsupported Media Type error before the method logic is ever reached.

Q3.2: Query vs. Path Parameters @QueryParam is superior for filtering because parameters are optional and combinable (e.g., ?type=CO2&status=ACTIVE). Path parameters are better suited for identifying specific, required resources (e.g., /sensors/{id}).

Part 4: Deep Nesting with Sub-Resources
Q4.1: Sub-Resource Locators The Sub-Resource Locator pattern (delegating /readings to SensorReadingResource) promotes Separation of Concerns. It prevents the main SensorResource from becoming a "God Class" and makes the codebase modular.

Part 5: Error Handling & Logging
Q5.1: Semantic Status Codes (422) An HTTP 422 Unprocessable Entity is more accurate than 404 because the URI exists and the JSON is valid, but the business logic fails (the referenced roomId does not exist).

Q5.2: Security & Information Disclosure Exposing Java stack traces leaks internal intelligence (class names, file paths). Attackers can use this to find specific vulnerabilities. Our GlobalExceptionMapper hides these details behind generic JSON errors.

Q5.3: Cross-Cutting Concerns Using JAX-RS Filters for logging addresses cross-cutting concerns centrally. A single filter ensures consistent observability across all resources, reducing boilerplate code.

GitHub Hosting Requirement
This repository is public. All coursework requirements, including the PDF content extract, are contained within this root README.md.
