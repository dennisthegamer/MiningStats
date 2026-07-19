package de.dennisthegamer.miningstats.hud;

import de.dennisthegamer.hudlib.color.HudColors;
import de.dennisthegamer.hudlib.color.HudFlash;
import de.dennisthegamer.miningstats.config.ModConfig;
import de.dennisthegamer.miningstats.data.OreType;
import de.dennisthegamer.miningstats.data.SessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;

import java.util.LinkedHashMap;
import java.util.Map;

public class HudRenderer {

    private static final int PADDING = 6;

    private static boolean compactMode = false;

    private static final HudFlash flash = new HudFlash(10);
    private static final HudFlash resetMsg = new HudFlash(40);

    public static void toggleCompactMode() {
        compactMode = !compactMode;
    }

    public static boolean isCompactMode() {
        return compactMode;
    }

    // --- HudEffects-Ersatz: von Client-Tick / Keybind / Tracker aufgerufen -----------

    /** Startet den Gold-Flash (z. B. beim Abbau eines seltenen Erzes). */
    public static void triggerFlash() {
        flash.trigger(HudColors.GOLD);
    }

    /** Startet die "Reset"-Overlay-Message. */
    public static void triggerResetMessage() {
        resetMsg.trigger(0);
    }

    /** Im Client-Tick aufrufen: zieht beide Timer um einen Tick zurück. */
    public static void tick() {
        flash.tick();
        resetMsg.tick();
    }

    // --- Anzeige-Daten: entweder Live-Session oder Beispiel-Snapshot für die Vorschau -

    /** Renderbare Daten, unabhängig davon ob sie aus der Live-Session oder einem Sample stammen. */
    private record HudData(Map<OreType, Integer> counts, Map<OreType, Integer> bonuses,
                            int totalOres, int totalFortuneBonus, String duration, boolean active) {}

    private static HudData liveData(ModConfig config, SessionData session) {
        boolean merge = config.mergeDeepslate;
        Map<OreType, Integer> counts = merge ? session.getMergedOreCounts() : session.getOreCounts();
        Map<OreType, Integer> bonuses;
        if (merge) {
            bonuses = session.getMergedFortuneBonuses();
        } else {
            bonuses = new LinkedHashMap<>();
            for (OreType type : counts.keySet()) {
                bonuses.put(type, session.getFortuneBonus(type));
            }
        }
        return new HudData(counts, bonuses, session.getTotalOres(), session.getTotalFortuneBonus(),
                session.getFormattedDuration(), session.isActive());
    }

    /** Beispiel-Snapshot, damit die Box im Editor immer sichtbar/realistisch groß ist. */
    private static HudData sampleData() {
        Map<OreType, Integer> counts = new LinkedHashMap<>();
        counts.put(OreType.DIAMOND, 12);
        counts.put(OreType.IRON, 34);
        counts.put(OreType.COAL, 56);
        Map<OreType, Integer> bonuses = new LinkedHashMap<>();
        bonuses.put(OreType.DIAMOND, 5);
        int totalOres = counts.values().stream().mapToInt(Integer::intValue).sum();
        int totalBonus = bonuses.values().stream().mapToInt(Integer::intValue).sum();
        return new HudData(counts, bonuses, totalOres, totalBonus, "00:12:34", true);
    }

    // --- Größenberechnung; von render(), measureBox() und draw() geteilt -------------

    private record Layout(int width, int height, boolean compact) {}

    private static Layout computeLayout(HudData data, Font font, boolean compact) {
        if (compact) {
            int textWidth = font.width(compactText(data));
            int hudWidth = textWidth + PADDING * 2;
            int hudHeight = font.lineHeight + PADDING * 2;
            return new Layout(hudWidth, hudHeight, true);
        }

        int lineHeight = Math.max(font.lineHeight, 16) + 2; // 16 for item icon height
        int oreLines = 0;
        for (int count : data.counts().values()) {
            if (count > 0) oreLines++;
        }
        int contentLines = 1 + oreLines; // title + ore lines (no more fortune total row)
        int hudWidth = 220;
        int hudHeight = PADDING * 2 + contentLines * lineHeight;
        return new Layout(hudWidth, hudHeight, false);
    }

    private static String compactText(HudData data) {
        return I18n.get("miningstats.hud.compact", data.totalOres(), data.totalFortuneBonus())
                + " " + (char) 0x00B7 + " " + data.duration()
                + (data.active() ? "" : " " + (char) 0x23F8);
    }

    /** Zeichnet die Box an (x,y). {@code allowFlash} nur fürs Live-HUD (nicht in der Vorschau). */
    private static void draw(GuiGraphics context, int x, int y, Layout layout, HudData data,
                              Font font, ModConfig config, boolean allowFlash) {
        int hudWidth = layout.width();
        int hudHeight = layout.height();
        int bgColor = HudColors.backgroundColor(config.hudOpacity);

        if (layout.compact()) {
            context.fill(x, y, x + hudWidth, y + hudHeight, bgColor);

            if (allowFlash && flash.isActive()) {
                int flashAlpha = (int) (flash.progress() * 100);
                context.fill(x, y, x + hudWidth, y + hudHeight, (flashAlpha << 24) | flash.color());
            }

            context.drawString(font, compactText(data), x + PADDING, y + PADDING, 0xFFFFFFFF, true);
            return;
        }

        int iconSize = 16;
        int textOffsetX = iconSize + 4;
        int lineHeight = Math.max(font.lineHeight, 16) + 2;

        // Background
        context.fill(x, y, x + hudWidth, y + hudHeight, bgColor);

        // Flash effect
        if (allowFlash && flash.isActive()) {
            int flashAlpha = (int) (flash.progress() * 100);
            context.fill(x - 2, y - 2, x + hudWidth + 2, y + hudHeight + 2, (flashAlpha << 24) | flash.color());
            context.fill(x, y, x + hudWidth, y + hudHeight, bgColor);
        }

        int currentY = y + PADDING;

        // Reset message overlay
        if (allowFlash && resetMsg.isActive()) {
            String resetText = I18n.get("miningstats.hud.reset");
            int resetX = x + (hudWidth - font.width(resetText)) / 2;
            int resetY = y + (hudHeight - font.lineHeight) / 2;
            context.drawString(font, resetText, resetX, resetY, 0xFFFFFF00, true);
            return;
        }

        // Title (centered): base name + running session time; pause glyph while paused
        String base = I18n.get("miningstats.hud.title.active");
        if (base.endsWith(":")) base = base.substring(0, base.length() - 1);
        String title = base + " " + (char) 0x00B7 + " " + data.duration()
                + (data.active() ? "" : " " + (char) 0x23F8);
        int titleColor = data.active() ? 0xFFFFFFFF : 0xFFFFAA00;
        int titleX = x + (hudWidth - font.width(title)) / 2;
        context.drawString(font, title, titleX, currentY, titleColor, true);
        currentY += lineHeight;

        // Ore lines with icons and right-aligned counts
        int rightEdge = x + hudWidth - PADDING;

        for (Map.Entry<OreType, Integer> entry : data.counts().entrySet()) {
            OreType type = entry.getKey();
            int count = entry.getValue();
            if (count <= 0) continue;

            // Draw item icon
            if (type.getDropItem() != null) {
                context.renderItem(new ItemStack(type.getDropItem()), x + PADDING, currentY - 4);
            }

            // Draw ore name (left-aligned after icon)
            context.drawString(font, type.getDisplayName(), x + PADDING + textOffsetX, currentY, 0xFFFFFFFF, true);

            // Draw fortune bonus (right-aligned at edge) if > 0
            int fortuneBonus = data.bonuses().getOrDefault(type, 0);
            if (fortuneBonus > 0) {
                String bonusText = "(+" + fortuneBonus + ")";
                int bonusWidth = font.width(bonusText);
                context.drawString(font, bonusText, rightEdge - bonusWidth, currentY, 0xFFFFD700, true);
            }

            // Draw count (right-aligned before bonus column)
            String countText = String.valueOf(count);
            int countWidth = font.width(countText);
            int countX = rightEdge - 50 - countWidth; // 50px reserved for bonus column
            context.drawString(font, countText, countX, currentY, 0xFFFFFFFF, true);

            currentY += lineHeight;
        }
    }

    // --- Live HUD --------------------------------------------------------------------

    public static void render(GuiGraphics context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        ModConfig config = ModConfig.getInstance();

        // Check visibility: only show when holding pickaxe (unless always visible)
        if (!config.hudVisibleAlways && !isHoldingPickaxe(client)) return;

        Font font = client.font;
        SessionData session = SessionData.getInstance();
        HudData data = liveData(config, session);

        Layout layout = computeLayout(data, font, compactMode);

        int scaledW = client.getWindow().getGuiScaledWidth();
        int scaledH = client.getWindow().getGuiScaledHeight();
        int x = config.hudPlacement.resolveX(scaledW, layout.width());
        int y = config.hudPlacement.resolveY(scaledH, layout.height());

        draw(context, x, y, layout, data, font, config, true);
    }

    private static boolean isHoldingPickaxe(Minecraft client) {
        if (client.player == null) return false;
        ItemStack mainHand = client.player.getMainHandItem();
        ItemStack offHand = client.player.getOffhandItem();
        return mainHand.is(ItemTags.PICKAXES)
                || offHand.is(ItemTags.PICKAXES);
    }

    // --- Editor-Vorschau ---------------------------------------------------------------

    /** Box-Maße {width,height} der Beispiel-Box (für den Drag-Editor der Lib). */
    public static int[] measureBox() {
        Font font = Minecraft.getInstance().font;
        Layout layout = computeLayout(sampleData(), font, compactMode);
        return new int[] { layout.width(), layout.height() };
    }

    /**
     * Zeichnet die Beispiel-Box im Editor an (x,y). Ohne Flash, ohne die Live-Session zu berühren.
     * {@code scale} ist Teil der hudlib-ui-{@code HudBoxProvider}-Signatur; MiningStats hat (anders
     * als FishingStats) noch kein einstellbares HUD-Scale, daher bleibt der Parameter hier ungenutzt.
     */
    public static void drawPreview(GuiGraphics graphics, int x, int y, float scale) {
        ModConfig config = ModConfig.getInstance();
        Font font = Minecraft.getInstance().font;
        HudData data = sampleData();
        Layout layout = computeLayout(data, font, compactMode);
        draw(graphics, x, y, layout, data, font, config, false);
    }
}
