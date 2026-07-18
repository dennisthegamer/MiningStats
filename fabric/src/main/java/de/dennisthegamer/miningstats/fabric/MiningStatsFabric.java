package de.dennisthegamer.miningstats.fabric;

import de.dennisthegamer.hudlib.fabric.HudLibFabric;
import de.dennisthegamer.miningstats.MiningStatsClient;
import de.dennisthegamer.miningstats.hud.HudRenderer;
import de.dennisthegamer.miningstats.keybind.KeybindHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.resources.Identifier;

public final class MiningStatsFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MiningStatsClient.init();
        KeyMappingHelper.registerKeyMapping(KeybindHandler.compactKey);
        KeyMappingHelper.registerKeyMapping(KeybindHandler.resetKey);
        KeyMappingHelper.registerKeyMapping(KeybindHandler.toggleSessionKey);
        HudLibFabric.register(
                Identifier.fromNamespaceAndPath(MiningStatsClient.MOD_ID, "hud"),
                HudRenderer::render
        );
        ClientTickEvents.END_CLIENT_TICK.register(MiningStatsClient::onTick);
    }
}
