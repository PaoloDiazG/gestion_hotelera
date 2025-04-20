package util;

import java.awt.*;
import model.RoomStatus;
import model.ReservationStatus;

/**
 * Utility class for defining the color scheme of the application.
 */
public class ColorScheme {
    // Primary colors
    public static final Color PRIMARY = new Color(41, 128, 185); // Blue
    public static final Color PRIMARY_DARK = new Color(44, 62, 80); // Dark Blue
    public static final Color PRIMARY_LIGHT = new Color(52, 152, 219); // Light Blue

    // Secondary colors
    public static final Color SECONDARY = new Color(46, 204, 113); // Green
    public static final Color SECONDARY_DARK = new Color(39, 174, 96); // Dark Green

    // Accent colors
    public static final Color ACCENT = new Color(230, 126, 34); // Orange
    public static final Color ACCENT_DARK = new Color(211, 84, 0); // Dark Orange

    // Status colors
    public static final Color SUCCESS = new Color(46, 204, 113); // Green
    public static final Color WARNING = new Color(241, 196, 15); // Yellow
    public static final Color ERROR = new Color(231, 76, 60); // Red
    public static final Color INFO = new Color(52, 152, 219); // Blue

    // Background colors
    public static final Color BACKGROUND = new Color(236, 240, 241); // Light Gray
    public static final Color BACKGROUND_DARK = new Color(189, 195, 199); // Dark Gray

    // Text colors
    public static final Color TEXT_PRIMARY = new Color(44, 62, 80); // Dark Blue
    public static final Color TEXT_SECONDARY = new Color(127, 140, 141); // Gray
    public static final Color TEXT_LIGHT = new Color(236, 240, 241); // Light Gray

    // Room status colors
    public static final Color ROOM_AVAILABLE = SUCCESS;
    public static final Color ROOM_OCCUPIED = ERROR;
    public static final Color ROOM_MAINTENANCE = WARNING;
    public static final Color ROOM_CLEANING = INFO;

    // Reservation status colors
    public static final Color RESERVATION_CONFIRMED = INFO;
    public static final Color RESERVATION_CHECKED_IN = SUCCESS;
    public static final Color RESERVATION_CHECKED_OUT = BACKGROUND_DARK;
    public static final Color RESERVATION_CANCELLED = ERROR;

    // Get color for room status
    public static Color getRoomStatusColor(String status) {
        if (status.equals(RoomStatus.AVAILABLE.getDisplayName())) {
            return ROOM_AVAILABLE;
        } else if (status.equals(RoomStatus.OCCUPIED.getDisplayName())) {
            return ROOM_OCCUPIED;
        } else if (status.equals(RoomStatus.MAINTENANCE.getDisplayName())) {
            return ROOM_MAINTENANCE;
        } else if (status.equals(RoomStatus.CLEANING.getDisplayName())) {
            return ROOM_CLEANING;
        } else {
            return BACKGROUND;
        }
    }

    // Get color for reservation status
    public static Color getReservationStatusColor(String status) {
        if (status.equals(ReservationStatus.CONFIRMED.getDisplayName())) {
            return RESERVATION_CONFIRMED;
        } else if (status.equals(ReservationStatus.CHECKED_IN.getDisplayName())) {
            return RESERVATION_CHECKED_IN;
        } else if (status.equals(ReservationStatus.CHECKED_OUT.getDisplayName())) {
            return RESERVATION_CHECKED_OUT;
        } else if (status.equals(ReservationStatus.CANCELLED.getDisplayName())) {
            return RESERVATION_CANCELLED;
        } else {
            return BACKGROUND;
        }
    }
}
