package com.breakinblocks.neosync.common.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.chat.ChatAbilities;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.client.gui.controller.DeathScreenController;
import com.breakinblocks.neosync.client.gui.hud.HudController;

import java.util.Objects;

@EventBusSubscriber(modid = NeoSync.MOD_ID, value = Dist.CLIENT)
public class PersistentCameraEntity extends LocalPlayer {
    public static final long GOAL_IDLE_TIMEOUT_MS = 10_000;
    private static final double TICK_MILLIS = 50.0;

    private Vec3 initialPos = Vec3.ZERO;
    private float initialYaw;
    private float initialPitch;
    private int transitionTick;
    private int transitionDurationTicks;
    private int transitionDelayTicks;
    private PersistentCameraEntityGoal goal;
    private long goalIdleSince = 0;

    private PersistentCameraEntity(Minecraft client, ClientLevel world, LocalPlayer player) {
        super(client, world, player.connection, player.getStats(), player.getRecipeBook(),
                Input.EMPTY, false, ChatAbilities.NO_RESTRICTIONS);
        this.snapTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        this.setYRot(player.getYRot());
        this.setXRot(player.getXRot());
        this.setYHeadRot(player.getYRot());
        this.setYBodyRot(player.getYRot());
        this.syncPreviousTransform();
        this.setDeltaMovement(Vec3.ZERO);
        this.noPhysics = true;
    }

    @Override
    public boolean isSpectator() {
        return true;
    }

    @Override
    public void aiStep() {
        PersistentCameraEntityGoal goal = this.goal;
        if (goal == null) {
            return;
        }

        // Minecraft renders entities between their previous and current tick transforms.
        // Use that native interpolation rather than wall-clock movement, otherwise tiny
        // variations in client tick timing show up as camera micro-stutter.
        this.syncPreviousTransform();
        this.setDeltaMovement(Vec3.ZERO);

        if (this.transitionDelayTicks > 0) {
            this.transitionDelayTicks--;
            return;
        }

        if (this.transitionDurationTicks <= 0) {
            this.applyTransform(goal.pos, goal.yaw, goal.pitch);
            // A zero-duration goal is a teleport. Keep previous/current transforms identical
            // so vanilla does not interpolate across the teleport for one frame.
            this.syncPreviousTransform();
            this.finishGoal(goal);
            return;
        }

        this.transitionTick = Math.min(this.transitionTick + 1, this.transitionDurationTicks);
        double progress = Mth.clamp(this.transitionTick / (double) this.transitionDurationTicks, 0.0, 1.0);

        // Quintic smootherstep has zero velocity and zero acceleration at both ends,
        // which avoids the small jerk that cubic smoothstep can still show at phase changes.
        double eased = progress * progress * progress * (progress * (progress * 6.0 - 15.0) + 10.0);

        Vec3 newPos = this.initialPos.lerp(goal.pos, eased);
        float yawDelta = Mth.wrapDegrees(goal.yaw - this.initialYaw);
        float newYaw = this.initialYaw + yawDelta * (float) eased;
        float newPitch = Mth.lerp((float) eased, this.initialPitch, goal.pitch);
        this.applyTransform(newPos, newYaw, newPitch);

        if (this.transitionTick >= this.transitionDurationTicks) {
            this.applyTransform(goal.pos, goal.yaw, goal.pitch);
            this.finishGoal(goal);
        }
    }

    private void applyTransform(Vec3 pos, float yaw, float pitch) {
        this.setPos(pos);
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.setYHeadRot(yaw);
        this.setYBodyRot(yaw);
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void syncPreviousTransform() {
        // 26.2's entity render path expects the full previous position/rotation state
        // to be maintained exactly like a normally ticked entity.
        this.setOldPosAndRot();
        this.yHeadRotO = this.yHeadRot;
        this.yBodyRotO = this.yBodyRot;
    }

    private void finishGoal(PersistentCameraEntityGoal completedGoal) {
        this.goal = null;
        this.transitionTick = 0;
        this.transitionDurationTicks = 0;
        this.transitionDelayTicks = 0;
        this.goalIdleSince = System.currentTimeMillis();
        completedGoal.finish(this);
    }

    public void setGoal(PersistentCameraEntityGoal goal) {
        this.goal = goal;
        this.initialPos = this.position();
        this.initialYaw = this.getYRot();
        this.initialPitch = this.getXRot();
        this.transitionTick = 0;
        this.transitionDurationTicks = goal == null ? 0 : millisToTicks(goal.duration);
        this.transitionDelayTicks = goal == null ? 0 : millisToTicks(goal.delay);
        this.setDeltaMovement(Vec3.ZERO);

        if (goal != null) {
            this.goalIdleSince = 0;
        }
    }

    private static int millisToTicks(long millis) {
        if (millis <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(millis / TICK_MILLIS));
    }

    public PersistentCameraEntityGoal getGoal() {
        return this.goal;
    }

    public static void setup(Minecraft client, PersistentCameraEntityGoal goal) {
        LocalPlayer player = client.player;
        if (player == null || player.level() == null) {
            return;
        }

        if (!(client.getCameraEntity() instanceof PersistentCameraEntity)) {
            client.setCameraEntity(new PersistentCameraEntity(client, (ClientLevel) player.level(), player));
        }

        PersistentCameraEntity camera = (PersistentCameraEntity) Objects.requireNonNull(client.getCameraEntity());
        camera.setGoal(goal);
    }

    public static void unset(Minecraft client) {
        if (client.getCameraEntity() instanceof PersistentCameraEntity camera) {
            camera.setGoal(null);
            client.setCameraEntity(null);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();
        if (!(client.getCameraEntity() instanceof PersistentCameraEntity camera)) {
            return;
        }
        if (camera.goal == null) {
            if (camera.goalIdleSince > 0 && System.currentTimeMillis() - camera.goalIdleSince > GOAL_IDLE_TIMEOUT_MS) {
                unset(client);
                HudController.restore();
                DeathScreenController.restore();
                if (client.player != null && client.player.isDeadOrDying()) {
                    client.gui.setScreen(null);
                }
            }
            return;
        }
        camera.aiStep();
    }

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        unset(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        unset(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onPlayerClone(ClientPlayerNetworkEvent.Clone event) {
        unset(Minecraft.getInstance());
    }
}
