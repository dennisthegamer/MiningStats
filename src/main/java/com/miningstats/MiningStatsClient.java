package com.miningstats;

import com.miningstats.config.ModConfig;
import com.miningstats.data.OreRegistry;
import com.miningstats.data.SessionData;
import com.miningstats.hud.HudEffects;
import com.miningstats.hud.HudRenderer;
import com.miningstats.keybind.KeybindHandler;
import com.miningstats.tracker.FortuneTracker;
import com.miningstats.tracker.OreTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiningStatsClient implements ClientModInitializer {

	public static final String MOD_ID = "miningstats";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private boolean wasInWorld = false;
	private boolean hintShown = false;

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
				// Just joined a world -- try to restore saved session
				SessionData session = SessionData.getInstance();
				session.reset();
				if (ModConfig.getInstance().persistSessions && session.loadFromDisk()) {
					LOGGER.info("World joined -- restored saved session");
				} else {
					LOGGER.info("World joined -- session ready to start");
				}
				hintShown = false;
			} else if (inWorld && !hintShown && client.player != null) {
				// Show hint once after joining
				SessionData session = SessionData.getInstance();
				if (session.getTotalOres() > 0) {
					client.player.sendMessage(
							Text.translatable("miningstats.session.restored")
									.styled(style -> style.withColor(0x55FF55)),
							false
					);
				} else {
					client.player.sendMessage(
							Text.translatable("miningstats.hint")
									.styled(style -> style.withColor(0xFFD700)),
							false
					);
				}
				hintShown = true;
			} else if (!inWorld && wasInWorld) {
				// Just left a world -- save/summarize session
				SessionData session = SessionData.getInstance();
				if (session.getTotalOres() > 0) {
					if (client.player != null) {
						KeybindHandler.sendSessionSummary(client);
					}
					if (ModConfig.getInstance().persistSessions) {
						session.saveToDisk();
						LOGGER.info("Session saved to disk");
					}
				}
				session.reset();
				OreTracker.invalidateCache();
				LOGGER.info("Session ended");
			}

			wasInWorld = inWorld;
		});
	}
}
