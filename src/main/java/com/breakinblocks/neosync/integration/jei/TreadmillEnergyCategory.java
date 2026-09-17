package com.breakinblocks.neosync.integration.jei;

import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.config.SyncConfig;
import com.breakinblocks.neosync.common.item.SyncItems;

import java.util.List;
import java.util.stream.Collectors;

public class TreadmillEnergyCategory implements IRecipeCategory<TreadmillEnergyCategory.TreadmillEnergyRecipe> {
    public static final RecipeType<TreadmillEnergyRecipe> RECIPE_TYPE =
            RecipeType.create(NeoSync.MOD_ID, "treadmill_energy", TreadmillEnergyRecipe.class);

    private final IDrawable icon;

    public TreadmillEnergyCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemStack(new ItemStack(SyncItems.TREADMILL.get()));
    }

    public static List<TreadmillEnergyRecipe> buildRecipes() {
        return SyncConfig.getInstance().energyMap().stream()
                .filter(entry -> entry.getEntityType() != EntityTypes.PLAYER)
                .map(entry -> new TreadmillEnergyRecipe(entry.getEntityType(), entry.outputEnergyQuantity()))
                .collect(Collectors.toList());
    }

    @Override
    public RecipeType<TreadmillEnergyRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.neosync.category.treadmill_energy");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 130;
    }

    @Override
    public int getHeight() {
        return 30;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TreadmillEnergyRecipe recipe, IFocusGroup focuses) {
        ItemStack display = SpawnEggItem.byId(recipe.entity())
                .map(holder -> new ItemStack(holder.value()))
                .orElse(new ItemStack(Items.EGG));
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 7).addItemStack(display);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, TreadmillEnergyRecipe recipe, IFocusGroup focuses) {
        Component text = Component.translatable("jei.neosync.energy_per_tick", recipe.energyPerTick());
        builder.addText(text, 96, 16).setPosition(30, 7);
    }

    public record TreadmillEnergyRecipe(EntityType<?> entity, long energyPerTick) {}
}
