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

    /**
     * Einmalige Migration: befüllt {@link #hudPlacement} aus dem Legacy-{@link #hudPosition}
     * (4-Ecken-Enum als String; Feld-Default BOTTOM_LEFT bleibt so optisch erhalten) und stoppt
     * das Persistieren des Legacy-Feldes (Gson lässt null-Felder weg).
     */
    public void migrateHudPosition() {
        if (hudPlacement == null) {
            hudPlacement = HudPositionMigration.fromLegacy(hudPosition);
        }
        hudPosition = null;
        if (hudSlots == null) {
            hudSlots = new ArrayList<>();
        }
    }

    /** Non-null-Zugriff für Renderer/Editor (defensiv, falls die JSON von Hand geleert wurde). */
    public HudPlacement getHudPlacement() {
        if (hudPlacement == null) {
            migrateHudPosition();
        }
        return hudPlacement;
    }
}
