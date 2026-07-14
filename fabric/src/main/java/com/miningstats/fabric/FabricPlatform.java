package com.miningstats.fabric;

import com.miningstats.platform.Platform;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;

public final class FabricPlatform implements Platform {
    @Override public Path getConfigDir() { return FabricLoader.getInstance().getConfigDir(); }
}
