package krypton1101.f10tracker;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neovisionaries.ws.client.WebSocket;

public class F10tracker implements ModInitializer {
	public static final String MOD_ID = "f10tracker";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("F10Tracker mod initialized - Position and velocity logging ready!");
		LOGGER.info("Use F9 to start logging, F10 to stop logging, F11 to toggle logging");
	}
}