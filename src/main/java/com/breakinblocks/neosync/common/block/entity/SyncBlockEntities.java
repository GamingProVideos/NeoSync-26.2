package com.breakinblocks.neosync.common.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.block.SyncBlocks;

import java.util.Set;

public class SyncBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeoSync.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShellStorageBlockEntity>> SHELL_STORAGE =
            BLOCK_ENTITIES.register("shell_storage",
                    () -> new BlockEntityType<ShellStorageBlockEntity>(ShellStorageBlockEntity::new,
                            Set.<Block>of(SyncBlocks.SHELL_STORAGE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShellConstructorBlockEntity>> SHELL_CONSTRUCTOR =
            BLOCK_ENTITIES.register("shell_constructor",
                    () -> new BlockEntityType<ShellConstructorBlockEntity>(ShellConstructorBlockEntity::new,
                            Set.<Block>of(SyncBlocks.SHELL_CONSTRUCTOR.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ZeroPointShellStorageBlockEntity>> ZERO_POINT_SHELL_STORAGE =
            BLOCK_ENTITIES.register("zero_point_shell_storage",
                    () -> new BlockEntityType<ZeroPointShellStorageBlockEntity>(ZeroPointShellStorageBlockEntity::new,
                            Set.<Block>of(SyncBlocks.ZERO_POINT_SHELL_STORAGE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManualShellStorageBlockEntity>> MANUAL_SHELL_STORAGE =
            BLOCK_ENTITIES.register("manual_shell_storage",
                    () -> new BlockEntityType<ManualShellStorageBlockEntity>(ManualShellStorageBlockEntity::new,
                            Set.<Block>of(SyncBlocks.MANUAL_SHELL_STORAGE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ZeroPointShellConstructorBlockEntity>> ZERO_POINT_SHELL_CONSTRUCTOR =
            BLOCK_ENTITIES.register("zero_point_shell_constructor",
                    () -> new BlockEntityType<ZeroPointShellConstructorBlockEntity>(ZeroPointShellConstructorBlockEntity::new,
                            Set.<Block>of(SyncBlocks.ZERO_POINT_SHELL_CONSTRUCTOR.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TreadmillBlockEntity>> TREADMILL =
            BLOCK_ENTITIES.register("treadmill",
                    () -> new BlockEntityType<TreadmillBlockEntity>(TreadmillBlockEntity::new,
                            Set.<Block>of(SyncBlocks.TREADMILL.get())));
}
