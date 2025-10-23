package krypton1101.f10tracker;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages WebSocket connection to send PlayerData to a remote server
 */
public class WebSocketManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("F10Tracker-WebSocket");
    
    private final MinecraftClient client;
    private final BlockingQueue<PlayerData> dataQueue;
    private final AtomicBoolean isConnected;
    private final AtomicBoolean shouldReconnect;
    
    private WebSocket webSocket;
    private String serverAddress;
    private int reconnectAttempts;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_DELAY_MS = 5000; // 5 seconds
    
    public WebSocketManager(MinecraftClient client) {
        this.client = client;
        this.dataQueue = new LinkedBlockingQueue<>();
        this.isConnected = new AtomicBoolean(false);
        this.shouldReconnect = new AtomicBoolean(false);
        this.reconnectAttempts = 0;
    }
    
    /**
     * Connect to the specified WebSocket server
     */
    public boolean connect(String serverAddress) {
        this.serverAddress = serverAddress;
        this.shouldReconnect.set(true);
        
        try {
            webSocket = new WebSocketFactory()
                    .createSocket(serverAddress)
                    .addListener(new WebSocketAdapter() {
                        @Override
                        public void onConnected(WebSocket websocket, java.util.Map<String, java.util.List<String>> headers) {
                            LOGGER.info("Connected to WebSocket server: {}", serverAddress);
                            isConnected.set(true);
                            reconnectAttempts = 0;
                            sendMessageToPlayer("Connected to tracking server");
                        }
                        
                        
                        @Override
                        public void onError(WebSocket websocket, WebSocketException cause) {
                            LOGGER.error("WebSocket error: {}", cause.getMessage(), cause);
                            isConnected.set(false);
                            sendMessageToPlayer("WebSocket connection error: " + cause.getMessage());
                            
                            if (shouldReconnect.get() && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                                scheduleReconnect();
                            } else if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                                sendMessageToPlayer("Failed to reconnect to tracking server after " + MAX_RECONNECT_ATTEMPTS + " attempts");
                                shouldReconnect.set(false);
                            }
                        }
                        
                        @Override
                        public void onTextMessage(WebSocket websocket, String text) {
                            LOGGER.debug("Received message from server: {}", text);
                        }
                    })
                    .connect();
            
            return true;
        } catch (IOException | WebSocketException e) {
            LOGGER.error("Failed to connect to WebSocket server: {}", e.getMessage(), e);
            sendMessageToPlayer("Failed to connect to tracking server: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Disconnect from the WebSocket server
     */
    public void disconnect() {
        shouldReconnect.set(false);
        isConnected.set(false);
        
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.disconnect();
        }
        
        LOGGER.info("Disconnected from WebSocket server");
        sendMessageToPlayer("Disconnected from tracking server");
    }
    
    /**
     * Send PlayerData to the server
     */
    public void sendPlayerData(PlayerData data) {
        if (!isConnected.get() || webSocket == null || !webSocket.isOpen()) {
            // Queue data for later sending if not connected
            if (shouldReconnect.get()) {
                dataQueue.offer(data);
            }
            return;
        }
        
        try {
            String jsonData = dataToJson(data);
            webSocket.sendText(jsonData);
            LOGGER.debug("Sent player data to server: {}", data);
        } catch (Exception e) {
            LOGGER.error("Failed to send player data: {}", e.getMessage(), e);
            sendMessageToPlayer("Failed to send data to tracking server");
        }
    }
    
    /**
     * Process queued data when connection is restored
     */
    public void processQueuedData() {
        if (!isConnected.get()) return;
        
        PlayerData data;
        while ((data = dataQueue.poll()) != null) {
            sendPlayerData(data);
        }
    }
    
    /**
     * Schedule a reconnection attempt
     */
    private void scheduleReconnect() {
        reconnectAttempts++;
        LOGGER.info("Scheduling reconnection attempt {} in {}ms", reconnectAttempts, RECONNECT_DELAY_MS);
        
        new Thread(() -> {
            try {
                Thread.sleep(RECONNECT_DELAY_MS);
                if (shouldReconnect.get()) {
                    LOGGER.info("Attempting to reconnect to WebSocket server...");
                    connect(serverAddress);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Reconnection thread interrupted");
            }
        }).start();
    }
    
    /**
     * Convert PlayerData to JSON format
     */
    private String dataToJson(PlayerData data) {
        return String.format(
            "{\"UUID\":\"%s\",\"timestamp\":%d,\"position\":{\"x\":%.6f,\"y\":%.6f,\"z\":%.6f},\"velocity\":{\"x\":%.6f,\"y\":%.6f,\"z\":%.6f},\"yaw\":%.3f,\"pitch\":%.3f}",
            data.getPlayerUuid(),
            data.getTimestamp(),
            data.getPosition().x, data.getPosition().y, data.getPosition().z,
            data.getVelocity().x, data.getVelocity().y, data.getVelocity().z,
            data.getYaw(), data.getPitch()
        );
    }
    
    /**
     * Send a message to the player
     */
    private void sendMessageToPlayer(String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[F10Tracker] " + message), false);
        }
    }
    
    /**
     * Check if connected to the server
     */
    public boolean isConnected() {
        return isConnected.get() && webSocket != null && webSocket.isOpen();
    }
    
    /**
     * Get the current server address
     */
    public String getServerAddress() {
        return serverAddress;
    }
    
    /**
     * Get the number of queued data items
     */
    public int getQueuedDataCount() {
        return dataQueue.size();
    }
}
