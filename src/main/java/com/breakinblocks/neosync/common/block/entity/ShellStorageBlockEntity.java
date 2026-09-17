package com.breakinblocks.neosync.common.block.entity;

import com.breakinblocks.neosync.common.block.ShellStorageBlock;
import com.breakinblocks.neosync.common.config.SyncConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import com.breakinblocks.neosync.common.utils.BlockPosUtil;

public class ShellStorageBlockEntity extends AbstractShellContainerBlockEntity implements EnergyHandler {
    private EntityState entityState;
    private int ticksWithoutPower;
    private int storedEnergy;
    private final BooleanAnimator connectorAnimator;

    public ShellStorageBlockEntity(BlockPos pos, BlockState state) {
        this(SyncBlockEntities.SHELL_STORAGE.get(), pos, state);
    }

    protected ShellStorageBlockEntity(BlockEntityType<? extends ShellStorageBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.entityState = EntityState.NONE;
        this.connectorAnimator = new BooleanAnimator(false);
    }

    protected boolean requiresPower() {
        return true;
    }

    protected boolean shouldOpenDoors(Level world, BlockPos pos, boolean isPowered) {
        return isPowered;
    }

    public DyeColor getIndicatorColor() {
        if (this.level != null && ShellStorageBlock.isPowered(this.getBlockState())) {
            return this.color == null ? DyeColor.LIME : this.color;
        }

        return DyeColor.RED;
    }

    public float getConnectorProgress(float tickDelta) {
        return this.getBottomPart().map(x -> ((ShellStorageBlockEntity)x).connectorAnimator.getProgress(tickDelta)).orElse(0f);
    }

    @Override
    public void onServerTick(Level world, BlockPos pos, BlockState state) {
        super.onServerTick(world, pos, state);

        SyncConfig config = SyncConfig.getInstance();
        boolean infinitePower = !this.requiresPower() || config.shellStorageConsumption() == 0;
        boolean isReceivingRedstonePower = !infinitePower
                && config.shellStorageAcceptsRedstone()
                && ShellStorageBlock.isEnabled(state);
        boolean hasEnergy = infinitePower ? true : this.storedEnergy > 0;
        boolean isPowered = infinitePower || isReceivingRedstonePower || hasEnergy;
        boolean shouldBeOpen = this.shouldOpenDoors(world, pos, isPowered) && this.getBottomPart().map(x -> x.shell == null).orElse(true);

        ShellStorageBlock.setPowered(state, world, pos, isPowered);
        ShellStorageBlock.setOpen(state, world, pos, shouldBeOpen);

        if (!infinitePower) {
            if (this.shell != null && !isPowered) {
                ++this.ticksWithoutPower;
                if (this.ticksWithoutPower >= config.shellStorageMaxUnpoweredLifespan()) {
                    this.destroyShell((ServerLevel)world, pos);
                }
            } else {
                this.ticksWithoutPower = 0;
            }
        }

        if (!infinitePower && !isReceivingRedstonePower && hasEnergy && this.shell != null) {
            this.storedEnergy = (int) Mth.clamp(this.storedEnergy - config.shellStorageConsumption(), 0, config.shellStorageCapacity());
        }
    }

    @Override
    public void onClientTick(Level world, BlockPos pos, BlockState state) {
        super.onClientTick(world, pos, state);
        this.connectorAnimator.setValue(this.shell != null);
        this.connectorAnimator.step();
        if (this.entityState == EntityState.LEAVING || this.entityState == EntityState.CHILLING) {
            this.entityState = BlockPosUtil.hasPlayerInside(pos, world) ? this.entityState : EntityState.NONE;
        }
    }

    public EntityState getEntityState() {
        return this.entityState;
    }

    public void setEntityState(EntityState entityState) {
        this.entityState = entityState;
    }

    @Override
    public InteractionResult onUse(Level world, BlockPos pos, Player player, InteractionHand hand) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.getCount() > 0 && stack.getItem() instanceof DyeItem) {
            DyeColor dyeColor = stack.get(DataComponents.DYE);
            if (dyeColor != null) {
                stack.shrink(1);
                this.color = dyeColor;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public long getAmountAsLong() {
        return this.getBottomPart().map(x -> (long) ((ShellStorageBlockEntity) x).storedEnergy).orElse(0L);
    }

    @Override
    public long getCapacityAsLong() {
        return !this.requiresPower() || SyncConfig.getInstance().shellStorageConsumption() == 0 ? 0L : SyncConfig.getInstance().shellStorageCapacity();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (!this.requiresPower() || SyncConfig.getInstance().shellStorageConsumption() == 0) return 0;
        ShellStorageBlockEntity bottom = (ShellStorageBlockEntity) this.getBottomPart().orElse(null);
        if (bottom == null) return 0;
        int capacity = (int) this.getCapacityAsLong();
        int maxEnergy = Mth.clamp(capacity - bottom.storedEnergy, 0, capacity);
        int inserted = Mth.clamp(amount, 0, maxEnergy);
        bottom.storedEnergy += inserted;
        return inserted;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("storedEnergy", this.storedEnergy);
        out.putInt("ticksWithoutPower", this.ticksWithoutPower);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        this.storedEnergy = in.getIntOr("storedEnergy", 0);
        this.ticksWithoutPower = in.getIntOr("ticksWithoutPower", 0);
    }

    public enum EntityState {
        NONE,
        ENTERING,
        CHILLING,
        LEAVING
    }

}