package util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Utility class for UI components.
 */
public class UIUtils {

    // Create a styled button
    public static JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 40));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    // Create a primary button
    public static JButton createPrimaryButton(String text) {
        return createButton(text, ColorScheme.PRIMARY, ColorScheme.TEXT_LIGHT);
    }

    // Create a secondary button
    public static JButton createSecondaryButton(String text) {
        return createButton(text, ColorScheme.SECONDARY, ColorScheme.TEXT_LIGHT);
    }

    // Create a danger button
    public static JButton createDangerButton(String text) {
        return createButton(text, ColorScheme.ERROR, ColorScheme.TEXT_LIGHT);
    }

    // Create a styled panel
    public static JPanel createPanel(LayoutManager layout, Color bgColor) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(bgColor);
        return panel;
    }

    // Create a styled label
    public static JLabel createLabel(String text, Font font, Color fgColor) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(fgColor);
        return label;
    }

    // Create a title label
    public static JLabel createTitleLabel(String text) {
        return createLabel(text, new Font("Arial", Font.BOLD, 24), ColorScheme.TEXT_PRIMARY);
    }

    // Create a subtitle label
    public static JLabel createSubtitleLabel(String text) {
        return createLabel(text, new Font("Arial", Font.BOLD, 18), ColorScheme.TEXT_PRIMARY);
    }

    // Create a regular label
    public static JLabel createRegularLabel(String text) {
        return createLabel(text, new Font("Arial", Font.PLAIN, 14), ColorScheme.TEXT_PRIMARY);
    }

    // Create a styled text field
    public static JTextField createTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(createTextFieldBorder());
        return textField;
    }

    // Create a styled password field
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField passwordField = new JPasswordField(columns);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(createTextFieldBorder());
        return passwordField;
    }

    // Create a styled combo box
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(createTextFieldBorder());
        return comboBox;
    }

    // Create a styled table
    public static JTable createTable() {
        JTable table = new JTable();
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(ColorScheme.BACKGROUND_DARK);
        table.setSelectionBackground(ColorScheme.PRIMARY_LIGHT);
        table.setSelectionForeground(ColorScheme.TEXT_LIGHT);

        // Style the header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(ColorScheme.PRIMARY);
        header.setForeground(ColorScheme.TEXT_LIGHT);

        // Center align the cell content
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        return table;
    }

    // Create a styled scroll pane
    public static JScrollPane createScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    // Create a border for text fields
    private static Border createTextFieldBorder() {
        Border line = new LineBorder(ColorScheme.BACKGROUND_DARK);
        Border empty = new EmptyBorder(5, 10, 5, 10);
        return new CompoundBorder(line, empty);
    }

    // Create a styled tabbed pane
    public static JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(ColorScheme.BACKGROUND);
        tabbedPane.setForeground(ColorScheme.TEXT_PRIMARY);
        return tabbedPane;
    }

    // Set look and feel to system look and feel
    public static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
