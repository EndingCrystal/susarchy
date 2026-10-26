package com.aelux.susarchy.compat.jei;

import com.aelux.susarchy.SusArchaeology;
import com.aelux.susarchy.loot.EntityLootSelector;
import com.aelux.susarchy.registry.ModContent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

@JeiPlugin
public final class SusJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            SusArchaeology.MOD_ID,
            "entity_archaeology"
    );

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new EntityArchaeologyCategory(registration.getJeiHelpers()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<EntityArchaeologyRecipe> recipes = new ArrayList<>();
        for (Map.Entry<String, String> entry : EntityLootSelector.entityCategories().entrySet()) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(
                    ResourceLocation.withDefaultNamespace(entry.getKey())
            );
            if (entityType == null) {
                continue;
            }
            SpawnEggItem egg = SpawnEggItem.byId(entityType);
            if (egg == null) {
                continue;
            }
            List<SusEntityLootData.Loot> loot = SusEntityLootData.LOOT.get(entry.getValue());
            if (loot == null) {
                continue;
            }
            List<EntityArchaeologyRecipe.LootDisplay> outputs = new ArrayList<>();
            for (SusEntityLootData.Loot entryLoot : loot) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entryLoot.item()));
                if (item == null || item == Items.AIR) {
                    continue;
                }
                int min = Math.max(1, entryLoot.min());
                int max = Math.max(min, entryLoot.max());
                outputs.add(new EntityArchaeologyRecipe.LootDisplay(
                        new ItemStack(item, Math.min(min, max)),
                        min,
                        max,
                        entryLoot.weight()
                ));
            }
            if (!outputs.isEmpty()) {
                recipes.add(new EntityArchaeologyRecipe(
                        new ItemStack(egg),
                        List.copyOf(outputs),
                        entityType.getDescription()
                ));
            }
        }
        recipes.sort(Comparator.comparing(recipe -> recipe.entityName().getString()));
        registration.addRecipes(EntityArchaeologyCategory.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ItemStack[] catalysts = ModContent.BRUSHES_BY_ID.values().stream()
                .map(brush -> new ItemStack(brush.get()))
                .toArray(ItemStack[]::new);
        registration.addRecipeCatalysts(EntityArchaeologyCategory.TYPE, catalysts);
    }
}
