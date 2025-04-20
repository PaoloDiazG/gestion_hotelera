package ui;

import model.Guest;
import service.GuestService;
import util.ColorScheme;
import util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for managing guests.
 */
public class GuestsPanel extends JPanel {
    private final GuestService guestService;
    private JTable guestsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public GuestsPanel() {
        guestService = GuestService.getInstance();
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        createUI();
        loadGuests();
    }
    
    private void createUI() {
        // Create header panel
        JPanel headerPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        JLabel titleLabel = UIUtils.createTitleLabel("Guest Management");
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create search panel
        JPanel searchPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.LEFT), ColorScheme.BACKGROUND);
        
        JLabel searchLabel = UIUtils.createRegularLabel("Search: ");
        searchField = UIUtils.createTextField(20);
        JButton searchButton = UIUtils.createPrimaryButton("Search");
        searchButton.addActionListener(e -> searchGuests());
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        
        JButton addButton = UIUtils.createPrimaryButton("Add Guest");
        addButton.addActionListener(e -> showAddGuestDialog());
        
        JButton editButton = UIUtils.createSecondaryButton("Edit Guest");
        editButton.addActionListener(e -> {
            int selectedRow = guestsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int guestId = (int) tableModel.getValueAt(selectedRow, 0);
                Guest guest = guestService.getGuestById(guestId);
                if (guest != null) {
                    showEditGuestDialog(guest);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a guest to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        JButton deleteButton = UIUtils.createDangerButton("Delete Guest");
        deleteButton.addActionListener(e -> {
            int selectedRow = guestsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int guestId = (int) tableModel.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete this guest?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    guestService.deleteGuest(guestId);
                    loadGuests();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a guest to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Create table
        String[] columnNames = {"ID", "First Name", "Last Name", "ID Number", "Phone", "Email", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        guestsTable = UIUtils.createTable();
        guestsTable.setModel(tableModel);
        
        JScrollPane scrollPane = UIUtils.createScrollPane(guestsTable);
        
        // Add components to panel
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadGuests() {
        tableModel.setRowCount(0);
        
        List<Guest> guests = guestService.getAllGuests();
        
        for (Guest guest : guests) {
            Object[] rowData = {
                    guest.getId(),
                    guest.getFirstName(),
                    guest.getLastName(),
                    guest.getIdNumber(),
                    guest.getPhone(),
                    guest.getEmail(),
                    guest.getAddress()
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void searchGuests() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadGuests();
            return;
        }
        
        tableModel.setRowCount(0);
        
        // Try to find by ID number first
        Guest guestByIdNumber = guestService.searchGuestByIdNumber(searchTerm);
        if (guestByIdNumber != null) {
            Object[] rowData = {
                    guestByIdNumber.getId(),
                    guestByIdNumber.getFirstName(),
                    guestByIdNumber.getLastName(),
                    guestByIdNumber.getIdNumber(),
                    guestByIdNumber.getPhone(),
                    guestByIdNumber.getEmail(),
                    guestByIdNumber.getAddress()
            };
            tableModel.addRow(rowData);
        } else {
            // Search by name
            List<Guest> guestsByName = guestService.searchGuestsByName(searchTerm);
            for (Guest guest : guestsByName) {
                Object[] rowData = {
                        guest.getId(),
                        guest.getFirstName(),
                        guest.getLastName(),
                        guest.getIdNumber(),
                        guest.getPhone(),
                        guest.getEmail(),
                        guest.getAddress()
                };
                tableModel.addRow(rowData);
            }
        }
    }
    
    private void showAddGuestDialog() {
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
            guestService.addGuest(guest);
            loadGuests();
            dialog.dispose();
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void showEditGuestDialog(Guest guest) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Guest", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = UIUtils.createPanel(new GridLayout(6, 2, 10, 10), ColorScheme.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel firstNameLabel = UIUtils.createRegularLabel("First Name:");
        JTextField firstNameField = UIUtils.createTextField(20);
        firstNameField.setText(guest.getFirstName());
        
        JLabel lastNameLabel = UIUtils.createRegularLabel("Last Name:");
        JTextField lastNameField = UIUtils.createTextField(20);
        lastNameField.setText(guest.getLastName());
        
        JLabel idNumberLabel = UIUtils.createRegularLabel("ID Number:");
        JTextField idNumberField = UIUtils.createTextField(20);
        idNumberField.setText(guest.getIdNumber());
        
        JLabel phoneLabel = UIUtils.createRegularLabel("Phone:");
        JTextField phoneField = UIUtils.createTextField(20);
        phoneField.setText(guest.getPhone());
        
        JLabel emailLabel = UIUtils.createRegularLabel("Email:");
        JTextField emailField = UIUtils.createTextField(20);
        emailField.setText(guest.getEmail());
        
        JLabel addressLabel = UIUtils.createRegularLabel("Address:");
        JTextField addressField = UIUtils.createTextField(20);
        addressField.setText(guest.getAddress());
        
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
            
            guest.setFirstName(firstName);
            guest.setLastName(lastName);
            guest.setIdNumber(idNumber);
            guest.setPhone(phone);
            guest.setEmail(email);
            guest.setAddress(address);
            
            guestService.updateGuest(guest);
            loadGuests();
            dialog.dispose();
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}