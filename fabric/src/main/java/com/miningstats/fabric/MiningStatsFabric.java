package com.miningstats.fabric;

import com.miningstats.MiningStatsClient;
import com.miningstats.hud.HudRenderer;
import com.miningstats.keybind.KeybindHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public final class MiningStatsFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MiningStatsClient.init();
        KeyMappingHelper.registerKeyMapping(KeybindHandler.compactKey);
        KeyMappingHelper.registerKeyMapping(KeybindHandler.resetKey);
        KeyMappingHelper.registerKeyMapping(KeybindHandler.toggleSessionKey);
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.BOSS_BAR,
                Identifier.fromNamespaceAndPath(MiningStatsClient.MOD_ID, "hud"),
                (graphics, deltaTracker) -> HudRenderer.render(graphics, deltaTracker)
        );
        ClientTickEvents.END_CLIENT_TICK.register(MiningStatsClient::onTick);
    }
}
