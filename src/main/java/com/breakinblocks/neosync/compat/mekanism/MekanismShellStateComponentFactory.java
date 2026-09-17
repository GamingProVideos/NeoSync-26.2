package com.breakinblocks.neosync.compat.mekanism;

import com.breakinblocks.neosync.api.shell.ShellStateComponent;
import com.breakinblocks.neosync.api.shell.ShellStateComponentFactoryRegistry;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class MekanismShellStateComponentFactory implements ShellStateComponentFactoryRegistry.ShellStateComponentFactory {
    @Override
    public ShellStateComponent empty() {
        return new StoredRadiation();
    }

    @Override
    public ShellStateComponent of(ServerPlayer player) {
        return new AbsorbedRadiation(player);
    }

    private static final class StoredRadiation extends MekanismShellStateComponent {
        private Double radiation;

        @Override
        @Nullable
        protected Double getRadiation() {
            return this.radiation;
        }

        @Override
        protected void setRadiation(@Nullable Double radiation) {
            this.radiation = radiation;
        }
    }

    private static final class AbsorbedRadiation extends MekanismShellStateComponent {
        private final ServerPlayer player;

        private AbsorbedRadiation(ServerPlayer player) {
            this.player = player;
        }

        @Override
        @Nullable
        protected Double getRadiation() {
            return MekanismRadiation.get(this.player);
        }

        @Override
        protected void setRadiation(@Nullable Double radiation) {
            MekanismRadiation.set(this.player, radiation);
        }
    }
}
