package test.service;

import model.Guest;
import model.Reservation;
import model.ReservationStatus;
import model.Room;
import model.RoomStatus;
import model.RoomType;
import org.junit.Before;
import org.junit.Test;
import service.GuestService;
import service.ReservationService;
import service.RoomService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the ReservationService class.
 */
public class ReservationServiceTest {
    private ReservationService reservationService;
    private RoomService roomService;
    private GuestService guestService;
    private Guest testGuest;
    private Room testRoom;
    private final LocalDate checkInDate = LocalDate.of(2023, 7, 1);
    private final LocalDate checkOutDate = LocalDate.of(2023, 7, 5);

    @Before
    public void setUp() {
        // Get the singleton instances of services
        reservationService = ReservationService.getInstance();
        roomService = RoomService.getInstance();
        guestService = GuestService.getInstance();

        // Create a test guest
        testGuest = new Guest("Test", "User", "TEST123456", "555-TEST", "test.user@example.com", "123 Test St");
        testGuest = guestService.addGuest(testGuest);

        // Create a test room
        testRoom = new Room(999, 9, RoomType.SUITE, RoomStatus.AVAILABLE, new BigDecimal("300.00"), "Test Room");
        testRoom = roomService.addRoom(testRoom);

        // Ensure the room status is AVAILABLE
        roomService.changeRoomStatus(testRoom.getRoomNumber(), RoomStatus.AVAILABLE);

        // Cancel any existing reservations for this room that might overlap with our test dates
        List<Reservation> existingReservations = reservationService.getReservationsByRoom(testRoom);
        for (Reservation reservation : existingReservations) {
            if (reservation.overlaps(checkInDate, checkOutDate) && 
                reservation.getStatus() != ReservationStatus.CANCELLED) {
                reservationService.cancelReservation(reservation.getId());
            }
        }
    }

    @Test
    public void testSingletonPattern() {
        // Test that getInstance always returns the same instance
        ReservationService instance1 = ReservationService.getInstance();
        ReservationService instance2 = ReservationService.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    public void testCreateReservation() {
        // Create a reservation
        Reservation reservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Verify the reservation was created and assigned an ID
        assertNotNull("Failed to create test reservation. Room might not be available for the given dates.", reservation);
        assertTrue(reservation.getId() > 0);
        assertEquals(testGuest, reservation.getGuest());
        assertEquals(testRoom, reservation.getRoom());
        assertEquals(checkInDate, reservation.getCheckInDate());
        assertEquals(checkOutDate, reservation.getCheckOutDate());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());

        // Verify we can retrieve the reservation
        Reservation retrievedReservation = reservationService.getReservationById(reservation.getId());
        assertNotNull(retrievedReservation);
        assertEquals(reservation.getId(), retrievedReservation.getId());
        assertEquals(testGuest.getId(), retrievedReservation.getGuest().getId());
        assertEquals(testRoom.getRoomNumber(), retrievedReservation.getRoom().getRoomNumber());

        // Clean up
        reservationService.cancelReservation(reservation.getId());
    }

    @Test
    public void testCreateReservationWithUnavailableRoom() {
        // Create a first reservation
        Reservation reservation1 = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);
        assertNotNull(reservation1);

        // Try to create a second reservation for the same room and overlapping dates
        Reservation reservation2 = reservationService.createReservation(testGuest, testRoom, checkInDate.plusDays(1), checkOutDate.plusDays(1));

        // Should return null as the room is not available
        assertNull(reservation2);

        // Clean up
        reservationService.cancelReservation(reservation1.getId());
    }

    @Test
    public void testUpdateReservation() {
        // Create a reservation
        Reservation reservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Modify the reservation
        LocalDate newCheckInDate = checkInDate.plusDays(1);
        LocalDate newCheckOutDate = checkOutDate.plusDays(1);
        reservation.setCheckInDate(newCheckInDate);
        reservation.setCheckOutDate(newCheckOutDate);
        reservation.setNotes("Updated notes");

        // Update the reservation
        Reservation updatedReservation = reservationService.updateReservation(reservation);

        // Verify the reservation was updated
        assertNotNull(updatedReservation);
        assertEquals(reservation.getId(), updatedReservation.getId());
        assertEquals(newCheckInDate, updatedReservation.getCheckInDate());
        assertEquals(newCheckOutDate, updatedReservation.getCheckOutDate());
        assertEquals("Updated notes", updatedReservation.getNotes());

        // Clean up
        reservationService.cancelReservation(reservation.getId());
    }

    @Test
    public void testUpdateNonExistentReservation() {
        // Create a reservation with a non-existent ID
        Reservation nonExistentReservation = new Reservation(9999, testGuest, testRoom, checkInDate, checkOutDate, 
                                                           ReservationStatus.CONFIRMED, new BigDecimal("1200.00"), "");

        // Try to update the reservation
        Reservation result = reservationService.updateReservation(nonExistentReservation);

        // Should return null
        assertNull(result);
    }

    @Test
    public void testCancelReservation() {
        // Create a reservation
        Reservation reservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Verify the reservation was created successfully
        assertNotNull("Failed to create test reservation. Room might not be available for the given dates.", reservation);

        // Cancel the reservation
        boolean cancelled = reservationService.cancelReservation(reservation.getId());

        // Verify the reservation was cancelled
        assertTrue(cancelled);
        Reservation retrievedReservation = reservationService.getReservationById(reservation.getId());
        assertEquals(ReservationStatus.CANCELLED, retrievedReservation.getStatus());
    }

    @Test
    public void testCancelNonExistentReservation() {
        // Try to cancel a reservation that doesn't exist
        boolean cancelled = reservationService.cancelReservation(9999);

        // Should return false
        assertFalse(cancelled);
    }

    @Test
    public void testCheckInAndCheckOut() {
        // Create a reservation
        Reservation reservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Check in
        boolean checkedIn = reservationService.checkIn(reservation.getId());

        // Verify check-in was successful
        assertTrue(checkedIn);
        Reservation retrievedReservation = reservationService.getReservationById(reservation.getId());
        assertEquals(ReservationStatus.CHECKED_IN, retrievedReservation.getStatus());
        Room retrievedRoom = roomService.getRoomByNumber(testRoom.getRoomNumber());
        assertEquals(RoomStatus.OCCUPIED, retrievedRoom.getStatus());

        // Check out
        boolean checkedOut = reservationService.checkOut(reservation.getId());

        // Verify check-out was successful
        assertTrue(checkedOut);
        retrievedReservation = reservationService.getReservationById(reservation.getId());
        assertEquals(ReservationStatus.CHECKED_OUT, retrievedReservation.getStatus());
        retrievedRoom = roomService.getRoomByNumber(testRoom.getRoomNumber());
        assertEquals(RoomStatus.CLEANING, retrievedRoom.getStatus());
    }

    @Test
    public void testGetAllReservations() {
        // Create a reservation
        Reservation reservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Verify the reservation was created successfully
        assertNotNull("Failed to create test reservation. Room might not be available for the given dates.", reservation);

        // Get all reservations
        List<Reservation> allReservations = reservationService.getAllReservations();

        // Should not be null and should contain our reservation
        assertNotNull(allReservations);
        assertTrue(allReservations.stream().anyMatch(r -> r.getId() == reservation.getId()));

        // Clean up
        reservationService.cancelReservation(reservation.getId());
    }

    @Test
    public void testGetReservationsByGuest() {
        // Create a reservation
        Reservation reservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Get reservations by guest
        List<Reservation> guestReservations = reservationService.getReservationsByGuest(testGuest);

        // Should not be null and should contain our reservation
        assertNotNull(guestReservations);
        assertTrue(guestReservations.stream().anyMatch(r -> r.getId() == reservation.getId()));

        // Clean up
        reservationService.cancelReservation(reservation.getId());
    }

    @Test
    public void testGetReservationsByRoom() {
        // Create a reservation
        Reservation reservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Get reservations by room
        List<Reservation> roomReservations = reservationService.getReservationsByRoom(testRoom);

        // Should not be null and should contain our reservation
        assertNotNull(roomReservations);
        assertTrue(roomReservations.stream().anyMatch(r -> r.getId() == reservation.getId()));

        // Clean up
        reservationService.cancelReservation(reservation.getId());
    }

    @Test
    public void testGetReservationsByStatus() {
        // Create a reservation
        Reservation reservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Verify the reservation was created successfully
        assertNotNull("Failed to create test reservation. Room might not be available for the given dates.", reservation);

        // Get reservations by status
        List<Reservation> confirmedReservations = reservationService.getReservationsByStatus(ReservationStatus.CONFIRMED);

        // Should not be null and should contain our reservation
        assertNotNull(confirmedReservations);
        assertTrue(confirmedReservations.stream().anyMatch(r -> r.getId() == reservation.getId()));

        // Clean up
        reservationService.cancelReservation(reservation.getId());
    }

    @Test
    public void testGetReservationsForDateRange() {
        // Create a reservation
        Reservation reservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Get reservations for date range
        List<Reservation> dateRangeReservations = reservationService.getReservationsForDateRange(
                checkInDate.minusDays(1), checkOutDate.plusDays(1));

        // Should not be null and should contain our reservation
        assertNotNull(dateRangeReservations);
        assertTrue(dateRangeReservations.stream().anyMatch(r -> r.getId() == reservation.getId()));

        // Clean up
        reservationService.cancelReservation(reservation.getId());
    }

    @Test
    public void testIsRoomAvailable() {
        // Initially the room should be available
        assertTrue(reservationService.isRoomAvailable(testRoom.getRoomNumber(), checkInDate, checkOutDate));

        // Create a reservation
        Reservation reservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Now the room should not be available for the same dates
        assertFalse(reservationService.isRoomAvailable(testRoom.getRoomNumber(), checkInDate, checkOutDate));

        // But it should be available for non-overlapping dates
        assertTrue(reservationService.isRoomAvailable(testRoom.getRoomNumber(), 
                checkOutDate.plusDays(1), checkOutDate.plusDays(5)));

        // Clean up
        reservationService.cancelReservation(reservation.getId());
    }

    @Test
    public void testGetAvailableRoomsForDates() {
        // Initially the room should be in the available rooms list
        List<Room> availableRooms = reservationService.getAvailableRoomsForDates(checkInDate, checkOutDate);
        assertTrue(availableRooms.stream().anyMatch(r -> r.getRoomNumber() == testRoom.getRoomNumber()));

        // Create a reservation
        Reservation reservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Verify the reservation was created successfully
        assertNotNull("Failed to create test reservation. Room might not be available for the given dates.", reservation);

        // Now the room should not be in the available rooms list for the same dates
        availableRooms = reservationService.getAvailableRoomsForDates(checkInDate, checkOutDate);
        assertFalse(availableRooms.stream().anyMatch(r -> r.getRoomNumber() == testRoom.getRoomNumber()));

        // But it should be available for non-overlapping dates
        availableRooms = reservationService.getAvailableRoomsForDates(
                checkOutDate.plusDays(1), checkOutDate.plusDays(5));
        assertTrue(availableRooms.stream().anyMatch(r -> r.getRoomNumber() == testRoom.getRoomNumber()));

        // Clean up
        reservationService.cancelReservation(reservation.getId());
    }

    @Test
    public void testGetAvailableRoomsByTypeForDates() {
        // Initially the room should be in the available rooms list
        List<Room> availableSuiteRooms = reservationService.getAvailableRoomsByTypeForDates(
                RoomType.SUITE, checkInDate, checkOutDate);
        assertTrue(availableSuiteRooms.stream().anyMatch(r -> r.getRoomNumber() == testRoom.getRoomNumber()));

        // Create a reservation
        Reservation reservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Verify the reservation was created successfully
        assertNotNull("Failed to create test reservation. Room might not be available for the given dates.", reservation);

        // Now the room should not be in the available rooms list for the same dates
        availableSuiteRooms = reservationService.getAvailableRoomsByTypeForDates(
                RoomType.SUITE, checkInDate, checkOutDate);
        assertFalse(availableSuiteRooms.stream().anyMatch(r -> r.getRoomNumber() == testRoom.getRoomNumber()));

        // Clean up
        reservationService.cancelReservation(reservation.getId());
    }
}
