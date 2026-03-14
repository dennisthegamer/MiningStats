package com.miningstats.hud;

import com.miningstats.config.ModConfig;
import com.miningstats.data.OreType;
import com.miningstats.data.SessionData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

public class HudRenderer {

    private static boolean compactMode = false;

    public static void toggleCompactMode() {
        compactMode = !compactMode;
    }

    public static boolean isCompactMode() {
        return compactMode;
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        ModConfig config = ModConfig.getInstance();

        // Check visibility: only show when holding pickaxe (unless always visible)
        if (!config.hudVisibleAlways && !isHoldingPickaxe(client)) return;

        if (compactMode) {
            renderCompact(context, client);
        } else {
            renderFull(context, client);
        }
    }

    private static boolean isHoldingPickaxe(MinecraftClient client) {
        if (client.player == null) return false;
        ItemStack mainHand = client.player.getMainHandStack();
        ItemStack offHand = client.player.getOffHandStack();
        return mainHand.isIn(net.minecraft.registry.tag.ItemTags.PICKAXES)
                || offHand.isIn(net.minecraft.registry.tag.ItemTags.PICKAXES);
    }

    private static void renderCompact(DrawContext context, MinecraftClient client) {
        TextRenderer textRenderer = client.textRenderer;
        SessionData session = SessionData.getInstance();
        ModConfig config = ModConfig.getInstance();

        String text = I18n.translate("miningstats.hud.compact", session.getTotalOres(), session.getTotalFortuneBonus());
        int textWidth = textRenderer.getWidth(text);
        int padding = HudLayout.getPadding();
        int hudWidth = textWidth + padding * 2;
        int hudHeight = textRenderer.fontHeight + padding * 2;

        int x = HudLayout.getX(client.getWindow().getScaledWidth(), hudWidth);
        int y = HudLayout.getY(client.getWindow().getScaledHeight(), hudHeight);

        // Background
        int bgColor = ((int) (config.hudOpacity * 255) << 24);
        context.fill(x, y, x + hudWidth, y + hudHeight, bgColor);

        // Flash effect
        if (HudEffects.isFlashing()) {
            int flashColor = (((int) (HudEffects.getFlashAlpha() * 100)) << 24) | 0xFFD700;
            context.fill(x, y, x + hudWidth, y + hudHeight, flashColor);
        }

        // Text
        context.drawText(textRenderer, text, x + padding, y + padding, 0xFFFFFFFF, true);
    }

    private static void renderFull(DrawContext context, MinecraftClient client) {
        TextRenderer textRenderer = client.textRenderer;
        SessionData session = SessionData.getInstance();
        ModConfig config = ModConfig.getInstance();

        int padding = HudLayout.getPadding();
        int lineHeight = Math.max(textRenderer.fontHeight, 16) + 2; // 16 for item icon height
        int iconSize = 16;
        int textOffsetX = iconSize + 4;

        // Calculate HUD dimensions
        int oreLines = 0;
        for (OreType type : OreType.values()) {
            if (session.getOreCount(type) > 0) oreLines++;
        }

        // Title + ore lines + fortune
        int contentLines = 1 + oreLines + 1;
        int hudWidth = 180;
        int hudHeight = padding * 2 + contentLines * lineHeight;

        int x = HudLayout.getX(client.getWindow().getScaledWidth(), hudWidth);
        int y = HudLayout.getY(client.getWindow().getScaledHeight(), hudHeight);

        // Background
        int bgColor = ((int) (config.hudOpacity * 255) << 24);
        context.fill(x, y, x + hudWidth, y + hudHeight, bgColor);

        // Flash effect
        if (HudEffects.isFlashing()) {
            int flashAlpha = (int) (HudEffects.getFlashAlpha() * 100);
            context.fill(x - 2, y - 2, x + hudWidth + 2, y + hudHeight + 2, (flashAlpha << 24) | 0xFFD700);
            context.fill(x, y, x + hudWidth, y + hudHeight, bgColor);
        }

        int currentY = y + padding;

        // Reset message overlay
        if (HudEffects.isShowingResetMessage()) {
            String resetText = I18n.translate("miningstats.hud.reset");
            int resetX = x + (hudWidth - textRenderer.getWidth(resetText)) / 2;
            int resetY = y + (hudHeight - textRenderer.fontHeight) / 2;
            context.drawText(textRenderer, resetText, resetX, resetY, 0xFFFFFF00, true);
            return;
        }

        // Title (centered) with session status
        String title = session.isActive()
                ? I18n.translate("miningstats.hud.title.active")
                : I18n.translate("miningstats.hud.title.paused");
        int titleColor = session.isActive() ? 0xFFFFFFFF : 0xFFFFAA00;
        int titleX = x + (hudWidth - textRenderer.getWidth(title)) / 2;
        context.drawText(textRenderer, title, titleX, currentY, titleColor, true);
        currentY += lineHeight;

        // Ore lines with icons and right-aligned counts
        int rightEdge = x + hudWidth - padding;

        for (OreType type : OreType.values()) {
            int count = session.getOreCount(type);
            if (count <= 0) continue;

            // Draw item icon
            if (type.getDropItem() != null) {
                context.drawItem(new ItemStack(type.getDropItem()), x + padding, currentY - 4);
            }

            // Draw ore name (left-aligned after icon)
            context.drawText(textRenderer, type.getDisplayName(), x + padding + textOffsetX, currentY, 0xFFFFFFFF, true);

            // Draw count (right-aligned)
            String countText = String.valueOf(count);
            int countWidth = textRenderer.getWidth(countText);
            context.drawText(textRenderer, countText, rightEdge - countWidth, currentY, 0xFFFFFFFF, true);

            currentY += lineHeight;
        }

        // Fortune Bonus with shamrock symbol
        String fortuneText = I18n.translate("miningstats.hud.fortune", session.getTotalFortuneBonus());
        int fortuneX = x + (hudWidth - textRenderer.getWidth(fortuneText)) / 2;
        context.drawText(textRenderer, fortuneText, fortuneX, currentY, 0xFFFFD700, true);
    }
}
