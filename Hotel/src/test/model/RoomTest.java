package test.model;

import model.Room;
import model.RoomStatus;
import model.RoomType;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * Unit tests for the Room class.
 */
public class RoomTest {
    private Room room;
    private final int roomNumber = 101;
    private final int floor = 1;
    private final RoomType type = RoomType.DOUBLE;
    private final BigDecimal pricePerNight = new BigDecimal("150.00");
    private final String description = "Test room description";

    @Before
    public void setUp() {
        // Create a new room before each test
        room = new Room(roomNumber, floor, type, pricePerNight);
    }

    @Test
    public void testConstructor() {
        // Test the first constructor
        assertEquals(roomNumber, room.getRoomNumber());
        assertEquals(floor, room.getFloor());
        assertEquals(type, room.getType());
        assertEquals(RoomStatus.AVAILABLE, room.getStatus()); // Default status should be AVAILABLE
        assertEquals(pricePerNight, room.getPricePerNight());
        assertEquals("", room.getDescription()); // Default description should be empty string

        // Test the second constructor
        Room room2 = new Room(roomNumber, floor, type, RoomStatus.MAINTENANCE, pricePerNight, description);
        assertEquals(roomNumber, room2.getRoomNumber());
        assertEquals(floor, room2.getFloor());
        assertEquals(type, room2.getType());
        assertEquals(RoomStatus.MAINTENANCE, room2.getStatus());
        assertEquals(pricePerNight, room2.getPricePerNight());
        assertEquals(description, room2.getDescription());
    }

    @Test
    public void testGettersAndSetters() {
        // Test setters
        room.setRoomNumber(102);
        room.setFloor(2);
        room.setType(RoomType.SUITE);
        room.setStatus(RoomStatus.OCCUPIED);
        room.setPricePerNight(new BigDecimal("200.00"));
        room.setDescription("New description");

        // Test getters
        assertEquals(102, room.getRoomNumber());
        assertEquals(2, room.getFloor());
        assertEquals(RoomType.SUITE, room.getType());
        assertEquals(RoomStatus.OCCUPIED, room.getStatus());
        assertEquals(new BigDecimal("200.00"), room.getPricePerNight());
        assertEquals("New description", room.getDescription());
    }

    @Test
    public void testGetCapacity() {
        // The capacity should match the room type's capacity
        assertEquals(type.getCapacity(), room.getCapacity());
        
        // Change the room type and verify capacity changes
        room.setType(RoomType.SIMPLE);
        assertEquals(RoomType.SIMPLE.getCapacity(), room.getCapacity());
        
        room.setType(RoomType.SUITE);
        assertEquals(RoomType.SUITE.getCapacity(), room.getCapacity());
    }

    @Test
    public void testToString() {
        // The toString method should return a string containing the room number and type
        String expected = "Room " + roomNumber + " (" + type.getDisplayName() + ")";
        assertEquals(expected, room.toString());
    }
}