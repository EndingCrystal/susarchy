package com.aelux.susarchy.block;

import com.aelux.susarchy.loot.LootSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SusBlock extends BrushableBlock {
    public SusBlock(Block turnsInto, Properties properties) {
        super(turnsInto, SoundEvents.BRUSH_GRAVEL, SoundEvents.BRUSH_GRAVEL_COMPLETED, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        BrushableBlockEntity blockEntity = new BrushableBlockEntity(pos, state);
        blockEntity.setLootTable(LootSelector.defaultTable(state.getBlock()), pos.asLong());
        return blockEntity;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof BrushableBlockEntity blockEntity) {
            blockEntity.checkReset();
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Suspicious blocks do not fall, even when they really look like they should.
    }
}
