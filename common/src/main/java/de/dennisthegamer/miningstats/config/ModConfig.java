package de.dennisthegamer.miningstats.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.dennisthegamer.hudlib.position.HudPlacement;
import de.dennisthegamer.hudlib.position.HudPositionMigration;
import de.dennisthegamer.hudlib.position.HudPreset;
import de.dennisthegamer.miningstats.data.OreType;
import de.dennisthegamer.miningstats.platform.Platforms;

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
            Platforms.get().getConfigDir().toFile(),
            "miningstats.json"
    );

    private static ModConfig INSTANCE = null;

    // HUD Settings
    /** @deprecated Legacy-4-Ecken-Feld; nur noch zum Migrieren gelesen. Wird nach load() genullt. */
    @Deprecated
    public String hudPosition = "BOTTOM_LEFT";
    /** Freie HUD-Position (Anker + Offset). Nach {@link #load()} immer non-null. */
    public HudPlacement hudPlacement = null;
    /** Vom Nutzer gespeicherte Positions-Slots. */
    public List<HudPreset> hudSlots = new ArrayList<>();
    public boolean hudVisibleAlways = false;
    public float hudOpacity = 0.6f;

    // Session Settings
    public boolean showSessionSummary = true;
    public boolean persistSessions = false;

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
                    config.migrateHudPosition();
                    return config;
                }
            } catch (IOException e) {
                System.err.println("Failed to load MiningStats config: " + e.getMessage());
            }
        }
        ModConfig config = new ModConfig();
        config.migrateHudPosition();
        config.save();
        return config;
    }

    /**
     * Einmalige Migration: befüllt {@link #hudPlacement} aus dem Legacy-{@link #hudPosition},
     * falls noch nicht gesetzt, und stoppt das Persistieren des Legacy-Feldes.
     * Gson serialisiert null-Felder standardmäßig nicht, daher verschwindet {@code hudPosition}
     * beim nächsten {@link #save()} aus der JSON.
     */
    public void migrateHudPosition() {
        if (hudPlacement == null) {
            hudPlacement = HudPositionMigration.fromLegacyCorner(hudPosition);
        }
        hudPosition = null;
        if (hudSlots == null) {
            hudSlots = new ArrayList<>();
        }
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
}
