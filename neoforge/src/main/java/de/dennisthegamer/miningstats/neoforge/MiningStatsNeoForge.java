package de.dennisthegamer.miningstats.neoforge;

import de.dennisthegamer.miningstats.MiningStatsClient;
import de.dennisthegamer.miningstats.config.ConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** NeoForge client entrypoint - wiring only, all logic lives in the shared sources. */
@Mod(value = MiningStatsClient.MOD_ID, dist = Dist.CLIENT)
public final class MiningStatsNeoForge {

    public MiningStatsNeoForge(ModContainer container) {
        MiningStatsClient.init();

        // Only register the config screen when YACL is present (ConfigScreen needs it).
        if (ModList.get().isLoaded("yet_another_config_lib_v3")) {
            container.registerExtensionPoint(IConfigScreenFactory.class,
                    (modContainer, parent) -> ConfigScreen.create(parent));
        }
    }
}
