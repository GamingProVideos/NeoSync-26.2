package com.breakinblocks.neosync.common.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.level.ServerPlayer;
import com.breakinblocks.neosync.api.shell.Shell;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.common.utils.WorldUtil;

import java.util.Collection;
import java.util.List;

public class AnchorCommand implements Command {
    @Override
    public String getName() {
        return "anchor";
    }

    @Override
    public boolean hasPermissions(CommandSourceStack commandSource) {
        return commandSource.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) || commandSource.getServer().isSingleplayer();
    }

    @Override
    public void build(ArgumentBuilder<CommandSourceStack, ?> builder) {
        builder.then(Commands.literal("set")
                .then(Commands.argument("target", EntityArgument.players())
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> executeSet(context, false, false))
                                        .then(Commands.argument("temporary", BoolArgumentType.bool())
                                                .executes(context -> executeSet(context, BoolArgumentType.getBool(context, "temporary"), false)))))));
        builder.then(Commands.literal("ensure")
                .then(Commands.argument("target", EntityArgument.players())
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> executeSet(context, true, true))))));
        builder.then(Commands.literal("remove")
                .then(Commands.argument("target", EntityArgument.players())
                        .executes(context -> executeRemove(context, false))
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> executeRemove(context, true))))));
        builder.then(Commands.literal("list")
                .then(Commands.argument("target", EntityArgument.players())
                        .executes(AnchorCommand::executeList)));
    }

    private static int executeSet(CommandContext<CommandSourceStack> context, boolean temporary, boolean onlyIfStranded) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");
        ServerLevel world = DimensionArgument.getDimension(context, "dimension");
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        Identifier worldId = WorldUtil.getId(world);

        int count = 0;
        for (ServerPlayer player : players) {
            Shell shell = (Shell)player;
            if (onlyIfStranded && shell.getAvailableShellStates().anyMatch(x -> x.getProgress() >= ShellState.PROGRESS_DONE && worldId.equals(x.getWorld()))) {
                context.getSource().sendSuccess(() -> Component.translatable("command.neosync.anchor.ensure.skipped",
                        player.getName().getString()), false);
                continue;
            }

            List<ShellState> existing = shell.getAvailableShellStates()
                    .filter(ShellState::isVirtual)
                    .filter(x -> worldId.equals(x.getWorld()) && pos.equals(x.getPos()))
                    .toList();
            existing.forEach(shell::remove);
            shell.add(ShellState.anchor(player, worldId, pos, temporary));
            context.getSource().sendSuccess(() -> Component.translatable(temporary ? "command.neosync.anchor.set.temporary" : "command.neosync.anchor.set",
                    player.getName().getString(), pos.toShortString(), worldId.toString()), false);
            ++count;
        }
        return count;
    }

    private static int executeRemove(CommandContext<CommandSourceStack> context, boolean hasPos) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");
        Identifier worldId = hasPos ? WorldUtil.getId(DimensionArgument.getDimension(context, "dimension")) : null;
        BlockPos pos = hasPos ? BlockPosArgument.getBlockPos(context, "pos") : null;

        int count = 0;
        for (ServerPlayer player : players) {
            Shell shell = (Shell)player;
            List<ShellState> anchors = shell.getAvailableShellStates()
                    .filter(ShellState::isVirtual)
                    .filter(x -> pos == null || worldId.equals(x.getWorld()) && pos.equals(x.getPos()))
                    .toList();
            anchors.forEach(shell::remove);
            context.getSource().sendSuccess(() -> Component.translatable("command.neosync.anchor.removed",
                    anchors.size(), player.getName().getString()), false);
            count += anchors.size();
        }
        return count;
    }

    private static int executeList(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");
        int count = 0;
        for (ServerPlayer player : players) {
            List<ShellState> anchors = ((Shell)player).getAvailableShellStates()
                    .filter(ShellState::isVirtual)
                    .toList();
            if (anchors.isEmpty()) {
                context.getSource().sendSuccess(() -> Component.translatable("command.neosync.anchor.list.empty",
                        player.getName().getString()), false);
            } else {
                for (ShellState anchor : anchors) {
                    context.getSource().sendSuccess(() -> Component.translatable(anchor.isTemporary() ? "command.neosync.anchor.list.entry.temporary" : "command.neosync.anchor.list.entry",
                            player.getName().getString(), anchor.getPos().toShortString(), anchor.getWorld().toString()), false);
                }
            }
            count += anchors.size();
        }
        return count;
    }
}
