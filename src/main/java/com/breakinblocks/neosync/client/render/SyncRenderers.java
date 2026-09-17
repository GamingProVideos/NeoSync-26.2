package com.breakinblocks.neosync.client.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.client.model.ShellConstructorModel;
import com.breakinblocks.neosync.client.model.ShellStorageModel;
import com.breakinblocks.neosync.client.model.SyncModelLayers;
import com.breakinblocks.neosync.client.model.TreadmillModel;
import com.breakinblocks.neosync.client.render.block.entity.ShellConstructorBlockEntityRenderer;
import com.breakinblocks.neosync.client.render.block.entity.ShellStorageBlockEntityRenderer;
import com.breakinblocks.neosync.client.render.block.entity.TreadmillBlockEntityRenderer;
import com.breakinblocks.neosync.client.render.item.SyncSpecialModelRenderers;
import com.breakinblocks.neosync.common.block.entity.SyncBlockEntities;

@EventBusSubscriber(modid = NeoSync.MOD_ID, value = Dist.CLIENT)
public final class SyncRenderers {
    private SyncRenderers() {}

    public static void initClient() {
        BlockEntityRenderers.register(SyncBlockEntities.SHELL_STORAGE.get(), ShellStorageBlockEntityRenderer::new);
        BlockEntityRenderers.register(SyncBlockEntities.SHELL_CONSTRUCTOR.get(), ShellConstructorBlockEntityRenderer::new);
        BlockEntityRenderers.register(SyncBlockEntities.ZERO_POINT_SHELL_STORAGE.get(), ShellStorageBlockEntityRenderer::new);
        BlockEntityRenderers.register(SyncBlockEntities.ZERO_POINT_SHELL_CONSTRUCTOR.get(), ShellConstructorBlockEntityRenderer::new);
        BlockEntityRenderers.register(SyncBlockEntities.MANUAL_SHELL_STORAGE.get(), ShellStorageBlockEntityRenderer::new);
        BlockEntityRenderers.register(SyncBlockEntities.TREADMILL.get(), TreadmillBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SyncModelLayers.SHELL_STORAGE, ShellStorageModel::createLayer);
        event.registerLayerDefinition(SyncModelLayers.SHELL_STORAGE_LED, ShellStorageModel::createLedLayer);
        event.registerLayerDefinition(SyncModelLayers.SHELL_CONSTRUCTOR, ShellConstructorModel::createLayer);
        event.registerLayerDefinition(SyncModelLayers.TREADMILL, TreadmillModel::createLayer);
    }

    @SubscribeEvent
    public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(NeoSync.locate("shell_storage"), SyncSpecialModelRenderers.ShellStorage.MAP_CODEC);
        event.register(NeoSync.locate("shell_constructor"), SyncSpecialModelRenderers.ShellConstructor.MAP_CODEC);
        event.register(NeoSync.locate("treadmill"), SyncSpecialModelRenderers.Treadmill.MAP_CODEC);
    }
}
