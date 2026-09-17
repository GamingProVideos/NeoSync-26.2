package com.breakinblocks.neosync.integration.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.api.shell.ShellStateContainer;
import com.breakinblocks.neosync.common.block.ShellStorageBlock;
import com.breakinblocks.neosync.common.block.entity.ShellStorageBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public final class ShellContainerComponentProvider implements IBlockComponentProvider {
    public static final ShellContainerComponentProvider INSTANCE = new ShellContainerComponentProvider();
    private static final Identifier UID = NeoSync.locate("shell_container");

    private ShellContainerComponentProvider() {}

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockEntity be = accessor.getBlockEntity();
        if (!(be instanceof ShellStateContainer container)) {
            return;
        }

        ShellState shell = container.getShellState();
        if (shell != null) {
            tooltip.add(Component.translatable("jade.neosync.owner",
                    Component.literal(shell.getOwnerName()).withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.GRAY));

            float progress = shell.getProgress();
            if (progress < ShellState.PROGRESS_DONE) {
                int pct = Math.round(progress * 100F);
                tooltip.add(Component.translatable("jade.neosync.progress",
                        Component.literal(pct + "%").withStyle(ChatFormatting.YELLOW))
                        .withStyle(ChatFormatting.GRAY));
            }

            DyeColor color = shell.getColor();
            if (color != null) {
                tooltip.add(Component.translatable("jade.neosync.color",
                        Component.translatable("color.minecraft." + color.getName()).withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.translatable("jade.neosync.empty").withStyle(ChatFormatting.DARK_GRAY));
        }

        if (be instanceof ShellStorageBlockEntity) {
            boolean powered = ShellStorageBlock.isPowered(accessor.getBlockState());
            tooltip.add(Component.translatable(powered ? "jade.neosync.powered" : "jade.neosync.unpowered")
                    .withStyle(powered ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
