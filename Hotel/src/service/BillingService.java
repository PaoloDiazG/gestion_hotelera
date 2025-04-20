package service;

import model.Bill;
import model.Reservation;
import model.ReservationStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing bills.
 */
public class BillingService {
    private static BillingService instance;
    private final Map<Integer, Bill> bills;
    private int nextId;
    
    private BillingService() {
        bills = new HashMap<>();
        nextId = 1;
    }
    
    public static synchronized BillingService getInstance() {
        if (instance == null) {
            instance = new BillingService();
        }
        return instance;
    }
    
    // Create a bill for a reservation
    public Bill createBill(Reservation reservation) {
        // Check if the reservation is checked out
        if (reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
            return null;
        }
        
        // Check if a bill already exists for this reservation
        Bill existingBill = getBillByReservation(reservation);
        if (existingBill != null) {
            return existingBill;
        }
        
        Bill bill = new Bill(reservation);
        bill.setId(nextId++);
        bills.put(bill.getId(), bill);
        return bill;
    }
    
    // Add an item to a bill
    public boolean addItemToBill(int billId, String description, BigDecimal amount) {
        Bill bill = getBillById(billId);
        if (bill != null) {
            bill.addItem(new Bill.BillItem(description, amount));
            return true;
        }
        return false;
    }
    
    // Remove an item from a bill
    public boolean removeItemFromBill(int billId, Bill.BillItem item) {
        Bill bill = getBillById(billId);
        if (bill != null) {
            bill.removeItem(item);
            return true;
        }
        return false;
    }
    
    // Mark a bill as paid
    public boolean markBillAsPaid(int billId) {
        Bill bill = getBillById(billId);
        if (bill != null) {
            bill.setPaid(true);
            return true;
        }
        return false;
    }
    
    // Get a bill by ID
    public Bill getBillById(int billId) {
        return bills.get(billId);
    }
    
    // Get a bill by reservation
    public Bill getBillByReservation(Reservation reservation) {
        return bills.values().stream()
                .filter(bill -> bill.getReservation().getId() == reservation.getId())
                .findFirst()
                .orElse(null);
    }
    
    // Get all bills
    public List<Bill> getAllBills() {
        return new ArrayList<>(bills.values());
    }
    
    // Get bills by paid status
    public List<Bill> getBillsByPaidStatus(boolean paid) {
        return bills.values().stream()
                .filter(bill -> bill.isPaid() == paid)
                .collect(Collectors.toList());
    }
    
    // Calculate total for a bill
    public BigDecimal calculateTotal(int billId) {
        Bill bill = getBillById(billId);
        if (bill != null) {
            return bill.calculateTotal();
        }
        return BigDecimal.ZERO;
    }
}