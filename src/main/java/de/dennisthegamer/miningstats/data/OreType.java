package de.dennisthegamer.miningstats.data;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum OreType {
    COAL("Coal", 1, Items.COAL, null),
    IRON("Iron", 1, Items.RAW_IRON, null),
    COPPER("Copper", 1, Items.RAW_COPPER, null),
    GOLD("Gold", 1, Items.RAW_GOLD, null),
    REDSTONE("Redstone", 4, Items.REDSTONE, null),
    LAPIS("Lapis", 4, Items.LAPIS_LAZULI, null),
    DIAMOND("Diamond", 1, Items.DIAMOND, null),
    EMERALD("Emerald", 1, Items.EMERALD, null),
    QUARTZ("Quartz", 1, Items.QUARTZ, null),
    ANCIENT_DEBRIS("Ancient Debris", 1, Items.ANCIENT_DEBRIS, null),

    // Deepslate variants
    DEEPSLATE_COAL("Deepslate Coal", 1, Items.COAL, null),
    DEEPSLATE_IRON("Deepslate Iron", 1, Items.RAW_IRON, null),
    DEEPSLATE_COPPER("Deepslate Copper", 1, Items.RAW_COPPER, null),
    DEEPSLATE_GOLD("Deepslate Gold", 1, Items.RAW_GOLD, null),
    DEEPSLATE_REDSTONE("Deepslate Redstone", 4, Items.REDSTONE, null),
    DEEPSLATE_LAPIS("Deepslate Lapis", 4, Items.LAPIS_LAZULI, null),
    DEEPSLATE_DIAMOND("Deepslate Diamond", 1, Items.DIAMOND, null),
    DEEPSLATE_EMERALD("Deepslate Emerald", 1, Items.EMERALD, null);

    private final String displayName;
    private final int baseDrop;
    private final Item dropItem;
    private OreType baseType;

    OreType(String displayName, int baseDrop, Item dropItem, OreType baseType) {
        this.displayName = displayName;
        this.baseDrop = baseDrop;
        this.dropItem = dropItem;
        this.baseType = baseType;
    }

    static {
        DEEPSLATE_COAL.baseType = COAL;
        DEEPSLATE_IRON.baseType = IRON;
        DEEPSLATE_COPPER.baseType = COPPER;
        DEEPSLATE_GOLD.baseType = GOLD;
        DEEPSLATE_REDSTONE.baseType = REDSTONE;
        DEEPSLATE_LAPIS.baseType = LAPIS;
        DEEPSLATE_DIAMOND.baseType = DIAMOND;
        DEEPSLATE_EMERALD.baseType = EMERALD;
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

    public boolean isDeepslate() {
        return baseType != null;
    }

    public OreType getBaseType() {
        return baseType != null ? baseType : this;
    }
}
