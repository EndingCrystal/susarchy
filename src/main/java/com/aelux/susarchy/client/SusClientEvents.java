package com.aelux.susarchy.client;

import com.aelux.susarchy.SusArchaeology;
import com.aelux.susarchy.registry.ModContent;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(
        modid = SusArchaeology.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class SusClientEvents {
    private SusClientEvents() {
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> tintIndex == 0
                        ? (level != null && pos != null
                        ? BiomeColors.getAverageGrassColor(level, pos)
                        : GrassColor.getDefaultColor())
                        : -1,
                ModContent.block("sus_grass_block")
        );

        event.register(
                (state, level, pos, tintIndex) -> tintIndex == 0
                        ? (level != null && pos != null
                        ? BiomeColors.getAverageFoliageColor(level, pos)
                        : FoliageColor.getDefaultColor())
                        : -1,
                ModContent.block("sus_oak_leaves"),
                ModContent.block("sus_jungle_leaves"),
                ModContent.block("sus_acacia_leaves"),
                ModContent.block("sus_dark_oak_leaves")
        );

        event.register(
                (state, level, pos, tintIndex) -> tintIndex == 0
                        ? (level != null && pos != null
                        ? BiomeColors.getAverageFoliageColor(level, pos)
                        : FoliageColor.getMangroveColor())
                        : -1,
                ModContent.block("sus_mangrove_leaves")
        );

        event.register(
                (state, level, pos, tintIndex) -> tintIndex == 0
                        ? FoliageColor.getEvergreenColor()
                        : -1,
                ModContent.block("sus_spruce_leaves")
        );

        event.register(
                (state, level, pos, tintIndex) -> tintIndex == 0
                        ? FoliageColor.getBirchColor()
                        : -1,
                ModContent.block("sus_birch_leaves")
        );
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        BlockColors blockColors = event.getBlockColors();
        ItemColor delegate = (stack, tintIndex) -> {
            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                return -1;
            }
            return blockColors.getColor(blockItem.getBlock().defaultBlockState(), null, null, tintIndex);
        };

        Block[] tinted = {
                ModContent.block("sus_grass_block"),
                ModContent.block("sus_oak_leaves"),
                ModContent.block("sus_spruce_leaves"),
                ModContent.block("sus_birch_leaves"),
                ModContent.block("sus_jungle_leaves"),
                ModContent.block("sus_acacia_leaves"),
                ModContent.block("sus_dark_oak_leaves"),
                ModContent.block("sus_mangrove_leaves")
        };
        event.register(delegate, tinted);
    }
}
