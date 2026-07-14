package com.miningstats.platform;

import java.util.ServiceLoader;

public final class Platforms {
    private static final Platform INSTANCE = ServiceLoader.load(Platform.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No MiningStats Platform impl on the classpath"));
    private Platforms() {}
    public static Platform get() { return INSTANCE; }
}
