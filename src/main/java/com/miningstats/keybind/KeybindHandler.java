package com.miningstats.keybind;

import com.miningstats.data.OreType;
import com.miningstats.data.SessionData;
import com.miningstats.config.ModConfig;
import com.miningstats.hud.HudEffects;
import com.miningstats.hud.HudRenderer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {

    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("miningstats", "miningstats"));

    private static KeyMapping compactKey;
    private static KeyMapping resetKey;
    private static KeyMapping toggleSessionKey;

    public static void register() {
        compactKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.miningstats.compact",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                CATEGORY
        ));

        resetKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.miningstats.reset",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                CATEGORY
        ));

        toggleSessionKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.miningstats.toggle_session",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                CATEGORY
        ));
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
            HudEffects.triggerResetMessage();

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

        // Per-ore breakdown
        StringBuilder breakdown = new StringBuilder("  ");
        boolean first = true;
        for (OreType type : OreType.values()) {
            int count = session.getOreCount(type);
            if (count <= 0) continue;
            if (!first) breakdown.append(" | ");
            breakdown.append(type.getDisplayName()).append(": ").append(count);
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
