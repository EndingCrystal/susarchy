package com.aelux.susarchy.event;

import com.aelux.susarchy.block.SusSpawnerBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

public final class SuspiciousSpawnerEvents {
    public static final String NAME_KEY = "susarchy.suspicious_name";

    private SuspiciousSpawnerEvents() {
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getSpawner() == null) {
            return;
        }
        BlockEntity blockEntity = event.getSpawner().left().orElse(null);
        if (!(blockEntity instanceof SpawnerBlockEntity) || blockEntity.getLevel() == null) {
            return;
        }
        if (!(blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getBlock() instanceof SusSpawnerBlock)) {
            return;
        }
        applySuspiciousName(event.getEntity());
    }

    public static void applySuspiciousName(Mob mob) {
        Component current = mob.getCustomName();
        if (isSuspiciousName(current)) {
            return;
        }
        Component baseName = current == null ? mob.getType().getDescription() : current;
        mob.setCustomName(Component.translatable(NAME_KEY, baseName));
        mob.setCustomNameVisible(false);
    }

    public static void stripSuspiciousName(LivingEntity entity) {
        Component name = entity.getCustomName();
        if (!isSuspiciousName(name)) {
            return;
        }
        TranslatableContents contents = (TranslatableContents) name.getContents();
        if (contents.getArgs().length == 0) {
            return;
        }
        Object base = contents.getArgs()[0];
        if (base instanceof Component baseName) {
            entity.setCustomName(baseName);
            entity.setCustomNameVisible(false);
        } else if (base instanceof String text) {
            entity.setCustomName(Component.literal(text));
            entity.setCustomNameVisible(false);
        }
    }

    public static boolean isSuspiciousName(Component name) {
        return name != null
                && name.getContents() instanceof TranslatableContents contents
                && contents.getKey().equals(NAME_KEY)
                && contents.getArgs().length > 0;
    }
}
