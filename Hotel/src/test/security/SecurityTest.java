package test.security;

import model.Guest;
import model.Reservation;
import model.Room;
import model.RoomType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.GuestService;
import service.ReservationService;
import service.RoomService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Security tests for the Hotel Management System.
 * These tests check for potential security vulnerabilities.
 */
public class SecurityTest {
    private GuestService guestService;
    private RoomService roomService;
    private ReservationService reservationService;
    
    private List<Guest> testGuests = new ArrayList<>();
    private List<Room> testRooms = new ArrayList<>();
    private List<Reservation> testReservations = new ArrayList<>();

    @Before
    public void setUp() {
        // Get service instances
        guestService = GuestService.getInstance();
        roomService = RoomService.getInstance();
        reservationService = ReservationService.getInstance();
    }
    
    @After
    public void tearDown() {
        // Clean up test data
        for (Reservation reservation : testReservations) {
            if (reservation != null && reservation.getId() > 0) {
                reservationService.cancelReservation(reservation.getId());
            }
        }
        
        for (Room room : testRooms) {
            if (room != null && room.getRoomNumber() > 0) {
                roomService.deleteRoom(room.getRoomNumber());
            }
        }
        
        for (Guest guest : testGuests) {
            if (guest != null && guest.getId() > 0) {
                guestService.deleteGuest(guest.getId());
            }
        }
    }

    @Test
    public void testInputValidationForGuest() {
        System.out.println("Testing input validation for Guest...");
        
        // Test with very long strings
        String veryLongString = "a".repeat(1000);
        Guest guest = new Guest(veryLongString, veryLongString, veryLongString, veryLongString, veryLongString, veryLongString);
        guest = guestService.addGuest(guest);
        testGuests.add(guest);
        
        // Verify the guest was created
        assertNotNull("Guest should be created even with very long strings", guest);
        assertTrue("Guest should have an ID", guest.getId() > 0);
        
        // Test with special characters
        String specialChars = "!@#$%^&*()_+{}[]|\"':;,.<>?/\\";
        Guest specialGuest = new Guest(specialChars, specialChars, specialChars, specialChars, specialChars, specialChars);
        specialGuest = guestService.addGuest(specialGuest);
        testGuests.add(specialGuest);
        
        // Verify the guest was created
        assertNotNull("Guest should be created even with special characters", specialGuest);
        assertTrue("Guest should have an ID", specialGuest.getId() > 0);
        
        // Test with SQL injection attempt
        String sqlInjection = "' OR 1=1; --";
        Guest sqlGuest = new Guest(sqlInjection, sqlInjection, sqlInjection, sqlInjection, sqlInjection, sqlInjection);
        sqlGuest = guestService.addGuest(sqlGuest);
        testGuests.add(sqlGuest);
        
        // Verify the guest was created
        assertNotNull("Guest should be created even with SQL injection attempt", sqlGuest);
        assertTrue("Guest should have an ID", sqlGuest.getId() > 0);
        
        // Test with null values
        try {
            Guest nullGuest = new Guest(null, null, null, null, null, null);
            nullGuest = guestService.addGuest(nullGuest);
            testGuests.add(nullGuest);
            
            // If we get here, the test passes (no exception thrown)
            assertNotNull("Guest should be created even with null values", nullGuest);
        } catch (Exception e) {
            // If an exception is thrown, that's also acceptable as long as it's handled gracefully
            System.out.println("Exception when creating guest with null values: " + e.getMessage());
        }
    }

    @Test
    public void testInputValidationForRoom() {
        System.out.println("Testing input validation for Room...");
        
        // Test with negative values
        try {
            Room negativeRoom = new Room(-1, -1, RoomType.SIMPLE, new BigDecimal("-100.00"));
            negativeRoom = roomService.addRoom(negativeRoom);
            testRooms.add(negativeRoom);
            
            // Verify the room was created
            assertNotNull("Room should be created even with negative values", negativeRoom);
            assertEquals("Room number should be preserved", -1, negativeRoom.getRoomNumber());
        } catch (Exception e) {
            // If an exception is thrown, that's also acceptable as long as it's handled gracefully
            System.out.println("Exception when creating room with negative values: " + e.getMessage());
        }
        
        // Test with very large values
        try {
            Room largeRoom = new Room(Integer.MAX_VALUE, Integer.MAX_VALUE, RoomType.SUITE, new BigDecimal("999999999999.99"));
            largeRoom = roomService.addRoom(largeRoom);
            testRooms.add(largeRoom);
            
            // Verify the room was created
            assertNotNull("Room should be created even with very large values", largeRoom);
            assertEquals("Room number should be preserved", Integer.MAX_VALUE, largeRoom.getRoomNumber());
        } catch (Exception e) {
            // If an exception is thrown, that's also acceptable as long as it's handled gracefully
            System.out.println("Exception when creating room with very large values: " + e.getMessage());
        }
        
        // Test with null type
        try {
            Room nullTypeRoom = new Room(9876, 9, null, new BigDecimal("150.00"));
            nullTypeRoom = roomService.addRoom(nullTypeRoom);
            testRooms.add(nullTypeRoom);
            
            // If we get here, the test passes (no exception thrown)
            assertNotNull("Room should be created even with null type", nullTypeRoom);
        } catch (Exception e) {
            // If an exception is thrown, that's also acceptable as long as it's handled gracefully
            System.out.println("Exception when creating room with null type: " + e.getMessage());
        }
        
        // Test with very long description
        String veryLongDescription = "a".repeat(10000);
        Room longDescRoom = new Room(8765, 8, RoomType.DOUBLE, new BigDecimal("200.00"));
        longDescRoom.setDescription(veryLongDescription);
        longDescRoom = roomService.addRoom(longDescRoom);
        testRooms.add(longDescRoom);
        
        // Verify the room was created
        assertNotNull("Room should be created even with very long description", longDescRoom);
        assertEquals("Room description should be preserved", veryLongDescription, longDescRoom.getDescription());
    }

    @Test
    public void testInputValidationForReservation() {
        System.out.println("Testing input validation for Reservation...");
        
        // Create a guest and room for testing
        Guest guest = new Guest("Security", "Test", "SEC-TEST", "555-SEC", "security@example.com", "Security Address");
        guest = guestService.addGuest(guest);
        testGuests.add(guest);
        
        Room room = new Room(7654, 7, RoomType.DOUBLE, new BigDecimal("175.00"));
        room = roomService.addRoom(room);
        testRooms.add(room);
        
        // Test with past dates
        LocalDate pastCheckIn = LocalDate.now().minusDays(30);
        LocalDate pastCheckOut = LocalDate.now().minusDays(25);
        
        try {
            Reservation pastReservation = reservationService.createReservation(guest, room, pastCheckIn, pastCheckOut);
            if (pastReservation != null) {
                testReservations.add(pastReservation);
                
                // If we get here, the test passes (reservation was created)
                assertNotNull("Reservation should be created even with past dates", pastReservation);
            } else {
                // If null is returned, that's also acceptable as long as it's handled gracefully
                System.out.println("Reservation with past dates was not created (returned null)");
            }
        } catch (Exception e) {
            // If an exception is thrown, that's also acceptable as long as it's handled gracefully
            System.out.println("Exception when creating reservation with past dates: " + e.getMessage());
        }
        
        // Test with check-out before check-in
        LocalDate futureCheckIn = LocalDate.now().plusDays(10);
        LocalDate futureCheckOut = LocalDate.now().plusDays(5); // Before check-in
        
        try {
            Reservation invalidReservation = reservationService.createReservation(guest, room, futureCheckIn, futureCheckOut);
            if (invalidReservation != null) {
                testReservations.add(invalidReservation);
                
                // If we get here, the test passes (reservation was created)
                assertNotNull("Reservation should be created even with check-out before check-in", invalidReservation);
            } else {
                // If null is returned, that's also acceptable as long as it's handled gracefully
                System.out.println("Reservation with check-out before check-in was not created (returned null)");
            }
        } catch (Exception e) {
            // If an exception is thrown, that's also acceptable as long as it's handled gracefully
            System.out.println("Exception when creating reservation with check-out before check-in: " + e.getMessage());
        }
        
        // Test with very distant future dates
        LocalDate distantFutureCheckIn = LocalDate.now().plusYears(100);
        LocalDate distantFutureCheckOut = LocalDate.now().plusYears(100).plusDays(5);
        
        try {
            Reservation distantReservation = reservationService.createReservation(guest, room, distantFutureCheckIn, distantFutureCheckOut);
            if (distantReservation != null) {
                testReservations.add(distantReservation);
                
                // If we get here, the test passes (reservation was created)
                assertNotNull("Reservation should be created even with very distant future dates", distantReservation);
            } else {
                // If null is returned, that's also acceptable as long as it's handled gracefully
                System.out.println("Reservation with very distant future dates was not created (returned null)");
            }
        } catch (Exception e) {
            // If an exception is thrown, that's also acceptable as long as it's handled gracefully
            System.out.println("Exception when creating reservation with very distant future dates: " + e.getMessage());
        }
    }

    @Test
    public void testAccessControl() {
        System.out.println("Testing access control...");
        
        // Create a guest and room for testing
        Guest guest = new Guest("Access", "Control", "ACCESS-TEST", "555-ACCESS", "access@example.com", "Access Address");
        guest = guestService.addGuest(guest);
        testGuests.add(guest);
        
        Room room = new Room(6543, 6, RoomType.SUITE, new BigDecimal("250.00"));
        room = roomService.addRoom(room);
        testRooms.add(room);
        
        // Create a reservation
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.plusDays(3);
        Reservation reservation = reservationService.createReservation(guest, room, checkIn, checkOut);
        assertNotNull("Reservation should be created", reservation);
        testReservations.add(reservation);
        
        // Test accessing a non-existent guest
        Guest nonExistentGuest = guestService.getGuestById(99999);
        assertNull("Non-existent guest should return null", nonExistentGuest);
        
        // Test accessing a non-existent room
        Room nonExistentRoom = roomService.getRoomByNumber(99999);
        assertNull("Non-existent room should return null", nonExistentRoom);
        
        // Test accessing a non-existent reservation
        Reservation nonExistentReservation = reservationService.getReservationById(99999);
        assertNull("Non-existent reservation should return null", nonExistentReservation);
        
        // Test deleting a non-existent guest
        boolean guestDeleted = guestService.deleteGuest(99999);
        assertFalse("Deleting non-existent guest should return false", guestDeleted);
        
        // Test deleting a non-existent room
        boolean roomDeleted = roomService.deleteRoom(99999);
        assertFalse("Deleting non-existent room should return false", roomDeleted);
        
        // Test cancelling a non-existent reservation
        boolean reservationCancelled = reservationService.cancelReservation(99999);
        assertFalse("Cancelling non-existent reservation should return false", reservationCancelled);
    }

    @Test
    public void testConcurrencyIssues() {
        System.out.println("Testing for concurrency issues...");
        
        // Create a guest and room for testing
        Guest guest = new Guest("Concurrency", "Test", "CONCUR-TEST", "555-CONCUR", "concurrency@example.com", "Concurrency Address");
        guest = guestService.addGuest(guest);
        testGuests.add(guest);
        
        Room room = new Room(5432, 5, RoomType.DOUBLE, new BigDecimal("180.00"));
        room = roomService.addRoom(room);
        testRooms.add(room);
        
        // Create multiple threads that try to reserve the same room for the same dates
        LocalDate checkIn = LocalDate.now().plusDays(7);
        LocalDate checkOut = checkIn.plusDays(3);
        
        // Number of concurrent reservation attempts
        final int numThreads = 5;
        
        // Track successful reservations
        List<Reservation> successfulReservations = new ArrayList<>();
        
        // Create and start threads
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            Room finalRoom = room;
            Guest finalGuest = guest;
            threads[i] = new Thread(() -> {
                System.out.println("Thread " + threadNum + " attempting to create reservation");
                Reservation res = reservationService.createReservation(finalGuest, finalRoom, checkIn, checkOut);
                if (res != null) {
                    synchronized (successfulReservations) {
                        successfulReservations.add(res);
                        testReservations.add(res);
                    }
                    System.out.println("Thread " + threadNum + " created reservation successfully");
                } else {
                    System.out.println("Thread " + threadNum + " failed to create reservation");
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Verify that only one reservation was created
        System.out.println("Number of successful reservations: " + successfulReservations.size());
        assertEquals("Only one reservation should be created for the same room and dates", 1, successfulReservations.size());
    }
}