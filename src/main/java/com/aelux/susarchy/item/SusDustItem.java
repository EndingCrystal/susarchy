package com.aelux.susarchy.item;

import com.aelux.susarchy.event.NaturalReplacementHandler;
import com.aelux.susarchy.loot.LootSelector;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SusDustItem extends Item {
    public SusDustItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null || SusVariants.replacement(level.getBlockState(pos)) == null) {
            return InteractionResult.PASS;
        }

        player.getCooldowns().addCooldown(this, 10);
        if (!level.isClientSide) {
            if (!apply((ServerLevel) level, pos, player, stack)) {
                return InteractionResult.FAIL;
            }
            level.playSound(null, pos, SoundEvents.BRUSH_GRAVEL, SoundSource.BLOCKS, 0.8F, 1.25F);
            ((ServerLevel) level).sendParticles(
                    ParticleTypes.WAX_ON,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.6D,
                    pos.getZ() + 0.5D,
                    8,
                    0.35D,
                    0.35D,
                    0.35D,
                    0.0D
            );
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static boolean apply(ServerLevel level, BlockPos pos, @Nullable Player player, ItemStack stack) {
        BlockState replacement = SusVariants.replacement(level.getBlockState(pos));
        if (replacement == null || !NaturalReplacementHandler.replaceBlock(level, pos, replacement)) {
            return false;
        }
        if (level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable) {
            brushable.setLootTable(LootSelector.select(level, pos), pos.asLong());
        }
        if (player != null && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return true;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable("tooltip.susarchy.sus_dust").withStyle(ChatFormatting.GRAY));
    }
}
