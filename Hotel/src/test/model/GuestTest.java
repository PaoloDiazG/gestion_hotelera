package test.model;

import model.Guest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the Guest class.
 */
public class GuestTest {
    private Guest guest;
    private final String firstName = "John";
    private final String lastName = "Doe";
    private final String idNumber = "123456789";
    private final String phone = "555-1234";
    private final String email = "john.doe@example.com";
    private final String address = "123 Main St";

    @Before
    public void setUp() {
        // Create a new guest before each test
        guest = new Guest(firstName, lastName, idNumber, phone, email, address);
    }

    @Test
    public void testConstructor() {
        // Test the first constructor (without ID)
        assertEquals(0, guest.getId()); // ID should be 0 until assigned
        assertEquals(firstName, guest.getFirstName());
        assertEquals(lastName, guest.getLastName());
        assertEquals(idNumber, guest.getIdNumber());
        assertEquals(phone, guest.getPhone());
        assertEquals(email, guest.getEmail());
        assertEquals(address, guest.getAddress());

        // Test the second constructor (with ID)
        int id = 1;
        Guest guest2 = new Guest(id, firstName, lastName, idNumber, phone, email, address);
        assertEquals(id, guest2.getId());
        assertEquals(firstName, guest2.getFirstName());
        assertEquals(lastName, guest2.getLastName());
        assertEquals(idNumber, guest2.getIdNumber());
        assertEquals(phone, guest2.getPhone());
        assertEquals(email, guest2.getEmail());
        assertEquals(address, guest2.getAddress());
    }

    @Test
    public void testGettersAndSetters() {
        // Test setters
        int id = 1;
        guest.setId(id);
        guest.setFirstName("Jane");
        guest.setLastName("Smith");
        guest.setIdNumber("987654321");
        guest.setPhone("555-5678");
        guest.setEmail("jane.smith@example.com");
        guest.setAddress("456 Oak Ave");

        // Test getters
        assertEquals(id, guest.getId());
        assertEquals("Jane", guest.getFirstName());
        assertEquals("Smith", guest.getLastName());
        assertEquals("987654321", guest.getIdNumber());
        assertEquals("555-5678", guest.getPhone());
        assertEquals("jane.smith@example.com", guest.getEmail());
        assertEquals("456 Oak Ave", guest.getAddress());
    }

    @Test
    public void testGetFullName() {
        // The getFullName method should return the first name and last name concatenated
        assertEquals(firstName + " " + lastName, guest.getFullName());
        
        // Test with different names
        guest.setFirstName("Jane");
        guest.setLastName("Smith");
        assertEquals("Jane Smith", guest.getFullName());
    }

    @Test
    public void testToString() {
        // The toString method should return a string containing the full name and ID number
        String expected = guest.getFullName() + " (ID: " + idNumber + ")";
        assertEquals(expected, guest.toString());
    }
}