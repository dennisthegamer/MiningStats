package de.dennisthegamer.miningstats.hud;

import de.dennisthegamer.hudlib.effect.HudEffects;
import de.dennisthegamer.hudlib.ui.HudPanel;
import de.dennisthegamer.miningstats.config.ModConfig;
import de.dennisthegamer.miningstats.data.OreType;
import de.dennisthegamer.miningstats.data.SessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;

import java.util.Map;

/**
 * Position/Skalierung/Hintergrund laufen über HudLibs HudPanel, die Effekt-Timer über
 * HudEffects (ersetzt die gelöschten mod-eigenen HudLayout/HudEffects-Klassen).
 * measureBox()/drawPreview() teilen sich die Vermessung mit dem Live-Pfad.
 */
public class HudRenderer {

    private static final int PADDING = 6;
    private static final int FULL_WIDTH = 220;
    private static final int FLASH_DURATION = 10;          // 0.5 Sekunden
    private static final int RESET_MESSAGE_DURATION = 40;  // 2 Sekunden

    private static final HudEffects EFFECTS = new HudEffects(FLASH_DURATION);

    private static boolean compactMode = false;

    public static void toggleCompactMode() {
        compactMode = !compactMode;
    }

    public static boolean isCompactMode() {
        return compactMode;
    }

    // Fassaden für die bisherigen HudEffects-Call-Sites (Tracker/Keybind/Client-Tick).
    public static void triggerFlash() {
        EFFECTS.triggerFlash();
    }

    public static void triggerResetMessage() {
        EFFECTS.showMessage(RESET_MESSAGE_DURATION);
    }

    public static void tickEffects() {
        EFFECTS.tick();
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        ModConfig config = ModConfig.getInstance();

        // Check visibility: only show when holding pickaxe (unless always visible)
        if (!config.hudVisibleAlways && !isHoldingPickaxe(client)) return;

        Font font = client.font;
        int[] box = measure(font);
        HudPanel.draw(graphics, config.getHudPlacement(), box[0], box[1], 1.0f, config.hudOpacity,
                (g, x, y) -> drawContent(g, x, y, box[0], box[1], font));
    }

    /** Breite/Höhe der aktuellen Box (für den HudBoxProvider des Editors). */
    public static int[] measureBox() {
        return measure(Minecraft.getInstance().font);
    }

    /** Editor-Vorschau an expliziten Koordinaten (MiningStats skaliert nicht — scale wird geklemmt weitergereicht). */
    public static void drawPreview(GuiGraphicsExtractor graphics, int x, int y, float scale) {
        Font font = Minecraft.getInstance().font;
        int[] box = measure(font);
        HudPanel.drawAt(graphics, x, y, box[0], box[1], scale, ModConfig.getInstance().hudOpacity,
                (g, bx, by) -> drawContent(g, bx, by, box[0], box[1], font));
    }

    private static boolean isHoldingPickaxe(Minecraft client) {
        if (client.player == null) return false;
        ItemStack mainHand = client.player.getMainHandItem();
        ItemStack offHand = client.player.getOffhandItem();
        return mainHand.is(holder -> holder.is(ItemTags.PICKAXES))
                || offHand.is(holder -> holder.is(ItemTags.PICKAXES));
    }

    /** {breite, hoehe} des jeweils aktiven Modus — identische Formeln wie der alte Renderer. */
    private static int[] measure(Font font) {
        SessionData session = SessionData.getInstance();
        if (compactMode) {
            int textWidth = font.width(compactText(session));
            return new int[] { textWidth + PADDING * 2, font.lineHeight + PADDING * 2 };
        }
        ModConfig config = ModConfig.getInstance();
        int lineHeight = Math.max(font.lineHeight, 16) + 2;
        Map<OreType, Integer> displayCounts = config.mergeDeepslate
                ? session.getMergedOreCounts()
                : session.getOreCounts();
        int oreLines = 0;
        for (int count : displayCounts.values()) {
            if (count > 0) oreLines++;
        }
        int contentLines = 1 + oreLines;
        return new int[] { FULL_WIDTH, PADDING * 2 + contentLines * lineHeight };
    }

    private static String compactText(SessionData session) {
        return I18n.get("miningstats.hud.compact", session.getTotalOres(), session.getTotalFortuneBonus())
                + " " + (char) 0x00B7 + " " + session.getFormattedDuration()
                + (session.isActive() ? "" : " " + (char) 0x23F8);
    }

    private static void drawContent(GuiGraphicsExtractor graphics, int x, int y,
                                    int hudWidth, int hudHeight, Font font) {
        if (compactMode) {
            drawCompact(graphics, x, y, hudWidth, hudHeight, font);
        } else {
            drawFull(graphics, x, y, hudWidth, hudHeight, font);
        }
    }

    private static void drawCompact(GuiGraphicsExtractor graphics, int x, int y,
                                    int hudWidth, int hudHeight, Font font) {
        SessionData session = SessionData.getInstance();

        // Flash effect — wie bisher: Overlay IN der Box, Alpha x100.
        if (EFFECTS.isFlashing()) {
            int flashColor = (((int) (EFFECTS.flashAlpha() * 100)) << 24) | 0xFFD700;
            graphics.fill(x, y, x + hudWidth, y + hudHeight, flashColor);
        }

        graphics.text(font, compactText(session), x + PADDING, y + PADDING, 0xFFFFFFFF, true);
    }

    private static void drawFull(GuiGraphicsExtractor graphics, int x, int y,
                                 int hudWidth, int hudHeight, Font font) {
        SessionData session = SessionData.getInstance();
        ModConfig config = ModConfig.getInstance();
        int lineHeight = Math.max(font.lineHeight, 16) + 2;
        int iconSize = 16;
        int textOffsetX = iconSize + 4;

        boolean merge = config.mergeDeepslate;
        Map<OreType, Integer> displayCounts = merge
                ? session.getMergedOreCounts()
                : session.getOreCounts();
        Map<OreType, Integer> displayBonuses = merge
                ? session.getMergedFortuneBonuses()
                : null;

        // Flash effect — wie bisher: 2px-Rahmen um die Box plus bg-Refill, Alpha x100.
        if (EFFECTS.isFlashing()) {
            int flashAlpha = (int) (EFFECTS.flashAlpha() * 100);
            graphics.fill(x - 2, y - 2, x + hudWidth + 2, y + hudHeight + 2, (flashAlpha << 24) | 0xFFD700);
            int bgColor = ((int) (config.hudOpacity * 255) << 24);
            graphics.fill(x, y, x + hudWidth, y + hudHeight, bgColor);
        }

        int currentY = y + PADDING;

        // Reset message overlay
        if (EFFECTS.isMessageVisible()) {
            String resetText = I18n.get("miningstats.hud.reset");
            int resetX = x + (hudWidth - font.width(resetText)) / 2;
            int resetY = y + (hudHeight - font.lineHeight) / 2;
            graphics.text(font, resetText, resetX, resetY, 0xFFFFFF00, true);
            return;
        }

        // Title (centered): base name + running session time; pause glyph while paused
        String base = I18n.get("miningstats.hud.title.active");
        if (base.endsWith(":")) base = base.substring(0, base.length() - 1);
        String title = base + " " + (char) 0x00B7 + " " + session.getFormattedDuration()
                + (session.isActive() ? "" : " " + (char) 0x23F8);
        int titleColor = session.isActive() ? 0xFFFFFFFF : 0xFFFFAA00;
        int titleX = x + (hudWidth - font.width(title)) / 2;
        graphics.text(font, title, titleX, currentY, titleColor, true);
        currentY += lineHeight;

        // Ore lines with icons and right-aligned counts
        int rightEdge = x + hudWidth - PADDING;

        for (Map.Entry<OreType, Integer> entry : displayCounts.entrySet()) {
            OreType type = entry.getKey();
            int count = entry.getValue();
            if (count <= 0) continue;

            if (type.getDropItem() != null) {
                graphics.item(new ItemStack(type.getDropItem()), x + PADDING, currentY - 4);
            }

            graphics.text(font, type.getDisplayName(), x + PADDING + textOffsetX, currentY, 0xFFFFFFFF, true);

            int fortuneBonus = displayBonuses != null
                    ? displayBonuses.getOrDefault(type, 0)
                    : session.getFortuneBonus(type);
            if (fortuneBonus > 0) {
                String bonusText = "(+" + fortuneBonus + ")";
                int bonusWidth = font.width(bonusText);
                graphics.text(font, bonusText, rightEdge - bonusWidth, currentY, 0xFFFFD700, true);
            }

            String countText = String.valueOf(count);
            int countWidth = font.width(countText);
            int countX = rightEdge - 50 - countWidth; // 50px reserved for bonus column
            graphics.text(font, countText, countX, currentY, 0xFFFFFFFF, true);

            currentY += lineHeight;
        }
    }
}
