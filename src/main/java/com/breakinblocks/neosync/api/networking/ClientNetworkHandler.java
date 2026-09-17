package com.breakinblocks.neosync.api.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import com.breakinblocks.neosync.api.shell.ClientShell;
import com.breakinblocks.neosync.client.gui.ShellSelectorGUI;
import com.breakinblocks.neosync.api.shell.Shell;
import com.breakinblocks.neosync.api.shell.ShellState;

public final class ClientNetworkHandler {
    private ClientNetworkHandler() {}

    public static void onSynchronizationResponse(SynchronizationResponsePacket payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        ((ClientShell) player).endSync(
                payload.startWorld(), payload.startPos(), payload.startFacing(),
                payload.targetWorld(), payload.targetPos(), payload.targetFacing(),
                payload.storedState().orElse(null));
    }

    public static void onShellUpdate(ShellUpdatePacket payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Shell shell = (Shell) player;
        shell.changeArtificialStatus(payload.isArtificial());
        shell.setDeathSyncEnabled(payload.autoSyncOnDeath());
        shell.setAvailableShellStates(payload.states().stream());
        refreshOpenShellSelector();
    }

    public static void onShellStateUpdate(ShellStateUpdatePacket payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Shell shell = (Shell) player;
        switch (payload.kind()) {
            case ADD -> shell.add(payload.addedState());
            case REMOVE -> {
                ShellState removed = shell.getShellStateByUuid(payload.targetUuid());
                if (removed != null) shell.remove(removed);
            }
            case UPDATE -> {
                ShellState updated = shell.getShellStateByUuid(payload.targetUuid());
                if (updated != null) {
                    updated.setProgress(payload.progress());
                    updated.setColor(payload.color());
                    updated.setPos(payload.pos());
                    updated.setName(payload.name());
                    updated.setManualOnly(payload.manualOnly());
                }
            }
            case NONE -> { }
        }
        refreshOpenShellSelector();
    }


    private static void refreshOpenShellSelector() {
        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() instanceof ShellSelectorGUI selector) {
            selector.refreshShells();
        }
    }

    public static void onPlayerIsAlive(PlayerIsAlivePacket payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Player updated = ((ClientLevel) player.level()).getPlayerByUUID(payload.playerUuid());
        if (updated == null) return;
        if (updated.getHealth() <= 0) {
            updated.setHealth(0.01F);
        }
        updated.deathTime = 0;
    }

    public static void onOpenShellSelector() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        client.gui.setScreen(new ShellSelectorGUI(null, null));
    }

    public static void onShellDestroyed(ShellDestroyedPacket payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        for (int i = 0; i < 3; ++i) {
            ((ClientLevel) player.level()).addDestroyBlockEffect(payload.pos(), Blocks.DEEPSLATE.defaultBlockState());
            ((ClientLevel) player.level()).addDestroyBlockEffect(payload.pos().above(), Blocks.DEEPSLATE.defaultBlockState());
        }
        ((ClientLevel) player.level()).playSound(player, payload.pos(), SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 1F, player.getVoicePitch());
    }
}
