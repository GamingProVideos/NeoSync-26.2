package com.breakinblocks.neosync.compat.mekanism;

import com.breakinblocks.neosync.api.shell.ShellStateComponent;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public abstract class MekanismShellStateComponent extends ShellStateComponent {
    private static final String ID = "neosync:mekanism";
    private static final String KEY = "radiation";

    @Override
    public String getId() {
        return ID;
    }

    @Nullable
    protected abstract Double getRadiation();

    protected abstract void setRadiation(@Nullable Double radiation);

    @Override
    public void clone(ShellStateComponent component) {
        MekanismShellStateComponent other = component.as(MekanismShellStateComponent.class);
        if (other != null) {
            this.setRadiation(other.getRadiation());
        }
    }

    @Override
    protected void readComponentNbt(CompoundTag nbt) {
        this.setRadiation(nbt.getDouble(KEY).orElse(null));
    }

    @Override
    protected CompoundTag writeComponentNbt(CompoundTag nbt) {
        Double radiation = this.getRadiation();
        if (radiation != null) {
            nbt.putDouble(KEY, radiation);
        }
        return nbt;
    }
}
