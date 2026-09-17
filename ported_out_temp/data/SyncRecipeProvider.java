package com.breakinblocks.neosync.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import com.breakinblocks.neosync.common.item.SyncItems;

import java.util.concurrent.CompletableFuture;

public final class SyncRecipeProvider extends RecipeProvider {
    public SyncRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SyncItems.SYNC_CORE.get())
                .pattern("DOD")
                .pattern("Q#Q")
                .pattern("ERE")
                .define('D', Items.DAYLIGHT_DETECTOR)
                .define('O', Items.LAPIS_BLOCK)
                .define('Q', Items.QUARTZ)
                .define('#', Items.ENDER_PEARL)
                .define('E', Items.EMERALD)
                .define('R', Items.REDSTONE_BLOCK)
                .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, SyncItems.SHELL_CONSTRUCTOR.get())
                .pattern("CSC")
                .pattern("GGG")
                .pattern("CRC")
                .define('C', Items.GRAY_CONCRETE)
                .define('S', SyncItems.SYNC_CORE.get())
                .define('G', Items.GLASS_PANE)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_sync_core", has(SyncItems.SYNC_CORE.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, SyncItems.SHELL_STORAGE.get())
                .pattern("CSC")
                .pattern("GIG")
                .pattern("CPC")
                .define('C', Items.GRAY_CONCRETE)
                .define('S', SyncItems.SYNC_CORE.get())
                .define('G', Items.GLASS_PANE)
                .define('I', Items.IRON_BLOCK)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_sync_core", has(SyncItems.SYNC_CORE.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, SyncItems.TREADMILL.get())
                .pattern("  D")
                .pattern("CCI")
                .pattern("GGR")
                .define('D', Items.DAYLIGHT_DETECTOR)
                .define('G', Items.GRAY_CONCRETE)
                .define('C', Items.GRAY_CARPET)
                .define('I', Items.IRON_BARS)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_daylight_detector", has(Items.DAYLIGHT_DETECTOR))
                .save(output);
    }
}
