package com.breakinblocks.neosync.common.utils.nbt;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import com.breakinblocks.neosync.client.utils.ClientRegistryHelper;

import java.util.ArrayDeque;

public final class SyncRegistries {
    private static final ThreadLocal<ArrayDeque<HolderLookup.Provider>> STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

    private SyncRegistries() {}

    public static void push(HolderLookup.Provider provider) {
        STACK.get().push(provider);
    }

    public static void pop() {
        ArrayDeque<HolderLookup.Provider> stack = STACK.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    public static HolderLookup.Provider provider() {
        ArrayDeque<HolderLookup.Provider> stack = STACK.get();
        if (!stack.isEmpty()) {
            return stack.peek();
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        }
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            HolderLookup.Provider client = ClientRegistryHelper.tryProvider();
            if (client != null) {
                return client;
            }
        }
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }
}
