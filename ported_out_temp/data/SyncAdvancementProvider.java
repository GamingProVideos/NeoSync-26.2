package com.breakinblocks.neosync.data;

import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.block.SyncBlocks;
import com.breakinblocks.neosync.common.item.SyncItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.data.AdvancementProvider.AdvancementGenerator;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public final class SyncAdvancementProvider implements AdvancementGenerator {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                        new ItemStack(SyncItems.SYNC_CORE.get()),
                        Component.translatable("advancements.neosync.root.title"),
                        Component.translatable("advancements.neosync.root.description"),
                        Identifier.withDefaultNamespace("textures/block/gray_concrete.png"),
                        AdvancementType.TASK,
                        true, true, false)
                .addCriterion("has_sync_core", InventoryChangeTrigger.TriggerInstance.hasItems(SyncItems.SYNC_CORE.get()))
                .save(saver, NeoSync.locate("main/root"), existingFileHelper);

        Advancement.Builder.advancement()
                .parent(root)
                .display(
                        new ItemStack(SyncBlocks.SHELL_CONSTRUCTOR.get()),
                        Component.translatable("advancements.neosync.place_constructor.title"),
                        Component.translatable("advancements.neosync.place_constructor.description"),
                        null,
                        AdvancementType.TASK,
                        true, true, false)
                .addCriterion("placed_constructor", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(SyncBlocks.SHELL_CONSTRUCTOR.get()))
                .save(saver, NeoSync.locate("main/place_constructor"), existingFileHelper);

        Advancement.Builder.advancement()
                .parent(root)
                .display(
                        new ItemStack(SyncBlocks.SHELL_STORAGE.get()),
                        Component.translatable("advancements.neosync.place_storage.title"),
                        Component.translatable("advancements.neosync.place_storage.description"),
                        null,
                        AdvancementType.TASK,
                        true, true, false)
                .addCriterion("placed_storage", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(SyncBlocks.SHELL_STORAGE.get()))
                .save(saver, NeoSync.locate("main/place_storage"), existingFileHelper);

        Advancement.Builder.advancement()
                .parent(root)
                .display(
                        new ItemStack(SyncBlocks.TREADMILL.get()),
                        Component.translatable("advancements.neosync.place_treadmill.title"),
                        Component.translatable("advancements.neosync.place_treadmill.description"),
                        null,
                        AdvancementType.TASK,
                        true, true, false)
                .addCriterion("placed_treadmill", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(SyncBlocks.TREADMILL.get()))
                .save(saver, NeoSync.locate("main/place_treadmill"), existingFileHelper);

        AdvancementHolder firstSync = Advancement.Builder.advancement()
                .parent(root)
                .display(
                        new ItemStack(SyncBlocks.SHELL_STORAGE.get()),
                        Component.translatable("advancements.neosync.first_sync.title"),
                        Component.translatable("advancements.neosync.first_sync.description"),
                        null,
                        AdvancementType.TASK,
                        true, true, false)
                .addCriterion("synced", CriterionBuilder.impossible())
                .save(saver, NeoSync.locate("main/first_sync"), existingFileHelper);

        Advancement.Builder.advancement()
                .parent(firstSync)
                .display(
                        new ItemStack(SyncItems.SYNC_CORE.get()),
                        Component.translatable("advancements.neosync.cross_dim_sync.title"),
                        Component.translatable("advancements.neosync.cross_dim_sync.description"),
                        null,
                        AdvancementType.GOAL,
                        true, true, false)
                .addCriterion("synced_cross_dim", CriterionBuilder.impossible())
                .save(saver, NeoSync.locate("main/cross_dim_sync"), existingFileHelper);
    }

    private static final class CriterionBuilder {
        static net.minecraft.advancements.Criterion<ImpossibleTrigger.TriggerInstance> impossible() {
            return CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance());
        }
    }
}
