package de.dennisthegamer.miningstats;

import de.dennisthegamer.miningstats.config.ModConfig;
import de.dennisthegamer.miningstats.data.OreRegistry;
import de.dennisthegamer.miningstats.data.SessionData;
import de.dennisthegamer.miningstats.hud.HudEffects;
import de.dennisthegamer.miningstats.hud.HudRenderer;
import de.dennisthegamer.miningstats.keybind.KeybindHandler;
import de.dennisthegamer.miningstats.tracker.FortuneTracker;
import de.dennisthegamer.miningstats.tracker.OreTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.BOSS_BAR,
				Identifier.fromNamespaceAndPath("miningstats", "hud"),
				(graphics, deltaTracker) -> HudRenderer.render(graphics, deltaTracker)
		);

		// Register tick handler
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Keybind handling
			KeybindHandler.tick(client);

			// Fortune tracker tick (delayed drop counting)
			FortuneTracker.tick();

			// HUD effects tick (flash, reset message)
			HudEffects.tick();

			// Session lifecycle: detect world join/leave
			boolean inWorld = client.level != null && client.player != null;

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
					client.player.sendSystemMessage(
							Component.translatable("miningstats.session.restored")
									.withStyle(style -> style.withColor(0x55FF55))
					);
				} else {
					client.player.sendSystemMessage(
							Component.translatable("miningstats.hint")
									.withStyle(style -> style.withColor(0xFFD700))
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
