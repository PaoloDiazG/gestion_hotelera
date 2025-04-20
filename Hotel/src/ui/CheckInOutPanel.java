package ui;

import model.Reservation;
import model.ReservationStatus;
import service.ReservationService;
import util.ColorScheme;
import util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for handling check-in and check-out operations.
 */
public class CheckInOutPanel extends JPanel {
    private final ReservationService reservationService;
    private JTable reservationsTable;
    private DefaultTableModel tableModel;
    private JTabbedPane tabbedPane;
    
    public CheckInOutPanel() {
        reservationService = ReservationService.getInstance();
        
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        createUI();
    }
    
    private void createUI() {
        // Create header panel
        JPanel headerPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        JLabel titleLabel = UIUtils.createTitleLabel("Check-in / Check-out");
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create tabbed pane
        tabbedPane = UIUtils.createTabbedPane();
        
        // Add tabs for check-in and check-out
        tabbedPane.addTab("Check-in", createCheckInPanel());
        tabbedPane.addTab("Check-out", createCheckOutPanel());
        
        // Add components to panel
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createCheckInPanel() {
        JPanel panel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Create table
        String[] columnNames = {"ID", "Guest", "Room", "Check-in Date", "Check-out Date", "Total Price"};
        DefaultTableModel checkInTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable checkInTable = UIUtils.createTable();
        checkInTable.setModel(checkInTableModel);
        
        JScrollPane scrollPane = UIUtils.createScrollPane(checkInTable);
        
        // Create button panel
        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        
        JButton refreshButton = UIUtils.createSecondaryButton("Refresh");
        refreshButton.addActionListener(e -> loadCheckInReservations(checkInTableModel));
        
        JButton checkInButton = UIUtils.createPrimaryButton("Check-in");
        checkInButton.addActionListener(e -> {
            int selectedRow = checkInTable.getSelectedRow();
            if (selectedRow >= 0) {
                int reservationId = (int) checkInTableModel.getValueAt(selectedRow, 0);
                performCheckIn(reservationId);
                loadCheckInReservations(checkInTableModel);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a reservation to check in.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(checkInButton);
        
        // Add components to panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Load reservations
        loadCheckInReservations(checkInTableModel);
        
        return panel;
    }
    
    private JPanel createCheckOutPanel() {
        JPanel panel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Create table
        String[] columnNames = {"ID", "Guest", "Room", "Check-in Date", "Check-out Date", "Total Price"};
        DefaultTableModel checkOutTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable checkOutTable = UIUtils.createTable();
        checkOutTable.setModel(checkOutTableModel);
        
        JScrollPane scrollPane = UIUtils.createScrollPane(checkOutTable);
        
        // Create button panel
        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        
        JButton refreshButton = UIUtils.createSecondaryButton("Refresh");
        refreshButton.addActionListener(e -> loadCheckOutReservations(checkOutTableModel));
        
        JButton checkOutButton = UIUtils.createPrimaryButton("Check-out");
        checkOutButton.addActionListener(e -> {
            int selectedRow = checkOutTable.getSelectedRow();
            if (selectedRow >= 0) {
                int reservationId = (int) checkOutTableModel.getValueAt(selectedRow, 0);
                performCheckOut(reservationId);
                loadCheckOutReservations(checkOutTableModel);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a reservation to check out.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(checkOutButton);
        
        // Add components to panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Load reservations
        loadCheckOutReservations(checkOutTableModel);
        
        return panel;
    }
    
    private void loadCheckInReservations(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        
        // Get reservations with status CONFIRMED and check-in date today or earlier
        List<Reservation> reservations = reservationService.getReservationsByStatus(ReservationStatus.CONFIRMED);
        LocalDate today = LocalDate.now();
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Reservation reservation : reservations) {
            if (!reservation.getCheckInDate().isAfter(today)) {
                Object[] rowData = {
                        reservation.getId(),
                        reservation.getGuest().getFullName(),
                        reservation.getRoom().toString(),
                        reservation.getCheckInDate().format(dateFormatter),
                        reservation.getCheckOutDate().format(dateFormatter),
                        reservation.getTotalPrice()
                };
                tableModel.addRow(rowData);
            }
        }
    }
    
    private void loadCheckOutReservations(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        
        // Get reservations with status CHECKED_IN
        List<Reservation> reservations = reservationService.getReservationsByStatus(ReservationStatus.CHECKED_IN);
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Reservation reservation : reservations) {
            Object[] rowData = {
                    reservation.getId(),
                    reservation.getGuest().getFullName(),
                    reservation.getRoom().toString(),
                    reservation.getCheckInDate().format(dateFormatter),
                    reservation.getCheckOutDate().format(dateFormatter),
                    reservation.getTotalPrice()
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void performCheckIn(int reservationId) {
        boolean success = reservationService.checkIn(reservationId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Check-in successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to check in. The reservation may have been cancelled or already checked in.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void performCheckOut(int reservationId) {
        boolean success = reservationService.checkOut(reservationId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Check-out successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Ask if the user wants to generate a bill
            int generateBill = JOptionPane.showConfirmDialog(this,
                    "Do you want to generate a bill for this reservation?",
                    "Generate Bill", JOptionPane.YES_NO_OPTION);
            if (generateBill == JOptionPane.YES_OPTION) {
                // Switch to the Billing tab
                JTabbedPane mainTabbedPane = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
                if (mainTabbedPane != null) {
                    for (int i = 0; i < mainTabbedPane.getTabCount(); i++) {
                        if (mainTabbedPane.getTitleAt(i).equals("Billing")) {
                            mainTabbedPane.setSelectedIndex(i);
                            break;
                        }
                    }
                }
                
                // TODO: Pass the reservation ID to the Billing panel to generate a bill
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to check out. The reservation may not be checked in.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}