package com.miningstats.data;

import java.util.EnumMap;
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

    public String getFormattedDuration() {
        long totalSeconds = getSessionDurationMillis() / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
