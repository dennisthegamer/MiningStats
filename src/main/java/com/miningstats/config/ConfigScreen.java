package com.miningstats.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigScreen {

    public static Screen create(Screen parent) {
        ModConfig config = ModConfig.getInstance();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.miningstats.title"))
                .setSavingRunnable(config::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // === HUD CATEGORY ===
        ConfigCategory hud = builder.getOrCreateCategory(
                Text.translatable("config.miningstats.category.hud")
        );

        hud.addEntry(entryBuilder.startSelector(
                        Text.translatable("config.miningstats.hud_position"),
                        new String[]{"TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT"},
                        config.hudPosition
                )
                .setDefaultValue("BOTTOM_LEFT")
                .setTooltip(Text.translatable("config.miningstats.hud_position.tooltip"))
                .setSaveConsumer(value -> config.hudPosition = value)
                .build());

        hud.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.miningstats.hud_visible_always"),
                        config.hudVisibleAlways
                )
                .setDefaultValue(false)
                .setTooltip(Text.translatable("config.miningstats.hud_visible_always.tooltip"))
                .setSaveConsumer(value -> config.hudVisibleAlways = value)
                .build());

        hud.addEntry(entryBuilder.startFloatField(
                        Text.translatable("config.miningstats.hud_opacity"),
                        config.hudOpacity
                )
                .setDefaultValue(0.6f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setTooltip(Text.translatable("config.miningstats.hud_opacity.tooltip"))
                .setSaveConsumer(value -> config.hudOpacity = value)
                .build());

        // === SESSION CATEGORY ===
        ConfigCategory session = builder.getOrCreateCategory(
                Text.translatable("config.miningstats.category.session")
        );

        session.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.miningstats.show_session_summary"),
                        config.showSessionSummary
                )
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.miningstats.show_session_summary.tooltip"))
                .setSaveConsumer(value -> config.showSessionSummary = value)
                .build());

        // === TRACKING CATEGORY ===
        ConfigCategory tracking = builder.getOrCreateCategory(
                Text.translatable("config.miningstats.category.tracking")
        );

        tracking.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.miningstats.merge_deepslate"),
                        config.mergeDeepslate
                )
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.miningstats.merge_deepslate.tooltip"))
                .setSaveConsumer(value -> config.mergeDeepslate = value)
                .build());

        tracking.addEntry(entryBuilder.startStrList(
                        Text.translatable("config.miningstats.tracked_ores"),
                        config.trackedOres
                )
                .setDefaultValue(java.util.List.of())
                .setTooltip(Text.translatable("config.miningstats.tracked_ores.tooltip"))
                .setSaveConsumer(value -> config.trackedOres = new java.util.ArrayList<>(value))
                .build());

        return builder.build();
    }
}
