package com.breakinblocks.neosync.api.shell;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

class ShellStateComponentFactoryRegistryImpl implements ShellStateComponentFactoryRegistry {
    public static final ShellStateComponentFactoryRegistryImpl INSTANCE = new ShellStateComponentFactoryRegistryImpl();

    private final Set<ShellStateComponentFactory> factories = new CopyOnWriteArraySet<>();

    @Override
    public Collection<ShellStateComponentFactory> getValues() {
        return Collections.unmodifiableSet(this.factories);
    }

    @Override
    public ShellStateComponentFactory register(ShellStateComponentFactory factory) {
        this.factories.add(factory);
        return factory;
    }
}