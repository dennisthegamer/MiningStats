package de.dennisthegamer.miningstats.hud;

import de.dennisthegamer.hudlib.ui.HudBoxProvider;
import net.minecraft.client.gui.GuiGraphics;

/** Adaptiert das MiningStats-HUD (Größe/Vorschau-Zeichnung) an die hudlib-ui-Schnittstelle. */
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
        return 1.0f;
    }

    @Override
    public void drawSample(GuiGraphics graphics, int x, int y, float scale) {
        HudRenderer.drawPreview(graphics, x, y, scale);
    }
}
