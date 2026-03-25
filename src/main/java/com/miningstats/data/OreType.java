package com.miningstats.data;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum OreType {
    COAL("Coal", 1, Items.COAL),
    IRON("Iron", 1, Items.RAW_IRON),
    COPPER("Copper", 1, Items.RAW_COPPER),
    GOLD("Gold", 1, Items.RAW_GOLD),
    REDSTONE("Redstone", 4, Items.REDSTONE),
    LAPIS("Lapis", 4, Items.LAPIS_LAZULI),
    DIAMOND("Diamond", 1, Items.DIAMOND),
    EMERALD("Emerald", 1, Items.EMERALD),
    QUARTZ("Quartz", 1, Items.QUARTZ),
    ANCIENT_DEBRIS("Ancient Debris", 1, Items.ANCIENT_DEBRIS);

    private final String displayName;
    private final int baseDrop;
    private final Item dropItem;

    OreType(String displayName, int baseDrop, Item dropItem) {
        this.displayName = displayName;
        this.baseDrop = baseDrop;
        this.dropItem = dropItem;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBaseDrop() {
        return baseDrop;
    }

    public Item getDropItem() {
        return dropItem;
    }

    public boolean hasFortuneEffect() {
        return this != ANCIENT_DEBRIS;
    }
}
