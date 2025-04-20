package test.performance;

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
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Performance tests for the Hotel Management System.
 * These tests measure the time it takes to perform various operations.
 */
public class PerformanceTest {
    private GuestService guestService;
    private RoomService roomService;
    private ReservationService reservationService;
    
    private List<Guest> testGuests = new ArrayList<>();
    private List<Room> testRooms = new ArrayList<>();
    private List<Reservation> testReservations = new ArrayList<>();
    
    private final int NUM_GUESTS = 100;
    private final int NUM_ROOMS = 50;
    private final int NUM_RESERVATIONS = 200;
    
    private final Random random = new Random();

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
    public void testGuestCreationPerformance() {
        System.out.println("Testing guest creation performance...");
        
        long startTime = System.currentTimeMillis();
        
        // Create a large number of guests
        for (int i = 0; i < NUM_GUESTS; i++) {
            Guest guest = new Guest(
                "FirstName" + i,
                "LastName" + i,
                "ID" + i,
                "555-" + i,
                "guest" + i + "@example.com",
                "Address " + i
            );
            guest = guestService.addGuest(guest);
            testGuests.add(guest);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Created " + NUM_GUESTS + " guests in " + duration + " ms");
        System.out.println("Average time per guest: " + (duration / (double) NUM_GUESTS) + " ms");
        
        // Verify all guests were created
        assertEquals(NUM_GUESTS, testGuests.size());
        for (Guest guest : testGuests) {
            assertNotNull(guest);
            assertTrue(guest.getId() > 0);
        }
    }

    @Test
    public void testRoomCreationPerformance() {
        System.out.println("Testing room creation performance...");
        
        long startTime = System.currentTimeMillis();
        
        // Create a large number of rooms
        for (int i = 0; i < NUM_ROOMS; i++) {
            int roomNumber = 1000 + i;
            int floor = (roomNumber / 100);
            RoomType type = RoomType.values()[random.nextInt(RoomType.values().length)];
            BigDecimal price = new BigDecimal(100 + random.nextInt(200));
            
            Room room = new Room(roomNumber, floor, type, price);
            room = roomService.addRoom(room);
            testRooms.add(room);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Created " + NUM_ROOMS + " rooms in " + duration + " ms");
        System.out.println("Average time per room: " + (duration / (double) NUM_ROOMS) + " ms");
        
        // Verify all rooms were created
        assertEquals(NUM_ROOMS, testRooms.size());
        for (Room room : testRooms) {
            assertNotNull(room);
            assertTrue(room.getRoomNumber() > 0);
        }
    }

    @Test
    public void testReservationCreationPerformance() {
        System.out.println("Testing reservation creation performance...");
        
        // First create guests and rooms
        for (int i = 0; i < 20; i++) {
            Guest guest = new Guest(
                "FirstName" + i,
                "LastName" + i,
                "ID" + i,
                "555-" + i,
                "guest" + i + "@example.com",
                "Address " + i
            );
            guest = guestService.addGuest(guest);
            testGuests.add(guest);
        }
        
        for (int i = 0; i < 10; i++) {
            int roomNumber = 2000 + i;
            int floor = (roomNumber / 100);
            RoomType type = RoomType.values()[random.nextInt(RoomType.values().length)];
            BigDecimal price = new BigDecimal(100 + random.nextInt(200));
            
            Room room = new Room(roomNumber, floor, type, price);
            room = roomService.addRoom(room);
            testRooms.add(room);
        }
        
        long startTime = System.currentTimeMillis();
        
        // Create a large number of reservations
        LocalDate baseDate = LocalDate.now();
        int successCount = 0;
        
        for (int i = 0; i < NUM_RESERVATIONS; i++) {
            Guest guest = testGuests.get(random.nextInt(testGuests.size()));
            Room room = testRooms.get(random.nextInt(testRooms.size()));
            
            LocalDate checkInDate = baseDate.plusDays(random.nextInt(365));
            LocalDate checkOutDate = checkInDate.plusDays(1 + random.nextInt(7));
            
            Reservation reservation = reservationService.createReservation(guest, room, checkInDate, checkOutDate);
            if (reservation != null) {
                testReservations.add(reservation);
                successCount++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Attempted to create " + NUM_RESERVATIONS + " reservations");
        System.out.println("Successfully created " + successCount + " reservations in " + duration + " ms");
        System.out.println("Average time per successful reservation: " + (duration / (double) successCount) + " ms");
        
        // Verify some reservations were created (not all will succeed due to availability conflicts)
        assertTrue("At least some reservations should be created", successCount > 0);
        assertEquals(successCount, testReservations.size());
    }

    @Test
    public void testSearchPerformance() {
        System.out.println("Testing search performance...");
        
        // First create guests with a pattern
        for (int i = 0; i < 50; i++) {
            Guest guest = new Guest(
                "John",
                "Doe" + i,
                "ID" + i,
                "555-" + i,
                "john.doe" + i + "@example.com",
                "Address " + i
            );
            guest = guestService.addGuest(guest);
            testGuests.add(guest);
        }
        
        for (int i = 0; i < 50; i++) {
            Guest guest = new Guest(
                "Jane",
                "Smith" + i,
                "ID" + (i + 50),
                "555-" + (i + 50),
                "jane.smith" + i + "@example.com",
                "Address " + (i + 50)
            );
            guest = guestService.addGuest(guest);
            testGuests.add(guest);
        }
        
        // Test search by name
        long startTime = System.currentTimeMillis();
        
        List<Guest> johnsResult = guestService.searchGuestsByName("John");
        
        long johnSearchTime = System.currentTimeMillis() - startTime;
        System.out.println("Found " + johnsResult.size() + " guests named 'John' in " + johnSearchTime + " ms");
        
        startTime = System.currentTimeMillis();
        
        List<Guest> janesResult = guestService.searchGuestsByName("Jane");
        
        long janeSearchTime = System.currentTimeMillis() - startTime;
        System.out.println("Found " + janesResult.size() + " guests named 'Jane' in " + janeSearchTime + " ms");
        
        // Test search by ID
        startTime = System.currentTimeMillis();
        
        Guest idResult = guestService.searchGuestByIdNumber("ID25");
        
        long idSearchTime = System.currentTimeMillis() - startTime;
        System.out.println("Found guest with ID 'ID25' in " + idSearchTime + " ms");
        
        // Verify search results
        assertEquals(50, johnsResult.size());
        assertEquals(50, janesResult.size());
        assertNotNull(idResult);
        assertEquals("ID25", idResult.getIdNumber());
    }

    @Test
    public void testAvailabilityCheckPerformance() {
        System.out.println("Testing availability check performance...");
        
        // Create rooms and some reservations
        for (int i = 0; i < 30; i++) {
            int roomNumber = 3000 + i;
            int floor = (roomNumber / 100);
            RoomType type = RoomType.values()[random.nextInt(RoomType.values().length)];
            BigDecimal price = new BigDecimal(100 + random.nextInt(200));
            
            Room room = new Room(roomNumber, floor, type, price);
            room = roomService.addRoom(room);
            testRooms.add(room);
        }
        
        Guest guest = new Guest("Perf", "Test", "PERF-TEST", "555-PERF", "perf@example.com", "Perf Address");
        guest = guestService.addGuest(guest);
        testGuests.add(guest);
        
        // Create some reservations to make the availability check more realistic
        LocalDate baseDate = LocalDate.now();
        for (int i = 0; i < 50; i++) {
            Room room = testRooms.get(random.nextInt(testRooms.size()));
            LocalDate checkInDate = baseDate.plusDays(random.nextInt(30));
            LocalDate checkOutDate = checkInDate.plusDays(1 + random.nextInt(5));
            
            Reservation reservation = reservationService.createReservation(guest, room, checkInDate, checkOutDate);
            if (reservation != null) {
                testReservations.add(reservation);
            }
        }
        
        // Test availability check performance
        long startTime = System.currentTimeMillis();
        
        LocalDate testCheckIn = baseDate.plusDays(15);
        LocalDate testCheckOut = testCheckIn.plusDays(3);
        List<Room> availableRooms = reservationService.getAvailableRoomsForDates(testCheckIn, testCheckOut);
        
        long availabilityCheckTime = System.currentTimeMillis() - startTime;
        System.out.println("Found " + availableRooms.size() + " available rooms in " + availabilityCheckTime + " ms");
        
        // Test availability check by type
        startTime = System.currentTimeMillis();
        
        List<Room> availableSuites = reservationService.getAvailableRoomsByTypeForDates(
            RoomType.SUITE, testCheckIn, testCheckOut);
        
        long typeAvailabilityCheckTime = System.currentTimeMillis() - startTime;
        System.out.println("Found " + availableSuites.size() + " available suites in " + typeAvailabilityCheckTime + " ms");
    }
}