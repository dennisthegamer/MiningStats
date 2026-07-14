package de.dennisthegamer.miningstats.neoforge;

import de.dennisthegamer.miningstats.platform.Platform;
import net.neoforged.fml.loading.FMLPaths;
import java.nio.file.Path;

public final class NeoForgePlatform implements Platform {
    @Override public Path getConfigDir() { return FMLPaths.CONFIGDIR.get(); }
}
