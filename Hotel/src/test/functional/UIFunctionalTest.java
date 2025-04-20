package test.functional;

import model.RoomStatus;
import model.RoomType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ui.MainFrame;
import ui.RoomsPanel;
import util.ColorScheme;
import util.Messages;
import util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Functional tests for the UI components.
 * These tests verify that the UI components are created correctly and behave as expected.
 */
public class UIFunctionalTest {
    
    @Before
    public void setUp() {
        // Ensure we're using the Spanish locale for testing
        Locale.setDefault(new Locale("es", "ES"));
    }
    
    @After
    public void tearDown() {
        // Reset to default locale if needed
    }
    
    @Test
    public void testMessagesTranslation() {
        // Test that the Messages class correctly loads and returns translations
        String appTitle = Messages.get("app.title");
        assertNotNull("App title should be translated", appTitle);
        assertFalse("App title should not be the key itself", appTitle.equals("app.title"));
        
        // Test a few specific translations
        assertEquals("Sistema de Gestión Hotelera", Messages.get("app.title"));
        assertEquals("Habitaciones", Messages.get("tab.rooms"));
        assertEquals("Individual", Messages.get("roomtype.simple"));
    }
    
    @Test
    public void testUIUtilsComponents() {
        // Test that UIUtils correctly creates UI components
        
        // Test button creation
        JButton primaryButton = UIUtils.createPrimaryButton(Messages.get("button.save"));
        assertEquals("Guardar", primaryButton.getText());
        assertEquals(ColorScheme.PRIMARY, primaryButton.getBackground());
        assertEquals(ColorScheme.TEXT_LIGHT, primaryButton.getForeground());
        assertTrue("Button should be opaque", primaryButton.isOpaque());
        
        // Test label creation
        JLabel titleLabel = UIUtils.createTitleLabel(Messages.get("app.title"));
        assertEquals("Sistema de Gestión Hotelera", titleLabel.getText());
        assertEquals(new Font("Arial", Font.BOLD, 24).getFamily(), titleLabel.getFont().getFamily());
        assertEquals(ColorScheme.TEXT_PRIMARY, titleLabel.getForeground());
        
        // Test panel creation
        JPanel panel = UIUtils.createPanel(new BorderLayout(), ColorScheme.BACKGROUND);
        assertEquals(BorderLayout.class, panel.getLayout().getClass());
        assertEquals(ColorScheme.BACKGROUND, panel.getBackground());
    }
    
    @Test
    public void testColorSchemeStatusColors() {
        // Test that ColorScheme correctly maps status strings to colors
        
        // Test room status colors
        assertEquals(ColorScheme.ROOM_AVAILABLE, 
                    ColorScheme.getRoomStatusColor(RoomStatus.AVAILABLE.getDisplayName()));
        assertEquals(ColorScheme.ROOM_OCCUPIED, 
                    ColorScheme.getRoomStatusColor(RoomStatus.OCCUPIED.getDisplayName()));
        assertEquals(ColorScheme.ROOM_MAINTENANCE, 
                    ColorScheme.getRoomStatusColor(RoomStatus.MAINTENANCE.getDisplayName()));
        assertEquals(ColorScheme.ROOM_CLEANING, 
                    ColorScheme.getRoomStatusColor(RoomStatus.CLEANING.getDisplayName()));
        
        // Test with non-existent status
        assertEquals(ColorScheme.BACKGROUND, 
                    ColorScheme.getRoomStatusColor("Non-existent Status"));
    }
    
    @Test
    public void testEnumTranslations() {
        // Test that enums correctly use the Messages class for translations
        
        // Test RoomType translations
        assertEquals("Individual", RoomType.SIMPLE.getDisplayName());
        assertEquals("Doble", RoomType.DOUBLE.getDisplayName());
        assertEquals("Suite", RoomType.SUITE.getDisplayName());
        
        // Test RoomStatus translations
        assertEquals("Disponible", RoomStatus.AVAILABLE.getDisplayName());
        assertEquals("Ocupada", RoomStatus.OCCUPIED.getDisplayName());
        assertEquals("Mantenimiento", RoomStatus.MAINTENANCE.getDisplayName());
        assertEquals("Limpieza", RoomStatus.CLEANING.getDisplayName());
    }
    
    @Test
    public void testMainFrameCreation() {
        // Test that MainFrame is created correctly with translated components
        // Note: This is a headless test that doesn't actually show the UI
        
        // Create the frame but make it not visible
        MainFrame frame = new MainFrame() {
            @Override
            public void setVisible(boolean visible) {
                // Override to prevent the frame from becoming visible
                // This is necessary for headless testing
            }
        };
        
        // Test that the frame has the correct title
        assertEquals("Sistema de Gestión Hotelera", frame.getTitle());
        
        // Test that the frame has the correct size
        assertEquals(new Dimension(1200, 800), frame.getSize());
        
        // Test that the frame has a menu bar
        assertNotNull("Frame should have a menu bar", frame.getJMenuBar());
        
        // Test that the menu bar has the correct menus
        JMenuBar menuBar = frame.getJMenuBar();
        assertTrue("Menu bar should have at least 2 menus", menuBar.getMenuCount() >= 2);
        
        // Test the first menu (File)
        JMenu fileMenu = menuBar.getMenu(0);
        assertEquals("Archivo", fileMenu.getText());
        
        // Test the second menu (Help)
        JMenu helpMenu = menuBar.getMenu(1);
        assertEquals("Ayuda", helpMenu.getText());
    }
    
    @Test
    public void testRoomsPanelComponents() {
        // Test that RoomsPanel is created correctly with translated components
        RoomsPanel roomsPanel = new RoomsPanel();
        
        // Test that the panel has the correct layout
        assertEquals(BorderLayout.class, roomsPanel.getLayout().getClass());
        
        // Test that the panel has the correct background color
        assertEquals(ColorScheme.BACKGROUND, roomsPanel.getBackground());
        
        // Count the components to ensure they're all there
        Component[] components = roomsPanel.getComponents();
        assertTrue("Panel should have at least 2 components", components.length >= 2);
        
        // Find the table by traversing the component hierarchy
        JTable roomsTable = findComponentByType(roomsPanel, JTable.class);
        assertNotNull("Panel should contain a JTable", roomsTable);
        
        // Test that the table has the correct column names
        assertEquals(Messages.get("table.room.number"), roomsTable.getColumnName(0));
        assertEquals(Messages.get("table.room.floor"), roomsTable.getColumnName(1));
        assertEquals(Messages.get("table.room.type"), roomsTable.getColumnName(2));
        assertEquals(Messages.get("table.room.status"), roomsTable.getColumnName(3));
        assertEquals(Messages.get("table.room.price"), roomsTable.getColumnName(4));
        assertEquals(Messages.get("table.room.description"), roomsTable.getColumnName(5));
    }
    
    /**
     * Helper method to find a component of a specific type in a container hierarchy.
     */
    @SuppressWarnings("unchecked")
    private <T extends Component> T findComponentByType(Container container, Class<T> type) {
        for (Component component : container.getComponents()) {
            if (type.isInstance(component)) {
                return (T) component;
            }
            if (component instanceof Container) {
                T found = findComponentByType((Container) component, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}