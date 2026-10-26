package com.aelux.susarchy.config;

import com.aelux.susarchy.SusBlockDefinitions;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class SusConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ENABLE_NATURAL_REPLACEMENT;
    private static final ModConfigSpec.IntValue CHUNKS_PER_TICK;
    private static final ModConfigSpec.BooleanValue ENABLE_CONTEXTUAL_LOOT;
    private static final Map<String, ModConfigSpec.DoubleValue> REPLACEMENT_CHANCES = new LinkedHashMap<>();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("loot");
        ENABLE_CONTEXTUAL_LOOT = builder
                .comment("Use biome- and structure-specific archaeology loot for naturally generated suspicious blocks.")
                .define("enable_contextual_loot", true);
        builder.pop();

        builder.push("worldgen");
        ENABLE_NATURAL_REPLACEMENT = builder
                .comment("Replace naturally generated blocks with suspicious variants in newly generated chunks.")
                .define("enable_natural_replacement", true);
        CHUNKS_PER_TICK = builder
                .comment("How many newly generated chunks may be processed per server tick.")
                .defineInRange("chunks_per_tick", 2, 1, 64);
        builder.push("chances");
        builder.comment("One independent replacement chance per natural source block. 0 disables that entry.");
        for (SusBlockDefinitions.Definition definition : SusBlockDefinitions.ALL) {
            for (String source : definition.sources()) {
                String path = ResourceLocation.parse(source).getPath();
                ModConfigSpec.DoubleValue value = builder
                        .comment(source + " -> " + definition.id())
                        .defineInRange(path, definition.chance(), 0.0D, 1.0D);
                REPLACEMENT_CHANCES.put(path, value);
            }
        }
        builder.pop();
        builder.pop();

        SPEC = builder.build();
    }

    private SusConfig() {
    }

    public static boolean naturalReplacementEnabled() {
        return ENABLE_NATURAL_REPLACEMENT.get();
    }

    public static int chunksPerTick() {
        return CHUNKS_PER_TICK.get();
    }

    public static boolean contextualLootEnabled() {
        return ENABLE_CONTEXTUAL_LOOT.get();
    }

    public static double replacementChance(String sourcePath) {
        ModConfigSpec.DoubleValue value = REPLACEMENT_CHANCES.get(sourcePath);
        return value == null ? 0.0D : value.get();
    }

    public static Map<String, ModConfigSpec.DoubleValue> replacementChances() {
        return Map.copyOf(REPLACEMENT_CHANCES);
    }
}
