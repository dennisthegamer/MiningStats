package com.miningstats.tracker;

import com.miningstats.data.OreType;
import com.miningstats.data.SessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import com.miningstats.mixin.ItemEntityAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class FortuneTracker {

    private static BlockPos pendingPos = null;
    private static OreType pendingType = null;
    private static int pendingFortuneLevel = 0;
    private static int ticksWaiting = 0;

    public static void scheduleDropCheck(BlockPos pos, OreType type, int fortuneLevel) {
        pendingPos = pos;
        pendingType = type;
        pendingFortuneLevel = fortuneLevel;
        ticksWaiting = 0;
    }

    public static void tick() {
        if (pendingPos == null) return;

        ticksWaiting++;

        // Wait 2 ticks for items to spawn, then count them
        if (ticksWaiting >= 2) {
            countDrops();
            pendingPos = null;
            pendingType = null;
            pendingFortuneLevel = 0;
            ticksWaiting = 0;
        }
    }

    private static void countDrops() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || pendingType == null || pendingPos == null) return;

        Item expectedItem = pendingType.getDropItem();
        if (expectedItem == null) return;

        // Search for item entities near the broken block (only freshly spawned ones)
        AABB searchBox = new AABB(pendingPos).inflate(2.0);
        List<ItemEntity> items = client.level.getEntitiesOfClass(
                ItemEntity.class, searchBox,
                entity -> entity.getItem().is(expectedItem) && ((ItemEntityAccessor) (Object) entity).miningStats$getAge() <= 5
        );

        int actualDrops = 0;
        for (ItemEntity itemEntity : items) {
            actualDrops += itemEntity.getItem().getCount();
        }

        int baseDrop = pendingType.getBaseDrop();
        int bonus = Math.max(0, actualDrops - baseDrop);

        if (bonus > 0) {
            SessionData.getInstance().addFortuneBonus(pendingType, bonus);
        }
    }
}
