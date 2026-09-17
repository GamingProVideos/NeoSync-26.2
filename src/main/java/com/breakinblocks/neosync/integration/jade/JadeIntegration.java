package com.breakinblocks.neosync.integration.jade;

import com.breakinblocks.neosync.common.block.ShellConstructorBlock;
import com.breakinblocks.neosync.common.block.ShellStorageBlock;
import com.breakinblocks.neosync.common.block.TreadmillBlock;
import snownee.jade.impl.WailaClientRegistration;

public final class JadeIntegration {
    private static boolean registered;

    private JadeIntegration() {}

    public static void register() {
        if (registered) return;
        registered = true;

        WailaClientRegistration registration = WailaClientRegistration.instance();
        registration.registerBlockComponent(ShellContainerComponentProvider.INSTANCE, ShellConstructorBlock.class);
        registration.registerBlockComponent(ShellContainerComponentProvider.INSTANCE, ShellStorageBlock.class);
        registration.registerBlockComponent(TreadmillComponentProvider.INSTANCE, TreadmillBlock.class);
    }
}
