package com.breakinblocks.neosync.common.block.entity;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;

import java.util.concurrent.atomic.AtomicInteger;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.compat.curios.CuriosClientCompat;

public class ShellEntity extends RemotePlayer {
    // Minecraft 26.2 reserves entity ID 0 for entities that have not been assigned an ID yet.
    // These client-only shell preview entities are never spawned through the normal packet path,
    // so give each one a unique negative ID before any renderer tries to extract its state.
    private static final AtomicInteger NEXT_RENDER_ENTITY_ID = new AtomicInteger(-1);

    public boolean isActive;
    public float pitchProgress;
    private final ShellState state;
    private Runnable onInitialized;

    public ShellEntity(ShellState state) {
        this(Minecraft.getInstance().level, state);
    }

    public ShellEntity(ClientLevel world, ShellState state) {
        super(world, buildProfile(state));
        this.setId(NEXT_RENDER_ENTITY_ID.getAndDecrement());
        this.isActive = false;
        this.pitchProgress = 0;
        this.state = state;
        state.getInventory().copyTo(this.getInventory());
        CuriosClientCompat.dressShell(this, state);
        this.snapTo(state.getPos().getX() + 0.5, state.getPos().getY(), state.getPos().getZ() + 0.5, 0F, 0F);

        if (this.onInitialized != null) {
            this.onInitialized.run();
            this.onInitialized = null;
        }
    }

    public void onInitialized(Runnable runnable) {
        if (this.state == null) {
            this.onInitialized = runnable;
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public ShellState getState() {
        return this.state;
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource damageSource) {
    }

    @Override
    public boolean isCreative() {
        return true;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public PlayerInfo getPlayerInfo() {
        return null;
    }

    private static GameProfile buildProfile(ShellState state) {
        PropertyMap props = new PropertyMap(ImmutableMultimap.of());
        String value = state.getTextureValue();
        if (value != null) {
            ImmutableMultimap.Builder<String, Property> builder = ImmutableMultimap.builder();
            builder.put("textures", new Property("textures", value, state.getTextureSignature()));
            props = new PropertyMap(builder.build());
        }
        return new GameProfile(state.getOwnerUuid(), state.getOwnerName(), props);
    }
}
