package de.dennisthegamer.miningstats.neoforge;

import de.dennisthegamer.miningstats.platform.Platform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public final class NeoForgePlatform implements Platform {

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public String blockId(Block block) {
        // 1.21.6-1.21.8 has no ResourceLocation rename in range, so a direct call is safe.
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }
}
