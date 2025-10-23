package krypton1101.f10tracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles timed logging of player position and velocity data
 */
public class PositionLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("F10Tracker-PositionLogger");
    private static final String LOG_DIR = "f10tracker_logs";
    private static final String LOG_FILE_PREFIX = "player_data_";
    private static final String LOG_FILE_EXTENSION = ".csv";
    
    private final MinecraftClient client;
    private final ScheduledExecutorService scheduler;
    private final List<PlayerData> dataBuffer;
    private final Object bufferLock = new Object();
    private final WebSocketManager webSocketManager;
    private final TrackerConfig config;
    
    private boolean isLogging = false;
    private long logIntervalMs = 1000; // Default 1 second interval
    private String currentLogFile;
    
    public PositionLogger(MinecraftClient client) {
        this.client = client;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.dataBuffer = new ArrayList<>();
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
        if (isLogging) {
            LOGGER.warn("Position logging is already active");
            return;
        }
        
        this.logIntervalMs = intervalMs;
        this.isLogging = true;
        this.currentLogFile = generateLogFileName();
        
        // Create log directory if it doesn't exist
        createLogDirectory();
        
        // Write CSV header
        writeCSVHeader();
        
        // Schedule periodic logging
        scheduler.scheduleAtFixedRate(this::logCurrentPosition, 0, intervalMs, TimeUnit.MILLISECONDS);
        
        LOGGER.info("Started position logging with interval: {}ms", intervalMs);
    }
    
    /**
     * Stop logging and flush any remaining data
     */
    public void stopLogging() {
        if (!isLogging) {
            LOGGER.warn("Position logging is not active");
            return;
        }
        
        isLogging = false;
        scheduler.shutdown();
        
        // Flush remaining data
        flushDataToFile();
        
        LOGGER.info("Stopped position logging. Data saved to: {}", currentLogFile);
    }
    
    /**
     * Log the current player position and velocity
     */
    private void logCurrentPosition() {
        if (!isLogging || client.player == null) {
            return;
        }
        
        ClientPlayerEntity player = client.player;
        long timestamp = System.currentTimeMillis();

        UUID playerUuid = player.getUuid();
        Vec3d position = player.getPos();
        Vec3d velocity = player.getVelocity();
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        
        PlayerData data = new PlayerData(timestamp, position, velocity, yaw, pitch, playerUuid);
        
        synchronized (bufferLock) {
            dataBuffer.add(data);
        }
        
        // Send to WebSocket if enabled and connected
        if (config.isWebSocketEnabled()) {
            webSocketManager.sendPlayerData(data);
        }
        
        // Log to console for debugging
        LOGGER.debug("Logged position: {}", data);
    }
    
    /**
     * Flush buffered data to file
     */
    private void flushDataToFile() {
        List<PlayerData> dataToWrite;
        
        synchronized (bufferLock) {
            if (dataBuffer.isEmpty()) {
                return;
            }
            dataToWrite = new ArrayList<>(dataBuffer);
            dataBuffer.clear();
        }
        
        try (FileWriter writer = new FileWriter(currentLogFile, true)) {
            for (PlayerData data : dataToWrite) {
                writer.write(dataToCSV(data) + "\n");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to write position data to file", e);
        }
    }
    
    /**
     * Convert PlayerData to CSV format
     */
    private String dataToCSV(PlayerData data) {
        return String.format("%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.3f,%.3f",
                data.getTimestamp(),
                data.getPosition().x, data.getPosition().y, data.getPosition().z,
                data.getVelocity().x, data.getVelocity().y, data.getVelocity().z,
                data.getYaw(), data.getPitch());
    }
    
    /**
     * Write CSV header to the log file
     */
    private void writeCSVHeader() {
        try (FileWriter writer = new FileWriter(currentLogFile)) {
            writer.write("timestamp,pos_x,pos_y,pos_z,vel_x,vel_y,vel_z,yaw,pitch\n");
        } catch (IOException e) {
            LOGGER.error("Failed to write CSV header", e);
        }
    }
    
    /**
     * Create the log directory if it doesn't exist
     */
    private void createLogDirectory() {
        try {
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                LOGGER.info("Created log directory: {}", logDir.toAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create log directory", e);
        }
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
}
