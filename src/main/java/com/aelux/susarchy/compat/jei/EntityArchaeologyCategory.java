package com.aelux.susarchy.compat.jei;

import com.aelux.susarchy.SusArchaeology;
import com.aelux.susarchy.registry.ModContent;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class EntityArchaeologyCategory extends AbstractRecipeCategory<EntityArchaeologyRecipe> {
    public static final RecipeType<EntityArchaeologyRecipe> TYPE = RecipeType.create(
            SusArchaeology.MOD_ID,
            "entity_archaeology",
            EntityArchaeologyRecipe.class
    );

    private final IDrawable arrow;

    public EntityArchaeologyCategory(IJeiHelpers helpers) {
        super(
                TYPE,
                Component.translatable("jei.susarchy.entity_archaeology"),
                helpers.getGuiHelper().createDrawableItemStack(
                        new ItemStack(ModContent.brush("diamond_brush").get())
                ),
                168,
                60
        );
        this.arrow = helpers.getGuiHelper().getRecipeArrow();
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            EntityArchaeologyRecipe recipe,
            IFocusGroup focuses
    ) {
        builder.addInputSlot(6, 20)
                .addItemStack(recipe.input())
                .addRichTooltipCallback((slot, tooltip) -> tooltip.add(
                        Component.translatable("jei.susarchy.entity_archaeology.input")
                ));
        int index = 0;
        for (EntityArchaeologyRecipe.LootDisplay loot : recipe.outputs()) {
            if (index >= 12) {
                break;
            }
            int x = 54 + (index % 6) * 18;
            int y = 8 + (index / 6) * 18;
            builder.addOutputSlot(x, y)
                    .addItemStack(loot.stack())
                    .addRichTooltipCallback((slot, tooltip) -> {
                        tooltip.add(Component.translatable(
                                "jei.susarchy.entity_loot.range",
                                loot.min(),
                                loot.max()
                        ));
                        tooltip.add(Component.translatable(
                                "jei.susarchy.entity_loot.weight",
                                loot.weight()
                        ));
                    });
            index++;
        }
    }

    @Override
    public void draw(
            EntityArchaeologyRecipe recipe,
            IRecipeSlotsView slotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
        arrow.draw(guiGraphics, 29, 23);
    }
}
