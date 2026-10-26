package com.aelux.susarchy.item;

import com.aelux.susarchy.SusArchaeology;
import com.aelux.susarchy.attachment.SusAttachments;
import com.aelux.susarchy.event.EntityBrushing;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SusBrushItem extends BrushItem {
    private static final int ENTITY_BRUSH_TICKS = 10;

    private final Tier tier;

    public SusBrushItem(Properties properties, Tier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!EntityBrushing.canBrush(target)) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("message.susarchy.entity_already_brushed"), true);
            }
            return InteractionResult.SUCCESS;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof Player player)) {
            super.onUseTick(level, user, stack, remainingUseTicks);
            return;
        }

        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
                player,
                entity -> entity instanceof LivingEntity living && EntityBrushing.canBrush(living),
                player.blockInteractionRange()
        );

        if (!(hitResult instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof LivingEntity target)) {
            super.onUseTick(level, user, stack, remainingUseTicks);
            return;
        }

        int elapsed = getUseDuration(stack, user) - remainingUseTicks + 1;
        if (elapsed == 5 || elapsed == ENTITY_BRUSH_TICKS) {
            playBrushEffects(level, target);
        }

        if (elapsed >= ENTITY_BRUSH_TICKS) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                boolean brushed = EntityBrushing.brush(serverLevel, player, target);
                if (brushed) {
                    EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                            ? EquipmentSlot.MAINHAND
                            : EquipmentSlot.OFFHAND;
                    stack.hurtAndBreak(1, player, slot);
                }
            }
            player.releaseUsingItem();
        }
    }

    private void playBrushEffects(Level level, LivingEntity target) {
        double x = target.getX();
        double y = target.getY() + target.getBbHeight() * 0.5D;
        double z = target.getZ();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 6, 0.25D, 0.25D, 0.25D, 0.01D);
        } else {
            for (int i = 0; i < 6; i++) {
                level.addParticle(
                        ParticleTypes.CRIT,
                        x + (level.random.nextDouble() - 0.5D) * 0.5D,
                        y + (level.random.nextDouble() - 0.5D) * 0.5D,
                        z + (level.random.nextDouble() - 0.5D) * 0.5D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }
        level.playSound(null, target.blockPosition(), SoundEvents.BRUSH_GENERIC, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.susarchy.brush").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getEnchantmentValue() {
        return tier.enchantability();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(tier.repairItem());
    }

    public Tier tier() {
        return tier;
    }

    public record Tier(String id, int durability, int enchantability, Item repairItem, Rarity rarity) {
        public Tier {
            if (durability <= 0) {
                throw new IllegalArgumentException("durability must be positive");
            }
        }
    }
}
