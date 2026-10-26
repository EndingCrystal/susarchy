package com.aelux.susarchy.event;

import com.aelux.susarchy.SusArchaeology;
import com.aelux.susarchy.SusBlockDefinitions;
import com.aelux.susarchy.block.SusSpawnerBlock;
import com.aelux.susarchy.config.SusConfig;
import com.aelux.susarchy.loot.LootSelector;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class NaturalReplacementHandler {
    private static final ConcurrentLinkedQueue<PendingChunk> PENDING = new ConcurrentLinkedQueue<>();
    private static volatile Map<Block, ReplacementRule> rules;

    private NaturalReplacementHandler() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !event.isNewChunk()) {
            return;
        }
        if (!SusConfig.naturalReplacementEnabled()) {
            return;
        }
        PENDING.add(new PendingChunk(level, event.getChunk().getPos()));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING.isEmpty()) {
            return;
        }
        int budget = SusConfig.chunksPerTick();
        for (int i = 0; i < budget; i++) {
            PendingChunk pending = PENDING.poll();
            if (pending == null) {
                return;
            }
            processChunk(pending.level(), pending.pos());
        }
    }

    private static void processChunk(ServerLevel level, ChunkPos pos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
        if (chunk == null) {
            return;
        }

        Map<Block, ReplacementRule> currentRules = ensureRules();
        if (currentRules.isEmpty()) {
            return;
        }

        RandomSource random = RandomSource.create(level.getSeed() ^ pos.toLong());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        LevelChunkSection[] sections = chunk.getSections();
        int minSection = level.getMinSection();
        int replaced = 0;

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section.hasOnlyAir()) {
                continue;
            }
            int sectionY = (minSection + sectionIndex) * 16;
            for (int localY = 0; localY < 16; localY++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        BlockState state = section.getBlockState(localX, localY, localZ);
                        if (state.isAir()) {
                            continue;
                        }
                        ReplacementRule rule = currentRules.get(state.getBlock());
                        if (rule == null || random.nextDouble() >= rule.chance()) {
                            continue;
                        }

                        cursor.set(
                                pos.getMinBlockX() + localX,
                                sectionY + localY,
                                pos.getMinBlockZ() + localZ
                        );
                        BlockState replacement = rule.state();
                        for (Property<?> property : state.getProperties()) {
                            if (replacement.hasProperty(property)) {
                                replacement = copyProperty(state, replacement, property);
                            }
                        }
                        if (replaceBlock(level, cursor, replacement)) {
                            replaced++;
                            if (level.getBlockEntity(cursor) instanceof BrushableBlockEntity brushable) {
                                brushable.setLootTable(LootSelector.select(level, cursor), cursor.asLong());
                            }
                        }
                    }
                }
            }
        }

        if (replaced > 0 && SusArchaeology.LOGGER.isDebugEnabled()) {
            SusArchaeology.LOGGER.debug("Made {} blocks suspicious in chunk {}", replaced, pos);
        }
    }

    public static boolean replaceBlock(ServerLevel level, BlockPos pos, BlockState replacement) {
        CompoundTag spawnerData = null;
        if (replacement.getBlock() instanceof SusSpawnerBlock
                && level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawnerData = spawner.getSpawner().save(new CompoundTag());
        }

        if (!level.setBlock(pos, replacement, 2)) {
            return false;
        }

        if (spawnerData != null
                && level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().load(level, pos, spawnerData);
            spawner.setChanged();
        }
        return true;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(
            BlockState source,
            BlockState target,
            Property<T> property
    ) {
        return target.setValue(property, source.getValue(property));
    }

    private static Map<Block, ReplacementRule> ensureRules() {
        Map<Block, ReplacementRule> current = rules;
        if (current != null) {
            return current;
        }
        synchronized (NaturalReplacementHandler.class) {
            current = rules;
            if (current != null) {
                return current;
            }

            Map<Block, ReplacementRule> built = new HashMap<>();
            for (SusBlockDefinitions.Definition definition : SusBlockDefinitions.ALL) {
                Block target = BuiltInRegistries.BLOCK.get(
                        ResourceLocation.fromNamespaceAndPath(SusArchaeology.MOD_ID, definition.id())
                );
                if (target == null) {
                    SusArchaeology.LOGGER.warn("Missing suspicious block target: {}", definition.id());
                    continue;
                }
                for (String sourceId : definition.sources()) {
                    ResourceLocation sourceLocation = ResourceLocation.parse(sourceId);
                    Block source = BuiltInRegistries.BLOCK.get(sourceLocation);
                    if (source == null) {
                        SusArchaeology.LOGGER.warn("Missing natural source block: {}", sourceId);
                        continue;
                    }
                    double chance = SusConfig.replacementChance(sourceLocation.getPath());
                    if (chance > 0.0D) {
                        built.put(source, new ReplacementRule(target.defaultBlockState(), chance));
                    }
                }
            }
            rules = Map.copyOf(built);
            SusArchaeology.LOGGER.info("Configured {} natural block replacements.", rules.size());
            return rules;
        }
    }

    public static void invalidateRules() {
        rules = null;
    }

    private record PendingChunk(ServerLevel level, ChunkPos pos) {
    }

    private record ReplacementRule(BlockState state, double chance) {
    }
}
