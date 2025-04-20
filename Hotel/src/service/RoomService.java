package service;

import model.Room;
import model.RoomStatus;
import model.RoomType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing rooms.
 */
public class RoomService {
    private static RoomService instance;
    private final Map<Integer, Room> rooms;
    private int nextId;
    
    private RoomService() {
        rooms = new HashMap<>();
        nextId = 1;
        
        // Add some sample rooms
        addRoom(new Room(101, 1, RoomType.SIMPLE, new BigDecimal("100.00")));
        addRoom(new Room(102, 1, RoomType.SIMPLE, new BigDecimal("100.00")));
        addRoom(new Room(103, 1, RoomType.DOUBLE, new BigDecimal("150.00")));
        addRoom(new Room(201, 2, RoomType.DOUBLE, new BigDecimal("150.00")));
        addRoom(new Room(202, 2, RoomType.SUITE, new BigDecimal("250.00")));
    }
    
    public static synchronized RoomService getInstance() {
        if (instance == null) {
            instance = new RoomService();
        }
        return instance;
    }
    
    // Add a room
    public Room addRoom(Room room) {
        rooms.put(room.getRoomNumber(), room);
        return room;
    }
    
    // Update a room
    public Room updateRoom(Room room) {
        if (rooms.containsKey(room.getRoomNumber())) {
            rooms.put(room.getRoomNumber(), room);
            return room;
        }
        return null;
    }
    
    // Delete a room
    public boolean deleteRoom(int roomNumber) {
        return rooms.remove(roomNumber) != null;
    }
    
    // Get a room by number
    public Room getRoomByNumber(int roomNumber) {
        return rooms.get(roomNumber);
    }
    
    // Get all rooms
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }
    
    // Get rooms by status
    public List<Room> getRoomsByStatus(RoomStatus status) {
        return rooms.values().stream()
                .filter(room -> room.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    // Get rooms by type
    public List<Room> getRoomsByType(RoomType type) {
        return rooms.values().stream()
                .filter(room -> room.getType() == type)
                .collect(Collectors.toList());
    }
    
    // Get available rooms
    public List<Room> getAvailableRooms() {
        return getRoomsByStatus(RoomStatus.AVAILABLE);
    }
    
    // Get available rooms by type
    public List<Room> getAvailableRoomsByType(RoomType type) {
        return rooms.values().stream()
                .filter(room -> room.getStatus() == RoomStatus.AVAILABLE && room.getType() == type)
                .collect(Collectors.toList());
    }
    
    // Change room status
    public boolean changeRoomStatus(int roomNumber, RoomStatus status) {
        Room room = getRoomByNumber(roomNumber);
        if (room != null) {
            room.setStatus(status);
            return true;
        }
        return false;
    }
}