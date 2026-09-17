package com.breakinblocks.neosync.data;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.minecraft.data.loot.BlockLootSubProvider;
import com.breakinblocks.neosync.common.block.SyncBlocks;
import com.breakinblocks.neosync.common.block.TreadmillBlock;

import java.util.Set;
import java.util.stream.Stream;

public final class SyncBlockLootSubProvider extends BlockLootSubProvider {
    public SyncBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        this.dropSelf(SyncBlocks.SHELL_STORAGE.get());
        this.dropSelf(SyncBlocks.SHELL_CONSTRUCTOR.get());
        this.add(SyncBlocks.TREADMILL.get(), block -> LootTable.lootTable().withPool(
                LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(block)
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(TreadmillBlock.PART, TreadmillBlock.Part.BACK))))
                        .when(ExplosionCondition.survivesExplosion())
        ));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Stream.of(SyncBlocks.SHELL_STORAGE, SyncBlocks.SHELL_CONSTRUCTOR, SyncBlocks.TREADMILL)
                .map(DeferredBlock::get)
                .map(b -> (Block) b)
                .toList();
    }
}
