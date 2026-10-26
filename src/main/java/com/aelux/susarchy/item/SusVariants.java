package com.aelux.susarchy.item;

import com.aelux.susarchy.SusArchaeology;
import com.aelux.susarchy.SusBlockDefinitions;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class SusVariants {
    private static volatile Map<Block, Block> variants;

    private SusVariants() {
    }

    @Nullable
    public static Block target(Block source) {
        return variants().get(source);
    }

    @Nullable
    public static BlockState replacement(BlockState source) {
        Block target = target(source.getBlock());
        if (target == null) {
            return null;
        }
        BlockState replacement = target.defaultBlockState();
        for (Property<?> property : source.getProperties()) {
            if (replacement.hasProperty(property)) {
                replacement = copyProperty(source, replacement, property);
            }
        }
        return replacement;
    }

    private static Map<Block, Block> variants() {
        Map<Block, Block> current = variants;
        if (current != null) {
            return current;
        }
        synchronized (SusVariants.class) {
            current = variants;
            if (current != null) {
                return current;
            }
            Map<Block, Block> built = new HashMap<>();
            for (SusBlockDefinitions.Definition definition : SusBlockDefinitions.ALL) {
                Block target = BuiltInRegistries.BLOCK.get(
                        ResourceLocation.fromNamespaceAndPath(SusArchaeology.MOD_ID, definition.id())
                );
                if (target == null) {
                    continue;
                }
                for (String sourceId : definition.sources()) {
                    Block source = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(sourceId));
                    if (source != null) {
                        built.put(source, target);
                    }
                }
            }
            variants = Map.copyOf(built);
            return variants;
        }
    }

    private static <T extends Comparable<T>> BlockState copyProperty(
            BlockState source,
            BlockState target,
            Property<T> property
    ) {
        return target.setValue(property, source.getValue(property));
    }
}
