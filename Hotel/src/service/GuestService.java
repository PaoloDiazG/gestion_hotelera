package service;

import model.Guest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing guests.
 */
public class GuestService {
    private static GuestService instance;
    private final Map<Integer, Guest> guests;
    private int nextId;
    
    private GuestService() {
        guests = new HashMap<>();
        nextId = 1;
        
        // Add some sample guests
        addGuest(new Guest("John", "Doe", "123456789", "555-1234", "john.doe@example.com", "123 Main St"));
        addGuest(new Guest("Jane", "Smith", "987654321", "555-5678", "jane.smith@example.com", "456 Oak Ave"));
    }
    
    public static synchronized GuestService getInstance() {
        if (instance == null) {
            instance = new GuestService();
        }
        return instance;
    }
    
    // Add a guest
    public Guest addGuest(Guest guest) {
        guest.setId(nextId++);
        guests.put(guest.getId(), guest);
        return guest;
    }
    
    // Update a guest
    public Guest updateGuest(Guest guest) {
        if (guests.containsKey(guest.getId())) {
            guests.put(guest.getId(), guest);
            return guest;
        }
        return null;
    }
    
    // Delete a guest
    public boolean deleteGuest(int guestId) {
        return guests.remove(guestId) != null;
    }
    
    // Get a guest by ID
    public Guest getGuestById(int guestId) {
        return guests.get(guestId);
    }
    
    // Get all guests
    public List<Guest> getAllGuests() {
        return new ArrayList<>(guests.values());
    }
    
    // Search guests by name
    public List<Guest> searchGuestsByName(String name) {
        String searchTerm = name.toLowerCase();
        return guests.values().stream()
                .filter(guest -> 
                    guest.getFirstName().toLowerCase().contains(searchTerm) || 
                    guest.getLastName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }
    
    // Search guests by ID number
    public Guest searchGuestByIdNumber(String idNumber) {
        return guests.values().stream()
                .filter(guest -> guest.getIdNumber().equals(idNumber))
                .findFirst()
                .orElse(null);
    }
}