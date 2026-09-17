package com.breakinblocks.neosync.common.block;

import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.block.entity.AbstractShellContainerBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = NeoSync.MOD_ID)
public final class TrimDyeHandler {
    private TrimDyeHandler() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.isShiftKeyDown()) {
            return;
        }

        ItemStack stack = event.getItemStack();
        DyeColor dye = stack.get(DataComponents.DYE);
        if (dye == null) {
            return;
        }

        Level world = event.getLevel();
        BlockState state = world.getBlockState(event.getPos());
        if (!(state.getBlock() instanceof AbstractShellContainerBlock)) {
            return;
        }

        if (!world.isClientSide() && world.getBlockEntity(event.getPos()) instanceof AbstractShellContainerBlockEntity container && container.getColor() != dye) {
            container.setColor(dye);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
