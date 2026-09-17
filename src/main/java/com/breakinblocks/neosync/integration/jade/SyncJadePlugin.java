package com.breakinblocks.neosync.integration.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade plugin entry point for production jars where Jade's annotation scanner picks up the
 * {@link WailaPlugin} annotation. Dev-mode registration goes through
 * {@link JadeIntegration#register()} directly — both paths are idempotent and safe to call
 * together.
 */
@WailaPlugin("neosync:plugin")
public class SyncJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        JadeIntegration.register();
    }
}
