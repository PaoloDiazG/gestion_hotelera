package util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class for handling internationalization (i18n) messages.
 */
public class Messages {
    private static final String BUNDLE_NAME = "resources.messages";
    private static ResourceBundle bundle;

    static {
        // Set Spanish as the default locale
        Locale.setDefault(new Locale("es", "ES"));
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
        } catch (Exception e) {
            System.err.println("Error loading resource bundle: " + e.getMessage());
            // Fallback to an empty bundle
            bundle = new ResourceBundle() {
                @Override
                protected Object handleGetObject(String key) {
                    return null;
                }

                @Override
                public Enumeration<String> getKeys() {
                    return Collections.emptyEnumeration();
                }
            };
        }
    }

    /**
     * Get a message from the resource bundle.
     * 
     * @param key the key for the message
     * @return the message in the current locale
     */
    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    /**
     * Get a message from the resource bundle with parameters.
     * 
     * @param key the key for the message
     * @param params the parameters to be inserted into the message
     * @return the formatted message in the current locale
     */
    public static String get(String key, Object... params) {
        try {
            String message = bundle.getString(key);
            return String.format(message, params);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }
}
