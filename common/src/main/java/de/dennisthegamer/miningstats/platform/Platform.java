package de.dennisthegamer.miningstats.platform;

import java.nio.file.Path;

/** Loader abstraction; one impl per loader module, registered via META-INF/services. */
public interface Platform {
    /** The loader's config directory (usually .minecraft/config). */
    Path getConfigDir();
}
