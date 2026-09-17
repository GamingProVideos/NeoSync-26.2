package com.breakinblocks.neosync.common.advancement;

import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.api.event.PlayerSyncEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = NeoSync.MOD_ID)
public final class SyncAdvancementGrants {
    private static final Identifier FIRST_SYNC = NeoSync.locate("main/first_sync");
    private static final Identifier CROSS_DIM_SYNC = NeoSync.locate("main/cross_dim_sync");

    private SyncAdvancementGrants() {}

    @SubscribeEvent
    public static void onStartSyncing(PlayerSyncEvents.StartSyncing event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        grant(serverPlayer, FIRST_SYNC, "synced");

        Identifier currentDim = serverPlayer.level().dimension().identifier();
        Identifier targetDim = event.getTargetState().getWorld();
        if (targetDim != null && !targetDim.equals(currentDim)) {
            grant(serverPlayer, CROSS_DIM_SYNC, "synced_cross_dim");
        }
    }

    private static void grant(ServerPlayer player, Identifier advancementId, String criterion) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return;
        }
        AdvancementHolder advancement = server.getAdvancements().get(advancementId);
        if (advancement == null) {
            return;
        }
        PlayerAdvancements tracker = player.getAdvancements();
        if (!tracker.getOrStartProgress(advancement).isDone()) {
            tracker.award(advancement, criterion);
        }
    }
}
