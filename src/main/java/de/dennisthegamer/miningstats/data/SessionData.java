package de.dennisthegamer.miningstats.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SessionData {

    private static final SessionData INSTANCE = new SessionData();

    private final Map<OreType, Integer> oreCounts = new EnumMap<>(OreType.class);
    private final Map<OreType, Integer> fortuneBonus = new EnumMap<>(OreType.class);
    private long sessionStartTime;
    private boolean active = false;
    private long pausedDurationMillis = 0;
    private long pauseStartTime = 0;

    private SessionData() {
        reset();
    }

    public static SessionData getInstance() {
        return INSTANCE;
    }

    public void reset() {
        oreCounts.clear();
        fortuneBonus.clear();
        sessionStartTime = System.currentTimeMillis();
        active = false;
        pausedDurationMillis = 0;
        pauseStartTime = 0;
    }

    public void start() {
        if (!active) {
            if (sessionStartTime == 0 || getTotalOres() == 0) {
                sessionStartTime = System.currentTimeMillis();
                pausedDurationMillis = 0;
            }
            if (pauseStartTime > 0) {
                pausedDurationMillis += System.currentTimeMillis() - pauseStartTime;
                pauseStartTime = 0;
            }
            active = true;
        }
    }

    public void pause() {
        if (active) {
            active = false;
            pauseStartTime = System.currentTimeMillis();
        }
    }

    public boolean isActive() {
        return active;
    }

    public void incrementOreCount(OreType type) {
        oreCounts.merge(type, 1, Integer::sum);
    }

    public void addFortuneBonus(OreType type, int bonus) {
        if (bonus > 0) {
            fortuneBonus.merge(type, bonus, Integer::sum);
        }
    }

    public int getOreCount(OreType type) {
        return oreCounts.getOrDefault(type, 0);
    }

    public int getFortuneBonus(OreType type) {
        return fortuneBonus.getOrDefault(type, 0);
    }

    public Map<OreType, Integer> getOreCounts() {
        return oreCounts;
    }

    public int getTotalOres() {
        return oreCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getTotalFortuneBonus() {
        return fortuneBonus.values().stream().mapToInt(Integer::intValue).sum();
    }

    public long getSessionDurationMillis() {
        long elapsed = System.currentTimeMillis() - sessionStartTime;
        long paused = pausedDurationMillis;
        if (!active && pauseStartTime > 0) {
            paused += System.currentTimeMillis() - pauseStartTime;
        }
        return elapsed - paused;
    }

    /**
     * Returns ore counts merged by base type (deepslate folded into normal).
     */
    public Map<OreType, Integer> getMergedOreCounts() {
        Map<OreType, Integer> merged = new LinkedHashMap<>();
        for (OreType type : OreType.values()) {
            int count = getOreCount(type);
            if (count <= 0) continue;
            OreType base = type.getBaseType();
            merged.merge(base, count, Integer::sum);
        }
        return merged;
    }

    /**
     * Returns fortune bonuses merged by base type.
     */
    public Map<OreType, Integer> getMergedFortuneBonuses() {
        Map<OreType, Integer> merged = new LinkedHashMap<>();
        for (OreType type : OreType.values()) {
            int bonus = getFortuneBonus(type);
            if (bonus <= 0) continue;
            OreType base = type.getBaseType();
            merged.merge(base, bonus, Integer::sum);
        }
        return merged;
    }

    public int getMergedOreCount(OreType baseType) {
        int count = getOreCount(baseType);
        for (OreType type : OreType.values()) {
            if (type.isDeepslate() && type.getBaseType() == baseType) {
                count += getOreCount(type);
            }
        }
        return count;
    }

    public String getFormattedDuration() {
        long totalSeconds = getSessionDurationMillis() / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // === Persistence ===

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File SESSION_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "miningstats_session.json"
    );

    public void saveToDisk() {
        try {
            Map<String, Object> data = new HashMap<>();

            Map<String, Integer> oreData = new HashMap<>();
            for (Map.Entry<OreType, Integer> e : oreCounts.entrySet()) {
                if (e.getValue() > 0) oreData.put(e.getKey().name(), e.getValue());
            }
            data.put("oreCounts", oreData);

            Map<String, Integer> fortuneData = new HashMap<>();
            for (Map.Entry<OreType, Integer> e : fortuneBonus.entrySet()) {
                if (e.getValue() > 0) fortuneData.put(e.getKey().name(), e.getValue());
            }
            data.put("fortuneBonus", fortuneData);

            data.put("durationMillis", getSessionDurationMillis());

            SESSION_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(SESSION_FILE)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save MiningStats session: " + e.getMessage());
        }
    }

    public boolean loadFromDisk() {
        if (!SESSION_FILE.exists()) return false;
        try (FileReader reader = new FileReader(SESSION_FILE)) {
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> data = GSON.fromJson(reader, mapType);
            if (data == null) return false;

            reset();

            @SuppressWarnings("unchecked")
            Map<String, Double> oreData = (Map<String, Double>) data.get("oreCounts");
            if (oreData != null) {
                for (Map.Entry<String, Double> e : oreData.entrySet()) {
                    try {
                        OreType type = OreType.valueOf(e.getKey());
                        oreCounts.put(type, e.getValue().intValue());
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Double> fortuneData = (Map<String, Double>) data.get("fortuneBonus");
            if (fortuneData != null) {
                for (Map.Entry<String, Double> e : fortuneData.entrySet()) {
                    try {
                        OreType type = OreType.valueOf(e.getKey());
                        fortuneBonus.put(type, e.getValue().intValue());
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            if (data.containsKey("durationMillis")) {
                long savedDuration = ((Double) data.get("durationMillis")).longValue();
                sessionStartTime = System.currentTimeMillis() - savedDuration;
                pausedDurationMillis = 0;
            }

            // Restored session starts paused
            active = false;
            pauseStartTime = System.currentTimeMillis();

            return getTotalOres() > 0;
        } catch (Exception e) {
            System.err.println("Failed to load MiningStats session: " + e.getMessage());
            return false;
        }
    }

    public void deleteSavedSession() {
        if (SESSION_FILE.exists()) {
            SESSION_FILE.delete();
        }
    }
}
