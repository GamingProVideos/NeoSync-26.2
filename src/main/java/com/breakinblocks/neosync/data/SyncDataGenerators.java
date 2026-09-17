package com.breakinblocks.neosync.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import com.breakinblocks.neosync.NeoSync;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = NeoSync.MOD_ID)
public final class SyncDataGenerators {
    private SyncDataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        PackOutput output = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        SyncDatapackProvider datapack = new SyncDatapackProvider(output, lookup);
        CompletableFuture<HolderLookup.Provider> datapackLookup = datapack.getRegistryProvider();

        event.addProvider(datapack);
        event.addProvider(new SyncRecipeProvider.Runner(output, datapackLookup));
        event.addProvider(new SyncLootTableProvider(output, datapackLookup));
        event.addProvider(new SyncBlockTagsProvider(output, datapackLookup));
        event.addProvider(new AdvancementProvider(output, datapackLookup, List.of(new SyncAdvancementProvider())));
        event.addProvider(new SyncLanguageProvider(output));
    }
}
