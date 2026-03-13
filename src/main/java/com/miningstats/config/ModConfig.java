package com.miningstats.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.miningstats.data.OreType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "miningstats.json"
    );

    private static ModConfig INSTANCE = null;

    // HUD Settings
    public String hudPosition = "BOTTOM_LEFT";
    public boolean hudVisibleAlways = false;
    public float hudOpacity = 0.6f;

    // Session Settings
    public boolean showSessionSummary = true;

    // Tracking Settings
    public boolean mergeDeepslate = true;
    public List<String> trackedOres = new ArrayList<>();

    // Milestone Settings
    public Map<String, Integer> milestones = new HashMap<>();

    public ModConfig() {
        // Default milestones
        milestones.put("diamond", 100);
        milestones.put("emerald", 50);
        milestones.put("ancient_debris", 25);
    }

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static ModConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                if (config != null) {
                    return config;
                }
            } catch (IOException e) {
                System.err.println("Failed to load MiningStats config: " + e.getMessage());
            }
        }
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save MiningStats config: " + e.getMessage());
        }
    }

    public int getMilestoneThreshold(OreType type) {
        String key = type.name().toLowerCase();
        return milestones.getOrDefault(key, 0);
    }

    public HudPosition getHudPosition() {
        try {
            return HudPosition.valueOf(hudPosition);
        } catch (IllegalArgumentException e) {
            return HudPosition.BOTTOM_LEFT;
        }
    }

    public enum HudPosition {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
}
