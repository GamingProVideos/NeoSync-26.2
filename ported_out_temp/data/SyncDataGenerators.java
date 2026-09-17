package com.breakinblocks.neosync.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import com.breakinblocks.neosync.NeoSync;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = NeoSync.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class SyncDataGenerators {
    private SyncDataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existing = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        SyncDatapackProvider datapack = new SyncDatapackProvider(output, lookup);
        CompletableFuture<HolderLookup.Provider> datapackLookup = datapack.getRegistryProvider();

        generator.addProvider(event.includeServer(), datapack);
        generator.addProvider(event.includeServer(), new SyncRecipeProvider(output, datapackLookup));
        generator.addProvider(event.includeServer(), new SyncLootTableProvider(output, datapackLookup));
        generator.addProvider(event.includeServer(), new SyncBlockTagsProvider(output, datapackLookup, existing));
        generator.addProvider(event.includeServer(), new AdvancementProvider(output, datapackLookup, existing, List.of(new SyncAdvancementProvider())));
        generator.addProvider(event.includeClient(), new SyncLanguageProvider(output));
    }
}
