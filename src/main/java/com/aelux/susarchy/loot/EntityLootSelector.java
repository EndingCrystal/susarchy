package com.aelux.susarchy.loot;

import com.aelux.susarchy.SusArchaeology;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootTable;

public final class EntityLootSelector {
    private static final String DEFAULT = "entity_generic";

    private static final Set<String> CATEGORIES = Set.of(
            "entity_generic", "entity_zombie", "entity_skeleton", "entity_wither_skeleton",
            "entity_creeper", "entity_spider", "entity_enderman", "entity_villager",
            "entity_illager", "entity_witch", "entity_piglin", "entity_nether",
            "entity_animal", "entity_aquatic", "entity_arthropod", "entity_golem"
    );

    private static final Map<String, String> ENTITY_CATEGORIES;
    private static final Map<String, ResourceKey<LootTable>> TABLE_KEYS = new ConcurrentHashMap<>();

    static {
        Map<String, String> categories = new HashMap<>();
        add(categories, "entity_zombie", "zombie", "husk", "drowned", "zombie_villager");
        add(categories, "entity_skeleton", "skeleton", "stray", "bogged");
        add(categories, "entity_wither_skeleton", "wither_skeleton");
        add(categories, "entity_creeper", "creeper");
        add(categories, "entity_spider", "spider", "cave_spider");
        add(categories, "entity_enderman", "enderman");
        add(categories, "entity_villager", "villager", "wandering_trader");
        add(categories, "entity_illager", "pillager", "vindicator", "evoker", "illusioner");
        add(categories, "entity_witch", "witch");
        add(categories, "entity_piglin", "piglin", "piglin_brute", "zombified_piglin");
        add(categories, "entity_nether", "blaze", "ghast", "magma_cube", "hoglin", "zoglin", "strider");
        add(
                categories,
                "entity_animal",
                "cow", "mooshroom", "pig", "sheep", "chicken", "rabbit", "horse", "donkey",
                "mule", "llama", "trader_llama", "goat", "wolf", "cat", "ocelot", "fox", "panda",
                "polar_bear", "armadillo", "camel", "sniffer"
        );
        add(
                categories,
                "entity_aquatic",
                "cod", "salmon", "tropical_fish", "pufferfish", "squid", "glow_squid", "dolphin",
                "turtle", "axolotl", "guardian", "elder_guardian", "tadpole", "frog"
        );
        add(categories, "entity_arthropod", "bee", "silverfish", "endermite");
        add(categories, "entity_golem", "iron_golem", "snow_golem");
        ENTITY_CATEGORIES = Map.copyOf(categories);
    }

    private EntityLootSelector() {
    }

    public static ResourceKey<LootTable> select(EntityType<?> type) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        String category = DEFAULT;
        if (id != null && id.getNamespace().equals("minecraft")) {
            category = ENTITY_CATEGORIES.getOrDefault(id.getPath(), DEFAULT);
        }
        return table(category);
    }

    public static List<ResourceKey<LootTable>> allTableKeys() {
        return CATEGORIES.stream().map(EntityLootSelector::table).toList();
    }

    public static Map<String, String> entityCategories() {
        return ENTITY_CATEGORIES;
    }

    public static ResourceKey<LootTable> table(String category) {
        return TABLE_KEYS.computeIfAbsent(category, key -> ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(SusArchaeology.MOD_ID, "archaeology/" + key)
        ));
    }

    private static void add(Map<String, String> categories, String category, String... entityPaths) {
        for (String entityPath : entityPaths) {
            categories.put(entityPath, category);
        }
    }
}
