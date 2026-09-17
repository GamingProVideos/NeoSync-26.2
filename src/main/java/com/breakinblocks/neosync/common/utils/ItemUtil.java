package com.breakinblocks.neosync.common.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
// ArmorType import not needed
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.registries.BuiltInRegistries;
import com.breakinblocks.neosync.common.config.SyncConfig;

public final class ItemUtil {
    private static final TagKey<Item> WRENCHES = ItemTags.create(Identifier.fromNamespaceAndPath("c", "tools/wrench"));

    public static boolean isWrench(ItemStack itemStack) {
        if (itemStack.is(WRENCHES)) {
            return true;
        }

        SyncConfig config = SyncConfig.getInstance();
        Identifier wrenchId = config.wrench() == null || config.wrench().isBlank() ? null : Identifier.tryParse(config.wrench());
        if (wrenchId == null) {
            return false;
        }
        Item wrench = BuiltInRegistries.ITEM.getOptional(wrenchId).orElse(null);
        return wrench != null && wrench != Items.AIR && itemStack.is(wrench);
    }

    public static boolean isWrench(ItemLike item) {
        return isWrench(new ItemStack(item));
    }

    public static boolean isArmor(ItemStack itemStack) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().isArmor();
    }

    public static boolean isArmor(ItemLike item) {
        return isArmor(new ItemStack(item));
    }

    public static EquipmentSlot getPreferredEquipmentSlot(ItemStack itemStack) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable != null) {
            return equippable.slot();
        }
        return EquipmentSlot.MAINHAND;
    }

    public static EquipmentSlot getPreferredEquipmentSlot(ItemLike item) {
        return getPreferredEquipmentSlot(new ItemStack(item));
    }

    private ItemUtil() {
    }
}