package de.dennisthegamer.miningstats.data;

import de.dennisthegamer.miningstats.platform.Platforms;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OreRegistry {

    private static final Map<Block, OreType> BLOCK_TO_ORE = new HashMap<>();
    // Custom ores keyed by "namespace:path" string (not the mojmap id class, which was
    // renamed 1.21.10 -> 1.21.11 and would break the NeoForge jar across the range).
    private static final Map<String, OreType> CUSTOM_ORES = new HashMap<>();

    static {
        // Normal ores
        register(Blocks.COAL_ORE, OreType.COAL);
        register(Blocks.IRON_ORE, OreType.IRON);
        register(Blocks.GOLD_ORE, OreType.GOLD);
        register(Blocks.NETHER_GOLD_ORE, OreType.GOLD);
        register(Blocks.DIAMOND_ORE, OreType.DIAMOND);
        register(Blocks.EMERALD_ORE, OreType.EMERALD);
        register(Blocks.LAPIS_ORE, OreType.LAPIS);
        register(Blocks.REDSTONE_ORE, OreType.REDSTONE);
        register(Blocks.COPPER_ORE, OreType.COPPER);
        register(Blocks.NETHER_QUARTZ_ORE, OreType.QUARTZ);
        register(Blocks.ANCIENT_DEBRIS, OreType.ANCIENT_DEBRIS);

        // Deepslate variants (tracked separately, merged at display level via config)
        register(Blocks.DEEPSLATE_COAL_ORE, OreType.DEEPSLATE_COAL);
        register(Blocks.DEEPSLATE_IRON_ORE, OreType.DEEPSLATE_IRON);
        register(Blocks.DEEPSLATE_GOLD_ORE, OreType.DEEPSLATE_GOLD);
        register(Blocks.DEEPSLATE_DIAMOND_ORE, OreType.DEEPSLATE_DIAMOND);
        register(Blocks.DEEPSLATE_EMERALD_ORE, OreType.DEEPSLATE_EMERALD);
        register(Blocks.DEEPSLATE_LAPIS_ORE, OreType.DEEPSLATE_LAPIS);
        register(Blocks.DEEPSLATE_REDSTONE_ORE, OreType.DEEPSLATE_REDSTONE);
        register(Blocks.DEEPSLATE_COPPER_ORE, OreType.DEEPSLATE_COPPER);
    }

    private static void register(Block block, OreType type) {
        BLOCK_TO_ORE.put(block, type);
    }

    public static Optional<OreType> getOreType(Block block) {
        OreType type = BLOCK_TO_ORE.get(block);
        if (type != null) {
            return Optional.of(type);
        }
        String id = Platforms.get().blockId(block);
        type = CUSTOM_ORES.get(id);
        return Optional.ofNullable(type);
    }

    public static void loadCustomOres(List<String> oreIds) {
        CUSTOM_ORES.clear();
        for (String oreId : oreIds) {
            if (oreId == null || oreId.isBlank()) continue;
            String norm = oreId.contains(":") ? oreId : "minecraft:" + oreId;
            CUSTOM_ORES.put(norm, OreType.COAL); // Custom ores default to baseDrop=1
        }
    }
}
