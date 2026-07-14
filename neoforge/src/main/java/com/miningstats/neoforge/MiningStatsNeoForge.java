package com.miningstats.neoforge;

import com.miningstats.MiningStatsClient;
import com.miningstats.config.ModConfigScreen;
import com.miningstats.hud.HudRenderer;
import com.miningstats.keybind.KeybindHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = MiningStatsClient.MOD_ID, dist = Dist.CLIENT)
public final class MiningStatsNeoForge {
    public MiningStatsNeoForge(ModContainer container, IEventBus modBus) {
        MiningStatsClient.init();
        modBus.addListener((RegisterKeyMappingsEvent event) -> {
            event.registerCategory(KeybindHandler.CATEGORY);
            event.register(KeybindHandler.compactKey);
            event.register(KeybindHandler.resetKey);
            event.register(KeybindHandler.toggleSessionKey);
        });
        modBus.addListener((RegisterGuiLayersEvent event) -> event.registerAbove(
                VanillaGuiLayers.BOSS_OVERLAY,
                Identifier.fromNamespaceAndPath(MiningStatsClient.MOD_ID, "hud"),
                (graphics, deltaTracker) -> HudRenderer.render(graphics, deltaTracker)
        ));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) ->
                MiningStatsClient.onTick(Minecraft.getInstance()));
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> ModConfigScreen.create(parent));
    }
}
