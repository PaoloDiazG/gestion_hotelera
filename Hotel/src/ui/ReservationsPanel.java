package ui;

import model.*;
import service.GuestService;
import service.ReservationService;
import service.RoomService;
import util.ColorScheme;
import util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing reservations.
 */
public class ReservationsPanel extends JPanel {
    private final ReservationService reservationService;
    private final GuestService guestService;
    private final RoomService roomService;
    private JTable reservationsTable;
    private DefaultTableModel tableModel;
    private JComboBox<ReservationStatus> statusFilterComboBox;
    private JTextField guestSearchField;
    
    public ReservationsPanel() {
        reservationService = ReservationService.getInstance();
        guestService = GuestService.getInstance();
        roomService = RoomService.getInstance();
        
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        createUI();
        loadReservations();
    }
    
    private void createUI() {
        // Create header panel
        JPanel headerPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        JLabel titleLabel = UIUtils.createTitleLabel("Reservation Management");
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create filter panel
        JPanel filterPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.LEFT), ColorScheme.BACKGROUND);
        
        JLabel statusFilterLabel = UIUtils.createRegularLabel("Filter by Status: ");
        statusFilterComboBox = UIUtils.createComboBox(ReservationStatus.values());
        statusFilterComboBox.insertItemAt(null, 0);
        statusFilterComboBox.setSelectedIndex(0);
        statusFilterComboBox.addActionListener(e -> loadReservations());
        
        JLabel guestSearchLabel = UIUtils.createRegularLabel("Search by Guest: ");
        guestSearchField = UIUtils.createTextField(15);
        JButton searchButton = UIUtils.createPrimaryButton("Search");
        searchButton.addActionListener(e -> loadReservations());
        
        filterPanel.add(statusFilterLabel);
        filterPanel.add(statusFilterComboBox);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(guestSearchLabel);
        filterPanel.add(guestSearchField);
        filterPanel.add(searchButton);
        
        headerPanel.add(filterPanel, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        
        JButton createButton = UIUtils.createPrimaryButton("Create Reservation");
        createButton.addActionListener(e -> showCreateReservationDialog());
        
        JButton editButton = UIUtils.createSecondaryButton("Edit Reservation");
        editButton.addActionListener(e -> {
            int selectedRow = reservationsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int reservationId = (int) tableModel.getValueAt(selectedRow, 0);
                Reservation reservation = reservationService.getReservationById(reservationId);
                if (reservation != null) {
                    showEditReservationDialog(reservation);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a reservation to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        JButton cancelButton = UIUtils.createDangerButton("Cancel Reservation");
        cancelButton.addActionListener(e -> {
            int selectedRow = reservationsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int reservationId = (int) tableModel.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to cancel this reservation?",
                        "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    reservationService.cancelReservation(reservationId);
                    loadReservations();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a reservation to cancel.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(createButton);
        buttonPanel.add(editButton);
        buttonPanel.add(cancelButton);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Create table
        String[] columnNames = {"ID", "Guest", "Room", "Check-in Date", "Check-out Date", "Status", "Total Price"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reservationsTable = UIUtils.createTable();
        reservationsTable.setModel(tableModel);
        
        JScrollPane scrollPane = UIUtils.createScrollPane(reservationsTable);
        
        // Add components to panel
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadReservations() {
        tableModel.setRowCount(0);
        
        List<Reservation> reservations;
        ReservationStatus statusFilter = (ReservationStatus) statusFilterComboBox.getSelectedItem();
        String guestSearch = guestSearchField.getText().trim();
        
        if (statusFilter != null && !guestSearch.isEmpty()) {
            // Filter by status and guest name
            List<Guest> guests = guestService.searchGuestsByName(guestSearch);
            reservations = reservationService.getReservationsByStatus(statusFilter);
            reservations = reservations.stream()
                    .filter(reservation -> guests.stream()
                            .anyMatch(guest -> guest.getId() == reservation.getGuest().getId()))
                    .toList();
        } else if (statusFilter != null) {
            // Filter by status only
            reservations = reservationService.getReservationsByStatus(statusFilter);
        } else if (!guestSearch.isEmpty()) {
            // Filter by guest name only
            List<Guest> guests = guestService.searchGuestsByName(guestSearch);
            reservations = reservationService.getAllReservations();
            reservations = reservations.stream()
                    .filter(reservation -> guests.stream()
                            .anyMatch(guest -> guest.getId() == reservation.getGuest().getId()))
                    .toList();
        } else {
            // No filters
            reservations = reservationService.getAllReservations();
        }
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Reservation reservation : reservations) {
            Object[] rowData = {
                    reservation.getId(),
                    reservation.getGuest().getFullName(),
                    reservation.getRoom().toString(),
                    reservation.getCheckInDate().format(dateFormatter),
                    reservation.getCheckOutDate().format(dateFormatter),
                    reservation.getStatus().getDisplayName(),
                    reservation.getTotalPrice()
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void showCreateReservationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Reservation", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = UIUtils.createPanel(new GridLayout(6, 2, 10, 10), ColorScheme.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Guest selection
        JLabel guestLabel = UIUtils.createRegularLabel("Guest:");
        JPanel guestPanel = UIUtils.createPanel(new BorderLayout(5, 0), ColorScheme.BACKGROUND);
        JComboBox<Guest> guestComboBox = new JComboBox<>();
        for (Guest guest : guestService.getAllGuests()) {
            guestComboBox.addItem(guest);
        }
        JButton newGuestButton = UIUtils.createSecondaryButton("New Guest");
        newGuestButton.setPreferredSize(new Dimension(120, 30));
        newGuestButton.addActionListener(e -> {
            // Show add guest dialog and refresh the combo box
            showAddGuestDialog(guestComboBox);
        });
        guestPanel.add(guestComboBox, BorderLayout.CENTER);
        guestPanel.add(newGuestButton, BorderLayout.EAST);
        
        // Room type selection
        JLabel roomTypeLabel = UIUtils.createRegularLabel("Room Type:");
        JComboBox<RoomType> roomTypeComboBox = UIUtils.createComboBox(RoomType.values());
        
        // Check-in date
        JLabel checkInLabel = UIUtils.createRegularLabel("Check-in Date (yyyy-MM-dd):");
        JTextField checkInField = UIUtils.createTextField(10);
        checkInField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        // Check-out date
        JLabel checkOutLabel = UIUtils.createRegularLabel("Check-out Date (yyyy-MM-dd):");
        JTextField checkOutField = UIUtils.createTextField(10);
        checkOutField.setText(LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        // Available rooms
        JLabel availableRoomsLabel = UIUtils.createRegularLabel("Available Rooms:");
        JComboBox<Room> availableRoomsComboBox = new JComboBox<>();
        
        // Check availability button
        JButton checkAvailabilityButton = UIUtils.createButton("Check Availability", ColorScheme.ACCENT, ColorScheme.TEXT_LIGHT);
        checkAvailabilityButton.addActionListener(e -> {
            try {
                LocalDate checkInDate = LocalDate.parse(checkInField.getText());
                LocalDate checkOutDate = LocalDate.parse(checkOutField.getText());
                RoomType roomType = (RoomType) roomTypeComboBox.getSelectedItem();
                
                if (checkInDate.isAfter(checkOutDate)) {
                    JOptionPane.showMessageDialog(dialog, "Check-in date must be before check-out date.", "Invalid Dates", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                List<Room> availableRooms = reservationService.getAvailableRoomsByTypeForDates(roomType, checkInDate, checkOutDate);
                availableRoomsComboBox.removeAllItems();
                for (Room room : availableRooms) {
                    availableRoomsComboBox.addItem(room);
                }
                
                if (availableRooms.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "No rooms available for the selected dates and room type.", "No Availability", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid dates in the format yyyy-MM-dd.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        formPanel.add(guestLabel);
        formPanel.add(guestPanel);
        formPanel.add(roomTypeLabel);
        formPanel.add(roomTypeComboBox);
        formPanel.add(checkInLabel);
        formPanel.add(checkInField);
        formPanel.add(checkOutLabel);
        formPanel.add(checkOutField);
        formPanel.add(availableRoomsLabel);
        formPanel.add(availableRoomsComboBox);
        formPanel.add(new JLabel()); // Empty cell
        formPanel.add(checkAvailabilityButton);
        
        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        
        JButton cancelButton = UIUtils.createButton("Cancel", ColorScheme.BACKGROUND_DARK, ColorScheme.TEXT_PRIMARY);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton saveButton = UIUtils.createPrimaryButton("Create Reservation");
        saveButton.addActionListener(e -> {
            try {
                Guest guest = (Guest) guestComboBox.getSelectedItem();
                Room room = (Room) availableRoomsComboBox.getSelectedItem();
                LocalDate checkInDate = LocalDate.parse(checkInField.getText());
                LocalDate checkOutDate = LocalDate.parse(checkOutField.getText());
                
                if (guest == null || room == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select a guest and a room.", "Missing Information", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (checkInDate.isAfter(checkOutDate)) {
                    JOptionPane.showMessageDialog(dialog, "Check-in date must be before check-out date.", "Invalid Dates", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Reservation reservation = reservationService.createReservation(guest, room, checkInDate, checkOutDate);
                if (reservation != null) {
                    loadReservations();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create reservation. The room may no longer be available.", "Reservation Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid information and check availability.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void showEditReservationDialog(Reservation reservation) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Reservation", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = UIUtils.createPanel(new GridLayout(5, 2, 10, 10), ColorScheme.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Reservation ID
        JLabel idLabel = UIUtils.createRegularLabel("Reservation ID:");
        JTextField idField = UIUtils.createTextField(10);
        idField.setText(String.valueOf(reservation.getId()));
        idField.setEditable(false);
        
        // Guest
        JLabel guestLabel = UIUtils.createRegularLabel("Guest:");
        JTextField guestField = UIUtils.createTextField(20);
        guestField.setText(reservation.getGuest().getFullName());
        guestField.setEditable(false);
        
        // Room
        JLabel roomLabel = UIUtils.createRegularLabel("Room:");
        JTextField roomField = UIUtils.createTextField(20);
        roomField.setText(reservation.getRoom().toString());
        roomField.setEditable(false);
        
        // Check-in date
        JLabel checkInLabel = UIUtils.createRegularLabel("Check-in Date (yyyy-MM-dd):");
        JTextField checkInField = UIUtils.createTextField(10);
        checkInField.setText(reservation.getCheckInDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        // Check-out date
        JLabel checkOutLabel = UIUtils.createRegularLabel("Check-out Date (yyyy-MM-dd):");
        JTextField checkOutField = UIUtils.createTextField(10);
        checkOutField.setText(reservation.getCheckOutDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(guestLabel);
        formPanel.add(guestField);
        formPanel.add(roomLabel);
        formPanel.add(roomField);
        formPanel.add(checkInLabel);
        formPanel.add(checkInField);
        formPanel.add(checkOutLabel);
        formPanel.add(checkOutField);
        
        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        
        JButton cancelButton = UIUtils.createButton("Cancel", ColorScheme.BACKGROUND_DARK, ColorScheme.TEXT_PRIMARY);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton saveButton = UIUtils.createPrimaryButton("Save Changes");
        saveButton.addActionListener(e -> {
            try {
                LocalDate checkInDate = LocalDate.parse(checkInField.getText());
                LocalDate checkOutDate = LocalDate.parse(checkOutField.getText());
                
                if (checkInDate.isAfter(checkOutDate)) {
                    JOptionPane.showMessageDialog(dialog, "Check-in date must be before check-out date.", "Invalid Dates", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Check if the room is still available for the new dates
                if (!reservation.getCheckInDate().equals(checkInDate) || !reservation.getCheckOutDate().equals(checkOutDate)) {
                    boolean isAvailable = reservationService.isRoomAvailable(reservation.getRoom().getRoomNumber(), checkInDate, checkOutDate);
                    if (!isAvailable) {
                        JOptionPane.showMessageDialog(dialog, "The room is not available for the selected dates.", "No Availability", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                
                reservation.setCheckInDate(checkInDate);
                reservation.setCheckOutDate(checkOutDate);
                
                reservationService.updateReservation(reservation);
                loadReservations();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid dates in the format yyyy-MM-dd.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void showAddGuestDialog(JComboBox<Guest> guestComboBox) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Guest", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = UIUtils.createPanel(new GridLayout(6, 2, 10, 10), ColorScheme.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel firstNameLabel = UIUtils.createRegularLabel("First Name:");
        JTextField firstNameField = UIUtils.createTextField(20);
        
        JLabel lastNameLabel = UIUtils.createRegularLabel("Last Name:");
        JTextField lastNameField = UIUtils.createTextField(20);
        
        JLabel idNumberLabel = UIUtils.createRegularLabel("ID Number:");
        JTextField idNumberField = UIUtils.createTextField(20);
        
        JLabel phoneLabel = UIUtils.createRegularLabel("Phone:");
        JTextField phoneField = UIUtils.createTextField(20);
        
        JLabel emailLabel = UIUtils.createRegularLabel("Email:");
        JTextField emailField = UIUtils.createTextField(20);
        
        JLabel addressLabel = UIUtils.createRegularLabel("Address:");
        JTextField addressField = UIUtils.createTextField(20);
        
        formPanel.add(firstNameLabel);
        formPanel.add(firstNameField);
        formPanel.add(lastNameLabel);
        formPanel.add(lastNameField);
        formPanel.add(idNumberLabel);
        formPanel.add(idNumberField);
        formPanel.add(phoneLabel);
        formPanel.add(phoneField);
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(addressLabel);
        formPanel.add(addressField);
        
        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        
        JButton cancelButton = UIUtils.createButton("Cancel", ColorScheme.BACKGROUND_DARK, ColorScheme.TEXT_PRIMARY);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton saveButton = UIUtils.createPrimaryButton("Save");
        saveButton.addActionListener(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String idNumber = idNumberField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            
            if (firstName.isEmpty() || lastName.isEmpty() || idNumber.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "First Name, Last Name, and ID Number are required.", "Missing Information", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Guest guest = new Guest(firstName, lastName, idNumber, phone, email, address);
            Guest addedGuest = guestService.addGuest(guest);
            
            // Refresh the guest combo box
            guestComboBox.removeAllItems();
            for (Guest g : guestService.getAllGuests()) {
                guestComboBox.addItem(g);
            }
            guestComboBox.setSelectedItem(addedGuest);
            
            dialog.dispose();
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}