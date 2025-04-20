package test.service;

import model.Guest;
import org.junit.Before;
import org.junit.Test;
import service.GuestService;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the GuestService class.
 */
public class GuestServiceTest {
    private GuestService guestService;
    private Guest testGuest;
    private final String firstName = "Test";
    private final String lastName = "User";
    private final String idNumber = "TEST123456";
    private final String phone = "555-TEST";
    private final String email = "test.user@example.com";
    private final String address = "123 Test St";

    @Before
    public void setUp() {
        // Get the singleton instance of GuestService
        guestService = GuestService.getInstance();
        
        // Create a test guest
        testGuest = new Guest(firstName, lastName, idNumber, phone, email, address);
    }

    @Test
    public void testSingletonPattern() {
        // Test that getInstance always returns the same instance
        GuestService instance1 = GuestService.getInstance();
        GuestService instance2 = GuestService.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    public void testAddGuest() {
        // Add the test guest
        Guest addedGuest = guestService.addGuest(testGuest);
        
        // Verify the guest was added and assigned an ID
        assertNotNull(addedGuest);
        assertTrue(addedGuest.getId() > 0);
        assertEquals(firstName, addedGuest.getFirstName());
        assertEquals(lastName, addedGuest.getLastName());
        assertEquals(idNumber, addedGuest.getIdNumber());
        
        // Verify we can retrieve the guest
        Guest retrievedGuest = guestService.getGuestById(addedGuest.getId());
        assertNotNull(retrievedGuest);
        assertEquals(addedGuest.getId(), retrievedGuest.getId());
        assertEquals(firstName, retrievedGuest.getFirstName());
        assertEquals(lastName, retrievedGuest.getLastName());
        assertEquals(idNumber, retrievedGuest.getIdNumber());
        assertEquals(phone, retrievedGuest.getPhone());
        assertEquals(email, retrievedGuest.getEmail());
        assertEquals(address, retrievedGuest.getAddress());
        
        // Clean up
        guestService.deleteGuest(addedGuest.getId());
    }

    @Test
    public void testUpdateGuest() {
        // Add the test guest
        Guest addedGuest = guestService.addGuest(testGuest);
        
        // Modify the guest
        addedGuest.setFirstName("Updated");
        addedGuest.setLastName("Person");
        addedGuest.setIdNumber("UPDATED123");
        addedGuest.setPhone("555-UPDATE");
        addedGuest.setEmail("updated.person@example.com");
        addedGuest.setAddress("456 Update Ave");
        
        // Update the guest
        Guest updatedGuest = guestService.updateGuest(addedGuest);
        
        // Verify the guest was updated
        assertNotNull(updatedGuest);
        assertEquals(addedGuest.getId(), updatedGuest.getId());
        assertEquals("Updated", updatedGuest.getFirstName());
        assertEquals("Person", updatedGuest.getLastName());
        assertEquals("UPDATED123", updatedGuest.getIdNumber());
        assertEquals("555-UPDATE", updatedGuest.getPhone());
        assertEquals("updated.person@example.com", updatedGuest.getEmail());
        assertEquals("456 Update Ave", updatedGuest.getAddress());
        
        // Verify we can retrieve the updated guest
        Guest retrievedGuest = guestService.getGuestById(addedGuest.getId());
        assertNotNull(retrievedGuest);
        assertEquals("Updated", retrievedGuest.getFirstName());
        assertEquals("Person", retrievedGuest.getLastName());
        assertEquals("UPDATED123", retrievedGuest.getIdNumber());
        assertEquals("555-UPDATE", retrievedGuest.getPhone());
        assertEquals("updated.person@example.com", retrievedGuest.getEmail());
        assertEquals("456 Update Ave", retrievedGuest.getAddress());
        
        // Clean up
        guestService.deleteGuest(addedGuest.getId());
    }

    @Test
    public void testUpdateNonExistentGuest() {
        // Create a guest with a non-existent ID
        Guest nonExistentGuest = new Guest(9999, "Non", "Existent", "NONEXIST123", "555-NONE", "non.existent@example.com", "999 Nowhere St");
        
        // Try to update the guest
        Guest result = guestService.updateGuest(nonExistentGuest);
        
        // Should return null
        assertNull(result);
    }

    @Test
    public void testDeleteGuest() {
        // Add the test guest
        Guest addedGuest = guestService.addGuest(testGuest);
        
        // Verify the guest exists
        assertNotNull(guestService.getGuestById(addedGuest.getId()));
        
        // Delete the guest
        boolean deleted = guestService.deleteGuest(addedGuest.getId());
        
        // Verify the guest was deleted
        assertTrue(deleted);
        assertNull(guestService.getGuestById(addedGuest.getId()));
    }

    @Test
    public void testDeleteNonExistentGuest() {
        // Try to delete a guest that doesn't exist
        boolean deleted = guestService.deleteGuest(9999);
        
        // Should return false
        assertFalse(deleted);
    }

    @Test
    public void testGetAllGuests() {
        // Get all guests
        List<Guest> allGuests = guestService.getAllGuests();
        
        // Should not be null and should have at least the sample guests
        assertNotNull(allGuests);
        assertTrue(allGuests.size() >= 2); // There are 2 sample guests added in the constructor
    }

    @Test
    public void testSearchGuestsByName() {
        // Add the test guest
        Guest addedGuest = guestService.addGuest(testGuest);
        
        // Search by first name
        List<Guest> resultsByFirstName = guestService.searchGuestsByName(firstName);
        assertNotNull(resultsByFirstName);
        assertTrue(resultsByFirstName.stream().anyMatch(guest -> guest.getId() == addedGuest.getId()));
        
        // Search by last name
        List<Guest> resultsByLastName = guestService.searchGuestsByName(lastName);
        assertNotNull(resultsByLastName);
        assertTrue(resultsByLastName.stream().anyMatch(guest -> guest.getId() == addedGuest.getId()));
        
        // Search by partial name
        List<Guest> resultsByPartialName = guestService.searchGuestsByName("es"); // Should match "Test"
        assertNotNull(resultsByPartialName);
        assertTrue(resultsByPartialName.stream().anyMatch(guest -> guest.getId() == addedGuest.getId()));
        
        // Search by non-existent name
        List<Guest> resultsByNonExistentName = guestService.searchGuestsByName("NonExistentName");
        assertNotNull(resultsByNonExistentName);
        assertTrue(resultsByNonExistentName.isEmpty());
        
        // Clean up
        guestService.deleteGuest(addedGuest.getId());
    }

    @Test
    public void testSearchGuestByIdNumber() {
        // Add the test guest
        Guest addedGuest = guestService.addGuest(testGuest);
        
        // Search by ID number
        Guest resultByIdNumber = guestService.searchGuestByIdNumber(idNumber);
        assertNotNull(resultByIdNumber);
        assertEquals(addedGuest.getId(), resultByIdNumber.getId());
        
        // Search by non-existent ID number
        Guest resultByNonExistentIdNumber = guestService.searchGuestByIdNumber("NONEXISTENT");
        assertNull(resultByNonExistentIdNumber);
        
        // Clean up
        guestService.deleteGuest(addedGuest.getId());
    }
}