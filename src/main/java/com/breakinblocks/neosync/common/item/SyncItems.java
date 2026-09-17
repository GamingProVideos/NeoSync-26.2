package com.breakinblocks.neosync.common.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.block.SyncBlocks;

public class SyncItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NeoSync.MOD_ID);

    public static final DeferredItem<Item> SYNC_CORE = ITEMS.register("sync_core",
            name -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, name))
                    .stacksTo(16)));

    public static final DeferredItem<ShellConstructorItem> SHELL_CONSTRUCTOR = ITEMS.register("shell_constructor",
            name -> new ShellConstructorItem(SyncBlocks.SHELL_CONSTRUCTOR.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, name))
                    .useBlockDescriptionPrefix()
                    .stacksTo(1)));

    public static final DeferredItem<ShellStorageItem> SHELL_STORAGE = ITEMS.register("shell_storage",
            name -> new ShellStorageItem(SyncBlocks.SHELL_STORAGE.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, name))
                    .useBlockDescriptionPrefix()
                    .stacksTo(1)));

    public static final DeferredItem<ShellConstructorItem> ZERO_POINT_SHELL_CONSTRUCTOR = ITEMS.register("zero_point_shell_constructor",
            name -> new ShellConstructorItem(SyncBlocks.ZERO_POINT_SHELL_CONSTRUCTOR.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, name))
                    .useBlockDescriptionPrefix()
                    .stacksTo(1)));

    public static final DeferredItem<ShellStorageItem> ZERO_POINT_SHELL_STORAGE = ITEMS.register("zero_point_shell_storage",
            name -> new ShellStorageItem(SyncBlocks.ZERO_POINT_SHELL_STORAGE.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, name))
                    .useBlockDescriptionPrefix()
                    .stacksTo(1)));

    public static final DeferredItem<ShellStorageItem> MANUAL_SHELL_STORAGE = ITEMS.register("manual_shell_storage",
            name -> new ShellStorageItem(SyncBlocks.MANUAL_SHELL_STORAGE.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, name))
                    .useBlockDescriptionPrefix()
                    .stacksTo(1)));

    public static final DeferredItem<TreadmillItem> TREADMILL = ITEMS.register("treadmill",
            name -> new TreadmillItem(SyncBlocks.TREADMILL.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, name))
                    .useBlockDescriptionPrefix()
                    .stacksTo(1)));
}
