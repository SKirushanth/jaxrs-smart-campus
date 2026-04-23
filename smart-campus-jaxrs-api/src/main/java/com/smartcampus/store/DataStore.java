package com.smartcampus.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public final class DataStore {
    private static final DataStore INSTANCE = new DataStore();
    private static final Set<String> VALID_SENSOR_TYPES = Set.of("TEMPERATURE", "CO2", "OCCUPANCY");
    private static final Set<String> VALID_SENSOR_STATUSES = Set.of("ACTIVE", "MAINTENANCE", "OFFLINE");

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    public List<Room> getAllRooms() {
        return rooms.values().stream()
                .map(this::copyRoom)
                .collect(Collectors.toList());
    }

    public Room getRoomOrThrow(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new NotFoundException("Room '" + roomId + "' was not found.");
        }
        return copyRoom(room);
    }

    public Room createRoom(Room room) {
        validateRoomForCreate(room);
        Room toStore = copyRoom(room);
        toStore.setSensorIds(new ArrayList<>());

        if (rooms.putIfAbsent(toStore.getId(), toStore) != null) {
            throw new WebApplicationException("Room with id '" + toStore.getId() + "' already exists.",
                    Response.Status.CONFLICT);
        }
        return getRoomOrThrow(toStore.getId());
    }

    public void deleteRoom(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new NotFoundException("Room '" + roomId + "' was not found.");
        }

        if (hasActiveSensors(room)) {
            throw new RoomNotEmptyException(roomId);
        }

        // Cleanup non-active linked sensors to avoid orphan references after room
        // removal.
        List<String> sensorIds = room.getSensorIds();
        if (sensorIds != null) {
            for (String sensorId : sensorIds) {
                sensors.remove(sensorId);
                sensorReadings.remove(sensorId);
            }
        }

        rooms.remove(roomId);
    }

    public List<Sensor> getAllSensors(String typeFilter) {
        return sensors.values().stream()
                .filter(sensor -> filterByType(sensor, typeFilter))
                .map(this::copySensor)
                .collect(Collectors.toList());
    }

    public Sensor getSensorOrThrow(String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor '" + sensorId + "' was not found.");
        }
        return copySensor(sensor);
    }

    public Sensor createSensor(Sensor sensor) {
        validateSensorForCreate(sensor);

        String roomId = sensor.getRoomId().trim();
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room", roomId);
        }

        Sensor toStore = copySensor(sensor);
        toStore.setType(toStore.getType().trim().toUpperCase(Locale.ROOT));
        toStore.setStatus(toStore.getStatus().trim().toUpperCase(Locale.ROOT));
        toStore.setRoomId(roomId);

        if (sensors.putIfAbsent(toStore.getId(), toStore) != null) {
            throw new WebApplicationException("Sensor with id '" + toStore.getId() + "' already exists.",
                    Response.Status.CONFLICT);
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        room.getSensorIds().add(toStore.getId());
        sensorReadings.putIfAbsent(toStore.getId(), new CopyOnWriteArrayList<>());

        return getSensorOrThrow(toStore.getId());
    }

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor '" + sensorId + "' was not found.");
        }

        List<SensorReading> readings = sensorReadings.getOrDefault(sensor.getId(), List.of());
        return readings.stream().map(this::copySensorReading).collect(Collectors.toList());
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor '" + sensorId + "' was not found.");
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        validateReading(reading);

        SensorReading toStore = copySensorReading(reading);
        if (isBlank(toStore.getId())) {
            toStore.setId(UUID.randomUUID().toString());
        } else {
            validateUuid(toStore.getId());
        }
        if (toStore.getTimestamp() <= 0) {
            toStore.setTimestamp(System.currentTimeMillis());
        }

        sensor.setCurrentValue(toStore.getValue());
        sensorReadings.computeIfAbsent(sensorId, key -> new CopyOnWriteArrayList<>()).add(toStore);

        return copySensorReading(toStore);
    }

    private boolean filterByType(Sensor sensor, String typeFilter) {
        if (isBlank(typeFilter)) {
            return true;
        }
        return sensor.getType() != null && sensor.getType().equalsIgnoreCase(typeFilter.trim());
    }

    private void validateRoomForCreate(Room room) {
        if (room == null) {
            throw new BadRequestException("Room payload must be provided.");
        }
        if (isBlank(room.getId())) {
            throw new BadRequestException("Room id is required.");
        }
        if (isBlank(room.getName())) {
            throw new BadRequestException("Room name is required.");
        }
        if (room.getCapacity() <= 0) {
            throw new BadRequestException("Room capacity must be greater than zero.");
        }
    }

    private void validateSensorForCreate(Sensor sensor) {
        if (sensor == null) {
            throw new BadRequestException("Sensor payload must be provided.");
        }
        if (isBlank(sensor.getId())) {
            throw new BadRequestException("Sensor id is required.");
        }
        if (isBlank(sensor.getType())) {
            throw new BadRequestException("Sensor type is required.");
        }
        if (isBlank(sensor.getStatus())) {
            throw new BadRequestException("Sensor status is required.");
        }
        if (isBlank(sensor.getRoomId())) {
            throw new BadRequestException("Sensor roomId is required.");
        }

        String type = sensor.getType().trim().toUpperCase(Locale.ROOT);
        if (!VALID_SENSOR_TYPES.contains(type)) {
            throw new BadRequestException("Sensor type must be one of " + VALID_SENSOR_TYPES + ".");
        }

        String status = sensor.getStatus().trim().toUpperCase(Locale.ROOT);
        if (!VALID_SENSOR_STATUSES.contains(status)) {
            throw new BadRequestException("Sensor status must be one of " + VALID_SENSOR_STATUSES + ".");
        }
    }

    private void validateReading(SensorReading reading) {
        if (reading == null) {
            throw new BadRequestException("SensorReading payload must be provided.");
        }
    }

    private boolean hasActiveSensors(Room room) {
        if (room.getSensorIds() == null || room.getSensorIds().isEmpty()) {
            return false;
        }

        for (String sensorId : room.getSensorIds()) {
            Sensor sensor = sensors.get(sensorId);
            if (sensor != null && "ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private void validateUuid(String value) {
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("SensorReading id must be a valid UUID.");
        }
    }

    private Room copyRoom(Room source) {
        return new Room(source.getId(), source.getName(), source.getCapacity(), source.getSensorIds());
    }

    private Sensor copySensor(Sensor source) {
        return new Sensor(source.getId(), source.getType(), source.getStatus(), source.getCurrentValue(),
                source.getRoomId());
    }

    private SensorReading copySensorReading(SensorReading source) {
        return new SensorReading(source.getId(), source.getTimestamp(), source.getValue());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}


