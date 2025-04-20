package service;

import model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing reservations.
 */
public class ReservationService {
    private static ReservationService instance;
    private final Map<Integer, Reservation> reservations;
    private int nextId;
    
    private final RoomService roomService;
    
    private ReservationService() {
        reservations = new HashMap<>();
        nextId = 1;
        roomService = RoomService.getInstance();
    }
    
    public static synchronized ReservationService getInstance() {
        if (instance == null) {
            instance = new ReservationService();
        }
        return instance;
    }
    
    // Create a reservation
    public Reservation createReservation(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        // Check if the room is available for the given dates
        if (!isRoomAvailable(room.getRoomNumber(), checkInDate, checkOutDate)) {
            return null;
        }
        
        Reservation reservation = new Reservation(guest, room, checkInDate, checkOutDate);
        reservation.setId(nextId++);
        reservations.put(reservation.getId(), reservation);
        return reservation;
    }
    
    // Update a reservation
    public Reservation updateReservation(Reservation reservation) {
        if (reservations.containsKey(reservation.getId())) {
            reservations.put(reservation.getId(), reservation);
            return reservation;
        }
        return null;
    }
    
    // Cancel a reservation
    public boolean cancelReservation(int reservationId) {
        Reservation reservation = getReservationById(reservationId);
        if (reservation != null) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            return true;
        }
        return false;
    }
    
    // Check-in a reservation
    public boolean checkIn(int reservationId) {
        Reservation reservation = getReservationById(reservationId);
        if (reservation != null && reservation.getStatus() == ReservationStatus.CONFIRMED) {
            reservation.setStatus(ReservationStatus.CHECKED_IN);
            roomService.changeRoomStatus(reservation.getRoom().getRoomNumber(), RoomStatus.OCCUPIED);
            return true;
        }
        return false;
    }
    
    // Check-out a reservation
    public boolean checkOut(int reservationId) {
        Reservation reservation = getReservationById(reservationId);
        if (reservation != null && reservation.getStatus() == ReservationStatus.CHECKED_IN) {
            reservation.setStatus(ReservationStatus.CHECKED_OUT);
            roomService.changeRoomStatus(reservation.getRoom().getRoomNumber(), RoomStatus.CLEANING);
            return true;
        }
        return false;
    }
    
    // Get a reservation by ID
    public Reservation getReservationById(int reservationId) {
        return reservations.get(reservationId);
    }
    
    // Get all reservations
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations.values());
    }
    
    // Get reservations by guest
    public List<Reservation> getReservationsByGuest(Guest guest) {
        return reservations.values().stream()
                .filter(reservation -> reservation.getGuest().getId() == guest.getId())
                .collect(Collectors.toList());
    }
    
    // Get reservations by room
    public List<Reservation> getReservationsByRoom(Room room) {
        return reservations.values().stream()
                .filter(reservation -> reservation.getRoom().getRoomNumber() == room.getRoomNumber())
                .collect(Collectors.toList());
    }
    
    // Get reservations by status
    public List<Reservation> getReservationsByStatus(ReservationStatus status) {
        return reservations.values().stream()
                .filter(reservation -> reservation.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    // Get reservations for a date range
    public List<Reservation> getReservationsForDateRange(LocalDate startDate, LocalDate endDate) {
        return reservations.values().stream()
                .filter(reservation -> reservation.overlaps(startDate, endDate))
                .collect(Collectors.toList());
    }
    
    // Check if a room is available for the given dates
    public boolean isRoomAvailable(int roomNumber, LocalDate checkInDate, LocalDate checkOutDate) {
        Room room = roomService.getRoomByNumber(roomNumber);
        if (room == null || room.getStatus() != RoomStatus.AVAILABLE) {
            return false;
        }
        
        // Check if there are any overlapping reservations
        List<Reservation> overlappingReservations = getReservationsForDateRange(checkInDate, checkOutDate);
        return overlappingReservations.stream()
                .noneMatch(reservation -> 
                    reservation.getRoom().getRoomNumber() == roomNumber && 
                    reservation.getStatus() != ReservationStatus.CANCELLED);
    }
    
    // Get available rooms for the given dates
    public List<Room> getAvailableRoomsForDates(LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> availableRooms = roomService.getAvailableRooms();
        List<Reservation> overlappingReservations = getReservationsForDateRange(checkInDate, checkOutDate);
        
        // Remove rooms that have overlapping reservations
        for (Reservation reservation : overlappingReservations) {
            if (reservation.getStatus() != ReservationStatus.CANCELLED) {
                availableRooms.removeIf(room -> room.getRoomNumber() == reservation.getRoom().getRoomNumber());
            }
        }
        
        return availableRooms;
    }
    
    // Get available rooms by type for the given dates
    public List<Room> getAvailableRoomsByTypeForDates(RoomType type, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> availableRooms = getAvailableRoomsForDates(checkInDate, checkOutDate);
        return availableRooms.stream()
                .filter(room -> room.getType() == type)
                .collect(Collectors.toList());
    }
}