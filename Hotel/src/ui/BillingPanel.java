package ui;

import model.Bill;
import model.Reservation;
import model.ReservationStatus;
import service.BillingService;
import service.ReservationService;
import util.ColorScheme;
import util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing bills.
 */
public class BillingPanel extends JPanel {
    private final BillingService billingService;
    private final ReservationService reservationService;
    private JTable billsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterComboBox;
    
    public BillingPanel() {
        billingService = BillingService.getInstance();
        reservationService = ReservationService.getInstance();
        
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        createUI();
        loadBills();
    }
    
    private void createUI() {
        // Create header panel
        JPanel headerPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        JLabel titleLabel = UIUtils.createTitleLabel("Billing Management");
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create filter panel
        JPanel filterPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.LEFT), ColorScheme.BACKGROUND);
        
        JLabel filterLabel = UIUtils.createRegularLabel("Filter: ");
        String[] filterOptions = {"All Bills", "Paid Bills", "Unpaid Bills"};
        filterComboBox = UIUtils.createComboBox(filterOptions);
        filterComboBox.addActionListener(e -> loadBills());
        
        filterPanel.add(filterLabel);
        filterPanel.add(filterComboBox);
        
        headerPanel.add(filterPanel, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        
        JButton createButton = UIUtils.createPrimaryButton("Create Bill");
        createButton.addActionListener(e -> showCreateBillDialog());
        
        JButton viewButton = UIUtils.createSecondaryButton("View Bill");
        viewButton.addActionListener(e -> {
            int selectedRow = billsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int billId = (int) tableModel.getValueAt(selectedRow, 0);
                Bill bill = billingService.getBillById(billId);
                if (bill != null) {
                    showViewBillDialog(bill);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a bill to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        JButton markPaidButton = UIUtils.createButton("Mark as Paid", ColorScheme.SUCCESS, ColorScheme.TEXT_LIGHT);
        markPaidButton.addActionListener(e -> {
            int selectedRow = billsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int billId = (int) tableModel.getValueAt(selectedRow, 0);
                boolean isPaid = (boolean) tableModel.getValueAt(selectedRow, 4);
                if (!isPaid) {
                    billingService.markBillAsPaid(billId);
                    loadBills();
                } else {
                    JOptionPane.showMessageDialog(this, "This bill is already marked as paid.", "Already Paid", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a bill to mark as paid.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(createButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(markPaidButton);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Create table
        String[] columnNames = {"ID", "Guest", "Room", "Issue Date", "Paid", "Total Amount"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) { // Paid column
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        billsTable = UIUtils.createTable();
        billsTable.setModel(tableModel);
        
        JScrollPane scrollPane = UIUtils.createScrollPane(billsTable);
        
        // Add components to panel
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadBills() {
        tableModel.setRowCount(0);
        
        List<Bill> bills;
        String filter = (String) filterComboBox.getSelectedItem();
        
        if ("Paid Bills".equals(filter)) {
            bills = billingService.getBillsByPaidStatus(true);
        } else if ("Unpaid Bills".equals(filter)) {
            bills = billingService.getBillsByPaidStatus(false);
        } else {
            bills = billingService.getAllBills();
        }
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Bill bill : bills) {
            Object[] rowData = {
                    bill.getId(),
                    bill.getReservation().getGuest().getFullName(),
                    bill.getReservation().getRoom().toString(),
                    bill.getIssueDate().format(dateFormatter),
                    bill.isPaid(),
                    bill.calculateTotal()
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void showCreateBillDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Bill", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Get checked-out reservations without bills
        List<Reservation> checkedOutReservations = reservationService.getReservationsByStatus(ReservationStatus.CHECKED_OUT);
        checkedOutReservations = checkedOutReservations.stream()
                .filter(reservation -> billingService.getBillByReservation(reservation) == null)
                .toList();
        
        if (checkedOutReservations.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No checked-out reservations without bills found.", "No Reservations", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            return;
        }
        
        // Reservation selection
        JLabel reservationLabel = UIUtils.createRegularLabel("Select Reservation:");
        JComboBox<Reservation> reservationComboBox = new JComboBox<>();
        for (Reservation reservation : checkedOutReservations) {
            reservationComboBox.addItem(reservation);
        }
        
        JPanel reservationPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        reservationPanel.add(reservationLabel, BorderLayout.NORTH);
        reservationPanel.add(reservationComboBox, BorderLayout.CENTER);
        
        // Additional items panel
        JPanel itemsPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JLabel itemsLabel = UIUtils.createSubtitleLabel("Additional Items");
        
        JPanel itemFormPanel = UIUtils.createPanel(new GridLayout(2, 2, 10, 10), ColorScheme.BACKGROUND);
        
        JLabel descriptionLabel = UIUtils.createRegularLabel("Description:");
        JTextField descriptionField = UIUtils.createTextField(20);
        
        JLabel amountLabel = UIUtils.createRegularLabel("Amount:");
        JTextField amountField = UIUtils.createTextField(10);
        
        itemFormPanel.add(descriptionLabel);
        itemFormPanel.add(descriptionField);
        itemFormPanel.add(amountLabel);
        itemFormPanel.add(amountField);
        
        JButton addItemButton = UIUtils.createSecondaryButton("Add Item");
        
        // Table for additional items
        String[] columnNames = {"Description", "Amount"};
        DefaultTableModel itemsTableModel = new DefaultTableModel(columnNames, 0);
        JTable itemsTable = UIUtils.createTable();
        itemsTable.setModel(itemsTableModel);
        
        JScrollPane itemsScrollPane = UIUtils.createScrollPane(itemsTable);
        
        addItemButton.addActionListener(e -> {
            String description = descriptionField.getText().trim();
            String amountText = amountField.getText().trim();
            
            if (description.isEmpty() || amountText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter both description and amount.", "Missing Information", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                BigDecimal amount = new BigDecimal(amountText);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Amount must be greater than zero.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Object[] rowData = {description, amount};
                itemsTableModel.addRow(rowData);
                
                descriptionField.setText("");
                amountField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid amount.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JPanel itemButtonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        itemButtonPanel.add(addItemButton);
        
        itemsPanel.add(itemsLabel, BorderLayout.NORTH);
        itemsPanel.add(itemFormPanel, BorderLayout.CENTER);
        itemsPanel.add(itemButtonPanel, BorderLayout.SOUTH);
        
        formPanel.add(reservationPanel, BorderLayout.NORTH);
        formPanel.add(itemsPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        
        JButton cancelButton = UIUtils.createButton("Cancel", ColorScheme.BACKGROUND_DARK, ColorScheme.TEXT_PRIMARY);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton createButton = UIUtils.createPrimaryButton("Create Bill");
        createButton.addActionListener(e -> {
            Reservation reservation = (Reservation) reservationComboBox.getSelectedItem();
            if (reservation != null) {
                Bill bill = billingService.createBill(reservation);
                if (bill != null) {
                    // Add additional items
                    for (int i = 0; i < itemsTableModel.getRowCount(); i++) {
                        String description = (String) itemsTableModel.getValueAt(i, 0);
                        BigDecimal amount = (BigDecimal) itemsTableModel.getValueAt(i, 1);
                        billingService.addItemToBill(bill.getId(), description, amount);
                    }
                    
                    loadBills();
                    dialog.dispose();
                    
                    // Show the bill
                    showViewBillDialog(bill);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create bill. The reservation may already have a bill.", "Bill Creation Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void showViewBillDialog(Bill bill) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "View Bill", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Bill header
        JPanel headerPanel = UIUtils.createPanel(new GridLayout(6, 2, 10, 10), ColorScheme.BACKGROUND);
        
        JLabel billIdLabel = UIUtils.createRegularLabel("Bill ID:");
        JLabel billIdValue = UIUtils.createRegularLabel(String.valueOf(bill.getId()));
        
        JLabel guestLabel = UIUtils.createRegularLabel("Guest:");
        JLabel guestValue = UIUtils.createRegularLabel(bill.getReservation().getGuest().getFullName());
        
        JLabel roomLabel = UIUtils.createRegularLabel("Room:");
        JLabel roomValue = UIUtils.createRegularLabel(bill.getReservation().getRoom().toString());
        
        JLabel checkInLabel = UIUtils.createRegularLabel("Check-in Date:");
        JLabel checkInValue = UIUtils.createRegularLabel(bill.getReservation().getCheckInDate().toString());
        
        JLabel checkOutLabel = UIUtils.createRegularLabel("Check-out Date:");
        JLabel checkOutValue = UIUtils.createRegularLabel(bill.getReservation().getCheckOutDate().toString());
        
        JLabel issueDateLabel = UIUtils.createRegularLabel("Issue Date:");
        JLabel issueDateValue = UIUtils.createRegularLabel(bill.getIssueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        headerPanel.add(billIdLabel);
        headerPanel.add(billIdValue);
        headerPanel.add(guestLabel);
        headerPanel.add(guestValue);
        headerPanel.add(roomLabel);
        headerPanel.add(roomValue);
        headerPanel.add(checkInLabel);
        headerPanel.add(checkInValue);
        headerPanel.add(checkOutLabel);
        headerPanel.add(checkOutValue);
        headerPanel.add(issueDateLabel);
        headerPanel.add(issueDateValue);
        
        // Bill items
        JLabel itemsLabel = UIUtils.createSubtitleLabel("Bill Items");
        
        String[] columnNames = {"Description", "Amount"};
        DefaultTableModel itemsTableModel = new DefaultTableModel(columnNames, 0);
        JTable itemsTable = UIUtils.createTable();
        itemsTable.setModel(itemsTableModel);
        
        for (Bill.BillItem item : bill.getItems()) {
            Object[] rowData = {item.getDescription(), item.getAmount()};
            itemsTableModel.addRow(rowData);
        }
        
        JScrollPane itemsScrollPane = UIUtils.createScrollPane(itemsTable);
        
        // Total
        JPanel totalPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        JLabel totalLabel = UIUtils.createRegularLabel("Total: ");
        JLabel totalValue = UIUtils.createLabel(bill.calculateTotal().toString(), new Font("Arial", Font.BOLD, 16), ColorScheme.TEXT_PRIMARY);
        totalPanel.add(totalLabel);
        totalPanel.add(totalValue);
        
        // Paid status
        JPanel statusPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        JLabel statusLabel = UIUtils.createRegularLabel("Status: ");
        JLabel statusValue;
        if (bill.isPaid()) {
            statusValue = UIUtils.createLabel("PAID", new Font("Arial", Font.BOLD, 16), ColorScheme.SUCCESS);
        } else {
            statusValue = UIUtils.createLabel("UNPAID", new Font("Arial", Font.BOLD, 16), ColorScheme.ERROR);
        }
        statusPanel.add(statusLabel);
        statusPanel.add(statusValue);
        
        JPanel itemsPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        itemsPanel.add(itemsLabel, BorderLayout.NORTH);
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);
        
        JPanel footerPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        footerPanel.add(totalPanel, BorderLayout.NORTH);
        footerPanel.add(statusPanel, BorderLayout.SOUTH);
        
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(itemsPanel, BorderLayout.CENTER);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
        
        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        
        JButton closeButton = UIUtils.createButton("Close", ColorScheme.BACKGROUND_DARK, ColorScheme.TEXT_PRIMARY);
        closeButton.addActionListener(e -> dialog.dispose());
        
        JButton printButton = UIUtils.createPrimaryButton("Print");
        printButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Printing functionality not implemented.", "Print", JOptionPane.INFORMATION_MESSAGE);
        });
        
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}