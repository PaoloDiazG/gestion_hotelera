package test.model;

import model.Bill;
import model.Guest;
import model.Reservation;
import model.ReservationStatus;
import model.Room;
import model.RoomType;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * Unit tests for the Bill class.
 */
public class BillTest {
    private Bill bill;
    private Reservation reservation;
    private Guest guest;
    private Room room;
    private final LocalDate checkInDate = LocalDate.of(2023, 7, 1);
    private final LocalDate checkOutDate = LocalDate.of(2023, 7, 5);
    private final LocalDateTime issueDate = LocalDateTime.of(2023, 7, 5, 12, 0);

    @Before
    public void setUp() {
        // Create a guest, room, and reservation for the bill
        guest = new Guest(1, "John", "Doe", "123456789", "555-1234", "john.doe@example.com", "123 Main St");
        room = new Room(101, 1, RoomType.DOUBLE, new BigDecimal("150.00"));
        reservation = new Reservation(1, guest, room, checkInDate, checkOutDate, 
                                     ReservationStatus.CHECKED_OUT, new BigDecimal("600.00"), "");
        
        // Create a new bill before each test
        bill = new Bill(reservation);
    }

    @Test
    public void testConstructor() {
        // Test the first constructor (without ID)
        assertEquals(0, bill.getId()); // ID should be 0 until assigned
        assertEquals(reservation, bill.getReservation());
        assertNotNull(bill.getIssueDate()); // Issue date should be set to current time
        assertFalse(bill.isPaid()); // Default paid status should be false
        assertEquals(1, bill.getItems().size()); // Should have one item for the room charge
        assertEquals(reservation.getTotalPrice(), bill.calculateTotal()); // Total should match reservation price

        // Test the second constructor (with ID)
        int id = 1;
        Bill bill2 = new Bill(id, reservation, issueDate, true);
        assertEquals(id, bill2.getId());
        assertEquals(reservation, bill2.getReservation());
        assertEquals(issueDate, bill2.getIssueDate());
        assertTrue(bill2.isPaid());
        assertEquals(1, bill2.getItems().size()); // Should have one item for the room charge
        assertEquals(reservation.getTotalPrice(), bill2.calculateTotal()); // Total should match reservation price
    }

    @Test
    public void testGettersAndSetters() {
        // Test setters
        int id = 1;
        Guest newGuest = new Guest(2, "Jane", "Smith", "987654321", "555-5678", "jane.smith@example.com", "456 Oak Ave");
        Room newRoom = new Room(102, 1, RoomType.SUITE, new BigDecimal("250.00"));
        Reservation newReservation = new Reservation(2, newGuest, newRoom, checkInDate, checkOutDate, 
                                                   ReservationStatus.CHECKED_OUT, new BigDecimal("1000.00"), "");
        
        bill.setId(id);
        bill.setReservation(newReservation);
        bill.setIssueDate(issueDate);
        bill.setPaid(true);

        // Test getters
        assertEquals(id, bill.getId());
        assertEquals(newReservation, bill.getReservation());
        assertEquals(issueDate, bill.getIssueDate());
        assertTrue(bill.isPaid());
    }

    @Test
    public void testAddAndRemoveItem() {
        // Initial bill should have one item (room charge)
        assertEquals(1, bill.getItems().size());
        assertEquals(reservation.getTotalPrice(), bill.calculateTotal());
        
        // Add an item
        Bill.BillItem item = new Bill.BillItem("Extra Service", new BigDecimal("50.00"));
        bill.addItem(item);
        assertEquals(2, bill.getItems().size());
        assertEquals(reservation.getTotalPrice().add(new BigDecimal("50.00")), bill.calculateTotal());
        
        // Add another item
        Bill.BillItem item2 = new Bill.BillItem("Another Service", new BigDecimal("25.00"));
        bill.addItem(item2);
        assertEquals(3, bill.getItems().size());
        assertEquals(reservation.getTotalPrice().add(new BigDecimal("75.00")), bill.calculateTotal());
        
        // Remove an item
        bill.removeItem(item);
        assertEquals(2, bill.getItems().size());
        assertEquals(reservation.getTotalPrice().add(new BigDecimal("25.00")), bill.calculateTotal());
    }

    @Test
    public void testCalculateTotal() {
        // Initial bill should have one item (room charge)
        assertEquals(reservation.getTotalPrice(), bill.calculateTotal());
        
        // Add items and verify total
        bill.addItem(new Bill.BillItem("Service 1", new BigDecimal("50.00")));
        bill.addItem(new Bill.BillItem("Service 2", new BigDecimal("25.00")));
        bill.addItem(new Bill.BillItem("Service 3", new BigDecimal("15.00")));
        
        BigDecimal expected = reservation.getTotalPrice()
                .add(new BigDecimal("50.00"))
                .add(new BigDecimal("25.00"))
                .add(new BigDecimal("15.00"));
        assertEquals(expected, bill.calculateTotal());
    }

    @Test
    public void testToString() {
        // The toString method should return a string containing the bill details
        bill.setId(1);
        String expected = "Bill #1 - " + guest.getFullName() + " - Total: " + bill.calculateTotal();
        assertEquals(expected, bill.toString());
    }

    @Test
    public void testBillItem() {
        // Test BillItem constructor and getters
        String description = "Test Item";
        BigDecimal amount = new BigDecimal("75.00");
        Bill.BillItem item = new Bill.BillItem(description, amount);
        
        assertEquals(description, item.getDescription());
        assertEquals(amount, item.getAmount());
        
        // Test setters
        String newDescription = "Updated Item";
        BigDecimal newAmount = new BigDecimal("100.00");
        item.setDescription(newDescription);
        item.setAmount(newAmount);
        
        assertEquals(newDescription, item.getDescription());
        assertEquals(newAmount, item.getAmount());
        
        // Test toString
        String expected = newDescription + ": " + newAmount;
        assertEquals(expected, item.toString());
    }
}