package com.aelux.susarchy.compat.jei;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record EntityArchaeologyRecipe(
        ItemStack input,
        List<LootDisplay> outputs,
        Component entityName
) {
    public record LootDisplay(ItemStack stack, int min, int max, int weight) {
    }
}
