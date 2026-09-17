package com.breakinblocks.neosync.common.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.chunk.LevelChunk;
import com.breakinblocks.neosync.api.shell.Shell;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.api.shell.ShellStateContainer;
import com.breakinblocks.neosync.common.utils.WorldUtil;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class GhostShellsCommand implements Command {
    private static final SimpleCommandExceptionType INVALID_ACTION_TYPE = new SimpleCommandExceptionType(
            Component.translatable("command.neosync.ghostshells.invalid_action")
    );

    @Override
    public String getName() {
        return "ghostshells";
    }

    @Override
    public boolean hasPermissions(CommandSourceStack commandSource) {
        return commandSource.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) || commandSource.getServer().isSingleplayer();
    }

    @Override
    public void build(ArgumentBuilder<CommandSourceStack, ?> builder) {
        builder.then(Commands.argument("type", StringArgumentType.word())
                .suggests((context, suggestions) -> SharedSuggestionProvider.suggest(Set.of("sync", "remove", "repair"), suggestions))
                .then(Commands.argument("target", EntityArgument.players())
                        .executes(GhostShellsCommand::execute)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(GhostShellsCommand::execute)
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String type = StringArgumentType.getString(context, "type");
        boolean repair;
        boolean canSkip;

        switch (type) {
            case "sync" -> {
                repair = true;
                canSkip = true;
            }
            case "remove" -> {
                repair = false;
                canSkip = true;
            }
            case "repair" -> {
                repair = true;
                canSkip = false;
            }
            default -> throw INVALID_ACTION_TYPE.create();
        }

        Consumer<Component> logger = component -> context.getSource().sendSuccess(() -> component, false);
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");
        BlockPos pos;
        try {
            pos = BlockPosArgument.getBlockPos(context, "pos");
        } catch (IllegalArgumentException e) {
            pos = null;
        }

        if (pos == null) {
            for (ServerPlayer player : players) {
                updateShells(player, repair, canSkip, logger);
            }
        } else {
            BlockPos finalPos = pos;
            for (ServerPlayer player : players) {
                ShellState shellState = ((Shell)player).getAvailableShellStates()
                        .filter(x -> x.getPos().equals(finalPos))
                        .findAny()
                        .orElse(null);
                if (shellState == null) {
                    logger.accept(Component.translatable("command.neosync.ghostshells.not_found",
                            player.getName().getString(), pos.toShortString()));
                } else {
                    updateShell(player, shellState, repair, canSkip, logger);
                }
            }
        }
        return 1;
    }

    private static void updateShells(ServerPlayer player, boolean shouldRepair, boolean skipOnFailure,
                                     Consumer<Component> logger) {
        for (ShellState shellState : (Iterable<ShellState>)((Shell)player).getAvailableShellStates()::iterator) {
            shellState.setProgress(100);
            updateShell(player, shellState, shouldRepair, skipOnFailure, logger);
        }
    }

    private static void updateShell(ServerPlayer player, ShellState shellState, boolean shouldRepair,
                                    boolean skipOnFailure, Consumer<Component> logger) {
        if (shellState.isVirtual() || shellExists(player.level().getServer(), shellState)) {
            return;
        }

        if (shouldRepair) {
            if (tryRepair(player.level().getServer(), shellState)) {
                logger.accept(Component.translatable("command.neosync.ghostshells.repaired",
                        player.getName().getString(), shellState.getPos().toShortString()));
                return;
            }

            if (!skipOnFailure) {
                logger.accept(Component.translatable("command.neosync.ghostshells.failed",
                        player.getName().getString(), shellState.getPos().toShortString()));
                return;
            }
        }

        ((Shell)player).remove(shellState);
        logger.accept(Component.translatable("command.neosync.ghostshells.removed",
                player.getName().getString(), shellState.getPos().toShortString()));
    }

    private static boolean shellExists(MinecraftServer server, ShellState shellState) {
        return getShellContainer(server, shellState)
                .map(x -> shellState.equals(x.getShellState()))
                .orElse(Boolean.FALSE);
    }

    private static boolean tryRepair(MinecraftServer server, ShellState shellState) {
        ShellStateContainer shellContainer = getShellContainer(server, shellState).orElse(null);
        if (shellContainer == null) {
            return false;
        }

        if (shellContainer.getShellState() == null) {
            shellContainer.setShellState(shellState);
        }

        return shellState.equals(shellContainer.getShellState());
    }

    private static Optional<ShellStateContainer> getShellContainer(MinecraftServer server, ShellState shellState) {
        ServerLevel world = WorldUtil.findWorld(server.getAllLevels(), shellState.getWorld()).orElse(null);
        if (world == null) {
            return Optional.empty();
        }

        LevelChunk chunk = world.getChunk(shellState.getPos().getX() >> 4, shellState.getPos().getZ() >> 4);
        if (chunk == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(ShellStateContainer.find(world, shellState));
    }
}