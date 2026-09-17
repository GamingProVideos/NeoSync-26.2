package com.breakinblocks.neosync.compat.curios.client;

import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.compat.curios.CuriosShellStateComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.curios.api.CuriosApi;

public final class ShellCurios {
    private ShellCurios() {}

    public static void dress(LivingEntity shellEntity, ShellState state) {
        CuriosShellStateComponent component = state.getComponent().as(CuriosShellStateComponent.class);
        if (component == null) {
            return;
        }

        ListTag curios = component.copyCurios();
        if (curios.isEmpty()) {
            return;
        }

        CuriosApi.getCuriosInventory(shellEntity).ifPresent(handler -> {
            handler.reset();
            handler.loadInventory(curios);

            for (int i = 0; i < curios.size(); i++) {
                CompoundTag slotType = curios.getCompound(i).orElse(null);
                if (slotType == null) {
                    continue;
                }

                byte[] stored = slotType.getByteArray(CuriosShellStateComponent.RENDERS).orElse(new byte[0]);
                handler.getStacksHandler(slotType.getStringOr(CuriosShellStateComponent.IDENTIFIER, ""))
                        .ifPresent(stacksHandler -> applyRenders(stacksHandler.getRenders(), stored));
            }
        });
    }

    private static void applyRenders(NonNullList<Boolean> renderStates, byte[] stored) {
        for (int i = 0; i < renderStates.size() && i < stored.length; i++) {
            renderStates.set(i, stored[i] != 0);
        }
    }
}
