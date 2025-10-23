package krypton1101.f10tracker;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration management for F10Tracker
 */
public class TrackerConfig {
    private static final String CONFIG_FILE = "f10tracker_config.properties";
    private static final String DEFAULT_SERVER = "ws://localhost:8080/ws";
    private static final boolean DEFAULT_WEBSOCKET_ENABLED = false;
    private static final long DEFAULT_LOG_INTERVAL = 1000; // 1 second
    
    private final Properties properties;
    private final File configFile;
    
    public TrackerConfig() {
        this.properties = new Properties();
        this.configFile = new File(CONFIG_FILE);
        loadConfig();
    }
    
    /**
     * Load configuration from file or create default
     */
    private void loadConfig() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                properties.load(reader);
            } catch (IOException e) {
                System.err.println("Failed to load config file: " + e.getMessage());
                createDefaultConfig();
            }
        } else {
            createDefaultConfig();
        }
    }
    
    /**
     * Create default configuration
     */
    private void createDefaultConfig() {
        properties.setProperty("websocket.enabled", String.valueOf(DEFAULT_WEBSOCKET_ENABLED));
        properties.setProperty("websocket.server", DEFAULT_SERVER);
        properties.setProperty("logging.interval", String.valueOf(DEFAULT_LOG_INTERVAL));
        saveConfig();
    }
    
    /**
     * Save configuration to file
     */
    public void saveConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            properties.store(writer, "F10Tracker Configuration");
        } catch (IOException e) {
            System.err.println("Failed to save config file: " + e.getMessage());
        }
    }
    
    /**
     * Get WebSocket server address
     */
    public String getWebSocketServer() {
        return properties.getProperty("websocket.server", DEFAULT_SERVER);
    }
    
    /**
     * Set WebSocket server address
     */
    public void setWebSocketServer(String server) {
        properties.setProperty("websocket.server", server);
        saveConfig();
    }
    
    /**
     * Check if WebSocket is enabled
     */
    public boolean isWebSocketEnabled() {
        return Boolean.parseBoolean(properties.getProperty("websocket.enabled", String.valueOf(DEFAULT_WEBSOCKET_ENABLED)));
    }
    
    /**
     * Enable or disable WebSocket
     */
    public void setWebSocketEnabled(boolean enabled) {
        properties.setProperty("websocket.enabled", String.valueOf(enabled));
        saveConfig();
    }
    
    /**
     * Get logging interval in milliseconds
     */
    public long getLogInterval() {
        return Long.parseLong(properties.getProperty("logging.interval", String.valueOf(DEFAULT_LOG_INTERVAL)));
    }
    
    /**
     * Set logging interval in milliseconds
     */
    public void setLogInterval(long interval) {
        properties.setProperty("logging.interval", String.valueOf(interval));
        saveConfig();
    }
}
