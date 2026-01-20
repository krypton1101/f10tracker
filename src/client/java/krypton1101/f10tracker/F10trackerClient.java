package krypton1101.f10tracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class F10trackerClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("F10Tracker-Client");
	
	private static PositionLogger positionLogger;
	private static KeyBinding connectWebSocketKey;
	private static KeyBinding disconnectWebSocketKey;
	private static KeyBinding toggleWebSocketKey;
	
	@Override
	public void onInitializeClient() {
		// Initialize position logger
		positionLogger = new PositionLogger(MinecraftClient.getInstance());
		
		// Register key bindings
		registerKeyBindings();
		
		// Register tick event for handling key presses
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
		
		// Register chat message event listener
		ClientReceiveMessageEvents.GAME.register(this::onChatMessage);
		
		LOGGER.info("F10Tracker client initialized with chat message processing capabilities");
	}
	
	private void onChatMessage(Text message, boolean overlay) {
		String messageText = message.getString();
		LOGGER.info("Received chat message: {}", messageText);
		
		// Process "Start!" message
		if ("Start!".equals(messageText)) {
			// Send start event with player's own UUID
			if (MinecraftClient.getInstance().player != null) {
				LapEvent startEvent = new LapEvent("0", System.currentTimeMillis(), true);
				if (positionLogger.isWebSocketConnected()) {
					positionLogger.getWebSocketManager().sendLapEvent(startEvent);
				}
			}
			return;
		}
		
		// Process "Player {UUID} finished lap." message
		if (messageText.startsWith("Player ") && messageText.endsWith(" finished lap.")) {
			// Extract UUID from message
			String uuidPart = messageText.substring(7, messageText.length() - 14); // "Player ".length() = 7, " finished lap.".length() = 14
			try {
				// Validate UUID format
				UUID.fromString(uuidPart);
				LapEvent lapEvent = new LapEvent(uuidPart, System.currentTimeMillis(), false);
				if (positionLogger.isWebSocketConnected()) {
					positionLogger.getWebSocketManager().sendLapEvent(lapEvent);
				}
			} catch (IllegalArgumentException e) {
				LOGGER.warn("Invalid UUID in chat message: {}", messageText);
			}
		}
	}
	
	private void registerKeyBindings() {
		connectWebSocketKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.f10tracker.connect_websocket",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN,
				"category.f10tracker.websocket"
		));
		
		disconnectWebSocketKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.f10tracker.disconnect_websocket",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN,
				"category.f10tracker.websocket"
		));
		
		toggleWebSocketKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.f10tracker.toggle_websocket",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F9,
				"category.f10tracker.websocket"
		));
	}
	
	private void onClientTick(MinecraftClient client) {
		if (client.player == null) return;
		
		// WebSocket key handling
		if (connectWebSocketKey.wasPressed()) {
			String serverAddress = positionLogger.getConfig().getWebSocketServer();
			if (positionLogger.connectWebSocket(serverAddress)) {
				client.player.sendMessage(net.minecraft.text.Text.literal("Connecting to WebSocket server: " + serverAddress), false);
			} else {
				client.player.sendMessage(net.minecraft.text.Text.literal("Failed to connect to WebSocket server"), false);
			}
		}
		
		if (disconnectWebSocketKey.wasPressed()) {
			positionLogger.disconnectWebSocket();
			client.player.sendMessage(net.minecraft.text.Text.literal("Disconnected from WebSocket server"), false);
		}
		
		if (toggleWebSocketKey.wasPressed()) {
			if (positionLogger.isWebSocketConnected()) {
				positionLogger.disconnectWebSocket();
				client.player.sendMessage(net.minecraft.text.Text.literal("Disconnected from WebSocket server"), false);
			} else {
				String serverAddress = positionLogger.getConfig().getWebSocketServer();
				if (positionLogger.connectWebSocket(serverAddress)) {
					client.player.sendMessage(net.minecraft.text.Text.literal("Connecting to WebSocket server: " + serverAddress), false);
				} else {
					client.player.sendMessage(net.minecraft.text.Text.literal("Failed to connect to WebSocket server"), false);
				}
			}
		}
	}
	
	public static PositionLogger getPositionLogger() {
		return positionLogger;
	}
}