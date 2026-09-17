package com.breakinblocks.neosync.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import com.breakinblocks.neosync.api.networking.OpenShellSelectorPacket;
import com.breakinblocks.neosync.api.shell.Shell;
import com.breakinblocks.neosync.api.shell.ShellState;

import java.util.Collection;
import java.util.List;

public class ShellSelectorCommand implements Command {
    @Override
    public String getName() {
        return "select";
    }

    @Override
    public boolean hasPermissions(CommandSourceStack commandSource) {
        return commandSource.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    @Override
    public void build(ArgumentBuilder<CommandSourceStack, ?> builder) {
        builder.executes(context -> open(context, List.of(context.getSource().getPlayerOrException())));
        builder.then(Commands.argument("target", EntityArgument.players())
                .executes(context -> open(context, EntityArgument.getPlayers(context, "target"))));
    }

    private static int open(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) throws CommandSyntaxException {
        int opened = 0;
        for (ServerPlayer player : players) {
            if (!hasAvailableShell(player)) {
                context.getSource().sendFailure(Component.translatable("command.neosync.select.no_shells", player.getDisplayName()));
                continue;
            }

            new OpenShellSelectorPacket().send(player);
            ++opened;
        }

        if (opened > 0) {
            int count = opened;
            context.getSource().sendSuccess(() -> Component.translatable("command.neosync.select.opened", count), true);
        }
        return opened;
    }

    private static boolean hasAvailableShell(ServerPlayer player) {
        return ((Shell) player).getAvailableShellStates()
                .anyMatch(state -> state.getProgress() >= ShellState.PROGRESS_DONE);
    }
}
