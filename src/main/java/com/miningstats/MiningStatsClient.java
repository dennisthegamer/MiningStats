package com.miningstats;

import com.miningstats.config.ModConfig;
import com.miningstats.data.OreRegistry;
import com.miningstats.data.SessionData;
import com.miningstats.hud.HudEffects;
import com.miningstats.hud.HudRenderer;
import com.miningstats.keybind.KeybindHandler;
import com.miningstats.tracker.FortuneTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiningStatsClient implements ClientModInitializer {

	public static final String MOD_ID = "miningstats";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private boolean wasInWorld = false;

	@Override
	public void onInitializeClient() {
		LOGGER.info("MiningStats loaded!");

		// Load config
		ModConfig config = ModConfig.getInstance();

		// Load custom ores from config
		OreRegistry.loadCustomOres(config.trackedOres);

		// Register keybinds
		KeybindHandler.register();

		// Register HUD renderer
		HudRenderCallback.EVENT.register(HudRenderer::render);

		// Register tick handler
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Keybind handling
			KeybindHandler.tick(client);

			// Fortune tracker tick (delayed drop counting)
			FortuneTracker.tick();

			// HUD effects tick (flash, reset message)
			HudEffects.tick();

			// Session lifecycle: detect world join/leave
			boolean inWorld = client.world != null && client.player != null;

			if (inWorld && !wasInWorld) {
				// Just joined a world — start session
				SessionData.getInstance().reset();
				LOGGER.info("Session started");
			} else if (!inWorld && wasInWorld) {
				// Just left a world — show summary
				if (client.player != null) {
					KeybindHandler.sendSessionSummary(client);
				}
				LOGGER.info("Session ended");
			}

			wasInWorld = inWorld;
		});
	}
}
