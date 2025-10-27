package krypton1101.f10tracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class F10trackerClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("F10Tracker-Client");
	
	private static PositionLogger positionLogger;
	private static KeyBinding startLoggingKey;
	private static KeyBinding stopLoggingKey;
	private static KeyBinding toggleLoggingKey;
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
		
		// Register tick event for continuous checkpoint checking
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (positionLogger != null && positionLogger.isLogging() && client.player != null && client.world != null) {
				// Perform checkpoint checking on every tick
				if (positionLogger.getConfig().isWebSocketEnabled() && positionLogger.getCheckpointManager().getCheckpointCount() > 0) {
					if (positionLogger.getCheckpointManager().checkPlayerPosition(client.player.getPos())) {
						positionLogger.logCurrentPosition();
					}
				}
			}
		});
		
		LOGGER.info("F10Tracker client initialized with position logging capabilities");
	}
	
	private void registerKeyBindings() {
		startLoggingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.f10tracker.start_logging",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN,
				"category.f10tracker.general"
		));
		
		stopLoggingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.f10tracker.stop_logging",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN,
				"category.f10tracker.general"
		));
		
		toggleLoggingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.f10tracker.toggle_logging",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F10,
				"category.f10tracker.general"
		));
		
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
		
		// Handle key presses
		if (startLoggingKey.wasPressed()) {
			long intervalMs = positionLogger.getConfig().getLogInterval();
			if (!positionLogger.isLogging()) {
				positionLogger.startLogging(intervalMs); // 1 second interval
				client.player.sendMessage(net.minecraft.text.Text.literal("Started position logging"), false);
			} else {
				client.player.sendMessage(net.minecraft.text.Text.literal("Position logging is already active"), false);
			}
		}
		
		if (stopLoggingKey.wasPressed()) {
			if (positionLogger.isLogging()) {
				positionLogger.stopLogging();
				client.player.sendMessage(net.minecraft.text.Text.literal("Stopped position logging"), false);
			} else {
				client.player.sendMessage(net.minecraft.text.Text.literal("Position logging is not active"), false);
			}
		}
		
		if (toggleLoggingKey.wasPressed()) {
			if (positionLogger.isLogging()) {
				positionLogger.stopLogging();
				client.player.sendMessage(net.minecraft.text.Text.literal("Stopped position logging"), false);
			} else {
				long intervalMs = positionLogger.getConfig().getLogInterval();
				positionLogger.startLogging(intervalMs); // 1 second interval
				client.player.sendMessage(net.minecraft.text.Text.literal("Started position logging"), false);
			}
		}
		
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