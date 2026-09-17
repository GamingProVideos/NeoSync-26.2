package com.breakinblocks.neosync.data;

import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.block.SyncBlocks;
import com.breakinblocks.neosync.common.item.SyncItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.ItemUsedOnLocationTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public final class SyncAdvancementProvider implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                        SyncItems.SYNC_CORE.get(),
                        Component.translatable("advancements.neosync.root.title"),
                        Component.translatable("advancements.neosync.root.description"),
                        Identifier.withDefaultNamespace("textures/block/gray_concrete.png"),
                        AdvancementType.TASK,
                        true, true, false)
                .addCriterion("has_sync_core", InventoryChangeTrigger.TriggerInstance.hasItems(SyncItems.SYNC_CORE.get()))
                .save(saver, NeoSync.locate("main/root").toString());

        Advancement.Builder.advancement()
                .parent(root)
                .display(
                        SyncBlocks.SHELL_CONSTRUCTOR.get(),
                        Component.translatable("advancements.neosync.place_constructor.title"),
                        Component.translatable("advancements.neosync.place_constructor.description"),
                        null,
                        AdvancementType.TASK,
                        true, true, false)
                .addCriterion("placed_constructor", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(SyncBlocks.SHELL_CONSTRUCTOR.get()))
                .addCriterion("placed_zero_point_constructor", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(SyncBlocks.ZERO_POINT_SHELL_CONSTRUCTOR.get()))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(saver, NeoSync.locate("main/place_constructor").toString());

        Advancement.Builder.advancement()
                .parent(root)
                .display(
                        SyncBlocks.SHELL_STORAGE.get(),
                        Component.translatable("advancements.neosync.place_storage.title"),
                        Component.translatable("advancements.neosync.place_storage.description"),
                        null,
                        AdvancementType.TASK,
                        true, true, false)
                .addCriterion("placed_storage", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(SyncBlocks.SHELL_STORAGE.get()))
                .addCriterion("placed_zero_point_storage", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(SyncBlocks.ZERO_POINT_SHELL_STORAGE.get()))
                .addCriterion("placed_manual_storage", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(SyncBlocks.MANUAL_SHELL_STORAGE.get()))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(saver, NeoSync.locate("main/place_storage").toString());

        Advancement.Builder.advancement()
                .parent(root)
                .display(
                        SyncBlocks.TREADMILL.get(),
                        Component.translatable("advancements.neosync.place_treadmill.title"),
                        Component.translatable("advancements.neosync.place_treadmill.description"),
                        null,
                        AdvancementType.TASK,
                        true, true, false)
                .addCriterion("placed_treadmill", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(SyncBlocks.TREADMILL.get()))
                .save(saver, NeoSync.locate("main/place_treadmill").toString());

        AdvancementHolder firstSync = Advancement.Builder.advancement()
                .parent(root)
                .display(
                        SyncBlocks.SHELL_STORAGE.get(),
                        Component.translatable("advancements.neosync.first_sync.title"),
                        Component.translatable("advancements.neosync.first_sync.description"),
                        null,
                        AdvancementType.TASK,
                        true, true, false)
                .addCriterion("synced", impossible())
                .save(saver, NeoSync.locate("main/first_sync").toString());

        Advancement.Builder.advancement()
                .parent(firstSync)
                .display(
                        SyncItems.SYNC_CORE.get(),
                        Component.translatable("advancements.neosync.cross_dim_sync.title"),
                        Component.translatable("advancements.neosync.cross_dim_sync.description"),
                        null,
                        AdvancementType.GOAL,
                        true, true, false)
                .addCriterion("synced_cross_dim", impossible())
                .save(saver, NeoSync.locate("main/cross_dim_sync").toString());
    }

    private static Criterion<ImpossibleTrigger.TriggerInstance> impossible() {
        return CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance());
    }
}
