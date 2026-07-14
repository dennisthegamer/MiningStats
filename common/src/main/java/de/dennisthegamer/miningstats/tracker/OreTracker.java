package de.dennisthegamer.miningstats.tracker;

import de.dennisthegamer.miningstats.MiningStatsClient;
import de.dennisthegamer.miningstats.data.OreRegistry;
import de.dennisthegamer.miningstats.data.OreType;
import de.dennisthegamer.miningstats.data.SessionData;
import de.dennisthegamer.miningstats.hud.HudEffects;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class OreTracker {

    private static Holder<Enchantment> cachedSilkTouch;
    private static Holder<Enchantment> cachedFortune;

    private static void ensureEnchantmentCache() {
        if (cachedSilkTouch == null || cachedFortune == null) {
            var registry = Minecraft.getInstance().level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT);
            cachedSilkTouch = registry.getOrThrow(Enchantments.SILK_TOUCH);
            cachedFortune = registry.getOrThrow(Enchantments.FORTUNE);
        }
    }

    public static void invalidateCache() {
        cachedSilkTouch = null;
        cachedFortune = null;
    }

    public static void onBlockBroken(BlockPos pos, BlockState state) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;

        // Only track when session is active
        if (!SessionData.getInstance().isActive()) return;

        // Check if player is holding a pickaxe
        ItemStack mainHand = player.getMainHandItem();
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
        return stack.is(net.minecraft.tags.ItemTags.PICKAXES);
    }

    private static boolean hasSilkTouch(ItemStack stack) {
        ensureEnchantmentCache();
        return EnchantmentHelper.getItemEnchantmentLevel(cachedSilkTouch, stack) > 0;
    }

    private static int getFortuneLevel(ItemStack stack) {
        ensureEnchantmentCache();
        return EnchantmentHelper.getItemEnchantmentLevel(cachedFortune, stack);
    }

    private static void checkMilestones(OreType type, int count) {
        de.dennisthegamer.miningstats.config.ModConfig config = de.dennisthegamer.miningstats.config.ModConfig.getInstance();
        int threshold = config.getMilestoneThreshold(type);
        if (threshold > 0 && count > 0 && count % threshold == 0) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                client.player.displayClientMessage(
                        Component.translatable("miningstats.milestone", count, type.getDisplayName())
                                .withStyle(style -> style.withColor(0xFFD700)),
                        false
                );
            }
        }
    }
}
