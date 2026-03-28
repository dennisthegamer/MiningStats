package com.miningstats.config;

import com.miningstats.data.OreType;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Optional;

public class ConfigScreen {

    public static Screen create(Screen parent) {
        ModConfig config = ModConfig.getInstance();
        ModConfig defaults = new ModConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.miningstats.title"))
                .setSavingRunnable(config::save);

        ConfigEntryBuilder entry = builder.entryBuilder();

        // === HUD Settings ===
        ConfigCategory hud = builder.getOrCreateCategory(
                Text.translatable("config.miningstats.category.hud"));

        hud.addEntry(entry.startSelector(
                        Text.translatable("config.miningstats.hud_position"),
                        new String[]{"TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT"},
                        config.hudPosition)
                .setDefaultValue("BOTTOM_LEFT")
                .setTooltip(Text.translatable("config.miningstats.hud_position.tooltip"))
                .setSaveConsumer(v -> config.hudPosition = v)
                .build());

        hud.addEntry(entry.startBooleanToggle(
                        Text.translatable("config.miningstats.hud_visible_always"),
                        config.hudVisibleAlways)
                .setDefaultValue(defaults.hudVisibleAlways)
                .setTooltip(Text.translatable("config.miningstats.hud_visible_always.tooltip"))
                .setSaveConsumer(v -> config.hudVisibleAlways = v)
                .build());

        hud.addEntry(entry.startFloatField(
                        Text.translatable("config.miningstats.hud_opacity"),
                        config.hudOpacity)
                .setDefaultValue(defaults.hudOpacity)
                .setMin(0.0f)
                .setMax(1.0f)
                .setTooltip(Text.translatable("config.miningstats.hud_opacity.tooltip"))
                .setSaveConsumer(v -> config.hudOpacity = v)
                .build());

        // === Session Settings ===
        ConfigCategory session = builder.getOrCreateCategory(
                Text.translatable("config.miningstats.category.session"));

        session.addEntry(entry.startBooleanToggle(
                        Text.translatable("config.miningstats.show_session_summary"),
                        config.showSessionSummary)
                .setDefaultValue(defaults.showSessionSummary)
                .setTooltip(Text.translatable("config.miningstats.show_session_summary.tooltip"))
                .setSaveConsumer(v -> config.showSessionSummary = v)
                .build());

        session.addEntry(entry.startBooleanToggle(
                        Text.translatable("config.miningstats.persist_sessions"),
                        config.persistSessions)
                .setDefaultValue(defaults.persistSessions)
                .setTooltip(Text.translatable("config.miningstats.persist_sessions.tooltip"))
                .setSaveConsumer(v -> config.persistSessions = v)
                .build());

        // === Tracking Settings ===
        ConfigCategory tracking = builder.getOrCreateCategory(
                Text.translatable("config.miningstats.category.tracking"));

        tracking.addEntry(entry.startBooleanToggle(
                        Text.translatable("config.miningstats.merge_deepslate"),
                        config.mergeDeepslate)
                .setDefaultValue(defaults.mergeDeepslate)
                .setTooltip(Text.translatable("config.miningstats.merge_deepslate.tooltip"))
                .setSaveConsumer(v -> config.mergeDeepslate = v)
                .build());

        tracking.addEntry(entry.startStrList(
                        Text.translatable("config.miningstats.tracked_ores"),
                        new ArrayList<>(config.trackedOres))
                .setDefaultValue(defaults.trackedOres)
                .setTooltip(Text.translatable("config.miningstats.tracked_ores.tooltip"))
                .setCellErrorSupplier(value -> {
                    if (value == null || value.isBlank()) return Optional.of(Text.literal("Cannot be empty"));
                    if (!value.contains(":")) return Optional.of(Text.literal("Use format mod_id:block_id"));
                    return Optional.empty();
                })
                .setSaveConsumer(v -> config.trackedOres = new ArrayList<>(v))
                .build());

        // === Milestone Settings ===
        ConfigCategory milestones = builder.getOrCreateCategory(
                Text.translatable("config.miningstats.category.milestones"));

        for (OreType type : OreType.values()) {
            if (type.isDeepslate()) continue;
            String key = type.name().toLowerCase();
            milestones.addEntry(entry.startIntField(
                            Text.translatable("config.miningstats.milestone." + key),
                            config.milestones.getOrDefault(key, 0))
                    .setDefaultValue(defaults.milestones.getOrDefault(key, 0))
                    .setMin(0)
                    .setTooltip(Text.translatable("config.miningstats.milestone.tooltip", type.getDisplayName()))
                    .setSaveConsumer(v -> {
                        if (v > 0) {
                            config.milestones.put(key, v);
                        } else {
                            config.milestones.remove(key);
                        }
                    })
                    .build());
        }

        return builder.build();
    }
}
