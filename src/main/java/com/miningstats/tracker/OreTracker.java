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
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class OreTracker {

    public static void onBlockBroken(BlockPos pos, BlockState state) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

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

        // Trigger flash effect for rare ores
        if (type == OreType.DIAMOND || type == OreType.ANCIENT_DEBRIS) {
            HudEffects.triggerFlash();
        }

        // Check milestones
        checkMilestones(type, session.getOreCount(type));

        MiningStatsClient.LOGGER.debug("Mined {} (total: {})", type.getDisplayName(), session.getOreCount(type));
    }

    private static boolean isPickaxe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        // Check using the item's mining tags - pickaxes are in the minecraft:pickaxes tag
        return stack.isIn(net.minecraft.registry.tag.ItemTags.PICKAXES);
    }

    private static boolean hasSilkTouch(ItemStack stack) {
        return EnchantmentHelper.getLevel(
                MinecraftClient.getInstance().world.getRegistryManager()
                        .getOrThrow(RegistryKeys.ENCHANTMENT)
                        .getOrThrow(Enchantments.SILK_TOUCH),
                stack
        ) > 0;
    }

    private static int getFortuneLevel(ItemStack stack) {
        return EnchantmentHelper.getLevel(
                MinecraftClient.getInstance().world.getRegistryManager()
                        .getOrThrow(RegistryKeys.ENCHANTMENT)
                        .getOrThrow(Enchantments.FORTUNE),
                stack
        );
    }

    private static void checkMilestones(OreType type, int count) {
        com.miningstats.config.ModConfig config = com.miningstats.config.ModConfig.getInstance();
        int threshold = config.getMilestoneThreshold(type);
        if (threshold > 0 && count > 0 && count % threshold == 0) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(
                        net.minecraft.text.Text.literal("\u26CF Meilenstein: " + count + " " + type.getDisplayName() + " abgebaut!")
                                .styled(style -> style.withColor(0xFFD700)),
                        false
                );
            }
        }
    }
}
