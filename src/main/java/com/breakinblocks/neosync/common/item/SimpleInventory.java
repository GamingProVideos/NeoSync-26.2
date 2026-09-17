package com.breakinblocks.neosync.common.item;

import com.breakinblocks.neosync.common.utils.nbt.SyncRegistries;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SimpleInventory implements Container, Nameable {
    public final NonNullList<ItemStack> main;
    public final NonNullList<ItemStack> armor;
    public final NonNullList<ItemStack> offHand;
    private final List<NonNullList<ItemStack>> combinedInventory;
    public int selectedSlot;
    private int changeCount;

    public SimpleInventory() {
        this.main = NonNullList.withSize(36, ItemStack.EMPTY);
        this.armor = NonNullList.withSize(4, ItemStack.EMPTY);
        this.offHand = NonNullList.withSize(1, ItemStack.EMPTY);
        this.combinedInventory = ImmutableList.of(this.main, this.armor, this.offHand);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        for (NonNullList<ItemStack> inventory : this.combinedInventory) {
            if (slot < inventory.size()) {
                inventory.set(slot, stack);
                return;
            }
            slot -= inventory.size();
        }
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        for (NonNullList<ItemStack> inventory : this.combinedInventory) {
            if (slot < inventory.size()) {
                return !inventory.get(slot).isEmpty() ? ContainerHelper.removeItem(inventory, slot, amount) : ItemStack.EMPTY;
            }
            slot -= inventory.size();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        for (NonNullList<ItemStack> inventory : this.combinedInventory) {
            if (slot < inventory.size()) {
                if (!inventory.get(slot).isEmpty()) {
                    ItemStack itemStack = inventory.get(slot);
                    inventory.set(slot, ItemStack.EMPTY);
                    return itemStack;
                } else {
                    return ItemStack.EMPTY;
                }
            }
            slot -= inventory.size();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getContainerSize() {
        return this.main.size() + this.armor.size() + this.offHand.size();
    }

    @Override
    public void clearContent() {
        for (NonNullList<ItemStack> itemStacks : this.combinedInventory) {
            itemStacks.clear();
        }
    }

    @Override
    public boolean isEmpty() {
        return this.combinedInventory.stream().flatMap(Collection::stream).allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        for (NonNullList<ItemStack> inventory : this.combinedInventory) {
            if (slot < inventory.size()) {
                return inventory.get(slot);
            }
            slot -= inventory.size();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Component getName() {
        return Component.translatable("container.inventory");
    }

    @Override
    public void setChanged() {
        ++this.changeCount;
    }

    public int getChangeCount() {
        return this.changeCount;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    public ListTag writeNbt(ListTag nbtList) {
        return writeNbt(nbtList, SyncRegistries.provider());
    }

    public void readNbt(ListTag nbtList) {
        readNbt(nbtList, SyncRegistries.provider());
    }

    public ListTag writeNbt(ListTag nbtList, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        for (Map.Entry<NonNullList<ItemStack>, Integer> inventoryInfo : Map.of(this.main, 0, this.armor, 100, this.offHand, 150).entrySet()) {
            NonNullList<ItemStack> inventory = inventoryInfo.getKey();
            int delta = inventoryInfo.getValue();
            for(int i = 0; i < inventory.size(); ++i) {
                ItemStack stack = inventory.get(i);
                if (!stack.isEmpty()) {
                    DataResult<Tag> result = ItemStack.CODEC.encodeStart(ops, stack);
                    Tag encoded = result.result().orElse(null);
                    if (encoded instanceof CompoundTag stackTag) {
                        stackTag.putByte("Slot", (byte) (i + delta));
                        nbtList.add(stackTag);
                    }
                }
            }
        }
        return nbtList;
    }

    public void readNbt(ListTag nbtList, HolderLookup.Provider registries) {
        this.main.clear();
        this.armor.clear();
        this.offHand.clear();

        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        for (int i = 0; i < nbtList.size(); ++i) {
            CompoundTag nbtCompound = nbtList.getCompound(i).orElse(new CompoundTag());
            int j = nbtCompound.getByteOr("Slot", (byte) 0) & 255;
            ItemStack itemStack = ItemStack.CODEC.parse(ops, nbtCompound).result().orElse(ItemStack.EMPTY);
            if (!itemStack.isEmpty()) {
                if (j < this.main.size()) {
                    this.main.set(j, itemStack);
                } else if (j >= 100 && j < this.armor.size() + 100) {
                    this.armor.set(j - 100, itemStack);
                } else if (j >= 150 && j < this.offHand.size() + 150) {
                    this.offHand.set(j - 150, itemStack);
                }
            }
        }
    }

    public void clone(Container other) {
        int thisSize = this.getContainerSize();
        int otherSize = other.getContainerSize();
        for(int i = 0; i < thisSize; ++i) {
            this.setItem(i, i < otherSize ? other.getItem(i).copy() : ItemStack.EMPTY);
        }

        if (other instanceof Inventory playerInventory) {
            this.selectedSlot = playerInventory.getSelectedSlot();
        } else if (other instanceof SimpleInventory simpleInventory) {
            this.selectedSlot = simpleInventory.selectedSlot;
        }
    }

    public void copyTo(Container other) {
        int thisSize = this.getContainerSize();
        int otherSize = other.getContainerSize();
        for(int i = 0; i < otherSize; ++i) {
            other.setItem(i, i < thisSize ? this.getItem(i).copy() : ItemStack.EMPTY);
        }

        if (other instanceof Inventory playerInventory) {
            playerInventory.setSelectedSlot(this.selectedSlot);
        } else if (other instanceof SimpleInventory simpleInventory) {
            simpleInventory.selectedSlot = this.selectedSlot;
        }
    }
}