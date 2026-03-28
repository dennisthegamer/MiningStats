package com.miningstats.tracker;

import com.miningstats.MiningStatsClient;
import com.miningstats.data.OreRegistry;
import com.miningstats.data.OreType;
import com.miningstats.data.SessionData;
import com.miningstats.hud.HudEffects;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;

import java.util.Optional;

public class OreTracker {

    private static RegistryEntry<Enchantment> cachedSilkTouch;
    private static RegistryEntry<Enchantment> cachedFortune;

    private static void ensureEnchantmentCache() {
        if (cachedSilkTouch == null || cachedFortune == null) {
            var registry = MinecraftClient.getInstance().world.getRegistryManager()
                    .getOrThrow(RegistryKeys.ENCHANTMENT);
            cachedSilkTouch = registry.getOrThrow(Enchantments.SILK_TOUCH);
            cachedFortune = registry.getOrThrow(Enchantments.FORTUNE);
        }
    }

    public static void invalidateCache() {
        cachedSilkTouch = null;
        cachedFortune = null;
    }

    public static void onBlockBroken(BlockPos pos, BlockState state) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        // Only track when session is active
        if (!SessionData.getInstance().isActive()) return;

        // Check if player is holding a pickaxe
        ItemStack mainHand = player.getMainHandStack();
        if (!isPickaxe(mainHand)) return;

        // Check if the block is a tracked ore
        Optional<OreType> oreType = OreRegistry.getOreType(state.getBlock());
        if (oreType.isEmpty()) return;

        OreType type = oreType.get();
        SessionData session = SessionData.getInstance();

        // Increment ore count
        session.incrementOreCount(type);

        // Calculate fortune bonus
        boolean hasSilkTouch = hasSilkTouch(mainHand);
        if (!hasSilkTouch && type.hasFortuneEffect()) {
            int fortuneLevel = getFortuneLevel(mainHand);
            if (fortuneLevel > 0) {
                FortuneTracker.scheduleDropCheck(pos, type, fortuneLevel);
            }
        }

        // Trigger flash effect for rare ores (including deepslate variants)
        OreType baseType = type.getBaseType();
        if (baseType == OreType.DIAMOND || baseType == OreType.ANCIENT_DEBRIS) {
            HudEffects.triggerFlash();
        }

        // Check milestones (use merged count so deepslate contributes)
        checkMilestones(baseType, session.getMergedOreCount(baseType));

        MiningStatsClient.LOGGER.debug("Mined {} (total: {})", type.getDisplayName(), session.getOreCount(type));
    }

    private static boolean isPickaxe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.isIn(net.minecraft.registry.tag.ItemTags.PICKAXES);
    }

    private static boolean hasSilkTouch(ItemStack stack) {
        ensureEnchantmentCache();
        return EnchantmentHelper.getLevel(cachedSilkTouch, stack) > 0;
    }

    private static int getFortuneLevel(ItemStack stack) {
        ensureEnchantmentCache();
        return EnchantmentHelper.getLevel(cachedFortune, stack);
    }

    private static void checkMilestones(OreType type, int count) {
        com.miningstats.config.ModConfig config = com.miningstats.config.ModConfig.getInstance();
        int threshold = config.getMilestoneThreshold(type);
        if (threshold > 0 && count > 0 && count % threshold == 0) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(
                        Text.translatable("miningstats.milestone", count, type.getDisplayName())
                                .styled(style -> style.withColor(0xFFD700)),
                        false
                );
            }
        }
    }
}
