package de.dennisthegamer.miningstats.hud;

import de.dennisthegamer.hudlib.position.HudPreset;
import de.dennisthegamer.hudlib.ui.HudSlotStore;
import de.dennisthegamer.miningstats.config.ModConfig;

import java.util.List;

/** Bildet die HUD-Preset-Slots von {@link ModConfig} auf die hudlib-ui-Schnittstelle ab. */
public class MiningStatsSlotStore implements HudSlotStore {

    @Override
    public List<HudPreset> slots() {
        return ModConfig.getInstance().hudSlots;
    }

    @Override
    public void save() {
        ModConfig.getInstance().save();
    }
}
