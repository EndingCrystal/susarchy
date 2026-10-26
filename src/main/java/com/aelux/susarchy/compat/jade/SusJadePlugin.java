package com.aelux.susarchy.compat.jade;

import com.aelux.susarchy.SusArchaeology;
import com.aelux.susarchy.event.SuspiciousSpawnerEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public final class SusJadePlugin implements IWailaPlugin {
    public static final ResourceLocation SUSPICIOUS_NAME = ResourceLocation.fromNamespaceAndPath(
            SusArchaeology.MOD_ID,
            "suspicious_name"
    );

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(SuspiciousNameProvider.INSTANCE, Entity.class);
    }

    public enum SuspiciousNameProvider implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            Component name = accessor.getEntity().getCustomName();
            if (!SuspiciousSpawnerEvents.isSuspiciousName(name)) {
                return;
            }
            if (!tooltip.getMessage().contains(name.getString())) {
                tooltip.add(0, name);
            }
        }

        @Override
        public ResourceLocation getUid() {
            return SUSPICIOUS_NAME;
        }

        @Override
        public int getDefaultPriority() {
            return 5000;
        }
    }
}
