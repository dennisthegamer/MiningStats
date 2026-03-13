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
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null) return;

        while (compactKey.wasPressed()) {
            HudRenderer.toggleCompactMode();
        }

        while (resetKey.wasPressed()) {
            sendSessionSummary(client);
            SessionData.getInstance().reset();
            HudEffects.triggerResetMessage();

            // Play sound
            client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
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
                Text.literal("\u26CF MiningStats \u2014 Session Zusammenfassung")
                        .styled(style -> style.withColor(0xFFD700).withBold(true)),
                false
        );

        // Duration
        client.player.sendMessage(
                Text.literal("Dauer: " + session.getFormattedDuration())
                        .styled(style -> style.withColor(0xFFFFFF)),
                false
        );

        // Total ores
        client.player.sendMessage(
                Text.literal("Abgebaute Erze: " + session.getTotalOres() + " gesamt")
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
                Text.literal("Fortune Bonus: +" + session.getTotalFortuneBonus() + " Items")
                        .styled(style -> style.withColor(0xFFD700)),
                false
        );
    }
}
