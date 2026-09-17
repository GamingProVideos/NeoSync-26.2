package com.breakinblocks.neosync.common.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.breakinblocks.neosync.NeoSync;

public class SyncCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NeoSync.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SYNC_TAB = CREATIVE_MODE_TABS.register("sync_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(SyncItems.SYNC_CORE.get()))
                    .title(Component.translatable("creativetab.neosync"))
                    .displayItems((parameters, output) -> {
                        output.accept(SyncItems.SYNC_CORE.get());
                        output.accept(SyncItems.SHELL_STORAGE.get());
                        output.accept(SyncItems.SHELL_CONSTRUCTOR.get());
                        output.accept(SyncItems.ZERO_POINT_SHELL_STORAGE.get());
                        output.accept(SyncItems.ZERO_POINT_SHELL_CONSTRUCTOR.get());
                        output.accept(SyncItems.MANUAL_SHELL_STORAGE.get());
                        output.accept(SyncItems.TREADMILL.get());
                    }).build()
    );
}
