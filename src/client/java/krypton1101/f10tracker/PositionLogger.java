package krypton1101.f10tracker;

import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles processing of chat messages for lap events and WebSocket communication
 */
public class PositionLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("F10Tracker-PositionLogger");
    private static final String LOG_DIR = "f10tracker_logs";
    private static final String LOG_FILE_PREFIX = "player_data_";
    private static final String LOG_FILE_EXTENSION = ".csv";
    
    private final WebSocketManager webSocketManager;
    private final TrackerConfig config;
    
    private boolean isLogging = false;
    private long logIntervalMs = 1000; // Default 1 second interval
    private String currentLogFile;
    
    public PositionLogger(MinecraftClient client) {
        this.webSocketManager = new WebSocketManager(client);
        this.config = new TrackerConfig();
        this.currentLogFile = generateLogFileName();
        
        // Initialize WebSocket connection if enabled
        if (config.isWebSocketEnabled()) {
            webSocketManager.connect(config.getWebSocketServer());
        }
    }
    
    /**
     * Start logging player position and velocity at the specified interval
     */
    public void startLogging(long intervalMs) {
        // Position logging is no longer needed, only processing chat messages
        LOGGER.info("Position logging is disabled. Only processing chat messages for lap events.");
    }
    
    /**
     * Stop logging and flush any remaining data
     */
    public void stopLogging() {
        // Position logging is no longer needed, only processing chat messages
        LOGGER.info("Position logging is disabled. Only processing chat messages for lap events.");
    }
    
    /**
     * Generate a unique log file name with timestamp
     */
    private String generateLogFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return LOG_DIR + "/" + LOG_FILE_PREFIX + timestamp + LOG_FILE_EXTENSION;
    }
    
    /**
     * Check if logging is currently active
     */
    public boolean isLogging() {
        return isLogging;
    }
    
    /**
     * Get the current log file path
     */
    public String getCurrentLogFile() {
        return currentLogFile;
    }
    
    /**
     * Get the current logging interval in milliseconds
     */
    public long getLogIntervalMs() {
        return logIntervalMs;
    }
    
    /**
     * Connect to WebSocket server
     */
    public boolean connectWebSocket(String serverAddress) {
        config.setWebSocketServer(serverAddress);
        config.setWebSocketEnabled(true);
        return webSocketManager.connect(serverAddress);
    }
    
    /**
     * Disconnect from WebSocket server
     */
    public void disconnectWebSocket() {
        config.setWebSocketEnabled(false);
        webSocketManager.disconnect();
    }
    
    /**
     * Check if WebSocket is connected
     */
    public boolean isWebSocketConnected() {
        return webSocketManager.isConnected();
    }
    
    /**
     * Get WebSocket server address
     */
    public String getWebSocketServer() {
        return webSocketManager.getServerAddress();
    }
    
    /**
     * Get the configuration object
     */
    public TrackerConfig getConfig() {
        return config;
    }
    
    public WebSocketManager getWebSocketManager() {
        return webSocketManager;
    }
}
