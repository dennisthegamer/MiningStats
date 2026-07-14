package de.dennisthegamer.miningstats.neoforge;

import de.dennisthegamer.miningstats.MiningStatsClient;
import de.dennisthegamer.miningstats.config.ConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** NeoForge client entrypoint - wiring only, all logic lives in the shared sources. */
@Mod(value = MiningStatsClient.MOD_ID, dist = Dist.CLIENT)
public final class MiningStatsNeoForge {

    public MiningStatsNeoForge(ModContainer container) {
        MiningStatsClient.init();

        // Config screen (equivalent of the ModMenu integration on Fabric; needs Cloth Config)
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> ConfigScreen.create(parent));
    }
}
