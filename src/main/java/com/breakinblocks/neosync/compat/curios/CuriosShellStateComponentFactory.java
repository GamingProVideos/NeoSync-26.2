package com.breakinblocks.neosync.compat.curios;

import com.breakinblocks.neosync.api.shell.ShellStateComponent;
import com.breakinblocks.neosync.api.shell.ShellStateComponentFactoryRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.List;

public class CuriosShellStateComponentFactory implements ShellStateComponentFactoryRegistry.ShellStateComponentFactory {
    @Override
    public ShellStateComponent empty() {
        return new StoredCurios();
    }

    @Override
    public ShellStateComponent of(ServerPlayer player) {
        return new WornCurios(player);
    }

    private static final class StoredCurios extends CuriosShellStateComponent {
        private ListTag curios = new ListTag();

        @Override
        protected ListTag getCurios() {
            return this.curios.copy();
        }

        @Override
        protected void setCurios(ListTag curios) {
            this.curios = curios == null ? new ListTag() : curios.copy();
        }
    }

    private static final class WornCurios extends CuriosShellStateComponent {
        private final ServerPlayer player;

        private WornCurios(ServerPlayer player) {
            this.player = player;
        }

        @Override
        protected ListTag getCurios() {
            return CuriosApi.getCuriosInventory(this.player).map(WornCurios::save).orElseGet(ListTag::new);
        }

        @Override
        protected void setCurios(ListTag curios) {
            CuriosApi.getCuriosInventory(this.player).ifPresent(handler -> {
                handler.saveInventory(true);
                handler.loadInventory(curios);
                this.recoverUnrestored(handler, readItems(curios));
            });
        }

        private static ListTag save(ICuriosItemHandler handler) {
            ListTag curios = handler.saveInventory(false);
            for (int i = 0; i < curios.size(); i++) {
                CompoundTag slotType = curios.getCompound(i).orElse(null);
                if (slotType == null) {
                    continue;
                }

                handler.getStacksHandler(slotType.getStringOr(IDENTIFIER, ""))
                        .ifPresent(stacksHandler -> slotType.putByteArray(RENDERS, renders(stacksHandler.getRenders())));
            }
            return curios;
        }

        private static byte[] renders(NonNullList<Boolean> renderStates) {
            byte[] renders = new byte[renderStates.size()];
            for (int i = 0; i < renders.length; i++) {
                renders[i] = (byte)(renderStates.get(i) ? 1 : 0);
            }
            return renders;
        }

        private void recoverUnrestored(ICuriosItemHandler handler, List<ItemStack> missing) {
            for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                forget(missing, stacksHandler.getStacks());
                forget(missing, stacksHandler.getCosmeticStacks());
            }

            for (ItemStack stack : missing) {
                this.player.getInventory().placeItemBackInInventory(stack);
            }
        }

        private static void forget(List<ItemStack> missing, IDynamicStackHandler stacks) {
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack stack = stacks.getStackInSlot(i);
                if (stack.isEmpty()) {
                    continue;
                }

                for (int j = 0; j < missing.size(); j++) {
                    if (ItemStack.matches(stack, missing.get(j))) {
                        missing.remove(j);
                        break;
                    }
                }
            }
        }
    }
}
