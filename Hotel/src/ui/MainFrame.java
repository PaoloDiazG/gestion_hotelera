package ui;

import util.ColorScheme;
import util.Messages;
import util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main application window.
 */
public class MainFrame extends JFrame {
    private JPanel statusBar;
    private JLabel statusLabel;
    private JLabel timeLabel;
    private Timer timer;

    public MainFrame() {
        // Set up the frame
        setTitle(Messages.get("app.title"));
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set system look and feel
        UIUtils.setSystemLookAndFeel();

        // Create components
        createMenuBar();
        createStatusBar();
        createMainPanel();

        // Start the timer for updating the time
        startTimer();

        // Make the frame visible
        setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(ColorScheme.PRIMARY_DARK);

        // File menu
        JMenu fileMenu = new JMenu(Messages.get("menu.file"));
        fileMenu.setForeground(ColorScheme.TEXT_LIGHT);

        JMenuItem exitItem = new JMenuItem(Messages.get("menu.exit"));
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Help menu
        JMenu helpMenu = new JMenu(Messages.get("menu.help"));
        helpMenu.setForeground(ColorScheme.TEXT_LIGHT);

        JMenuItem aboutItem = new JMenuItem(Messages.get("menu.about"));
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                Messages.get("app.title") + "\nVersion 1.0\n© 2023",
                Messages.get("menu.about"), JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void createStatusBar() {
        statusBar = UIUtils.createPanel(new BorderLayout(), ColorScheme.PRIMARY_DARK);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = UIUtils.createLabel(Messages.get("status.ready"), new Font("Arial", Font.PLAIN, 12), ColorScheme.TEXT_LIGHT);
        timeLabel = UIUtils.createLabel("", new Font("Arial", Font.PLAIN, 12), ColorScheme.TEXT_LIGHT);

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(timeLabel, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    private void createMainPanel() {
        JTabbedPane tabbedPane = UIUtils.createTabbedPane();

        // Add tabs for different screens
        tabbedPane.addTab(Messages.get("tab.dashboard"), createDashboardPanel());
        tabbedPane.addTab(Messages.get("tab.rooms"), new RoomsPanel());
        tabbedPane.addTab(Messages.get("tab.guests"), new GuestsPanel());
        tabbedPane.addTab(Messages.get("tab.reservations"), new ReservationsPanel());
        tabbedPane.addTab(Messages.get("tab.checkinout"), new CheckInOutPanel());
        tabbedPane.addTab(Messages.get("tab.billing"), new BillingPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = UIUtils.createTitleLabel(Messages.get("dashboard.title"));
        JLabel subtitleLabel = UIUtils.createSubtitleLabel(Messages.get("dashboard.subtitle"));

        JPanel headerPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel contentPanel = UIUtils.createPanel(new GridLayout(2, 2, 20, 20), ColorScheme.BACKGROUND);

        // Add dashboard cards
        contentPanel.add(createDashboardCard(Messages.get("dashboard.rooms"), Messages.get("dashboard.rooms.desc"), ColorScheme.PRIMARY));
        contentPanel.add(createDashboardCard(Messages.get("dashboard.guests"), Messages.get("dashboard.guests.desc"), ColorScheme.SECONDARY));
        contentPanel.add(createDashboardCard(Messages.get("dashboard.reservations"), Messages.get("dashboard.reservations.desc"), ColorScheme.ACCENT));
        contentPanel.add(createDashboardCard(Messages.get("dashboard.billing"), Messages.get("dashboard.billing.desc"), ColorScheme.PRIMARY_DARK));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDashboardCard(String title, String description, Color color) {
        JPanel panel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        panel.setBorder(BorderFactory.createLineBorder(color, 2));

        JPanel headerPanel = UIUtils.createPanel(new BorderLayout(), color);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = UIUtils.createLabel(title, new Font("Arial", Font.BOLD, 18), ColorScheme.TEXT_LIGHT);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel contentPanel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel descLabel = UIUtils.createRegularLabel(description);
        contentPanel.add(descLabel, BorderLayout.NORTH);

        JButton button = UIUtils.createButton(Messages.get("dashboard.button.open"), color, ColorScheme.TEXT_LIGHT);
        JPanel buttonPanel = UIUtils.createPanel(new FlowLayout(FlowLayout.RIGHT), ColorScheme.BACKGROUND);
        buttonPanel.add(button);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private void startTimer() {
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTime();
            }
        });
        timer.start();
    }

    private void updateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        timeLabel.setText(now.format(formatter));
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
