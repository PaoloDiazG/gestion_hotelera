package model;

import util.Messages;

/**
 * Enum representing the different statuses a room can have in the hotel.
 */
public enum RoomStatus {
    AVAILABLE("roomstatus.available"),
    OCCUPIED("roomstatus.occupied"),
    MAINTENANCE("roomstatus.maintenance"),
    CLEANING("roomstatus.cleaning");

    private final String displayName;

    RoomStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return Messages.get(displayName);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
