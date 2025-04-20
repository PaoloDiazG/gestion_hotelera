package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bill/invoice for a guest's stay.
 */
public class Bill {
    private int id;
    private Reservation reservation;
    private LocalDateTime issueDate;
    private boolean paid;
    private List<BillItem> items;
    
    // For new bills (ID will be assigned later)
    public Bill(Reservation reservation) {
        this.reservation = reservation;
        this.issueDate = LocalDateTime.now();
        this.paid = false;
        this.items = new ArrayList<>();
        
        // Add the room charge as the first item
        addItem(new BillItem("Room Charge", reservation.getTotalPrice()));
    }
    
    // For existing bills with ID
    public Bill(int id, Reservation reservation, LocalDateTime issueDate, boolean paid) {
        this.id = id;
        this.reservation = reservation;
        this.issueDate = issueDate;
        this.paid = paid;
        this.items = new ArrayList<>();
        
        // Add the room charge as the first item
        addItem(new BillItem("Room Charge", reservation.getTotalPrice()));
    }
    
    // Add an item to the bill
    public void addItem(BillItem item) {
        items.add(item);
    }
    
    // Remove an item from the bill
    public void removeItem(BillItem item) {
        items.remove(item);
    }
    
    // Calculate the total amount
    public BigDecimal calculateTotal() {
        return items.stream()
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Reservation getReservation() {
        return reservation;
    }
    
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }
    
    public LocalDateTime getIssueDate() {
        return issueDate;
    }
    
    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }
    
    public boolean isPaid() {
        return paid;
    }
    
    public void setPaid(boolean paid) {
        this.paid = paid;
    }
    
    public List<BillItem> getItems() {
        return items;
    }
    
    public void setItems(List<BillItem> items) {
        this.items = items;
    }
    
    @Override
    public String toString() {
        return "Bill #" + id + " - " + reservation.getGuest().getFullName() + 
               " - Total: " + calculateTotal();
    }
    
    /**
     * Inner class representing an item in the bill.
     */
    public static class BillItem {
        private String description;
        private BigDecimal amount;
        
        public BillItem(String description, BigDecimal amount) {
            this.description = description;
            this.amount = amount;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
        
        @Override
        public String toString() {
            return description + ": " + amount;
        }
    }
}