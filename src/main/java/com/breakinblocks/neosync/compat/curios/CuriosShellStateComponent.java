package com.breakinblocks.neosync.compat.curios;

import com.breakinblocks.neosync.api.shell.ShellStateComponent;
import com.breakinblocks.neosync.common.utils.nbt.SyncRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class CuriosShellStateComponent extends ShellStateComponent {
    public static final String IDENTIFIER = "Identifier";
    public static final String STACKS = "Stacks";
    public static final String COSMETICS = "Cosmetics";
    public static final String RENDERS = "Renders";

    private static final String ID = "neosync:curios";
    private static final String KEY = "curios";

    @Override
    public String getId() {
        return ID;
    }

    /**
     * @return A copy of the curios this shell is wearing, in {@code saveInventory} form.
     */
    public ListTag copyCurios() {
        return this.getCurios();
    }

    protected abstract ListTag getCurios();

    protected abstract void setCurios(ListTag curios);

    @Override
    public Collection<ItemStack> getItems() {
        return readItems(this.getCurios());
    }

    @Override
    public void clone(ShellStateComponent component) {
        CuriosShellStateComponent other = component.as(CuriosShellStateComponent.class);
        if (other != null) {
            this.setCurios(other.getCurios());
        }
    }

    @Override
    protected void readComponentNbt(CompoundTag nbt) {
        this.setCurios(nbt.getListOrEmpty(KEY));
    }

    @Override
    protected CompoundTag writeComponentNbt(CompoundTag nbt) {
        nbt.put(KEY, this.getCurios());
        return nbt;
    }

    protected static List<ItemStack> readItems(ListTag curios) {
        List<ItemStack> items = new ArrayList<>();
        for (CompoundTag slotType : slotTypes(curios)) {
            collect(readSlots(slotType, STACKS), items);
            collect(readSlots(slotType, COSMETICS), items);
        }
        return items;
    }

    private static List<CompoundTag> slotTypes(ListTag curios) {
        return curios.compoundStream().toList();
    }

    private static List<ItemStack> readSlots(CompoundTag slotType, String key) {
        CompoundTag nbt = slotType.getCompound(key).orElse(null);
        if (nbt == null || nbt.isEmpty()) {
            return List.of();
        }

        ValueInput in = TagValueInput.create(ProblemReporter.DISCARDING, SyncRegistries.provider(), nbt);
        int size = in.getIntOr("Size", 0);
        List<ItemStack> stacks = new ArrayList<>(Collections.nCopies(size, ItemStack.EMPTY));
        in.listOrEmpty("Items", ItemStackWithSlot.CODEC).forEach(entry -> {
            if (entry.isValidInContainer(size)) {
                stacks.set(entry.slot(), entry.stack());
            }
        });
        return stacks;
    }

    private static void collect(List<ItemStack> stacks, List<ItemStack> items) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }
    }
}
