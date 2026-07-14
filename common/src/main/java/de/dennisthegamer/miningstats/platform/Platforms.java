package de.dennisthegamer.miningstats.platform;

import java.util.ServiceLoader;

/** Holds the {@link Platform} implementation provided by the active loader module. */
public final class Platforms {

    private static final Platform INSTANCE = ServiceLoader.load(Platform.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                    "No MiningStats Platform implementation found on the classpath"));

    private Platforms() {
    }

    public static Platform get() {
        return INSTANCE;
    }
}
