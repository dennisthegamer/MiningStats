package de.dennisthegamer.miningstats.config;

import de.dennisthegamer.miningstats.data.OreType;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Optional;

public class ModConfigScreen {

    public static Screen create(Screen parent) {
        ModConfig config = ModConfig.getInstance();
        ModConfig defaults = new ModConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.miningstats.title"))
                .setSavingRunnable(config::save);

        ConfigEntryBuilder entry = builder.entryBuilder();

        // === HUD Settings ===
        ConfigCategory hud = builder.getOrCreateCategory(
                Component.translatable("config.miningstats.category.hud"));

        hud.addEntry(entry.startEnumSelector(
                        Component.translatable("config.miningstats.hud_position"),
                        ModConfig.HudPosition.class,
                        config.getHudPosition())
                .setDefaultValue(defaults.getHudPosition())
                .setTooltip(Component.translatable("config.miningstats.hud_position.tooltip"))
                .setSaveConsumer(v -> config.hudPosition = v.name())
                .build());

        hud.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.miningstats.hud_visible_always"),
                        config.hudVisibleAlways)
                .setDefaultValue(defaults.hudVisibleAlways)
                .setTooltip(Component.translatable("config.miningstats.hud_visible_always.tooltip"))
                .setSaveConsumer(v -> config.hudVisibleAlways = v)
                .build());

        hud.addEntry(entry.startIntSlider(
                        Component.translatable("config.miningstats.hud_opacity"),
                        (int) (config.hudOpacity * 100), 0, 100)
                .setDefaultValue((int) (defaults.hudOpacity * 100))
                .setTooltip(Component.translatable("config.miningstats.hud_opacity.tooltip"))
                .setSaveConsumer(v -> config.hudOpacity = v / 100f)
                .build());

        // === Session Settings ===
        ConfigCategory session = builder.getOrCreateCategory(
                Component.translatable("config.miningstats.category.session"));

        session.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.miningstats.show_session_summary"),
                        config.showSessionSummary)
                .setDefaultValue(defaults.showSessionSummary)
                .setTooltip(Component.translatable("config.miningstats.show_session_summary.tooltip"))
                .setSaveConsumer(v -> config.showSessionSummary = v)
                .build());

        session.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.miningstats.persist_sessions"),
                        config.persistSessions)
                .setDefaultValue(defaults.persistSessions)
                .setTooltip(Component.translatable("config.miningstats.persist_sessions.tooltip"))
                .setSaveConsumer(v -> config.persistSessions = v)
                .build());

        // === Tracking Settings ===
        ConfigCategory tracking = builder.getOrCreateCategory(
                Component.translatable("config.miningstats.category.tracking"));

        tracking.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.miningstats.merge_deepslate"),
                        config.mergeDeepslate)
                .setDefaultValue(defaults.mergeDeepslate)
                .setTooltip(Component.translatable("config.miningstats.merge_deepslate.tooltip"))
                .setSaveConsumer(v -> config.mergeDeepslate = v)
                .build());

        tracking.addEntry(entry.startStrList(
                        Component.translatable("config.miningstats.tracked_ores"),
                        new ArrayList<>(config.trackedOres))
                .setDefaultValue(defaults.trackedOres)
                .setTooltip(Component.translatable("config.miningstats.tracked_ores.tooltip"))
                .setCellErrorSupplier(value -> {
                    if (value == null || value.isBlank()) return Optional.of(Component.literal("Cannot be empty"));
                    if (!value.contains(":")) return Optional.of(Component.literal("Use format mod_id:block_id"));
                    return Optional.empty();
                })
                .setSaveConsumer(v -> config.trackedOres = new ArrayList<>(v))
                .build());

        // === Milestone Settings ===
        ConfigCategory milestones = builder.getOrCreateCategory(
                Component.translatable("config.miningstats.category.milestones"));

        for (OreType type : OreType.values()) {
            if (type.isDeepslate()) continue;
            String key = type.name().toLowerCase();
            milestones.addEntry(entry.startIntField(
                            Component.translatable("config.miningstats.milestone." + key),
                            config.milestones.getOrDefault(key, 0))
                    .setDefaultValue(defaults.milestones.getOrDefault(key, 0))
                    .setMin(0)
                    .setTooltip(Component.translatable("config.miningstats.milestone.tooltip", type.getDisplayName()))
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
