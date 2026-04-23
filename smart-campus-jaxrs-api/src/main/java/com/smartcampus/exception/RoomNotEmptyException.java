package com.smartcampus.exception;

public class RoomNotEmptyException extends RuntimeException {
    private final String roomId;

    public RoomNotEmptyException(String roomId) {
        super("Room '" + roomId + "' contains ACTIVE sensors and cannot be deleted.");
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}
