package de.dennisthegamer.miningstats.hud;

import de.dennisthegamer.hudlib.ui.HudBoxProvider;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Adaptiert das MiningStats-HUD (Größe/Vorschau) an die hudlib-ui-Schnittstelle. */
public class MiningStatsHudBox implements HudBoxProvider {

    @Override
    public int width() {
        return HudRenderer.measureBox()[0];
    }

    @Override
    public int height() {
        return HudRenderer.measureBox()[1];
    }

    @Override
    public float scale() {
        return 1f; // MiningStats hat keine HUD-Skalierung.
    }

    @Override
    public void drawSample(GuiGraphicsExtractor graphics, int x, int y, float scale) {
        HudRenderer.drawPreview(graphics, x, y, scale);
    }
}
