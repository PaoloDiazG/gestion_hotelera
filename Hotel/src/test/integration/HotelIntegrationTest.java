package test.integration;

import model.Bill;
import model.Guest;
import model.Reservation;
import model.ReservationStatus;
import model.Room;
import model.RoomStatus;
import model.RoomType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.BillingService;
import service.GuestService;
import service.ReservationService;
import service.RoomService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for the Hotel Management System.
 * These tests verify that the different services work together correctly.
 */
public class HotelIntegrationTest {
    private GuestService guestService;
    private RoomService roomService;
    private ReservationService reservationService;
    private BillingService billingService;
    
    private Guest testGuest;
    private Room testRoom;
    private Reservation testReservation;
    private Bill testBill;
    
    private final LocalDate checkInDate = LocalDate.now();
    private final LocalDate checkOutDate = checkInDate.plusDays(3);

    @Before
    public void setUp() {
        // Get service instances
        guestService = GuestService.getInstance();
        roomService = RoomService.getInstance();
        reservationService = ReservationService.getInstance();
        billingService = BillingService.getInstance();
    }
    
    @After
    public void tearDown() {
        // Clean up any test data
        if (testReservation != null && testReservation.getId() > 0) {
            reservationService.cancelReservation(testReservation.getId());
        }
        
        if (testRoom != null && testRoom.getRoomNumber() > 0) {
            roomService.deleteRoom(testRoom.getRoomNumber());
        }
        
        if (testGuest != null && testGuest.getId() > 0) {
            guestService.deleteGuest(testGuest.getId());
        }
    }

    @Test
    public void testCompleteHotelWorkflow() {
        // 1. Create a guest
        testGuest = new Guest("Integration", "Test", "INT-TEST-123", "555-INT-TEST", 
                             "integration.test@example.com", "123 Integration St");
        testGuest = guestService.addGuest(testGuest);
        
        assertNotNull("Guest should be created", testGuest);
        assertTrue("Guest should have an ID", testGuest.getId() > 0);
        
        // 2. Create a room
        testRoom = new Room(888, 8, RoomType.DOUBLE, new BigDecimal("200.00"));
        testRoom = roomService.addRoom(testRoom);
        
        assertNotNull("Room should be created", testRoom);
        assertEquals("Room should have the correct number", 888, testRoom.getRoomNumber());
        assertEquals("Room should have AVAILABLE status", RoomStatus.AVAILABLE, testRoom.getStatus());
        
        // 3. Create a reservation
        testReservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);
        
        assertNotNull("Reservation should be created", testReservation);
        assertTrue("Reservation should have an ID", testReservation.getId() > 0);
        assertEquals("Reservation should have CONFIRMED status", ReservationStatus.CONFIRMED, testReservation.getStatus());
        
        // 4. Check room availability
        assertFalse("Room should not be available for the reserved dates", 
                   reservationService.isRoomAvailable(testRoom.getRoomNumber(), checkInDate, checkOutDate));
        
        List<Room> availableRooms = reservationService.getAvailableRoomsForDates(checkInDate, checkOutDate);
        assertFalse("Available rooms list should not contain the reserved room",
                   availableRooms.stream().anyMatch(r -> r.getRoomNumber() == testRoom.getRoomNumber()));
        
        // 5. Check in
        boolean checkedIn = reservationService.checkIn(testReservation.getId());
        assertTrue("Check-in should succeed", checkedIn);
        
        Reservation updatedReservation = reservationService.getReservationById(testReservation.getId());
        assertEquals("Reservation status should be CHECKED_IN", 
                    ReservationStatus.CHECKED_IN, updatedReservation.getStatus());
        
        Room updatedRoom = roomService.getRoomByNumber(testRoom.getRoomNumber());
        assertEquals("Room status should be OCCUPIED", RoomStatus.OCCUPIED, updatedRoom.getStatus());
        
        // 6. Check out
        boolean checkedOut = reservationService.checkOut(testReservation.getId());
        assertTrue("Check-out should succeed", checkedOut);
        
        updatedReservation = reservationService.getReservationById(testReservation.getId());
        assertEquals("Reservation status should be CHECKED_OUT", 
                    ReservationStatus.CHECKED_OUT, updatedReservation.getStatus());
        
        updatedRoom = roomService.getRoomByNumber(testRoom.getRoomNumber());
        assertEquals("Room status should be CLEANING", RoomStatus.CLEANING, updatedRoom.getStatus());
        
        // 7. Create a bill
        testBill = billingService.createBill(updatedReservation);
        
        assertNotNull("Bill should be created", testBill);
        assertTrue("Bill should have an ID", testBill.getId() > 0);
        assertEquals("Bill should have one item for room charge", 1, testBill.getItems().size());
        assertEquals("Bill total should match reservation total", 
                    updatedReservation.getTotalPrice(), testBill.calculateTotal());
        
        // 8. Add extra services to the bill
        boolean itemAdded = billingService.addItemToBill(testBill.getId(), "Room Service", new BigDecimal("50.00"));
        assertTrue("Adding item to bill should succeed", itemAdded);
        
        Bill updatedBill = billingService.getBillById(testBill.getId());
        assertEquals("Bill should have two items", 2, updatedBill.getItems().size());
        
        BigDecimal expectedTotal = updatedReservation.getTotalPrice().add(new BigDecimal("50.00"));
        assertEquals("Bill total should include extra service", 
                    expectedTotal, updatedBill.calculateTotal());
        
        // 9. Mark bill as paid
        boolean marked = billingService.markBillAsPaid(testBill.getId());
        assertTrue("Marking bill as paid should succeed", marked);
        
        updatedBill = billingService.getBillById(testBill.getId());
        assertTrue("Bill should be marked as paid", updatedBill.isPaid());
        
        // 10. Verify bill appears in paid bills list
        List<Bill> paidBills = billingService.getBillsByPaidStatus(true);
        assertTrue("Paid bills list should contain our bill",
                  paidBills.stream().anyMatch(b -> b.getId() == testBill.getId()));
        
        // 11. Make room available again
        boolean statusChanged = roomService.changeRoomStatus(testRoom.getRoomNumber(), RoomStatus.AVAILABLE);
        assertTrue("Changing room status should succeed", statusChanged);
        
        updatedRoom = roomService.getRoomByNumber(testRoom.getRoomNumber());
        assertEquals("Room status should be AVAILABLE", RoomStatus.AVAILABLE, updatedRoom.getStatus());
    }
    
    @Test
    public void testReservationCancellation() {
        // 1. Create a guest
        testGuest = new Guest("Cancel", "Test", "CANCEL-TEST-123", "555-CANCEL", 
                             "cancel.test@example.com", "123 Cancel St");
        testGuest = guestService.addGuest(testGuest);
        
        // 2. Create a room
        testRoom = new Room(777, 7, RoomType.SIMPLE, new BigDecimal("100.00"));
        testRoom = roomService.addRoom(testRoom);
        
        // 3. Create a reservation
        testReservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);
        
        // 4. Cancel the reservation
        boolean cancelled = reservationService.cancelReservation(testReservation.getId());
        assertTrue("Cancellation should succeed", cancelled);
        
        Reservation updatedReservation = reservationService.getReservationById(testReservation.getId());
        assertEquals("Reservation status should be CANCELLED", 
                    ReservationStatus.CANCELLED, updatedReservation.getStatus());
        
        // 5. Verify room is available again
        assertTrue("Room should be available after cancellation", 
                  reservationService.isRoomAvailable(testRoom.getRoomNumber(), checkInDate, checkOutDate));
        
        List<Room> availableRooms = reservationService.getAvailableRoomsForDates(checkInDate, checkOutDate);
        assertTrue("Available rooms list should contain the room after cancellation",
                  availableRooms.stream().anyMatch(r -> r.getRoomNumber() == testRoom.getRoomNumber()));
    }
    
    @Test
    public void testMultipleReservationsForSameRoom() {
        // 1. Create a guest
        testGuest = new Guest("Multiple", "Test", "MULTI-TEST-123", "555-MULTI", 
                             "multiple.test@example.com", "123 Multiple St");
        testGuest = guestService.addGuest(testGuest);
        
        // 2. Create a room
        testRoom = new Room(666, 6, RoomType.SUITE, new BigDecimal("300.00"));
        testRoom = roomService.addRoom(testRoom);
        
        // 3. Create first reservation
        LocalDate firstCheckIn = checkInDate;
        LocalDate firstCheckOut = checkInDate.plusDays(3);
        Reservation firstReservation = reservationService.createReservation(
            testGuest, testRoom, firstCheckIn, firstCheckOut);
        
        assertNotNull("First reservation should be created", firstReservation);
        
        // 4. Try to create an overlapping reservation (should fail)
        LocalDate overlapCheckIn = checkInDate.plusDays(1);
        LocalDate overlapCheckOut = checkInDate.plusDays(4);
        Reservation overlapReservation = reservationService.createReservation(
            testGuest, testRoom, overlapCheckIn, overlapCheckOut);
        
        assertNull("Overlapping reservation should not be created", overlapReservation);
        
        // 5. Create a non-overlapping reservation
        LocalDate secondCheckIn = firstCheckOut.plusDays(1); // One day after first checkout
        LocalDate secondCheckOut = secondCheckIn.plusDays(2);
        Reservation secondReservation = reservationService.createReservation(
            testGuest, testRoom, secondCheckIn, secondCheckOut);
        
        assertNotNull("Non-overlapping reservation should be created", secondReservation);
        
        // Clean up the second reservation
        reservationService.cancelReservation(secondReservation.getId());
        
        // Set testReservation to the first one so it gets cleaned up in tearDown
        testReservation = firstReservation;
    }
}