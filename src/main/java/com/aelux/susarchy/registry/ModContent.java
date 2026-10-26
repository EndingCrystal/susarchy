package com.aelux.susarchy.registry;

import com.aelux.susarchy.SusArchaeology;
import com.aelux.susarchy.SusBlockDefinitions;
import com.aelux.susarchy.block.SusAmethystClusterBlock;
import com.aelux.susarchy.block.SusBlock;
import com.aelux.susarchy.block.SusCarpetBlock;
import com.aelux.susarchy.block.SusPathBlock;
import com.aelux.susarchy.block.SusPillarBlock;
import com.aelux.susarchy.block.SusPointedDripstoneBlock;
import com.aelux.susarchy.block.SusSnowLayerBlock;
import com.aelux.susarchy.block.SusSpawnerBlock;
import com.aelux.susarchy.item.SusBrushItem;
import com.aelux.susarchy.item.SusDustItem;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SusArchaeology.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SusArchaeology.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SusArchaeology.MOD_ID);

    public static final Map<String, DeferredBlock<? extends Block>> BLOCKS_BY_ID = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<BlockItem>> BLOCK_ITEMS_BY_ID = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<SusBrushItem>> BRUSHES_BY_ID = new LinkedHashMap<>();

    public static final List<SusBrushItem.Tier> BRUSH_TIERS = List.of(
            new SusBrushItem.Tier("feather_brush", 32, 15, Items.FEATHER, Rarity.COMMON),
            new SusBrushItem.Tier("wood_brush", 64, 15, Items.OAK_PLANKS, Rarity.COMMON),
            new SusBrushItem.Tier("stone_brush", 96, 15, Items.COBBLESTONE, Rarity.COMMON),
            new SusBrushItem.Tier("flint_brush", 128, 15, Items.FLINT, Rarity.COMMON),
            new SusBrushItem.Tier("iron_brush", 250, 15, Items.IRON_INGOT, Rarity.COMMON),
            new SusBrushItem.Tier("gold_brush", 96, 22, Items.GOLD_INGOT, Rarity.COMMON),
            new SusBrushItem.Tier("diamond_brush", 1024, 10, Items.DIAMOND, Rarity.UNCOMMON),
            new SusBrushItem.Tier("netherite_brush", 1536, 15, Items.NETHERITE_INGOT, Rarity.RARE)
    );

    public static final DeferredItem<SusDustItem> SUS_DUST = ITEMS.registerItem(
            "sus_dust",
            SusDustItem::new,
            new Item.Properties()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB;

    static {
        for (SusBlockDefinitions.Definition definition : SusBlockDefinitions.ALL) {
            ResourceLocation baseId = ResourceLocation.parse(definition.baseBlock());
            Block base = BuiltInRegistries.BLOCK.get(baseId);
            if (base == null) {
                throw new IllegalStateException("Missing base block for " + definition.id() + ": " + definition.baseBlock());
            }

            BlockBehaviour.Properties properties = safeProperties(base, definition.style());
            applySpecialProperties(properties, definition.id());
            DeferredBlock<? extends Block> block = switch (definition.style()) {
                case PILLAR -> BLOCKS.registerBlock(
                        definition.id(),
                        props -> new SusPillarBlock(base, props),
                        properties
                );
                case PATH -> BLOCKS.registerBlock(
                        definition.id(),
                        props -> new SusPathBlock(base, props),
                        properties
                );
                case SNOW_LAYER -> BLOCKS.registerBlock(
                        definition.id(),
                        props -> new SusSnowLayerBlock(base, props),
                        properties
                );
                case POINTED_DRIPSTONE -> BLOCKS.registerBlock(
                        definition.id(),
                        props -> new SusPointedDripstoneBlock(base, props),
                        properties
                );
                case CARPET -> BLOCKS.registerBlock(
                        definition.id(),
                        props -> new SusCarpetBlock(base, props),
                        properties
                );
                case SPAWNER -> BLOCKS.registerBlock(
                        definition.id(),
                        SusSpawnerBlock::new,
                        properties
                );
                case AMETHYST -> {
                    float height = switch (definition.id()) {
                        case "sus_amethyst_cluster" -> 7.0F;
                        case "sus_large_amethyst_bud" -> 5.0F;
                        case "sus_medium_amethyst_bud" -> 4.0F;
                        case "sus_small_amethyst_bud" -> 3.0F;
                        default -> 7.0F;
                    };
                    float offset = definition.id().equals("sus_small_amethyst_bud") ? 4.0F : 3.0F;
                    yield BLOCKS.registerBlock(
                            definition.id(),
                            props -> new SusAmethystClusterBlock(base, height, offset, props),
                            properties
                    );
                }
                default -> BLOCKS.registerBlock(
                        definition.id(),
                        props -> new SusBlock(base, props),
                        properties
                );
            };

            BLOCKS_BY_ID.put(definition.id(), block);
            BLOCK_ITEMS_BY_ID.put(definition.id(), ITEMS.registerSimpleBlockItem(definition.id(), block));
        }

        for (SusBrushItem.Tier tier : BRUSH_TIERS) {
            DeferredItem<SusBrushItem> brush = ITEMS.registerItem(
                    tier.id(),
                    properties -> new SusBrushItem(properties, tier),
                    new Item.Properties().durability(tier.durability()).rarity(tier.rarity())
            );
            BRUSHES_BY_ID.put(tier.id(), brush);
        }

        CREATIVE_TAB = CREATIVE_TABS.register("susarchy", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.susarchy"))
                .icon(() -> new ItemStack(BRUSHES_BY_ID.get("diamond_brush").get()))
                .displayItems((parameters, output) -> {
                    BRUSHES_BY_ID.values().forEach(output::accept);
                    output.accept(SUS_DUST.get());
                    BLOCK_ITEMS_BY_ID.values().forEach(output::accept);
                })
                .build());
    }

    private ModContent() {
    }

    private static BlockBehaviour.Properties safeProperties(Block base, SusBlockDefinitions.Style style) {
        BlockState baseState = base.defaultBlockState();
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(base.defaultMapColor())
                .destroyTime(base.defaultDestroyTime())
                .explosionResistance(base.getExplosionResistance())
                .sound(baseState.getSoundType())
                .friction(base.getFriction())
                .speedFactor(base.getSpeedFactor())
                .jumpFactor(base.getJumpFactor())
                .dropsLike(base)
                .pushReaction(baseState.getPistonPushReaction());

        if (baseState.requiresCorrectToolForDrops()) {
            properties.requiresCorrectToolForDrops();
        }
        if (style == SusBlockDefinitions.Style.LEAVES
                || style == SusBlockDefinitions.Style.ICE
                || style == SusBlockDefinitions.Style.GRATE
                || style == SusBlockDefinitions.Style.SPAWNER
                || style == SusBlockDefinitions.Style.SNOW_LAYER
                || style == SusBlockDefinitions.Style.POINTED_DRIPSTONE
                || style == SusBlockDefinitions.Style.CARPET
                || style == SusBlockDefinitions.Style.AMETHYST) {
            properties.noOcclusion();
        }
        return properties;
    }

    private static void applySpecialProperties(BlockBehaviour.Properties properties, String id) {
        switch (id) {
            case "sus_glowstone", "sus_shroomlight" -> properties.lightLevel(state -> 15);
            case "sus_crying_obsidian" -> properties.lightLevel(state -> 10);
            case "sus_magma_block" -> properties.lightLevel(state -> 3);
            case "sus_waxed_copper_bulb",
                 "sus_waxed_exposed_copper_bulb",
                 "sus_waxed_weathered_copper_bulb",
                 "sus_waxed_oxidized_copper_bulb" -> properties.lightLevel(state -> 15);
            case "sus_amethyst_cluster" -> properties.lightLevel(state -> 5);
            case "sus_large_amethyst_bud" -> properties.lightLevel(state -> 4);
            case "sus_medium_amethyst_bud" -> properties.lightLevel(state -> 2);
            case "sus_small_amethyst_bud" -> properties.lightLevel(state -> 1);
            default -> {
            }
        }
    }

    public static Block[] brushableBlocks() {
        return BLOCKS_BY_ID.values().stream()
                .map(DeferredBlock::get)
                .filter(block -> block instanceof BrushableBlock)
                .toArray(Block[]::new);
    }

    public static Block[] suspiciousSpawners() {
        return BLOCKS_BY_ID.values().stream()
                .map(DeferredBlock::get)
                .filter(block -> block instanceof SpawnerBlock)
                .toArray(Block[]::new);
    }

    public static Block block(String id) {
        DeferredBlock<? extends Block> block = BLOCKS_BY_ID.get(id);
        if (block == null) {
            throw new IllegalArgumentException("Unknown sus block: " + id);
        }
        return block.get();
    }

    public static DeferredItem<SusBrushItem> brush(String id) {
        DeferredItem<SusBrushItem> brush = BRUSHES_BY_ID.get(id);
        if (brush == null) {
            throw new IllegalArgumentException("Unknown sus brush: " + id);
        }
        return brush;
    }
}
