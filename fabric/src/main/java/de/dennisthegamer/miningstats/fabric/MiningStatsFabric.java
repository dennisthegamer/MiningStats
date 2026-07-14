package de.dennisthegamer.miningstats.fabric;

import de.dennisthegamer.miningstats.MiningStatsClient;
import net.fabricmc.api.ClientModInitializer;

/** Fabric client entrypoint - wiring only, all logic lives in the shared sources. */
public final class MiningStatsFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MiningStatsClient.init();
    }
}
