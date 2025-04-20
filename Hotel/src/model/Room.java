package model;

import java.math.BigDecimal;

/**
 * Represents a room in the hotel.
 */
public class Room {
    private int roomNumber;
    private int floor;
    private RoomType type;
    private RoomStatus status;
    private BigDecimal pricePerNight;
    private String description;
    
    public Room(int roomNumber, int floor, RoomType type, BigDecimal pricePerNight) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.type = type;
        this.status = RoomStatus.AVAILABLE; // Default status
        this.pricePerNight = pricePerNight;
        this.description = "";
    }
    
    public Room(int roomNumber, int floor, RoomType type, RoomStatus status, BigDecimal pricePerNight, String description) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.type = type;
        this.status = status;
        this.pricePerNight = pricePerNight;
        this.description = description;
    }
    
    // Getters and setters
    public int getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public int getFloor() {
        return floor;
    }
    
    public void setFloor(int floor) {
        this.floor = floor;
    }
    
    public RoomType getType() {
        return type;
    }
    
    public void setType(RoomType type) {
        this.type = type;
    }
    
    public RoomStatus getStatus() {
        return status;
    }
    
    public void setStatus(RoomStatus status) {
        this.status = status;
    }
    
    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }
    
    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getCapacity() {
        return type.getCapacity();
    }
    
    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + type.getDisplayName() + ")";
    }
}