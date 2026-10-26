package com.aelux.susarchy;

import com.aelux.susarchy.attachment.SusAttachments;
import com.aelux.susarchy.config.SusConfig;
import com.aelux.susarchy.event.NaturalReplacementHandler;
import com.aelux.susarchy.event.SuspiciousSpawnerEvents;
import com.aelux.susarchy.registry.ModContent;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import org.slf4j.Logger;

@Mod(SusArchaeology.MOD_ID)
public final class SusArchaeology {
    public static final String MOD_ID = "susarchy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SusArchaeology(IEventBus modBus, ModContainer modContainer) {
        ModContent.BLOCKS.register(modBus);
        ModContent.ITEMS.register(modBus);
        ModContent.CREATIVE_TABS.register(modBus);
        SusAttachments.ATTACHMENTS.register(modBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, SusConfig.SPEC);
        modBus.addListener(SusArchaeology::addBrushableBlocks);

        NeoForge.EVENT_BUS.addListener(NaturalReplacementHandler::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(NaturalReplacementHandler::onServerTick);
        NeoForge.EVENT_BUS.addListener(SuspiciousSpawnerEvents::onFinalizeSpawn);
    }

    private static void addBrushableBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.BRUSHABLE_BLOCK, ModContent.brushableBlocks());
        event.modify(BlockEntityType.MOB_SPAWNER, ModContent.suspiciousSpawners());
    }
}
