package de.dennisthegamer.miningstats;

import de.dennisthegamer.miningstats.config.ModConfig;
import de.dennisthegamer.miningstats.data.OreRegistry;
import de.dennisthegamer.miningstats.data.SessionData;
import de.dennisthegamer.miningstats.hud.HudEffects;
import de.dennisthegamer.miningstats.hud.HudRenderer;
import de.dennisthegamer.miningstats.keybind.KeybindHandler;
import de.dennisthegamer.miningstats.tracker.FortuneTracker;
import de.dennisthegamer.miningstats.tracker.OreTracker;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared (loader-free) client logic. Keybinds, the HUD layer and the tick hook are registered
 * through Architectury API so the same code runs on Fabric and NeoForge.
 */
public final class MiningStatsClient {

    public static final String MOD_ID = "miningstats";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean wasInWorld = false;
    private static boolean hintShown = false;

    private MiningStatsClient() {
    }

    /** Called once from each loader's client entrypoint. */
    public static void init() {
        LOGGER.info("MiningStats loaded!");

        // Load config
        ModConfig config = ModConfig.getInstance();

        // Load custom ores from config
        OreRegistry.loadCustomOres(config.trackedOres);

        // Register keybinds (Architectury handles both loaders)
        KeybindHandler.register();

        // Register HUD renderer (Architectury RENDER_HUD is cross-version safe)
        ClientGuiEvent.RENDER_HUD.register(HudRenderer::render);

        // Register tick handler
        ClientTickEvent.CLIENT_POST.register(MiningStatsClient::onTick);
    }

    /** End-of-client-tick hook. */
    public static void onTick(Minecraft client) {
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
                client.player.displayClientMessage(
                        Component.translatable("miningstats.session.restored")
                                .withStyle(style -> style.withColor(0x55FF55)),
                        false
                );
            } else {
                client.player.displayClientMessage(
                        Component.translatable("miningstats.hint")
                                .withStyle(style -> style.withColor(0xFFD700)),
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
    }
}
