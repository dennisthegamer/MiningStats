package de.dennisthegamer.miningstats;

import de.dennisthegamer.miningstats.config.ModConfig;
import de.dennisthegamer.miningstats.data.OreRegistry;
import de.dennisthegamer.miningstats.data.SessionData;
import de.dennisthegamer.miningstats.hud.HudRenderer;
import de.dennisthegamer.miningstats.keybind.KeybindHandler;
import de.dennisthegamer.miningstats.tracker.FortuneTracker;
import de.dennisthegamer.miningstats.tracker.OreTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared (loader-free) client logic. The keybinds, HUD layer and end-of-tick hook are
 * registered by the two loader modules, which delegate to {@link #init()} / {@link #onTick}.
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
        ModConfig config = ModConfig.getInstance();
        OreRegistry.loadCustomOres(config.trackedOres);
    }

    /** End-of-client-tick hook, wired up by the loader modules. */
    public static void onTick(Minecraft client) {
        KeybindHandler.tick(client);
        FortuneTracker.tick();
        HudRenderer.tickEffects();

        boolean inWorld = client.level != null && client.player != null;

        if (inWorld && !wasInWorld) {
            SessionData session = SessionData.getInstance();
            session.reset();
            if (ModConfig.getInstance().persistSessions && session.loadFromDisk()) {
                LOGGER.info("World joined -- restored saved session");
            } else {
                LOGGER.info("World joined -- session ready to start");
            }
            hintShown = false;
        } else if (inWorld && !hintShown && client.player != null) {
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
