package com.breakinblocks.neosync.client.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import com.breakinblocks.neosync.NeoSync;

public final class SyncModelLayers {
    public static final ModelLayerLocation SHELL_STORAGE = create("shell_storage", "main");
    public static final ModelLayerLocation SHELL_STORAGE_LED = create("shell_storage", "led");
    public static final ModelLayerLocation SHELL_CONSTRUCTOR = create("shell_constructor", "main");
    public static final ModelLayerLocation TREADMILL = create("treadmill", "main");

    private SyncModelLayers() {}

    private static ModelLayerLocation create(String name, String layer) {
        return new ModelLayerLocation(NeoSync.locate(name), layer);
    }
}
