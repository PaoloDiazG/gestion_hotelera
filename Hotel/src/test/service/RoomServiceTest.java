package test.service;

import model.Room;
import model.RoomStatus;
import model.RoomType;
import org.junit.Before;
import org.junit.Test;
import service.RoomService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the RoomService class.
 */
public class RoomServiceTest {
    private RoomService roomService;
    private Room testRoom;

    @Before
    public void setUp() {
        // Get the singleton instance of RoomService
        roomService = RoomService.getInstance();
        
        // Create a test room
        testRoom = new Room(999, 9, RoomType.SUITE, new BigDecimal("300.00"));
    }

    @Test
    public void testSingletonPattern() {
        // Test that getInstance always returns the same instance
        RoomService instance1 = RoomService.getInstance();
        RoomService instance2 = RoomService.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    public void testAddRoom() {
        // Add the test room
        Room addedRoom = roomService.addRoom(testRoom);
        
        // Verify the room was added
        assertNotNull(addedRoom);
        assertEquals(testRoom.getRoomNumber(), addedRoom.getRoomNumber());
        
        // Verify we can retrieve the room
        Room retrievedRoom = roomService.getRoomByNumber(testRoom.getRoomNumber());
        assertNotNull(retrievedRoom);
        assertEquals(testRoom.getRoomNumber(), retrievedRoom.getRoomNumber());
        assertEquals(testRoom.getFloor(), retrievedRoom.getFloor());
        assertEquals(testRoom.getType(), retrievedRoom.getType());
        assertEquals(testRoom.getStatus(), retrievedRoom.getStatus());
        assertEquals(testRoom.getPricePerNight(), retrievedRoom.getPricePerNight());
        
        // Clean up
        roomService.deleteRoom(testRoom.getRoomNumber());
    }

    @Test
    public void testUpdateRoom() {
        // Add the test room
        roomService.addRoom(testRoom);
        
        // Modify the room
        testRoom.setFloor(10);
        testRoom.setType(RoomType.DOUBLE);
        testRoom.setStatus(RoomStatus.MAINTENANCE);
        testRoom.setPricePerNight(new BigDecimal("250.00"));
        testRoom.setDescription("Updated description");
        
        // Update the room
        Room updatedRoom = roomService.updateRoom(testRoom);
        
        // Verify the room was updated
        assertNotNull(updatedRoom);
        assertEquals(testRoom.getRoomNumber(), updatedRoom.getRoomNumber());
        assertEquals(testRoom.getFloor(), updatedRoom.getFloor());
        assertEquals(testRoom.getType(), updatedRoom.getType());
        assertEquals(testRoom.getStatus(), updatedRoom.getStatus());
        assertEquals(testRoom.getPricePerNight(), updatedRoom.getPricePerNight());
        assertEquals(testRoom.getDescription(), updatedRoom.getDescription());
        
        // Verify we can retrieve the updated room
        Room retrievedRoom = roomService.getRoomByNumber(testRoom.getRoomNumber());
        assertNotNull(retrievedRoom);
        assertEquals(testRoom.getFloor(), retrievedRoom.getFloor());
        assertEquals(testRoom.getType(), retrievedRoom.getType());
        assertEquals(testRoom.getStatus(), retrievedRoom.getStatus());
        assertEquals(testRoom.getPricePerNight(), retrievedRoom.getPricePerNight());
        assertEquals(testRoom.getDescription(), retrievedRoom.getDescription());
        
        // Clean up
        roomService.deleteRoom(testRoom.getRoomNumber());
    }

    @Test
    public void testUpdateNonExistentRoom() {
        // Try to update a room that doesn't exist
        Room nonExistentRoom = new Room(9999, 9, RoomType.SUITE, new BigDecimal("300.00"));
        Room result = roomService.updateRoom(nonExistentRoom);
        
        // Should return null
        assertNull(result);
    }

    @Test
    public void testDeleteRoom() {
        // Add the test room
        roomService.addRoom(testRoom);
        
        // Verify the room exists
        assertNotNull(roomService.getRoomByNumber(testRoom.getRoomNumber()));
        
        // Delete the room
        boolean deleted = roomService.deleteRoom(testRoom.getRoomNumber());
        
        // Verify the room was deleted
        assertTrue(deleted);
        assertNull(roomService.getRoomByNumber(testRoom.getRoomNumber()));
    }

    @Test
    public void testDeleteNonExistentRoom() {
        // Try to delete a room that doesn't exist
        boolean deleted = roomService.deleteRoom(9999);
        
        // Should return false
        assertFalse(deleted);
    }

    @Test
    public void testGetAllRooms() {
        // Get all rooms
        List<Room> allRooms = roomService.getAllRooms();
        
        // Should not be null and should have at least the sample rooms
        assertNotNull(allRooms);
        assertTrue(allRooms.size() >= 5); // There are 5 sample rooms added in the constructor
    }

    @Test
    public void testGetRoomsByStatus() {
        // Add the test room with a specific status
        testRoom.setStatus(RoomStatus.MAINTENANCE);
        roomService.addRoom(testRoom);
        
        // Get rooms by status
        List<Room> maintenanceRooms = roomService.getRoomsByStatus(RoomStatus.MAINTENANCE);
        
        // Should not be null and should contain our test room
        assertNotNull(maintenanceRooms);
        assertTrue(maintenanceRooms.stream().anyMatch(room -> room.getRoomNumber() == testRoom.getRoomNumber()));
        
        // Clean up
        roomService.deleteRoom(testRoom.getRoomNumber());
    }

    @Test
    public void testGetRoomsByType() {
        // Add the test room with a specific type
        testRoom.setType(RoomType.SUITE);
        roomService.addRoom(testRoom);
        
        // Get rooms by type
        List<Room> suiteRooms = roomService.getRoomsByType(RoomType.SUITE);
        
        // Should not be null and should contain our test room
        assertNotNull(suiteRooms);
        assertTrue(suiteRooms.stream().anyMatch(room -> room.getRoomNumber() == testRoom.getRoomNumber()));
        
        // Clean up
        roomService.deleteRoom(testRoom.getRoomNumber());
    }

    @Test
    public void testGetAvailableRooms() {
        // Add the test room with AVAILABLE status
        testRoom.setStatus(RoomStatus.AVAILABLE);
        roomService.addRoom(testRoom);
        
        // Get available rooms
        List<Room> availableRooms = roomService.getAvailableRooms();
        
        // Should not be null and should contain our test room
        assertNotNull(availableRooms);
        assertTrue(availableRooms.stream().anyMatch(room -> room.getRoomNumber() == testRoom.getRoomNumber()));
        
        // Clean up
        roomService.deleteRoom(testRoom.getRoomNumber());
    }

    @Test
    public void testGetAvailableRoomsByType() {
        // Add the test room with AVAILABLE status and SUITE type
        testRoom.setStatus(RoomStatus.AVAILABLE);
        testRoom.setType(RoomType.SUITE);
        roomService.addRoom(testRoom);
        
        // Get available suite rooms
        List<Room> availableSuiteRooms = roomService.getAvailableRoomsByType(RoomType.SUITE);
        
        // Should not be null and should contain our test room
        assertNotNull(availableSuiteRooms);
        assertTrue(availableSuiteRooms.stream().anyMatch(room -> room.getRoomNumber() == testRoom.getRoomNumber()));
        
        // Clean up
        roomService.deleteRoom(testRoom.getRoomNumber());
    }

    @Test
    public void testChangeRoomStatus() {
        // Add the test room
        roomService.addRoom(testRoom);
        
        // Change the status
        boolean changed = roomService.changeRoomStatus(testRoom.getRoomNumber(), RoomStatus.CLEANING);
        
        // Verify the status was changed
        assertTrue(changed);
        Room retrievedRoom = roomService.getRoomByNumber(testRoom.getRoomNumber());
        assertEquals(RoomStatus.CLEANING, retrievedRoom.getStatus());
        
        // Clean up
        roomService.deleteRoom(testRoom.getRoomNumber());
    }

    @Test
    public void testChangeStatusOfNonExistentRoom() {
        // Try to change the status of a room that doesn't exist
        boolean changed = roomService.changeRoomStatus(9999, RoomStatus.CLEANING);
        
        // Should return false
        assertFalse(changed);
    }
}