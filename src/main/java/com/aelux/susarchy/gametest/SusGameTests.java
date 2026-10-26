package com.aelux.susarchy.gametest;

import com.aelux.susarchy.SusArchaeology;
import com.aelux.susarchy.SusBlockDefinitions;
import com.aelux.susarchy.attachment.SusAttachments;
import com.aelux.susarchy.config.SusConfig;
import com.aelux.susarchy.event.EntityBrushing;
import com.aelux.susarchy.event.NaturalReplacementHandler;
import com.aelux.susarchy.event.SuspiciousSpawnerEvents;
import com.aelux.susarchy.item.SusDustItem;
import com.aelux.susarchy.loot.EntityLootSelector;
import com.aelux.susarchy.loot.LootSelector;
import com.aelux.susarchy.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(SusArchaeology.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SusGameTests {
    private SusGameTests() {
    }

    @GameTest(template = "empty")
    public static void allSuspiciousBlocksAreRegisteredAndBrushable(GameTestHelper helper) {
        for (SusBlockDefinitions.Definition definition : SusBlockDefinitions.ALL) {
            var block = ModContent.block(definition.id());
            BlockEntityType<?> expectedType = block instanceof SpawnerBlock
                    ? BlockEntityType.MOB_SPAWNER
                    : BlockEntityType.BRUSHABLE_BLOCK;
            helper.assertTrue(
                    expectedType.getValidBlocks().contains(block),
                    "Block is not registered with its expected block entity type: " + definition.id()
            );
            helper.assertFalse(ModContent.BLOCK_ITEMS_BY_ID.get(definition.id()) == null, "Missing block item: " + definition.id());
        }
        int expectedSources = SusBlockDefinitions.ALL.stream()
                .mapToInt(definition -> definition.sources().size())
                .sum();
        helper.assertTrue(
                SusConfig.replacementChances().size() == expectedSources,
                "Expected " + expectedSources + " configurable source blocks"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nonFullBlocksHaveNonFullShapes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos absolutePos = helper.absolutePos(new BlockPos(1, 1, 1));

        BlockState path = ModContent.block("sus_dirt_path").defaultBlockState();
        BlockState amethyst = ModContent.block("sus_amethyst_cluster").defaultBlockState();

        for (BlockState state : new BlockState[]{path, amethyst}) {
            helper.assertFalse(state.getShape(level, absolutePos).isEmpty(), "Non-full block has no shape");
            helper.assertFalse(
                    Block.isShapeFullBlock(state.getShape(level, absolutePos)),
                    "Non-full block has a full block shape: " + state.getBlock()
            );
        }
        helper.assertTrue(
                Math.abs(path.getShape(level, absolutePos).max(Direction.Axis.Y) - 0.9375D) < 1.0E-6D,
                "Suspicious dirt path is not 15 pixels high"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void brushingSuspiciousBlockCompletes(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModContent.block("sus_stone"));

        BlockEntity blockEntity = helper.getBlockEntity(pos);
        helper.assertTrue(blockEntity instanceof BrushableBlockEntity, "Suspicious block has no brushable block entity");
        BrushableBlockEntity brushable = (BrushableBlockEntity) blockEntity;
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        long gameTime = helper.getLevel().getGameTime();

        for (int i = 0; i < 10; i++) {
            brushable.brush(gameTime + i * 11L, player, Direction.UP);
        }

        helper.assertBlockPresent(Blocks.STONE, pos);
        BlockPos absolutePos = helper.absolutePos(pos);
        helper.succeedWhen(() -> helper.assertFalse(
                helper.getLevel()
                        .getEntitiesOfClass(
                                ItemEntity.class,
                                new net.minecraft.world.phys.AABB(absolutePos).inflate(4.0D)
                        )
                        .isEmpty(),
                "Brushed block did not drop its archaeology loot"
        ));
    }

    @GameTest(template = "empty")
    public static void suspiciousSpawnerKeepsVanillaSpawnData(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.SPAWNER);
        BlockEntity blockEntity = helper.getBlockEntity(pos);
        helper.assertTrue(blockEntity instanceof SpawnerBlockEntity, "Vanilla spawner has no spawner block entity");
        SpawnerBlockEntity spawner = (SpawnerBlockEntity) blockEntity;
        spawner.setEntityId(EntityType.ZOMBIE, helper.getLevel().getRandom());

        boolean replacedOk = NaturalReplacementHandler.replaceBlock(
                helper.getLevel(),
                helper.absolutePos(pos),
                ModContent.block("sus_spawner").defaultBlockState()
        );
        helper.assertTrue(replacedOk, "Could not replace vanilla spawner");

        BlockEntity replaced = helper.getBlockEntity(pos);
        helper.assertTrue(replaced instanceof SpawnerBlockEntity, "Sus spawner lost its spawner block entity");
        SpawnerBlockEntity replacedSpawner = (SpawnerBlockEntity) replaced;
        CompoundTag saved = replacedSpawner.getSpawner().save(new CompoundTag());
        String entityId = saved.getCompound("SpawnData")
                .getCompound("entity")
                .getString("id");
        helper.assertTrue("minecraft:zombie".equals(entityId), "Sus spawner did not keep its spawn data: " + entityId);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void contextualLootTablesExist(GameTestHelper helper) {
        var registries = helper.getLevel().getServer().reloadableRegistries();
        for (var key : LootSelector.allTableKeys()) {
            LootTable table = registries.getLootTable(key);
            helper.assertFalse(table == LootTable.EMPTY, "Missing contextual loot table: " + key.location());
        }
        var selected = LootSelector.select(
                helper.getLevel(),
                helper.absolutePos(new BlockPos(1, 1, 1))
        );
        helper.assertFalse(
                registries.getLootTable(selected) == LootTable.EMPTY,
                "Selected contextual loot table is missing: " + selected.location()
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void blockSpecificLootUsesDedicatedTables(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModContent.block("sus_iron_ore"));

        String selected = LootSelector.select(helper.getLevel(), helper.absolutePos(pos))
                .location()
                .getPath();
        helper.assertTrue(
                selected.equals("archaeology/ore_iron"),
                "Ore block selected the wrong loot table: " + selected
        );
        String deepslate = LootSelector.defaultTable(ModContent.block("sus_deepslate_iron_ore"))
                .location()
                .getPath();
        helper.assertTrue(
                deepslate.equals("archaeology/ore_iron"),
                "Deepslate ore selected the wrong loot table: " + deepslate
        );
        String bookshelf = LootSelector.defaultTable(ModContent.block("sus_bookshelf"))
                .location()
                .getPath();
        helper.assertTrue(
                bookshelf.equals("archaeology/bookshelf"),
                "Bookshelf selected the wrong loot table: " + bookshelf
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void suspiciousDustCoatsBlocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack dust = new ItemStack(ModContent.SUS_DUST.get(), 2);

        helper.assertTrue(
                SusDustItem.apply(level, helper.absolutePos(pos), player, dust),
                "Could not coat stone"
        );
        helper.assertBlockPresent(ModContent.block("sus_stone"), pos);
        helper.assertTrue(dust.getCount() == 1, "Coating did not consume exactly one dust");

        BlockPos invalidPos = new BlockPos(2, 1, 1);
        helper.setBlock(invalidPos, Blocks.GLASS);
        helper.assertFalse(
                SusDustItem.apply(level, helper.absolutePos(invalidPos), player, dust),
                "Coating should reject blocks without a suspicious variant"
        );
        helper.assertBlockPresent(Blocks.GLASS, invalidPos);
        helper.assertTrue(dust.getCount() == 1, "Failed coating consumed dust");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void entityLootTablesExist(GameTestHelper helper) {
        var registries = helper.getLevel().getServer().reloadableRegistries();
        for (var key : EntityLootSelector.allTableKeys()) {
            LootTable table = registries.getLootTable(key);
            helper.assertFalse(table == LootTable.EMPTY, "Missing entity loot table: " + key.location());
        }
        String zombie = EntityLootSelector.select(EntityType.ZOMBIE).location().getPath();
        helper.assertTrue(
                zombie.equals("archaeology/entity_zombie"),
                "Zombie selected the wrong archaeology table: " + zombie
        );
        String creeper = EntityLootSelector.select(EntityType.CREEPER).location().getPath();
        helper.assertTrue(
                creeper.equals("archaeology/entity_creeper"),
                "Creeper selected the wrong archaeology table: " + creeper
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void suspiciousCopperBulbsEmitLight(GameTestHelper helper) {
        for (String id : new String[]{
                "sus_waxed_copper_bulb",
                "sus_waxed_exposed_copper_bulb",
                "sus_waxed_weathered_copper_bulb",
                "sus_waxed_oxidized_copper_bulb"
        }) {
            int light = ModContent.block(id).defaultBlockState().getLightEmission();
            helper.assertTrue(light == 15, "Suspicious copper bulb has the wrong light level: " + id + " = " + light);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void livingEntityIsBrushedOnlyOnce(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos targetPos = helper.absolutePos(new BlockPos(3, 1, 3));
        Zombie zombie = EntityType.ZOMBIE.create(level);
        helper.assertTrue(zombie != null, "Could not create test zombie");
        zombie.moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 0.0F, 0.0F);
        zombie.setNoAi(true);
        level.addFreshEntity(zombie);

        SuspiciousSpawnerEvents.applySuspiciousName(zombie);
        helper.assertFalse(zombie.isCustomNameVisible(), "Suspicious name should be hidden");
        helper.assertTrue(
                SuspiciousSpawnerEvents.isSuspiciousName(zombie.getCustomName()),
                "Spawned mob was not given a suspicious name"
        );

        helper.assertTrue(zombie.getData(SusAttachments.SUSPICIOUS), "New entity was not suspicious");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack brush = new ItemStack(ModContent.brush("diamond_brush").get());

        boolean first = EntityBrushing.brush(level, player, zombie);
        helper.assertTrue(first, "First brush failed");
        helper.assertFalse(zombie.getData(SusAttachments.SUSPICIOUS), "Attachment was not consumed");
        helper.assertFalse(
                SuspiciousSpawnerEvents.isSuspiciousName(zombie.getCustomName()),
                "Suspicious prefix was not removed"
        );

        boolean second = EntityBrushing.brush(level, player, zombie);
        helper.assertFalse(second, "Second brush succeeded");
        helper.assertFalse(zombie.getData(SusAttachments.SUSPICIOUS), "Attachment was restored");
        helper.succeedWhen(() -> helper.assertFalse(
                level.getEntitiesOfClass(ItemEntity.class, zombie.getBoundingBox().inflate(4.0D)).isEmpty(),
                "No normal loot was dropped"
        ));
    }
}

