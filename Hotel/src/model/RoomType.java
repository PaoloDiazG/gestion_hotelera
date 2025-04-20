package model;

import util.Messages;

/**
 * Enum representing the different types of rooms available in the hotel.
 */
public enum RoomType {
    SIMPLE("roomtype.simple", 1),
    DOUBLE("roomtype.double", 2),
    SUITE("roomtype.suite", 3);

    private final String displayName;
    private final int capacity;

    RoomType(String displayName, int capacity) {
        this.displayName = displayName;
        this.capacity = capacity;
    }

    public String getDisplayName() {
        return Messages.get(displayName);
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
