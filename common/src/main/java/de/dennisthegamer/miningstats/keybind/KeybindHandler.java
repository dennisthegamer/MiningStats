package de.dennisthegamer.miningstats.keybind;

import de.dennisthegamer.miningstats.data.OreType;
import de.dennisthegamer.miningstats.data.SessionData;
import de.dennisthegamer.miningstats.config.ModConfig;
import de.dennisthegamer.miningstats.hud.HudEffects;
import de.dennisthegamer.miningstats.hud.HudRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

/**
 * Keybinds use a vanilla {@link KeyMapping.Category} on purpose: a custom category needs the
 * Identifier class, whose mojmap name differs between 1.21.10 and 1.21.11, which would crash
 * the single NeoForge jar on 1.21.9/1.21.10.
 */
public class KeybindHandler {

    private static final String CATEGORY = "key.categories.misc";

    private static KeyMapping compactKey;
    private static KeyMapping resetKey;
    private static KeyMapping toggleSessionKey;

    public static void register() {
        compactKey = new KeyMapping("key.miningstats.compact",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY);
        resetKey = new KeyMapping("key.miningstats.reset",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);
        toggleSessionKey = new KeyMapping("key.miningstats.toggle_session",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);

        KeyMappingRegistry.register(compactKey);
        KeyMappingRegistry.register(resetKey);
        KeyMappingRegistry.register(toggleSessionKey);
    }

    public static void tick(Minecraft client) {
        if (client.player == null) return;

        while (compactKey.consumeClick()) {
            HudRenderer.toggleCompactMode();
        }

        while (resetKey.consumeClick()) {
            SessionData session = SessionData.getInstance();
            sendSessionSummary(client);
            session.reset();
            session.deleteSavedSession();
            HudEffects.triggerResetMessage();

            client.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }

        while (toggleSessionKey.consumeClick()) {
            SessionData session = SessionData.getInstance();
            if (session.isActive()) {
                session.pause();
                client.player.displayClientMessage(
                        Component.translatable("miningstats.session.paused")
                                .withStyle(style -> style.withColor(0xFFAA00)),
                        true
                );
            } else {
                session.start();
                client.player.displayClientMessage(
                        Component.translatable("miningstats.session.started")
                                .withStyle(style -> style.withColor(0x55FF55)),
                        true
                );
            }
        }
    }

    public static void sendSessionSummary(Minecraft client) {
        if (client.player == null) return;

        ModConfig config = ModConfig.getInstance();
        if (!config.showSessionSummary) return;

        SessionData session = SessionData.getInstance();

        // Header
        client.player.displayClientMessage(Component.literal(""), false);
        client.player.displayClientMessage(
                Component.translatable("miningstats.session.summary_header")
                        .withStyle(style -> style.withColor(0xFFD700).withBold(true)),
                false
        );

        // Duration
        client.player.displayClientMessage(
                Component.translatable("miningstats.session.duration", session.getFormattedDuration())
                        .withStyle(style -> style.withColor(0xFFFFFF)),
                false
        );

        // Total ores
        client.player.displayClientMessage(
                Component.translatable("miningstats.session.ores_mined", session.getTotalOres())
                        .withStyle(style -> style.withColor(0xFFFFFF)),
                false
        );

        // Per-ore breakdown (respect mergeDeepslate config)
        Map<OreType, Integer> displayCounts = config.mergeDeepslate
                ? session.getMergedOreCounts()
                : session.getOreCounts();
        StringBuilder breakdown = new StringBuilder("  ");
        boolean first = true;
        for (Map.Entry<OreType, Integer> entry : displayCounts.entrySet()) {
            int count = entry.getValue();
            if (count <= 0) continue;
            if (!first) breakdown.append(" | ");
            breakdown.append(entry.getKey().getDisplayName()).append(": ").append(count);
            first = false;
        }
        if (!first) {
            client.player.displayClientMessage(
                    Component.literal(breakdown.toString())
                            .withStyle(style -> style.withColor(0xAAAAAA)),
                    false
            );
        }

        // Fortune bonus
        client.player.displayClientMessage(
                Component.translatable("miningstats.session.fortune_bonus", session.getTotalFortuneBonus())
                        .withStyle(style -> style.withColor(0xFFD700)),
                false
        );
    }
}
