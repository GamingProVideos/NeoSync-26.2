package com.breakinblocks.neosync.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import com.breakinblocks.neosync.api.event.PlayerSyncEvents;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.common.block.AbstractShellContainerBlock;
import com.breakinblocks.neosync.common.block.ShellConstructorBlock;
import com.breakinblocks.neosync.common.config.SyncConfig;
import com.breakinblocks.neosync.common.entity.damage.FingerstickDamageSource;
import com.breakinblocks.neosync.common.utils.BlockPosUtil;
import org.jetbrains.annotations.Nullable;

public class ShellConstructorBlockEntity extends AbstractShellContainerBlockEntity implements EnergyHandler {
    public ShellConstructorBlockEntity(BlockPos pos, BlockState state) {
        this(SyncBlockEntities.SHELL_CONSTRUCTOR.get(), pos, state);
    }

    protected ShellConstructorBlockEntity(BlockEntityType<? extends ShellConstructorBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onServerTick(Level world, BlockPos pos, BlockState state) {
        super.onServerTick(world, pos, state);
        if (ShellConstructorBlock.isOpen(state)) {
            ShellConstructorBlock.setOpen(state, world, pos, BlockPosUtil.hasPlayerInside(pos, world));
        }
    }

    @Override
    public void onClientTick(Level world, BlockPos pos, BlockState state) {
        super.onClientTick(world, pos, state);
        if (!AbstractShellContainerBlock.isBottom(state)) return;
        ShellState s = this.shell;
        if (s == null || s.getProgress() >= ShellState.PROGRESS_DONE) return;

        RandomSource rand = world.getRandom();
        double px = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 0.8;
        double pz = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 0.8;
        double py = pos.getY() + s.getProgress() * 1.8 + rand.nextDouble() * 0.2;
        world.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz,
                (rand.nextDouble() - 0.5) * 0.02, rand.nextDouble() * 0.04, (rand.nextDouble() - 0.5) * 0.02);
    }

    public InteractionResult onUse(Level world, BlockPos pos, Player player, InteractionHand hand) {
        PlayerSyncEvents.ShellConstructionFailureReason failureReason = this.beginShellConstruction(player);
        if (failureReason == null) {
            return InteractionResult.SUCCESS;
        } else {
            player.sendSystemMessage(failureReason.toText());
            return InteractionResult.CONSUME;
        }
    }

    @Nullable
    private PlayerSyncEvents.ShellConstructionFailureReason beginShellConstruction(Player player) {
        PlayerSyncEvents.ShellConstructionFailureReason failureReason = this.shell == null
                ? PlayerSyncEvents.ALLOW_SHELL_CONSTRUCTION.invoker().allowShellConstruction(player, this)
                : PlayerSyncEvents.ShellConstructionFailureReason.OCCUPIED;

        if (failureReason != null) {
            return failureReason;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            SyncConfig config = SyncConfig.getInstance();

            float damage = serverPlayer.level().getServer().isHardcore() ? config.hardcoreFingerstickDamage() : config.fingerstickDamage();

            boolean isCreative = !serverPlayer.gameMode.isSurvival();
            boolean isLowOnHealth = (player.getHealth() + player.getAbsorptionAmount()) <= damage;
            boolean hasTotemOfUndying = player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) || player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
            if (isLowOnHealth && !isCreative && !hasTotemOfUndying && config.warnPlayerInsteadOfKilling()) {
                return PlayerSyncEvents.ShellConstructionFailureReason.NOT_ENOUGH_HEALTH;
            }

            player.hurt(FingerstickDamageSource.fingerstick(player), damage);
            this.shell = ShellState.empty(serverPlayer, this.worldPosition);
            if (isCreative && config.enableInstantShellConstruction()) {
                this.shell.setProgress(ShellState.PROGRESS_DONE);
            }
            this.setChanged();
            this.sync();
        }
        return null;
    }

    @Override
    public long getAmountAsLong() {
        ShellConstructorBlockEntity bottom = (ShellConstructorBlockEntity) this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null) return 0L;
        long cap = SyncConfig.getInstance().shellConstructorEnergyRequirement();
        return (long) (bottom.shell.getProgress() * cap);
    }

    @Override
    public long getCapacityAsLong() {
        ShellConstructorBlockEntity bottom = (ShellConstructorBlockEntity) this.getBottomPart().orElse(null);
        return bottom != null && bottom.shell != null ? SyncConfig.getInstance().shellConstructorEnergyRequirement() : 0L;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        ShellConstructorBlockEntity bottom = (ShellConstructorBlockEntity) this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null || bottom.shell.getProgress() >= ShellState.PROGRESS_DONE) return 0;
        int capacity = (int) SyncConfig.getInstance().shellConstructorEnergyRequirement();
        int missingFE = (int) Math.ceil((ShellState.PROGRESS_DONE - bottom.shell.getProgress()) * capacity);
        int accepted = Math.min(amount, missingFE);
        if (accepted > 0) {
            bottom.shell.setProgress(bottom.shell.getProgress() + (float) accepted / capacity);
            bottom.setChanged();
            bottom.sync();
        }
        return accepted;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }
}