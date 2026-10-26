package com.aelux.susarchy.compat.jei;

import java.util.List;
import java.util.Map;

public final class SusEntityLootData {
    public record Loot(String item, int weight, int min, int max) {
    }

    public static final Map<String, List<Loot>> LOOT = Map.ofEntries(
            Map.entry("entity_generic", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:dirt", 3, 1, 2), new Loot("minecraft:gravel", 3, 1, 2), new Loot("minecraft:bone", 4, 1, 2), new Loot("minecraft:stick", 3, 1, 2), new Loot("minecraft:flint", 3, 1, 1), new Loot("minecraft:clay_ball", 2, 1, 2), new Loot("minecraft:coal", 1, 1, 1), new Loot("minecraft:emerald", 1, 1, 1))),
            Map.entry("entity_zombie", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:rotten_flesh", 6, 1, 3), new Loot("minecraft:bone", 4, 1, 2), new Loot("minecraft:iron_nugget", 3, 1, 2), new Loot("minecraft:potato", 2, 1, 1), new Loot("minecraft:carrot", 2, 1, 1), new Loot("minecraft:clay_ball", 2, 1, 2), new Loot("minecraft:iron_ingot", 1, 1, 1), new Loot("minecraft:golden_carrot", 1, 1, 1))),
            Map.entry("entity_skeleton", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:bone", 6, 1, 3), new Loot("minecraft:arrow", 4, 1, 3), new Loot("minecraft:flint", 3, 1, 2), new Loot("minecraft:string", 2, 1, 2), new Loot("minecraft:coal", 2, 1, 1), new Loot("minecraft:bone_meal", 2, 1, 2), new Loot("minecraft:bone_block", 1, 1, 1))),
            Map.entry("entity_wither_skeleton", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:bone", 6, 1, 3), new Loot("minecraft:coal", 4, 1, 2), new Loot("minecraft:blackstone", 3, 1, 2), new Loot("minecraft:charcoal", 2, 1, 2), new Loot("minecraft:wither_skeleton_skull", 1, 1, 1))),
            Map.entry("entity_creeper", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:gunpowder", 6, 1, 3), new Loot("minecraft:coal", 3, 1, 2), new Loot("minecraft:flint", 2, 1, 2), new Loot("minecraft:tnt", 1, 1, 1), new Loot("minecraft:creeper_head", 1, 1, 1))),
            Map.entry("entity_spider", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:string", 6, 1, 3), new Loot("minecraft:spider_eye", 4, 1, 2), new Loot("minecraft:cobweb", 3, 1, 2), new Loot("minecraft:fermented_spider_eye", 1, 1, 1), new Loot("minecraft:emerald", 1, 1, 1))),
            Map.entry("entity_enderman", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:ender_pearl", 4, 1, 2), new Loot("minecraft:chorus_fruit", 4, 1, 2), new Loot("minecraft:end_stone", 3, 1, 2), new Loot("minecraft:amethyst_shard", 2, 1, 2), new Loot("minecraft:ender_eye", 1, 1, 1))),
            Map.entry("entity_villager", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:book", 4, 1, 2), new Loot("minecraft:paper", 4, 1, 3), new Loot("minecraft:emerald", 3, 1, 2), new Loot("minecraft:bread", 3, 1, 2), new Loot("minecraft:iron_nugget", 2, 1, 2), new Loot("minecraft:name_tag", 1, 1, 1))),
            Map.entry("entity_illager", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:emerald", 4, 1, 2), new Loot("minecraft:arrow", 3, 1, 3), new Loot("minecraft:iron_axe", 1, 1, 1), new Loot("minecraft:dark_oak_planks", 3, 1, 2), new Loot("minecraft:ominous_bottle", 1, 1, 1), new Loot("minecraft:totem_of_undying", 1, 1, 1))),
            Map.entry("entity_witch", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:redstone", 4, 1, 2), new Loot("minecraft:glowstone_dust", 3, 1, 2), new Loot("minecraft:sugar", 3, 1, 2), new Loot("minecraft:spider_eye", 2, 1, 1), new Loot("minecraft:glass_bottle", 3, 1, 2), new Loot("minecraft:gunpowder", 2, 1, 2), new Loot("minecraft:experience_bottle", 1, 1, 1))),
            Map.entry("entity_piglin", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:gold_nugget", 5, 1, 3), new Loot("minecraft:gold_ingot", 3, 1, 2), new Loot("minecraft:raw_gold", 2, 1, 2), new Loot("minecraft:leather", 2, 1, 2), new Loot("minecraft:fire_charge", 1, 1, 1), new Loot("minecraft:gold_block", 1, 1, 1))),
            Map.entry("entity_nether", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:netherrack", 5, 1, 2), new Loot("minecraft:quartz", 4, 1, 2), new Loot("minecraft:gold_nugget", 3, 1, 2), new Loot("minecraft:magma_cream", 2, 1, 1), new Loot("minecraft:blaze_powder", 2, 1, 1), new Loot("minecraft:glowstone_dust", 3, 1, 2), new Loot("minecraft:ghast_tear", 1, 1, 1))),
            Map.entry("entity_animal", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:leather", 4, 1, 2), new Loot("minecraft:feather", 4, 1, 3), new Loot("minecraft:wheat", 4, 1, 3), new Loot("minecraft:wheat_seeds", 3, 1, 2), new Loot("minecraft:white_wool", 3, 1, 2), new Loot("minecraft:carrot", 2, 1, 2), new Loot("minecraft:egg", 2, 1, 1), new Loot("minecraft:golden_apple", 1, 1, 1))),
            Map.entry("entity_aquatic", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:cod", 4, 1, 2), new Loot("minecraft:salmon", 4, 1, 2), new Loot("minecraft:tropical_fish", 3, 1, 2), new Loot("minecraft:kelp", 3, 1, 2), new Loot("minecraft:ink_sac", 2, 1, 2), new Loot("minecraft:prismarine_shard", 2, 1, 2), new Loot("minecraft:nautilus_shell", 1, 1, 1))),
            Map.entry("entity_arthropod", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:string", 4, 1, 3), new Loot("minecraft:spider_eye", 3, 1, 2), new Loot("minecraft:honeycomb", 3, 1, 2), new Loot("minecraft:glowstone_dust", 2, 1, 2), new Loot("minecraft:slime_ball", 1, 1, 1), new Loot("minecraft:emerald", 1, 1, 1))),
            Map.entry("entity_golem", List.of(new Loot("susarchy:sus_dust", 3, 1, 2), new Loot("minecraft:iron_ingot", 4, 1, 2), new Loot("minecraft:poppy", 4, 1, 2), new Loot("minecraft:snowball", 4, 1, 3), new Loot("minecraft:pumpkin", 2, 1, 2), new Loot("minecraft:iron_block", 1, 1, 1)))
    );

    private SusEntityLootData() {
    }
}
