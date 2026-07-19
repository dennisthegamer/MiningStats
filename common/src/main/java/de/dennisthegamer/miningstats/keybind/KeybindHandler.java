package de.dennisthegamer.miningstats.keybind;

import de.dennisthegamer.miningstats.data.OreType;
import de.dennisthegamer.miningstats.data.SessionData;
import de.dennisthegamer.miningstats.config.ModConfig;
import de.dennisthegamer.miningstats.hud.HudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {

    // Created in common; each loader module registers them (Fabric KeyMappingHelper /
    // NeoForge RegisterKeyMappingsEvent) from its client entrypoint.
    public static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("miningstats", "miningstats"));

    public static final KeyMapping compactKey = new KeyMapping(
            "key.miningstats.compact", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY);
    public static final KeyMapping resetKey = new KeyMapping(
            "key.miningstats.reset", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);
    public static final KeyMapping toggleSessionKey = new KeyMapping(
            "key.miningstats.toggle_session", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);

    public static void tick(Minecraft client) {
        if (client.player == null) return;

        while (compactKey.consumeClick()) {
            HudRenderer.toggleCompactMode();
        }

        while (resetKey.consumeClick()) {
            SessionData session = SessionData.getInstance();
            sendSessionSummary(client);
            session.resetKeepingRunState();
            session.deleteSavedSession();
            HudRenderer.triggerResetMessage();

            client.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }

        while (toggleSessionKey.consumeClick()) {
            SessionData session = SessionData.getInstance();
            if (session.isActive()) {
                session.pause();
                client.player.sendOverlayMessage(
                        Component.translatable("miningstats.session.paused")
                                .withStyle(style -> style.withColor(0xFFAA00))
                );
            } else {
                session.start();
                client.player.sendOverlayMessage(
                        Component.translatable("miningstats.session.started")
                                .withStyle(style -> style.withColor(0x55FF55))
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
        client.player.sendSystemMessage(Component.literal(""));
        client.player.sendSystemMessage(
                Component.translatable("miningstats.session.summary_header")
                        .withStyle(style -> style.withColor(0xFFD700).withBold(true))
        );

        // Duration
        client.player.sendSystemMessage(
                Component.translatable("miningstats.session.duration", session.getFormattedDuration())
                        .withStyle(style -> style.withColor(0xFFFFFF))
        );

        // Total ores
        client.player.sendSystemMessage(
                Component.translatable("miningstats.session.ores_mined", session.getTotalOres())
                        .withStyle(style -> style.withColor(0xFFFFFF))
        );

        // Per-ore breakdown (respect mergeDeepslate config)
        java.util.Map<OreType, Integer> displayCounts = config.mergeDeepslate
                ? session.getMergedOreCounts()
                : session.getOreCounts();
        StringBuilder breakdown = new StringBuilder("  ");
        boolean first = true;
        for (java.util.Map.Entry<OreType, Integer> entry : displayCounts.entrySet()) {
            int count = entry.getValue();
            if (count <= 0) continue;
            if (!first) breakdown.append(" | ");
            breakdown.append(entry.getKey().getDisplayName()).append(": ").append(count);
            first = false;
        }
        if (!first) {
            client.player.sendSystemMessage(
                    Component.literal(breakdown.toString())
                            .withStyle(style -> style.withColor(0xAAAAAA))
            );
        }

        // Fortune bonus
        client.player.sendSystemMessage(
                Component.translatable("miningstats.session.fortune_bonus", session.getTotalFortuneBonus())
                        .withStyle(style -> style.withColor(0xFFD700))
        );
    }
}
