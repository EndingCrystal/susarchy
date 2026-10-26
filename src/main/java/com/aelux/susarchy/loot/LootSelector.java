package com.aelux.susarchy.loot;

import com.aelux.susarchy.SusArchaeology;
import com.aelux.susarchy.SusBlockDefinitions;
import com.aelux.susarchy.config.SusConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.LootTable;

public final class LootSelector {
    private static final String DEFAULT = "sus";

    private static final Set<String> CATEGORIES = Set.of(
            "sus",
            "forest", "plains", "taiga", "snowy", "jungle", "desert", "badlands", "savanna",
            "swamp", "ocean", "lush_caves", "dripstone_caves", "deep_dark", "mushroom",
            "nether", "crimson_forest", "warped_forest", "soul_sand_valley", "basalt_deltas", "end",
            "village", "mineshaft", "stronghold", "ancient_city", "trail_ruins", "ocean_monument",
            "ocean_ruin", "woodland_mansion", "desert_pyramid", "jungle_temple", "igloo",
            "ruined_portal", "shipwreck", "nether_fortress", "bastion", "end_city", "swamp_hut",
            "pillager_outpost", "trial_chambers", "nether_fossil",
            "bookshelf", "ore_coal", "ore_iron", "ore_copper", "ore_gold", "ore_redstone",
            "ore_emerald", "ore_lapis", "ore_diamond", "ore_nether_gold", "ore_quartz",
            "ore_ancient_debris"
    );

    private static final Set<ResourceLocation> SNOWY_BIOMES = Set.of(
            id("snowy_plains"),
            id("snowy_taiga"),
            id("snowy_beach"),
            id("ice_spikes"),
            id("frozen_river"),
            id("frozen_ocean"),
            id("deep_frozen_ocean"),
            id("snowy_slopes"),
            id("frozen_peaks"),
            id("jagged_peaks"),
            id("grove")
    );

    private static final ResourceLocation MUSHROOM_FIELDS = id("mushroom_fields");
    private static final ResourceLocation LUSH_CAVES = id("lush_caves");
    private static final ResourceLocation DRIPSTONE_CAVES = id("dripstone_caves");
    private static final ResourceLocation DEEP_DARK = id("deep_dark");
    private static final ResourceLocation DESERT = id("desert");
    private static final ResourceLocation SWAMP = id("swamp");
    private static final ResourceLocation MANGROVE_SWAMP = id("mangrove_swamp");

    private static final Map<ResourceLocation, String> NETHER_BIOMES = Map.of(
            id("crimson_forest"), "crimson_forest",
            id("warped_forest"), "warped_forest",
            id("soul_sand_valley"), "soul_sand_valley",
            id("basalt_deltas"), "basalt_deltas"
    );

    private static final Map<ResourceLocation, String> STRUCTURE_CATEGORIES = Map.ofEntries(
            Map.entry(id("village_plains"), "village"),
            Map.entry(id("village_desert"), "village"),
            Map.entry(id("village_savanna"), "village"),
            Map.entry(id("village_snowy"), "village"),
            Map.entry(id("village_taiga"), "village"),
            Map.entry(id("mineshaft"), "mineshaft"),
            Map.entry(id("mineshaft_mesa"), "mineshaft"),
            Map.entry(id("stronghold"), "stronghold"),
            Map.entry(id("ancient_city"), "ancient_city"),
            Map.entry(id("trail_ruins"), "trail_ruins"),
            Map.entry(id("monument"), "ocean_monument"),
            Map.entry(id("ocean_ruin_cold"), "ocean_ruin"),
            Map.entry(id("ocean_ruin_warm"), "ocean_ruin"),
            Map.entry(id("mansion"), "woodland_mansion"),
            Map.entry(id("desert_pyramid"), "desert_pyramid"),
            Map.entry(id("jungle_pyramid"), "jungle_temple"),
            Map.entry(id("igloo"), "igloo"),
            Map.entry(id("ruined_portal"), "ruined_portal"),
            Map.entry(id("ruined_portal_desert"), "ruined_portal"),
            Map.entry(id("ruined_portal_jungle"), "ruined_portal"),
            Map.entry(id("ruined_portal_mountain"), "ruined_portal"),
            Map.entry(id("ruined_portal_nether"), "ruined_portal"),
            Map.entry(id("ruined_portal_ocean"), "ruined_portal"),
            Map.entry(id("ruined_portal_swamp"), "ruined_portal"),
            Map.entry(id("shipwreck"), "shipwreck"),
            Map.entry(id("shipwreck_beached"), "shipwreck"),
            Map.entry(id("fortress"), "nether_fortress"),
            Map.entry(id("bastion_remnant"), "bastion"),
            Map.entry(id("end_city"), "end_city"),
            Map.entry(id("swamp_hut"), "swamp_hut"),
            Map.entry(id("pillager_outpost"), "pillager_outpost"),
            Map.entry(id("trial_chambers"), "trial_chambers"),
            Map.entry(id("nether_fossil"), "nether_fossil")
    );

    private static final Map<String, String> BLOCK_LOOT;

    static {
        Map<String, String> blockLoot = new HashMap<>();
        for (SusBlockDefinitions.Definition definition : SusBlockDefinitions.ALL) {
            if (definition.loot() != null) {
                blockLoot.put(definition.id(), definition.loot());
            }
        }
        BLOCK_LOOT = Map.copyOf(blockLoot);
    }

    private static final Map<String, ResourceKey<LootTable>> TABLE_KEYS = new ConcurrentHashMap<>();

    private LootSelector() {
    }

    public static ResourceKey<LootTable> select(ServerLevel level, BlockPos pos) {
        ResourceKey<LootTable> blockTable = blockTable(level.getBlockState(pos).getBlock());
        if (blockTable != null) {
            return blockTable;
        }
        if (!SusConfig.contextualLootEnabled()) {
            return table(DEFAULT);
        }

        String structure = structureCategory(level, pos);
        if (structure != null) {
            return table(structure);
        }
        return table(biomeCategory(level.getBiome(pos)));
    }

    public static ResourceKey<LootTable> defaultTable(Block block) {
        ResourceKey<LootTable> blockTable = blockTable(block);
        return blockTable == null ? table(DEFAULT) : blockTable;
    }

    public static List<ResourceKey<LootTable>> allTableKeys() {
        return CATEGORIES.stream().map(LootSelector::table).toList();
    }

    public static ResourceKey<LootTable> table(String category) {
        return TABLE_KEYS.computeIfAbsent(category, key -> ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(SusArchaeology.MOD_ID, "archaeology/" + key)
        ));
    }

    private static ResourceKey<LootTable> blockTable(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id == null || !SusArchaeology.MOD_ID.equals(id.getNamespace())) {
            return null;
        }
        String category = BLOCK_LOOT.get(id.getPath());
        return category == null ? null : table(category);
    }

    private static String structureCategory(ServerLevel level, BlockPos pos) {
        StructureManager structureManager = level.structureManager();
        try {
            if (!structureManager.hasAnyStructureAt(pos)) {
                return null;
            }
            StructureStart start = structureManager.getStructureWithPieceAt(
                    pos,
                    holder -> holder.unwrapKey()
                            .map(key -> STRUCTURE_CATEGORIES.containsKey(key.location()))
                            .orElse(false)
            );
            if (!start.isValid()) {
                return null;
            }
            Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            ResourceLocation structureId = registry.getKey(start.getStructure());
            return structureId == null ? null : STRUCTURE_CATEGORIES.get(structureId);
        } catch (RuntimeException exception) {
            SusArchaeology.LOGGER.debug("Could not resolve structure loot at {}", pos, exception);
            return null;
        }
    }

    private static String biomeCategory(Holder<Biome> biome) {
        ResourceLocation biomeId = biome.unwrapKey().map(key -> key.location()).orElse(null);

        if (biomeId != null) {
            if (biomeId.equals(LUSH_CAVES)) {
                return "lush_caves";
            }
            if (biomeId.equals(DRIPSTONE_CAVES)) {
                return "dripstone_caves";
            }
            if (biomeId.equals(DEEP_DARK)) {
                return "deep_dark";
            }
            if (biomeId.equals(MUSHROOM_FIELDS)) {
                return "mushroom";
            }
            String nether = NETHER_BIOMES.get(biomeId);
            if (nether != null) {
                return nether;
            }
        }

        if (biome.is(BiomeTags.IS_NETHER)) {
            return "nether";
        }
        if (biome.is(BiomeTags.IS_END)) {
            return "end";
        }
        if (biomeId != null && SNOWY_BIOMES.contains(biomeId)) {
            return "snowy";
        }
        if (biome.is(BiomeTags.IS_BADLANDS)) {
            return "badlands";
        }
        if (biomeId != null && biomeId.equals(DESERT)) {
            return "desert";
        }
        if (biome.is(BiomeTags.IS_JUNGLE)) {
            return "jungle";
        }
        if (biome.is(BiomeTags.IS_SAVANNA)) {
            return "savanna";
        }
        if (biomeId != null && (biomeId.equals(SWAMP) || biomeId.equals(MANGROVE_SWAMP))) {
            return "swamp";
        }
        if (biome.is(BiomeTags.IS_TAIGA)) {
            return "taiga";
        }
        if (biome.is(BiomeTags.IS_FOREST)) {
            return "forest";
        }
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH) || biome.is(BiomeTags.IS_RIVER)) {
            return "ocean";
        }
        return "plains";
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }
}
