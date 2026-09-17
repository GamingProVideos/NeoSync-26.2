package com.breakinblocks.neosync.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.utils.reflect.Activator;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = NeoSync.MOD_ID)
public class SyncCommands {
    private static final Set<Command> COMMANDS = new HashSet<>();

    static {
        register(GhostShellsCommand.class);
        register(AnchorCommand.class);
        register(ShellSelectorCommand.class);
        register(PurgeShellsCommand.class);
    }

    public static void init() {
    }

    private static <T extends Command> void register(Class<T> type) {
        COMMANDS.add(Activator.createInstance(type));
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> commandBuilder = LiteralArgumentBuilder.literal(NeoSync.MOD_ID);
        commandBuilder.requires(source -> COMMANDS.stream().anyMatch(c -> c.hasPermissions(source)));

        for (Command commandInfo : COMMANDS) {
            LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(commandInfo.getName());
            command.requires(commandInfo::hasPermissions);
            commandInfo.build(command);
            commandBuilder.then(command);
        }

        dispatcher.register(commandBuilder);
    }
}