package com.miningstats.keybind;

import com.miningstats.data.OreType;
import com.miningstats.data.SessionData;
import com.miningstats.config.ModConfig;
import com.miningstats.hud.HudEffects;
import com.miningstats.hud.HudRenderer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {

    private static KeyBinding compactKey;
    private static KeyBinding resetKey;
    private static KeyBinding toggleSessionKey;

    public static void register() {
        compactKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.miningstats.compact",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.miningstats"
        ));

        resetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.miningstats.reset",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.miningstats"
        ));

        toggleSessionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.miningstats.toggle_session",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.miningstats"
        ));
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null) return;

        while (compactKey.wasPressed()) {
            HudRenderer.toggleCompactMode();
        }

        while (resetKey.wasPressed()) {
            SessionData session = SessionData.getInstance();
            sendSessionSummary(client);
            session.reset();
            HudEffects.triggerResetMessage();

            client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }

        while (toggleSessionKey.wasPressed()) {
            SessionData session = SessionData.getInstance();
            if (session.isActive()) {
                session.pause();
                client.player.sendMessage(
                        Text.translatable("miningstats.session.paused")
                                .styled(style -> style.withColor(0xFFAA00)),
                        true
                );
            } else {
                session.start();
                client.player.sendMessage(
                        Text.translatable("miningstats.session.started")
                                .styled(style -> style.withColor(0x55FF55)),
                        true
                );
            }
        }
    }

    public static void sendSessionSummary(MinecraftClient client) {
        if (client.player == null) return;

        ModConfig config = ModConfig.getInstance();
        if (!config.showSessionSummary) return;

        SessionData session = SessionData.getInstance();

        // Header
        client.player.sendMessage(Text.literal(""), false);
        client.player.sendMessage(
                Text.translatable("miningstats.session.summary_header")
                        .styled(style -> style.withColor(0xFFD700).withBold(true)),
                false
        );

        // Duration
        client.player.sendMessage(
                Text.translatable("miningstats.session.duration", session.getFormattedDuration())
                        .styled(style -> style.withColor(0xFFFFFF)),
                false
        );

        // Total ores
        client.player.sendMessage(
                Text.translatable("miningstats.session.ores_mined", session.getTotalOres())
                        .styled(style -> style.withColor(0xFFFFFF)),
                false
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
            client.player.sendMessage(
                    Text.literal(breakdown.toString())
                            .styled(style -> style.withColor(0xAAAAAA)),
                    false
            );
        }

        // Fortune bonus
        client.player.sendMessage(
                Text.translatable("miningstats.session.fortune_bonus", session.getTotalFortuneBonus())
                        .styled(style -> style.withColor(0xFFD700)),
                false
        );
    }
}
