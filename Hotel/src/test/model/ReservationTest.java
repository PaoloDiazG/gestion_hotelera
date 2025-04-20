package test.model;

import model.Guest;
import model.Reservation;
import model.ReservationStatus;
import model.Room;
import model.RoomType;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Unit tests for the Reservation class.
 */
public class ReservationTest {
    private Reservation reservation;
    private Guest guest;
    private Room room;
    private final LocalDate checkInDate = LocalDate.of(2023, 7, 1);
    private final LocalDate checkOutDate = LocalDate.of(2023, 7, 5);
    private final String notes = "Test reservation notes";

    @Before
    public void setUp() {
        // Create a guest and room for the reservation
        guest = new Guest(1, "John", "Doe", "123456789", "555-1234", "john.doe@example.com", "123 Main St");
        room = new Room(101, 1, RoomType.DOUBLE, new BigDecimal("150.00"));
        
        // Create a new reservation before each test
        reservation = new Reservation(guest, room, checkInDate, checkOutDate);
    }

    @Test
    public void testConstructor() {
        // Test the first constructor (without ID)
        assertEquals(0, reservation.getId()); // ID should be 0 until assigned
        assertEquals(guest, reservation.getGuest());
        assertEquals(room, reservation.getRoom());
        assertEquals(checkInDate, reservation.getCheckInDate());
        assertEquals(checkOutDate, reservation.getCheckOutDate());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus()); // Default status should be CONFIRMED
        assertEquals(new BigDecimal("600.00"), reservation.getTotalPrice()); // 4 nights * 150.00
        assertEquals("", reservation.getNotes()); // Default notes should be empty string

        // Test the second constructor (with ID and all parameters)
        int id = 1;
        BigDecimal totalPrice = new BigDecimal("600.00");
        Reservation reservation2 = new Reservation(id, guest, room, checkInDate, checkOutDate, 
                                                 ReservationStatus.CHECKED_IN, totalPrice, notes);
        assertEquals(id, reservation2.getId());
        assertEquals(guest, reservation2.getGuest());
        assertEquals(room, reservation2.getRoom());
        assertEquals(checkInDate, reservation2.getCheckInDate());
        assertEquals(checkOutDate, reservation2.getCheckOutDate());
        assertEquals(ReservationStatus.CHECKED_IN, reservation2.getStatus());
        assertEquals(totalPrice, reservation2.getTotalPrice());
        assertEquals(notes, reservation2.getNotes());
    }

    @Test
    public void testGettersAndSetters() {
        // Test setters
        int id = 1;
        Guest newGuest = new Guest(2, "Jane", "Smith", "987654321", "555-5678", "jane.smith@example.com", "456 Oak Ave");
        Room newRoom = new Room(102, 1, RoomType.SUITE, new BigDecimal("250.00"));
        LocalDate newCheckInDate = LocalDate.of(2023, 8, 1);
        LocalDate newCheckOutDate = LocalDate.of(2023, 8, 5);
        ReservationStatus newStatus = ReservationStatus.CHECKED_IN;
        BigDecimal newTotalPrice = new BigDecimal("1000.00");
        String newNotes = "New notes";

        reservation.setId(id);
        reservation.setGuest(newGuest);
        reservation.setRoom(newRoom);
        reservation.setCheckInDate(newCheckInDate);
        reservation.setCheckOutDate(newCheckOutDate);
        reservation.setStatus(newStatus);
        reservation.setTotalPrice(newTotalPrice);
        reservation.setNotes(newNotes);

        // Test getters
        assertEquals(id, reservation.getId());
        assertEquals(newGuest, reservation.getGuest());
        assertEquals(newRoom, reservation.getRoom());
        assertEquals(newCheckInDate, reservation.getCheckInDate());
        assertEquals(newCheckOutDate, reservation.getCheckOutDate());
        assertEquals(newStatus, reservation.getStatus());
        assertEquals(newTotalPrice, reservation.getTotalPrice());
        assertEquals(newNotes, reservation.getNotes());
    }

    @Test
    public void testCalculateTotalPrice() {
        // The total price should be the room price per night multiplied by the number of nights
        BigDecimal expected = room.getPricePerNight().multiply(new BigDecimal(4)); // 4 nights
        assertEquals(expected, reservation.calculateTotalPrice());
        
        // Test with different dates
        reservation.setCheckInDate(LocalDate.of(2023, 7, 1));
        reservation.setCheckOutDate(LocalDate.of(2023, 7, 3));
        expected = room.getPricePerNight().multiply(new BigDecimal(2)); // 2 nights
        assertEquals(expected, reservation.calculateTotalPrice());
        
        // Test with different room price
        room.setPricePerNight(new BigDecimal("200.00"));
        expected = new BigDecimal("400.00"); // 2 nights * 200.00
        assertEquals(expected, reservation.calculateTotalPrice());
    }

    @Test
    public void testGetNights() {
        // The getNights method should return the number of nights between check-in and check-out
        assertEquals(4, reservation.getNights()); // July 1 to July 5 is 4 nights
        
        // Test with different dates
        reservation.setCheckInDate(LocalDate.of(2023, 7, 1));
        reservation.setCheckOutDate(LocalDate.of(2023, 7, 3));
        assertEquals(2, reservation.getNights()); // July 1 to July 3 is 2 nights
    }

    @Test
    public void testOverlaps() {
        // Test with overlapping dates
        assertTrue(reservation.overlaps(LocalDate.of(2023, 7, 3), LocalDate.of(2023, 7, 7))); // Overlaps at the end
        assertTrue(reservation.overlaps(LocalDate.of(2023, 6, 29), LocalDate.of(2023, 7, 2))); // Overlaps at the beginning
        assertTrue(reservation.overlaps(LocalDate.of(2023, 6, 29), LocalDate.of(2023, 7, 7))); // Completely contains
        assertTrue(reservation.overlaps(LocalDate.of(2023, 7, 2), LocalDate.of(2023, 7, 4))); // Completely contained
        
        // Test with non-overlapping dates
        assertFalse(reservation.overlaps(LocalDate.of(2023, 6, 25), LocalDate.of(2023, 6, 30))); // Before
        assertFalse(reservation.overlaps(LocalDate.of(2023, 7, 6), LocalDate.of(2023, 7, 10))); // After
    }

    @Test
    public void testToString() {
        // The toString method should return a string containing the reservation details
        String expected = "Reservation #" + reservation.getId() + " - " + guest.getFullName() + " - " + room.toString() + 
                         " - " + checkInDate + " to " + checkOutDate;
        assertEquals(expected, reservation.toString());
    }
}