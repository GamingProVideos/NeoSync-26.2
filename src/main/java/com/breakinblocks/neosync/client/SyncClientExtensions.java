package com.breakinblocks.neosync.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.integration.jade.JadeIntegration;

@EventBusSubscriber(modid = NeoSync.MOD_ID, value = Dist.CLIENT)
public final class SyncClientExtensions {
    private SyncClientExtensions() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("jade")) {
            event.enqueueWork(JadeIntegration::register);
        }
    }
}
