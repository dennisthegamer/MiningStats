package de.dennisthegamer.miningstats.hud;

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

public class HudRenderer {

    private static boolean compactMode = false;

    public static void toggleCompactMode() {
        compactMode = !compactMode;
    }

    public static boolean isCompactMode() {
        return compactMode;
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui.hud.isHidden()) return;

        ModConfig config = ModConfig.getInstance();

        // Check visibility: only show when holding pickaxe (unless always visible)
        if (!config.hudVisibleAlways && !isHoldingPickaxe(client)) return;

        if (compactMode) {
            renderCompact(graphics, client);
        } else {
            renderFull(graphics, client);
        }
    }

    private static boolean isHoldingPickaxe(Minecraft client) {
        if (client.player == null) return false;
        ItemStack mainHand = client.player.getMainHandItem();
        ItemStack offHand = client.player.getOffhandItem();
        return mainHand.is(holder -> holder.is(ItemTags.PICKAXES))
                || offHand.is(holder -> holder.is(ItemTags.PICKAXES));
    }

    private static void renderCompact(GuiGraphicsExtractor graphics, Minecraft client) {
        Font font = client.font;
        SessionData session = SessionData.getInstance();
        ModConfig config = ModConfig.getInstance();

        String text = I18n.get("miningstats.hud.compact", session.getTotalOres(), session.getTotalFortuneBonus());
        int textWidth = font.width(text);
        int padding = HudLayout.getPadding();
        int hudWidth = textWidth + padding * 2;
        int hudHeight = font.lineHeight + padding * 2;

        int x = HudLayout.getX(client.getWindow().getGuiScaledWidth(), hudWidth);
        int y = HudLayout.getY(client.getWindow().getGuiScaledHeight(), hudHeight);

        // Background
        int bgColor = ((int) (config.hudOpacity * 255) << 24);
        graphics.fill(x, y, x + hudWidth, y + hudHeight, bgColor);

        // Flash effect
        if (HudEffects.isFlashing()) {
            int flashColor = (((int) (HudEffects.getFlashAlpha() * 100)) << 24) | 0xFFD700;
            graphics.fill(x, y, x + hudWidth, y + hudHeight, flashColor);
        }

        // Text
        graphics.text(font, text, x + padding, y + padding, 0xFFFFFFFF, true);
    }

    private static void renderFull(GuiGraphicsExtractor graphics, Minecraft client) {
        Font font = client.font;
        SessionData session = SessionData.getInstance();
        ModConfig config = ModConfig.getInstance();

        int padding = HudLayout.getPadding();
        int lineHeight = Math.max(font.lineHeight, 16) + 2; // 16 for item icon height
        int iconSize = 16;
        int textOffsetX = iconSize + 4;

        // Get display counts (merged or separate based on config)
        boolean merge = config.mergeDeepslate;
        Map<OreType, Integer> displayCounts = merge
                ? session.getMergedOreCounts()
                : session.getOreCounts();
        Map<OreType, Integer> displayBonuses = merge
                ? session.getMergedFortuneBonuses()
                : null;

        // Calculate HUD dimensions
        int oreLines = 0;
        for (int count : displayCounts.values()) {
            if (count > 0) oreLines++;
        }

        // Title + ore lines (no more fortune total row)
        int contentLines = 1 + oreLines;
        int hudWidth = 220;
        int hudHeight = padding * 2 + contentLines * lineHeight;

        int x = HudLayout.getX(client.getWindow().getGuiScaledWidth(), hudWidth);
        int y = HudLayout.getY(client.getWindow().getGuiScaledHeight(), hudHeight);

        // Background
        int bgColor = ((int) (config.hudOpacity * 255) << 24);
        graphics.fill(x, y, x + hudWidth, y + hudHeight, bgColor);

        // Flash effect
        if (HudEffects.isFlashing()) {
            int flashAlpha = (int) (HudEffects.getFlashAlpha() * 100);
            graphics.fill(x - 2, y - 2, x + hudWidth + 2, y + hudHeight + 2, (flashAlpha << 24) | 0xFFD700);
            graphics.fill(x, y, x + hudWidth, y + hudHeight, bgColor);
        }

        int currentY = y + padding;

        // Reset message overlay
        if (HudEffects.isShowingResetMessage()) {
            String resetText = I18n.get("miningstats.hud.reset");
            int resetX = x + (hudWidth - font.width(resetText)) / 2;
            int resetY = y + (hudHeight - font.lineHeight) / 2;
            graphics.text(font, resetText, resetX, resetY, 0xFFFFFF00, true);
            return;
        }

        // Title (centered) with session status
        String title = session.isActive()
                ? I18n.get("miningstats.hud.title.active")
                : I18n.get("miningstats.hud.title.paused");
        int titleColor = session.isActive() ? 0xFFFFFFFF : 0xFFFFAA00;
        int titleX = x + (hudWidth - font.width(title)) / 2;
        graphics.text(font, title, titleX, currentY, titleColor, true);
        currentY += lineHeight;

        // Ore lines with icons and right-aligned counts
        int rightEdge = x + hudWidth - padding;

        for (Map.Entry<OreType, Integer> entry : displayCounts.entrySet()) {
            OreType type = entry.getKey();
            int count = entry.getValue();
            if (count <= 0) continue;

            // Draw item icon
            if (type.getDropItem() != null) {
                graphics.item(new ItemStack(type.getDropItem()), x + padding, currentY - 4);
            }

            // Draw ore name (left-aligned after icon)
            graphics.text(font, type.getDisplayName(), x + padding + textOffsetX, currentY, 0xFFFFFFFF, true);

            // Draw fortune bonus (right-aligned at edge) if > 0
            int fortuneBonus = displayBonuses != null
                    ? displayBonuses.getOrDefault(type, 0)
                    : session.getFortuneBonus(type);
            if (fortuneBonus > 0) {
                String bonusText = "(+" + fortuneBonus + ")";
                int bonusWidth = font.width(bonusText);
                graphics.text(font, bonusText, rightEdge - bonusWidth, currentY, 0xFFFFD700, true);
            }

            // Draw count (right-aligned before bonus column)
            String countText = String.valueOf(count);
            int countWidth = font.width(countText);
            int countX = rightEdge - 50 - countWidth; // 50px reserved for bonus column
            graphics.text(font, countText, countX, currentY, 0xFFFFFFFF, true);

            currentY += lineHeight;
        }
    }
}
