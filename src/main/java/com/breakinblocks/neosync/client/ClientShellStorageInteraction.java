package com.breakinblocks.neosync.client;

import com.breakinblocks.neosync.api.event.PlayerSyncEvents;
import com.breakinblocks.neosync.client.gui.ShellSelectorGUI;
import com.breakinblocks.neosync.common.block.ShellStorageBlock;
import com.breakinblocks.neosync.common.block.entity.ShellStorageBlockEntity;
import com.breakinblocks.neosync.common.block.entity.ShellStorageBlockEntity.EntityState;
import com.breakinblocks.neosync.common.utils.BlockPosUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public final class ClientShellStorageInteraction {
    private ClientShellStorageInteraction() {}

    public static void onEntityCollision(ShellStorageBlockEntity blockEntity, Entity entity, BlockState state) {
        Minecraft client = Minecraft.getInstance();
        if (!(entity instanceof Player player)) {
            return;
        }

        if (blockEntity.getEntityState() == EntityState.NONE) {
            boolean isInside = BlockPosUtil.isEntityInside(entity, blockEntity.getBlockPos());
            PlayerSyncEvents.ShellSelectionFailureReason failureReason = !isInside && client.player == entity ? PlayerSyncEvents.ALLOW_SHELL_SELECTION.invoker().allowShellSelection(player, blockEntity) : null;
            blockEntity.setEntityState(isInside || failureReason != null ? EntityState.CHILLING : EntityState.ENTERING);
            if (failureReason != null) {
                player.sendSystemMessage(failureReason.toText());
            }
        } else if (blockEntity.getEntityState() != EntityState.CHILLING && client.gui.screen() == null) {
            BlockPosUtil.moveEntity(entity, blockEntity.getBlockPos(), state.getValue(ShellStorageBlock.FACING), blockEntity.getEntityState() == EntityState.ENTERING);
        }

        if (blockEntity.getEntityState() == EntityState.ENTERING && client.player == entity && client.gui.screen() == null && BlockPosUtil.isEntityInside(entity, blockEntity.getBlockPos())) {
            client.gui.setScreen(new ShellSelectorGUI(() -> blockEntity.setEntityState(EntityState.LEAVING), () -> blockEntity.setEntityState(EntityState.CHILLING)));
        }
    }
}
