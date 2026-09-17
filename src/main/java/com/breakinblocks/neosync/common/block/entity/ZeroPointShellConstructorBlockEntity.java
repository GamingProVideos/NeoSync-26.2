package com.breakinblocks.neosync.common.block.entity;

import com.breakinblocks.neosync.api.shell.ShellState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ZeroPointShellConstructorBlockEntity extends ShellConstructorBlockEntity {
    public ZeroPointShellConstructorBlockEntity(BlockPos pos, BlockState state) {
        super(SyncBlockEntities.ZERO_POINT_SHELL_CONSTRUCTOR.get(), pos, state);
        this.color = DyeColor.CYAN;
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        if (this.color == null) {
            this.color = DyeColor.CYAN;
        }
    }

    @Override
    public void onServerTick(Level world, BlockPos pos, BlockState state) {
        super.onServerTick(world, pos, state);
        if (this.shell != null && this.shell.getProgress() < ShellState.PROGRESS_DONE) {
            this.shell.setProgress(ShellState.PROGRESS_DONE);
            this.setChanged();
            this.sync();
        }
    }

    @Override
    public long getAmountAsLong() {
        return 0L;
    }

    @Override
    public long getCapacityAsLong() {
        return 0L;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return 0;
    }
}
