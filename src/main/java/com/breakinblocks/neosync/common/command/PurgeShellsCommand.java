package com.breakinblocks.neosync.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neosync.api.shell.Shell;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.api.shell.ShellStateContainer;
import com.breakinblocks.neosync.common.block.entity.AbstractShellContainerBlockEntity;
import com.breakinblocks.neosync.common.utils.WorldUtil;

import java.util.Collection;
import java.util.List;

public class PurgeShellsCommand implements Command {
    @Override
    public String getName() {
        return "purge";
    }

    @Override
    public boolean hasPermissions(CommandSourceStack commandSource) {
        return commandSource.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) || commandSource.getServer().isSingleplayer();
    }

    @Override
    public void build(ArgumentBuilder<CommandSourceStack, ?> builder) {
        builder.then(Commands.argument("target", EntityArgument.players())
                .executes(context -> execute(context, null))
                .then(Commands.argument("dimension", IdentifierArgument.id())
                        .suggests((context, suggestions) -> SharedSuggestionProvider.suggestResource(
                                context.getSource().getServer().levelKeys().stream().map(ResourceKey::identifier),
                                suggestions))
                        .executes(context -> execute(context, IdentifierArgument.getId(context, "dimension")))
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context, @Nullable Identifier worldId)
            throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");
        MinecraftServer server = context.getSource().getServer();

        int total = 0;
        for (ServerPlayer player : players) {
            int removed = purge(server, player, worldId);
            total += removed;
            context.getSource().sendSuccess(() -> worldId == null
                    ? Component.translatable("command.neosync.purge.result_all",
                            player.getName().getString(), removed)
                    : Component.translatable("command.neosync.purge.result",
                            player.getName().getString(), removed, worldId.toString()), true);
        }
        return total;
    }

    public static int purge(MinecraftServer server, ServerPlayer player, @Nullable Identifier worldId) {
        Shell shell = (Shell) player;
        List<ShellState> doomed = shell.getAvailableShellStates()
                .filter(x -> worldId == null || worldId.equals(x.getWorld()))
                .toList();

        for (ShellState state : doomed) {
            if (!state.isVirtual()) {
                ServerLevel world = WorldUtil.findWorld(server.getAllLevels(), state.getWorld()).orElse(null);
                if (world != null) {
                    destroyContainedShell(world, state);
                }
            }
            shell.remove(state);
        }

        if (shell.isArtificial() && shell.getAvailableShellStates().findAny().isEmpty()) {
            shell.changeArtificialStatus(false);
        }
        return doomed.size();
    }

    private static void destroyContainedShell(ServerLevel world, ShellState state) {
        BlockPos pos = state.getPos();
        world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);

        ShellStateContainer container = ShellStateContainer.find(world, pos);
        if (container instanceof AbstractShellContainerBlockEntity blockEntity
                && state.equals(blockEntity.getShellState())) {
            blockEntity.onBreak(world, pos);
        }
    }
}
