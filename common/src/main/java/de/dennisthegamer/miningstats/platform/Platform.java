package de.dennisthegamer.miningstats.platform;

import net.minecraft.world.level.block.Block;

import java.nio.file.Path;

/**
 * Loader abstraction: everything the shared code needs from Fabric Loader / NeoForge.
 * One implementation per loader module, registered via {@code META-INF/services}.
 *
 * <p>{@link #blockId} exists because the resource-id class was renamed between 1.21.10
 * ({@code ResourceLocation}) and 1.21.11 ({@code Identifier}); a NeoForge jar bakes the
 * mojmap name it compiled against, so touching {@code Registry.getKey} in shared code would
 * crash on the other patch. Fabric remaps to intermediary; NeoForge resolves it reflectively.
 */
public interface Platform {

    /** The loader's config directory (usually {@code .minecraft/config}). */
    Path getConfigDir();

    /** {@code "namespace:path"} id of a block's registry entry. */
    String blockId(Block block);
}
