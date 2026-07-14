package com.miningstats.neoforge;

import com.miningstats.platform.Platform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * NeoForge runs on the runtime version's mojmap names. {@code Registry.getKey} returns the
 * resource-id class, renamed between 1.21.10 ({@code ResourceLocation}) and 1.21.11
 * ({@code Identifier}); this one jar covers 1.21.9-1.21.11, so the call is resolved
 * reflectively. The method name ({@code getKey}) stayed stable - only its return class changed.
 */
public final class NeoForgePlatform implements Platform {

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public String blockId(Block block) {
        try {
            Object id = BuiltInRegistries.BLOCK.getClass()
                    .getMethod("getKey", Object.class)
                    .invoke(BuiltInRegistries.BLOCK, block);
            return String.valueOf(id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("blockId failed", e);
        }
    }
}
