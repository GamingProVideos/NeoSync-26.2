package com.breakinblocks.neosync.common.block.entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import com.breakinblocks.neosync.NeoSync;

@EventBusSubscriber(modid = NeoSync.MOD_ID)
public final class SyncCapabilities {
    private SyncCapabilities() {}

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                SyncBlockEntities.SHELL_STORAGE.get(),
                (be, side) -> be);

        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                SyncBlockEntities.MANUAL_SHELL_STORAGE.get(),
                (be, side) -> be);

        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                SyncBlockEntities.SHELL_CONSTRUCTOR.get(),
                (be, side) -> be);

        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                SyncBlockEntities.TREADMILL.get(),
                (be, side) -> be);
    }
}
