package test.service;

import model.Bill;
import model.Guest;
import model.Reservation;
import model.ReservationStatus;
import model.Room;
import model.RoomStatus;
import model.RoomType;
import org.junit.Before;
import org.junit.Test;
import service.BillingService;
import service.GuestService;
import service.ReservationService;
import service.RoomService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the BillingService class.
 */
public class BillingServiceTest {
    private BillingService billingService;
    private ReservationService reservationService;
    private RoomService roomService;
    private GuestService guestService;
    private Guest testGuest;
    private Room testRoom;
    private Reservation testReservation;
    private final LocalDate checkInDate = LocalDate.of(2023, 7, 1);
    private final LocalDate checkOutDate = LocalDate.of(2023, 7, 5);

    @Before
    public void setUp() {
        // Get the singleton instances of services
        billingService = BillingService.getInstance();
        reservationService = ReservationService.getInstance();
        roomService = RoomService.getInstance();
        guestService = GuestService.getInstance();

        // Create a test guest
        testGuest = new Guest("Test", "User", "TEST123456", "555-TEST", "test.user@example.com", "123 Test St");
        testGuest = guestService.addGuest(testGuest);

        // Create a test room
        testRoom = new Room(999, 9, RoomType.SUITE, new BigDecimal("300.00"));
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

        // Create a test reservation
        testReservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);

        // Check in and check out the reservation
        if (testReservation != null) {
            reservationService.checkIn(testReservation.getId());
            reservationService.checkOut(testReservation.getId());
        } else {
            throw new RuntimeException("Failed to create test reservation. Room might not be available for the given dates.");
        }
    }

    @Test
    public void testSingletonPattern() {
        // Test that getInstance always returns the same instance
        BillingService instance1 = BillingService.getInstance();
        BillingService instance2 = BillingService.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    public void testCreateBill() {
        // Create a bill
        Bill bill = billingService.createBill(testReservation);

        // Verify the bill was created and assigned an ID
        assertNotNull(bill);
        assertTrue(bill.getId() > 0);
        assertEquals(testReservation, bill.getReservation());
        assertFalse(bill.isPaid());
        assertEquals(1, bill.getItems().size()); // Should have one item for the room charge
        assertEquals(testReservation.getTotalPrice(), bill.calculateTotal());

        // Verify we can retrieve the bill
        Bill retrievedBill = billingService.getBillById(bill.getId());
        assertNotNull(retrievedBill);
        assertEquals(bill.getId(), retrievedBill.getId());
        assertEquals(testReservation.getId(), retrievedBill.getReservation().getId());
    }

    @Test
    public void testCreateBillForNonCheckedOutReservation() {
        // Define new dates for this test
        LocalDate newCheckInDate = checkInDate.plusDays(10);
        LocalDate newCheckOutDate = checkOutDate.plusDays(10);

        // Ensure the room status is AVAILABLE
        roomService.changeRoomStatus(testRoom.getRoomNumber(), RoomStatus.AVAILABLE);

        // Cancel any existing reservations for this room that might overlap with our new test dates
        List<Reservation> existingReservations = reservationService.getReservationsByRoom(testRoom);
        for (Reservation reservation : existingReservations) {
            if (reservation.overlaps(newCheckInDate, newCheckOutDate) && 
                reservation.getStatus() != ReservationStatus.CANCELLED) {
                reservationService.cancelReservation(reservation.getId());
            }
        }

        // Create a new reservation that is not checked out
        Reservation newReservation = reservationService.createReservation(testGuest, testRoom, 
                newCheckInDate, newCheckOutDate);

        // Ensure the reservation was created successfully
        assertNotNull("Failed to create test reservation. Room might not be available for the given dates.", newReservation);

        // Try to create a bill
        Bill bill = billingService.createBill(newReservation);

        // Should return null because the reservation is not checked out
        assertNull(bill);

        // Clean up
        reservationService.cancelReservation(newReservation.getId());
    }

    @Test
    public void testCreateBillForReservationWithExistingBill() {
        // Create a bill
        Bill bill1 = billingService.createBill(testReservation);
        assertNotNull(bill1);

        // Try to create another bill for the same reservation
        Bill bill2 = billingService.createBill(testReservation);

        // Should return the existing bill
        assertNotNull(bill2);
        assertEquals(bill1.getId(), bill2.getId());
    }

    @Test
    public void testAddItemToBill() {
        // Create a bill
        Bill bill = billingService.createBill(testReservation);

        // Add an item
        boolean added = billingService.addItemToBill(bill.getId(), "Extra Service", new BigDecimal("50.00"));

        // Verify the item was added
        assertTrue(added);
        Bill retrievedBill = billingService.getBillById(bill.getId());
        assertEquals(2, retrievedBill.getItems().size());
        assertEquals(testReservation.getTotalPrice().add(new BigDecimal("50.00")), retrievedBill.calculateTotal());
    }

    @Test
    public void testAddItemToNonExistentBill() {
        // Try to add an item to a bill that doesn't exist
        boolean added = billingService.addItemToBill(9999, "Extra Service", new BigDecimal("50.00"));

        // Should return false
        assertFalse(added);
    }

    @Test
    public void testRemoveItemFromBill() {
        // Create a bill
        Bill bill = billingService.createBill(testReservation);

        // Add an item
        billingService.addItemToBill(bill.getId(), "Extra Service", new BigDecimal("50.00"));

        // Get the bill with the added item
        Bill retrievedBill = billingService.getBillById(bill.getId());
        assertEquals(2, retrievedBill.getItems().size());

        // Get the added item
        Bill.BillItem itemToRemove = retrievedBill.getItems().get(1);

        // Remove the item
        boolean removed = billingService.removeItemFromBill(bill.getId(), itemToRemove);

        // Verify the item was removed
        assertTrue(removed);
        retrievedBill = billingService.getBillById(bill.getId());
        assertEquals(1, retrievedBill.getItems().size());
        assertEquals(testReservation.getTotalPrice(), retrievedBill.calculateTotal());
    }

    @Test
    public void testRemoveItemFromNonExistentBill() {
        // Create a bill item
        Bill.BillItem item = new Bill.BillItem("Extra Service", new BigDecimal("50.00"));

        // Try to remove the item from a bill that doesn't exist
        boolean removed = billingService.removeItemFromBill(9999, item);

        // Should return false
        assertFalse(removed);
    }

    @Test
    public void testMarkBillAsPaid() {
        // Create a bill
        Bill bill = billingService.createBill(testReservation);

        // Mark the bill as paid
        boolean marked = billingService.markBillAsPaid(bill.getId());

        // Verify the bill was marked as paid
        assertTrue(marked);
        Bill retrievedBill = billingService.getBillById(bill.getId());
        assertTrue(retrievedBill.isPaid());
    }

    @Test
    public void testMarkNonExistentBillAsPaid() {
        // Try to mark a bill that doesn't exist as paid
        boolean marked = billingService.markBillAsPaid(9999);

        // Should return false
        assertFalse(marked);
    }

    @Test
    public void testGetBillByReservation() {
        // Create a bill
        Bill bill = billingService.createBill(testReservation);

        // Get the bill by reservation
        Bill retrievedBill = billingService.getBillByReservation(testReservation);

        // Verify the bill was retrieved
        assertNotNull(retrievedBill);
        assertEquals(bill.getId(), retrievedBill.getId());
    }

    @Test
    public void testGetBillByNonExistentReservation() {
        // Create a reservation with a non-existent ID
        Reservation nonExistentReservation = new Reservation(9999, testGuest, testRoom, checkInDate, checkOutDate, 
                                                           ReservationStatus.CHECKED_OUT, new BigDecimal("1200.00"), "");

        // Try to get a bill for the reservation
        Bill bill = billingService.getBillByReservation(nonExistentReservation);

        // Should return null
        assertNull(bill);
    }

    @Test
    public void testGetAllBills() {
        // Create a bill
        Bill bill = billingService.createBill(testReservation);

        // Get all bills
        List<Bill> allBills = billingService.getAllBills();

        // Should not be null and should contain our bill
        assertNotNull(allBills);
        assertTrue(allBills.stream().anyMatch(b -> b.getId() == bill.getId()));
    }

    @Test
    public void testGetBillsByPaidStatus() {
        // Create a bill
        Bill bill = billingService.createBill(testReservation);

        // Get unpaid bills
        List<Bill> unpaidBills = billingService.getBillsByPaidStatus(false);

        // Should not be null and should contain our bill
        assertNotNull(unpaidBills);
        assertTrue(unpaidBills.stream().anyMatch(b -> b.getId() == bill.getId()));

        // Mark the bill as paid
        billingService.markBillAsPaid(bill.getId());

        // Get paid bills
        List<Bill> paidBills = billingService.getBillsByPaidStatus(true);

        // Should not be null and should contain our bill
        assertNotNull(paidBills);
        assertTrue(paidBills.stream().anyMatch(b -> b.getId() == bill.getId()));

        // Get unpaid bills again
        unpaidBills = billingService.getBillsByPaidStatus(false);

        // Should not contain our bill anymore
        assertFalse(unpaidBills.stream().anyMatch(b -> b.getId() == bill.getId()));
    }

    @Test
    public void testCalculateTotal() {
        // Create a bill
        Bill bill = billingService.createBill(testReservation);

        // Add some items
        billingService.addItemToBill(bill.getId(), "Service 1", new BigDecimal("50.00"));
        billingService.addItemToBill(bill.getId(), "Service 2", new BigDecimal("25.00"));

        // Calculate the total
        BigDecimal total = billingService.calculateTotal(bill.getId());

        // Verify the total
        BigDecimal expected = testReservation.getTotalPrice()
                .add(new BigDecimal("50.00"))
                .add(new BigDecimal("25.00"));
        assertEquals(expected, total);
    }

    @Test
    public void testCalculateTotalForNonExistentBill() {
        // Try to calculate the total for a bill that doesn't exist
        BigDecimal total = billingService.calculateTotal(9999);

        // Should return zero
        assertEquals(BigDecimal.ZERO, total);
    }
}
