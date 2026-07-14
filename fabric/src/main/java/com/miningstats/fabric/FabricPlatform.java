package com.miningstats.fabric;

import com.miningstats.platform.Platform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.nio.file.Path;

public final class FabricPlatform implements Platform {

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public String blockId(Block block) {
        // Remapped to intermediary on Fabric, stable across 1.21.9-1.21.11.
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }
}
