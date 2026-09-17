package com.breakinblocks.neosync.data;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import com.breakinblocks.neosync.common.item.SyncItems;

import java.util.concurrent.CompletableFuture;

public final class SyncRecipeProvider extends RecipeProvider {
    private final HolderGetter<Item> items;

    protected SyncRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.items = registries.lookupOrThrow(Registries.ITEM);
    }

    @Override
    protected void buildRecipes() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, SyncItems.SYNC_CORE.get())
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
                .save(this.output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, SyncItems.SHELL_CONSTRUCTOR.get())
                .pattern("CSC")
                .pattern("GGG")
                .pattern("CRC")
                .define('C', Blocks.CONCRETE.gray())
                .define('S', SyncItems.SYNC_CORE.get())
                .define('G', Items.GLASS_PANE)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_sync_core", has(SyncItems.SYNC_CORE.get()))
                .save(this.output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, SyncItems.SHELL_STORAGE.get())
                .pattern("CSC")
                .pattern("GIG")
                .pattern("CPC")
                .define('C', Blocks.CONCRETE.gray())
                .define('S', SyncItems.SYNC_CORE.get())
                .define('G', Items.GLASS_PANE)
                .define('I', Items.IRON_BLOCK)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_sync_core", has(SyncItems.SYNC_CORE.get()))
                .save(this.output);

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, SyncItems.ZERO_POINT_SHELL_CONSTRUCTOR.get())
                .requires(SyncItems.SHELL_CONSTRUCTOR.get())
                .requires(Items.NETHER_STAR)
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
                .save(this.output);

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, SyncItems.ZERO_POINT_SHELL_STORAGE.get())
                .requires(SyncItems.SHELL_STORAGE.get())
                .requires(Items.NETHER_STAR)
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
                .save(this.output);

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, SyncItems.MANUAL_SHELL_STORAGE.get())
                .requires(SyncItems.SHELL_STORAGE.get())
                .requires(Items.LEVER)
                .unlockedBy("has_shell_storage", has(SyncItems.SHELL_STORAGE.get()))
                .save(this.output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, SyncItems.TREADMILL.get())
                .pattern("  D")
                .pattern("CCI")
                .pattern("GGR")
                .define('D', Items.DAYLIGHT_DETECTOR)
                .define('G', Blocks.CONCRETE.gray())
                .define('C', Blocks.CARPET.gray())
                .define('I', Items.IRON_BARS)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_daylight_detector", has(Items.DAYLIGHT_DETECTOR))
                .save(this.output);
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new SyncRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "NeoSync Recipes";
        }
    }
}
