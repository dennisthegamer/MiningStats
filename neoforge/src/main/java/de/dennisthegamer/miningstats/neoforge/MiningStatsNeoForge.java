package de.dennisthegamer.miningstats.neoforge;

import de.dennisthegamer.miningstats.MiningStatsClient;
import de.dennisthegamer.miningstats.config.ModConfigScreen;
import de.dennisthegamer.miningstats.hud.HudRenderer;
import de.dennisthegamer.miningstats.keybind.KeybindHandler;
import de.dennisthegamer.hudlib.neoforge.HudLibNeoForge;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
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
        modBus.addListener((RegisterGuiLayersEvent event) -> HudLibNeoForge.register(
                event,
                Identifier.fromNamespaceAndPath(MiningStatsClient.MOD_ID, "hud"),
                HudRenderer::render
        ));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) ->
                MiningStatsClient.onTick(Minecraft.getInstance()));
        // Only register the config screen when YACL is present (ModConfigScreen needs it).
        if (ModList.get().isLoaded("yet_another_config_lib_v3")) {
            container.registerExtensionPoint(IConfigScreenFactory.class,
                    (modContainer, parent) -> ModConfigScreen.create(parent));
        }
    }
}
