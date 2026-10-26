package com.aelux.susarchy.event;

import com.aelux.susarchy.attachment.SusAttachments;
import com.aelux.susarchy.loot.EntityLootSelector;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class EntityBrushing {
    private EntityBrushing() {
    }

    public static boolean canBrush(LivingEntity entity) {
        return entity.isAlive()
                && !entity.isRemoved()
                && !entity.isSpectator()
                && entity.getData(SusAttachments.SUSPICIOUS);
    }

    public static boolean brush(ServerLevel level, Player player, LivingEntity target) {
        if (!canBrush(target)) {
            return false;
        }

        SuspiciousSpawnerEvents.stripSuspiciousName(target);
        List<ItemStack> drops = rollLoot(level, player, target);
        target.setData(SusAttachments.SUSPICIOUS, Boolean.FALSE);
        target.syncData(SusAttachments.SUSPICIOUS);

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                target.spawnAtLocation(drop);
            }
        }

        Component message = drops.isEmpty()
                ? Component.translatable("message.susarchy.entity_nothing")
                : Component.translatable("message.susarchy.entity_brushed");
        player.displayClientMessage(message, true);
        level.playSound(
                null,
                target.blockPosition(),
                SoundEvents.BRUSH_GRAVEL_COMPLETED,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F
        );
        return true;
    }

    private static List<ItemStack> rollLoot(ServerLevel level, Player player, LivingEntity target) {
        ResourceKey<LootTable> lootTableKey = EntityLootSelector.select(target.getType());
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(lootTableKey);
        DamageSource damageSource = level.damageSources().playerAttack(player);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, target)
                .withParameter(LootContextParams.ORIGIN, target.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
                .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.ENTITY);
        return new ArrayList<>(lootTable.getRandomItems(params));
    }
}

