package ui;

import model.Room;
import model.RoomStatus;
import model.RoomType;
import service.RoomService;
import util.ColorScheme;
import util.Messages;
import util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel for managing rooms.
 */
public class RoomsPanel extends JPanel {
    private final RoomService roomService;
    private JTable roomsTable;
    private DefaultTableModel tableModel;
    private JComboBox<RoomStatus> statusFilterComboBox;
    private JComboBox<RoomType> typeFilterComboBox;

    public RoomsPanel() {
        roomService = RoomService.getInstance();
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createUI();
        loadRooms();
    }

    private void createUI() {
        // Create header panel
        JPanel headerPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        JLabel titleLabel = UIUtils.createTitleLabel(Messages.get("rooms.title"));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Create filter panel
        JPanel filterPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.LEFT), ColorScheme.BACKGROUND);

        JLabel statusFilterLabel = UIUtils.createRegularLabel(Messages.get("rooms.filter.status") + " ");
        statusFilterComboBox = UIUtils.createComboBox(RoomStatus.values());
        statusFilterComboBox.insertItemAt(null, 0);
        statusFilterComboBox.setSelectedIndex(0);
        statusFilterComboBox.addActionListener(e -> loadRooms());

        JLabel typeFilterLabel = UIUtils.createRegularLabel(Messages.get("rooms.filter.type") + " ");
        typeFilterComboBox = UIUtils.createComboBox(RoomType.values());
        typeFilterComboBox.insertItemAt(null, 0);
        typeFilterComboBox.setSelectedIndex(0);
        typeFilterComboBox.addActionListener(e -> loadRooms());

        filterPanel.add(statusFilterLabel);
        filterPanel.add(statusFilterComboBox);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(typeFilterLabel);
        filterPanel.add(typeFilterComboBox);

        headerPanel.add(filterPanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);

        JButton addButton = UIUtils.createPrimaryButton(Messages.get("rooms.button.add"));
        addButton.addActionListener(e -> showAddRoomDialog());

        JButton editButton = UIUtils.createSecondaryButton(Messages.get("rooms.button.edit"));
        editButton.addActionListener(e -> {
            int selectedRow = roomsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int roomNumber = (int) tableModel.getValueAt(selectedRow, 0);
                Room room = roomService.getRoomByNumber(roomNumber);
                if (room != null) {
                    showEditRoomDialog(room);
                }
            } else {
                JOptionPane.showMessageDialog(this, Messages.get("rooms.message.selecttoedit"), 
                    Messages.get("dialog.title.noselection"), JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton deleteButton = UIUtils.createDangerButton(Messages.get("rooms.button.delete"));
        deleteButton.addActionListener(e -> {
            int selectedRow = roomsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int roomNumber = (int) tableModel.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                        Messages.get("rooms.message.confirmdelete", roomNumber),
                        Messages.get("dialog.title.confirmdeletion"), JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    roomService.deleteRoom(roomNumber);
                    loadRooms();
                }
            } else {
                JOptionPane.showMessageDialog(this, Messages.get("rooms.message.selecttodelete"), 
                    Messages.get("dialog.title.noselection"), JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton changeStatusButton = UIUtils.createButton(Messages.get("rooms.button.changestatus"), ColorScheme.ACCENT, ColorScheme.TEXT_LIGHT);
        changeStatusButton.addActionListener(e -> {
            int selectedRow = roomsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int roomNumber = (int) tableModel.getValueAt(selectedRow, 0);
                Room room = roomService.getRoomByNumber(roomNumber);
                if (room != null) {
                    showChangeStatusDialog(room);
                }
            } else {
                JOptionPane.showMessageDialog(this, Messages.get("rooms.message.selecttochangestatus"), 
                    Messages.get("dialog.title.noselection"), JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(changeStatusButton);

        headerPanel.add(buttonPanel, BorderLayout.EAST);

        // Create table
        String[] columnNames = {
            Messages.get("table.room.number"),
            Messages.get("table.room.floor"),
            Messages.get("table.room.type"),
            Messages.get("table.room.status"),
            Messages.get("table.room.price"),
            Messages.get("table.room.description")
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomsTable = UIUtils.createTable();
        roomsTable.setModel(tableModel);

        JScrollPane scrollPane = UIUtils.createScrollPane(roomsTable);

        // Add components to panel
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadRooms() {
        tableModel.setRowCount(0);

        List<Room> rooms;
        RoomStatus statusFilter = (RoomStatus) statusFilterComboBox.getSelectedItem();
        RoomType typeFilter = (RoomType) typeFilterComboBox.getSelectedItem();

        if (statusFilter != null && typeFilter != null) {
            rooms = roomService.getRoomsByStatus(statusFilter).stream()
                    .filter(room -> room.getType() == typeFilter)
                    .toList();
        } else if (statusFilter != null) {
            rooms = roomService.getRoomsByStatus(statusFilter);
        } else if (typeFilter != null) {
            rooms = roomService.getRoomsByType(typeFilter);
        } else {
            rooms = roomService.getAllRooms();
        }

        for (Room room : rooms) {
            Object[] rowData = {
                    room.getRoomNumber(),
                    room.getFloor(),
                    room.getType().getDisplayName(),
                    room.getStatus().getDisplayName(),
                    room.getPricePerNight(),
                    room.getDescription()
            };
            tableModel.addRow(rowData);
        }
    }

    private void showAddRoomDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), Messages.get("rooms.dialog.add"), true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = UIUtils.createPanel(new GridLayout(6, 2, 10, 10), ColorScheme.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel roomNumberLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.number"));
        JTextField roomNumberField = UIUtils.createTextField(10);

        JLabel floorLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.floor"));
        JTextField floorField = UIUtils.createTextField(10);

        JLabel typeLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.type"));
        JComboBox<RoomType> typeComboBox = UIUtils.createComboBox(RoomType.values());

        JLabel statusLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.status"));
        JComboBox<RoomStatus> statusComboBox = UIUtils.createComboBox(RoomStatus.values());

        JLabel priceLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.price"));
        JTextField priceField = UIUtils.createTextField(10);

        JLabel descriptionLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.description"));
        JTextField descriptionField = UIUtils.createTextField(20);

        formPanel.add(roomNumberLabel);
        formPanel.add(roomNumberField);
        formPanel.add(floorLabel);
        formPanel.add(floorField);
        formPanel.add(typeLabel);
        formPanel.add(typeComboBox);
        formPanel.add(statusLabel);
        formPanel.add(statusComboBox);
        formPanel.add(priceLabel);
        formPanel.add(priceField);
        formPanel.add(descriptionLabel);
        formPanel.add(descriptionField);

        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);

        JButton cancelButton = UIUtils.createButton(Messages.get("button.cancel"), ColorScheme.BACKGROUND_DARK, ColorScheme.TEXT_PRIMARY);
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton saveButton = UIUtils.createPrimaryButton(Messages.get("button.save"));
        saveButton.addActionListener(e -> {
            try {
                int roomNumber = Integer.parseInt(roomNumberField.getText());
                int floor = Integer.parseInt(floorField.getText());
                RoomType type = (RoomType) typeComboBox.getSelectedItem();
                RoomStatus status = (RoomStatus) statusComboBox.getSelectedItem();
                BigDecimal price = new BigDecimal(priceField.getText());
                String description = descriptionField.getText();

                Room room = new Room(roomNumber, floor, type, status, price, description);
                roomService.addRoom(room);
                loadRooms();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, Messages.get("rooms.message.invalidinput"), 
                    Messages.get("dialog.title.invalidinput"), JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showEditRoomDialog(Room room) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), Messages.get("rooms.dialog.edit"), true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = UIUtils.createPanel(new GridLayout(6, 2, 10, 10), ColorScheme.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel roomNumberLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.number"));
        JTextField roomNumberField = UIUtils.createTextField(10);
        roomNumberField.setText(String.valueOf(room.getRoomNumber()));
        roomNumberField.setEditable(false);

        JLabel floorLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.floor"));
        JTextField floorField = UIUtils.createTextField(10);
        floorField.setText(String.valueOf(room.getFloor()));

        JLabel typeLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.type"));
        JComboBox<RoomType> typeComboBox = UIUtils.createComboBox(RoomType.values());
        typeComboBox.setSelectedItem(room.getType());

        JLabel statusLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.status"));
        JComboBox<RoomStatus> statusComboBox = UIUtils.createComboBox(RoomStatus.values());
        statusComboBox.setSelectedItem(room.getStatus());

        JLabel priceLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.price"));
        JTextField priceField = UIUtils.createTextField(10);
        priceField.setText(room.getPricePerNight().toString());

        JLabel descriptionLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.description"));
        JTextField descriptionField = UIUtils.createTextField(20);
        descriptionField.setText(room.getDescription());

        formPanel.add(roomNumberLabel);
        formPanel.add(roomNumberField);
        formPanel.add(floorLabel);
        formPanel.add(floorField);
        formPanel.add(typeLabel);
        formPanel.add(typeComboBox);
        formPanel.add(statusLabel);
        formPanel.add(statusComboBox);
        formPanel.add(priceLabel);
        formPanel.add(priceField);
        formPanel.add(descriptionLabel);
        formPanel.add(descriptionField);

        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);

        JButton cancelButton = UIUtils.createButton(Messages.get("button.cancel"), ColorScheme.BACKGROUND_DARK, ColorScheme.TEXT_PRIMARY);
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton saveButton = UIUtils.createPrimaryButton(Messages.get("button.save"));
        saveButton.addActionListener(e -> {
            try {
                int roomNumber = Integer.parseInt(roomNumberField.getText());
                int floor = Integer.parseInt(floorField.getText());
                RoomType type = (RoomType) typeComboBox.getSelectedItem();
                RoomStatus status = (RoomStatus) statusComboBox.getSelectedItem();
                BigDecimal price = new BigDecimal(priceField.getText());
                String description = descriptionField.getText();

                room.setFloor(floor);
                room.setType(type);
                room.setStatus(status);
                room.setPricePerNight(price);
                room.setDescription(description);

                roomService.updateRoom(room);
                loadRooms();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, Messages.get("rooms.message.invalidfloorprice"), 
                    Messages.get("dialog.title.invalidinput"), JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showChangeStatusDialog(Room room) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), Messages.get("rooms.dialog.changestatus"), true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = UIUtils.createPanel(new GridLayout(1, 2, 10, 10), ColorScheme.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel statusLabel = UIUtils.createRegularLabel(Messages.get("rooms.field.status"));
        JComboBox<RoomStatus> statusComboBox = UIUtils.createComboBox(RoomStatus.values());
        statusComboBox.setSelectedItem(room.getStatus());

        formPanel.add(statusLabel);
        formPanel.add(statusComboBox);

        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);

        JButton cancelButton = UIUtils.createButton(Messages.get("button.cancel"), ColorScheme.BACKGROUND_DARK, ColorScheme.TEXT_PRIMARY);
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton saveButton = UIUtils.createPrimaryButton(Messages.get("button.save"));
        saveButton.addActionListener(e -> {
            RoomStatus status = (RoomStatus) statusComboBox.getSelectedItem();
            roomService.changeRoomStatus(room.getRoomNumber(), status);
            loadRooms();
            dialog.dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
