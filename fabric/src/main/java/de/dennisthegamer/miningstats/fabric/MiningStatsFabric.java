package de.dennisthegamer.miningstats.fabric;

import de.dennisthegamer.miningstats.MiningStatsClient;
import de.dennisthegamer.miningstats.hud.HudRenderer;
import de.dennisthegamer.miningstats.keybind.KeybindHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

/** Fabric client entrypoint - wiring only, all logic lives in the shared sources. */
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
