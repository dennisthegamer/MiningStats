package de.dennisthegamer.miningstats.config;

import de.dennisthegamer.miningstats.data.OreType;
import de.dennisthegamer.miningstats.hud.MiningStatsHudBox;
import de.dennisthegamer.miningstats.hud.MiningStatsSlotStore;
import de.dennisthegamer.hudlib.ui.HudEditorScreen;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class ModConfigScreen {

    public static Screen create(Screen parent) {
        ModConfig config = ModConfig.getInstance();
        ModConfig defaults = new ModConfig();

        var hudCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("config.miningstats.category.hud"))
                .option(ButtonOption.createBuilder()
                        .name(Component.translatable("config.miningstats.hud_edit"))
                        .description(OptionDescription.of(
                                Component.translatable("config.miningstats.hud_edit.tooltip")))
                        .action((yaclScreen, opt) -> {
                            ModConfig cfg = ModConfig.getInstance();
                            Minecraft.getInstance().gui.setScreen(new HudEditorScreen(
                                    yaclScreen, new MiningStatsHudBox(), new MiningStatsSlotStore(),
                                    cfg::getHudPlacement, p -> { cfg.hudPlacement = p; cfg.save(); }));
                        })
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("config.miningstats.hud_visible_always"))
                        .description(val -> OptionDescription.of(
                                Component.translatable("config.miningstats.hud_visible_always.tooltip")))
                        .binding(defaults.hudVisibleAlways, () -> config.hudVisibleAlways, v -> config.hudVisibleAlways = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(Component.translatable("config.miningstats.hud_opacity"))
                        .description(val -> OptionDescription.of(
                                Component.translatable("config.miningstats.hud_opacity.tooltip")))
                        .binding(defaults.hudOpacity, () -> config.hudOpacity, v -> config.hudOpacity = v)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.0f, 1.0f).step(0.01f))
                        .build())
                .build();

        var sessionCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("config.miningstats.category.session"))
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("config.miningstats.show_session_summary"))
                        .description(val -> OptionDescription.of(
                                Component.translatable("config.miningstats.show_session_summary.tooltip")))
                        .binding(defaults.showSessionSummary, () -> config.showSessionSummary, v -> config.showSessionSummary = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("config.miningstats.persist_sessions"))
                        .description(val -> OptionDescription.of(
                                Component.translatable("config.miningstats.persist_sessions.tooltip")))
                        .binding(defaults.persistSessions, () -> config.persistSessions, v -> config.persistSessions = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .build();

        var trackingCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("config.miningstats.category.tracking"))
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("config.miningstats.merge_deepslate"))
                        .description(val -> OptionDescription.of(
                                Component.translatable("config.miningstats.merge_deepslate.tooltip")))
                        .binding(defaults.mergeDeepslate, () -> config.mergeDeepslate, v -> config.mergeDeepslate = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .group(ListOption.<String>createBuilder()
                        .name(Component.translatable("config.miningstats.tracked_ores"))
                        .description(OptionDescription.of(
                                Component.translatable("config.miningstats.tracked_ores.tooltip")))
                        .binding(defaults.trackedOres, () -> config.trackedOres, v -> config.trackedOres = new ArrayList<>(v))
                        .controller(StringControllerBuilder::create)
                        .initial("minecraft:")
                        .build())
                .build();

        var milestoneCategoryBuilder = ConfigCategory.createBuilder()
                .name(Component.translatable("config.miningstats.category.milestones"));

        for (OreType type : OreType.values()) {
            if (type.isDeepslate()) continue;
            String key = type.name().toLowerCase();
            milestoneCategoryBuilder.option(Option.<Integer>createBuilder()
                    .name(Component.translatable("config.miningstats.milestone." + key))
                    .description(val -> OptionDescription.of(
                            Component.translatable("config.miningstats.milestone.tooltip", type.getDisplayName())))
                    .binding(
                            defaults.milestones.getOrDefault(key, 0),
                            () -> config.milestones.getOrDefault(key, 0),
                            v -> {
                                if (v > 0) {
                                    config.milestones.put(key, v);
                                } else {
                                    config.milestones.remove(key);
                                }
                            })
                    .controller(IntegerFieldControllerBuilder::create)
                    .build());
        }

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.miningstats.title"))
                .save(config::save)
                .category(hudCategory)
                .category(sessionCategory)
                .category(trackingCategory)
                .category(milestoneCategoryBuilder.build())
                .build()
                .generateScreen(parent);
    }
}
