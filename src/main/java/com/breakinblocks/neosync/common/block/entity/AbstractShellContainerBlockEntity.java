package com.breakinblocks.neosync.common.block.entity;

import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import com.breakinblocks.neosync.api.networking.ShellDestroyedPacket;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.api.shell.ShellStateContainer;
import com.breakinblocks.neosync.api.shell.ShellStateManager;
import com.breakinblocks.neosync.common.block.AbstractShellContainerBlock;
import com.breakinblocks.neosync.common.item.SimpleInventory;
import com.breakinblocks.neosync.common.utils.nbt.SyncRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.breakinblocks.neosync.common.utils.ItemUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractShellContainerBlockEntity extends BlockEntity implements ShellStateContainer, DoubleBlockEntity, TickableBlockEntity, Container {
    protected final BooleanAnimator doorAnimator;
    protected ShellState shell;
    protected DyeColor color;
    protected int progressComparatorOutput;
    protected int inventoryComparatorOutput;
    private AbstractShellContainerBlockEntity bottomPart;

    private ShellState syncedShell;
    private BlockPos syncedShellPos;
    private DyeColor syncedShellColor;
    private boolean syncedShellManualOnly;
    private float syncedShellProgress;
    private DyeColor syncedColor;
    private boolean inventoryDirty;
    private boolean visibleInventoryDirty;


    public AbstractShellContainerBlockEntity(@NotNull BlockEntityType<? extends AbstractShellContainerBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.doorAnimator = new BooleanAnimator(AbstractShellContainerBlock.isOpen(state));
    }


    @Override
    public void setShellState(ShellState shell) {
        this.shell = shell;

        if (shell != null && this.worldPosition != null) {
            shell.setPos(this.worldPosition);
        }

        if (this.level != null && !this.level.isClientSide() && this.worldPosition != null && this.getBlockState() != null) {
            this.checkShellState(this.level, this.worldPosition, this.getBlockState());
        }
    }

    @Override
    public ShellState getShellState() {
        return this.shell;
    }

    @Override
    @Nullable
    public DyeColor getColor() {
        return this.getBottomPart().map(x -> x.color).orElse(this.color);
    }

    public void setColor(@Nullable DyeColor color) {
        AbstractShellContainerBlockEntity bottom = this.getBottomPart().orElse(this);
        bottom.color = color;
        bottom.setChanged();
    }

    public int getProgressComparatorOutput() {
        return this.getBottomPart().map(x -> x.progressComparatorOutput).orElse(0);
    }

    public int getInventoryComparatorOutput() {
        return this.getBottomPart().map(x -> x.inventoryComparatorOutput).orElse(0);
    }

    protected ShellStateManager getShellStateManager() {
        return (ShellStateManager)Objects.requireNonNull(this.level).getServer();
    }

    public Optional<AbstractShellContainerBlockEntity> getBottomPart() {
        if (this.bottomPart == null && this.level != null) {
            this.bottomPart = AbstractShellContainerBlock.isBottom(this.getBlockState()) ? this : (this.level.getBlockEntity(this.worldPosition.relative(Direction.DOWN)) instanceof AbstractShellContainerBlockEntity x ? x : null);
        }
        return Optional.ofNullable(this.bottomPart);
    }

    @Override
    public void onServerTick(Level world, BlockPos pos, BlockState state) {
        this.checkShellState(world, pos, state);
    }

    protected boolean isManualOnly() {
        return false;
    }

    private void checkShellState(Level world, BlockPos pos, BlockState state) {
        if (this.shell != null && this.shell.getColor() != this.color) {
            this.shell.setColor(this.color);
        }
        if (this.shell != null && this.shell.isManualOnly() != this.isManualOnly()) {
            this.shell.setManualOnly(this.isManualOnly());
        }

        if (this.requiresSync()) {
            this.updateShell(this.shell != this.syncedShell, !this.visibleInventoryDirty);
            this.updateComparatorOutput(world, pos, state);

            this.syncedShellPos = this.shell == null ? null : this.shell.getPos();
            this.syncedShellColor = this.shell == null ? null : this.shell.getColor();
            this.syncedShellManualOnly = this.shell != null && this.shell.isManualOnly();
            this.syncedShellProgress = this.shell == null ? -1 : this.shell.getProgress();
            this.syncedShell = this.shell;
            this.syncedColor = this.color;
            this.inventoryDirty = false;
            this.visibleInventoryDirty = false;

            this.sync();
            this.setChanged();
        }

        if (this.inventoryDirty) {
            this.updateComparatorOutput(world, pos, state);
            this.inventoryDirty = false;
            this.setChanged();
        }
    }

    private boolean requiresSync() {
        return (
                this.visibleInventoryDirty ||
                        this.syncedShell != this.shell ||
                        this.syncedColor != this.color ||
                        this.shell != null && (
                                !this.shell.getPos().equals(this.syncedShellPos) ||
                                        !Objects.equals(this.shell.getColor(), this.syncedShellColor) ||
                                        this.shell.isManualOnly() != this.syncedShellManualOnly ||
                                        this.shell.getProgress() != this.syncedShellProgress
                        )
        );
    }

    private void updateShell(boolean isNew, boolean partialUpdate) {
        ShellStateManager shellManager = this.getShellStateManager();
        if (isNew) {
            shellManager.remove(this.syncedShell);
            shellManager.add(this.shell);
        } else if (partialUpdate) {
            shellManager.update(this.shell);
        } else {
            shellManager.add(this.shell);
        }
    }

    private void updateComparatorOutput(Level world, BlockPos pos, BlockState state) {
        int currentProgressOutput = this.shell == null ? 0 : Mth.clamp((int)(this.shell.getProgress() * 15), 1, 15);
        int currentInventoryOutput = this.shell == null ? 0 : AbstractContainerMenu.getRedstoneSignalFromContainer(this.shell.getInventory());
        BlockPos topPartPos = pos.relative(AbstractShellContainerBlock.getDirectionTowardsAnotherPart(state));
        BlockState topPartState = world.getBlockState(topPartPos);
        if (this.progressComparatorOutput != currentProgressOutput) {
            this.progressComparatorOutput = currentProgressOutput;
            if (state.getValue(AbstractShellContainerBlock.OUTPUT) == AbstractShellContainerBlock.ComparatorOutputType.PROGRESS) {
                world.updateNeighbourForOutputSignal(pos, state.getBlock());
            }
            if (topPartState.hasProperty(AbstractShellContainerBlock.OUTPUT) && topPartState.getValue(AbstractShellContainerBlock.OUTPUT) == AbstractShellContainerBlock.ComparatorOutputType.PROGRESS) {
                world.updateNeighbourForOutputSignal(topPartPos, topPartState.getBlock());
            }
        }
        if (this.inventoryComparatorOutput != currentInventoryOutput) {
            this.inventoryComparatorOutput = currentInventoryOutput;
            if (state.getValue(AbstractShellContainerBlock.OUTPUT) == AbstractShellContainerBlock.ComparatorOutputType.INVENTORY) {
                world.updateNeighbourForOutputSignal(pos, state.getBlock());
            }
            if (topPartState.hasProperty(AbstractShellContainerBlock.OUTPUT) && topPartState.getValue(AbstractShellContainerBlock.OUTPUT) == AbstractShellContainerBlock.ComparatorOutputType.INVENTORY) {
                world.updateNeighbourForOutputSignal(topPartPos, topPartState.getBlock());
            }
        }
    }

    @Override
    public void onClientTick(Level world, BlockPos pos, BlockState state) {
        this.doorAnimator.setValue(AbstractShellContainerBlock.isOpen(state));
        this.doorAnimator.step();
    }

    public void onBreak(Level world, BlockPos pos) {
        if (this.shell != null && world instanceof ServerLevel serverWorld) {
            this.getShellStateManager().remove(this.shell);
            this.destroyShell(serverWorld, pos);
        }
    }

    protected void destroyShell(ServerLevel world, BlockPos pos) {
        if (this.shell != null) {
            this.shell.drop(world, pos);
            new ShellDestroyedPacket(pos).send(world, pos, 32);
            this.shell = null;
        }
    }

    public abstract InteractionResult onUse(Level world, BlockPos pos, Player player, InteractionHand hand);

    public float getDoorOpenProgress(float tickDelta) {
        return this.getBottomPart().map(x -> x.doorAnimator.getProgress(tickDelta)).orElse(0f);
    }

    @Override
    public DoubleBlockHalf getBlockType(BlockState state) {
        return AbstractShellContainerBlock.getShellContainerHalf(state);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        this.saveCustomOnly(out);
        return out.buildResult();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (this.shell != null) {
            out.store("shell", CompoundTag.CODEC, this.shell.writeNbt(new CompoundTag()));
        }
        out.putInt("color", this.color == null ? -1 : this.color.getId());
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        SyncRegistries.push(in.lookup());
        try {
            this.shell = in.read("shell", CompoundTag.CODEC).map(ShellState::fromNbt).orElse(null);
        } finally {
            SyncRegistries.pop();
        }

        if (this.shell != null && this.worldPosition != null) {
            this.shell.setPos(this.worldPosition);
        }

        int colorId = in.getIntOr("color", -1);
        this.color = colorId == -1 ? null : DyeColor.byId(colorId);
    }

    private static int reorderSlotIndex(int slot, SimpleInventory inventory) {
        final int mainSize = inventory.main.size();
        final int armorSize = inventory.armor.size();
        final int offHandSize = inventory.offHand.size();
        return (
                slot >= 0 && slot < armorSize
                        ? (slot + mainSize)
                        : slot >= armorSize && slot < (armorSize + offHandSize)
                        ? (slot + mainSize)
                        : (slot - armorSize - offHandSize)
        );
    }

    private static boolean isVisibleSlot(int slot, SimpleInventory inventory) {
        final int armorSize = inventory.armor.size();
        final int offHandSize = inventory.offHand.size();
        return slot >= 0 && slot <= (armorSize + offHandSize);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        AbstractShellContainerBlockEntity bottom = this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null) {
            return false;
        }

        SimpleInventory inventory = bottom.shell.getInventory();
        final int armorSize = inventory.armor.size();
        boolean isArmorSlot = slot >= 0 && slot < armorSize;
        if (isArmorSlot) {
            EquipmentSlot equipmentSlot = ItemUtil.getPreferredEquipmentSlot(stack);
            return ItemUtil.isArmor(stack) && equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && slot == equipmentSlot.getIndex();
        }

        boolean isOffHandSlot = slot >= armorSize && slot < (armorSize + inventory.offHand.size());
        if (isOffHandSlot) {
            return ItemUtil.getPreferredEquipmentSlot(stack) == EquipmentSlot.OFFHAND || inventory.main.stream().noneMatch(x -> x.isEmpty() || (x.getCount() + stack.getCount()) <= x.getMaxStackSize() && ItemStack.isSameItemSameComponents(x, stack));
        }

        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.getBottomPart().filter(x -> x.shell != null).map(x -> x.shell.getInventory().getItem(reorderSlotIndex(slot, x.shell.getInventory()))).orElse(ItemStack.EMPTY);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        AbstractShellContainerBlockEntity bottom = this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null || bottom.shell.getProgress() < ShellState.PROGRESS_DONE) {
            return;
        }

        SimpleInventory inventory = bottom.shell.getInventory();
        inventory.setItem(reorderSlotIndex(slot, inventory), stack);
        bottom.inventoryDirty = true;
        bottom.visibleInventoryDirty |= isVisibleSlot(slot, inventory);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        AbstractShellContainerBlockEntity bottom = this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null) {
            return ItemStack.EMPTY;
        }

        SimpleInventory inventory = bottom.shell.getInventory();
        ItemStack removed = inventory.removeItem(reorderSlotIndex(slot, inventory), amount);
        bottom.inventoryDirty = true;
        bottom.visibleInventoryDirty |= !removed.isEmpty() && isVisibleSlot(slot, inventory);
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        AbstractShellContainerBlockEntity bottom = this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null) {
            return ItemStack.EMPTY;
        }

        SimpleInventory inventory = bottom.shell.getInventory();
        ItemStack removed = inventory.removeItemNoUpdate(reorderSlotIndex(slot, inventory));
        bottom.inventoryDirty = true;
        bottom.visibleInventoryDirty |= !removed.isEmpty() && isVisibleSlot(slot, inventory);
        return removed;
    }

    @Override
    public void clearContent() {
        AbstractShellContainerBlockEntity bottom = this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null) {
            return;
        }

        bottom.shell.getInventory().clearContent();
        bottom.inventoryDirty = true;
        bottom.visibleInventoryDirty = true;
    }

    @Override
    public int getContainerSize() {
        return this.getBottomPart().map(x -> x.shell == null || x.shell.getProgress() < ShellState.PROGRESS_DONE ? 0 : x.shell.getInventory().getContainerSize()).orElse(0);
    }

    @Override
    public boolean isEmpty() {
        return this.getBottomPart().map(x -> x.shell == null || x.shell.getInventory().isEmpty()).orElse(true);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    protected void sync() {
        if (this.level instanceof ServerLevel serverWorld) {
            serverWorld.getChunkSource().blockChanged(this.worldPosition);
        }
    }
}