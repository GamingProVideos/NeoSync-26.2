package com.breakinblocks.neosync.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import com.breakinblocks.neosync.api.event.EntityFitnessEvents;
import com.breakinblocks.neosync.common.block.TreadmillBlock;
import com.breakinblocks.neosync.common.config.SyncConfig;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TreadmillBlockEntity extends BlockEntity implements DoubleBlockEntity, TickableBlockEntity, EnergyHandler {
    private static final int MAX_RUNNING_TIME = 20 * 60 * 15;
    private static final double MAX_SQUARED_DISTANCE = 0.5;
    private static final Map<EntityType<? extends Entity>, Long> ENERGY_MAP;
    private static final Vec3[] HORIZONTAL_MOTION = new Vec3[Direction.values().length];

    static {
        for (Direction d : Direction.values()) {
            HORIZONTAL_MOTION[d.ordinal()] = new Vec3(d.getStepX() * -0.08, 0, d.getStepZ() * -0.08);
        }
    }

    private UUID runnerUUID;
    private Integer runnerId;
    private Entity runner;
    private int runningTime;
    private long storedEnergy;
    private long producibleEnergyQuantity;
    private TreadmillBlockEntity cachedBackPart;

    public TreadmillBlockEntity(BlockPos pos, BlockState state) {
        super(SyncBlockEntities.TREADMILL.get(), pos, state);
    }

    private void setRunner(Entity entity) {
        if (this.runner == entity) {
            return;
        }

        if (this.runner != null) {
            if (this.runner instanceof LivingEntity livingEntity) {
                livingEntity.setSpeed(0);
                livingEntity.setDeltaMovement(Vec3.ZERO);
                if (livingEntity instanceof Mob mob) {
                    mob.xxa = 0;
                    mob.zza = 0;
                }
            }

            EntityFitnessEvents.STOP_RUNNING.invoker().onStopRunning(this.runner, this);
        }

        if (entity == null) {
            this.runningTime = 0;
            this.producibleEnergyQuantity = 0;
        }
        this.runner = entity;

        if (this.runner != null) {
            EntityFitnessEvents.START_RUNNING.invoker().onStartRunning(this.runner, this);
        }

        if (this.level == null) {
            return;
        }

        if (!this.level.isClientSide()) {
            this.setChanged();
            this.sync();
        }
    }

    @Override
    public void onClientTick(Level world, BlockPos pos, BlockState state) {
        if (this.runnerId != null) {
            this.setRunner(world.getEntity(this.runnerId));
            this.runnerId = null;
        }

        if (this.runner == null) {
            return;
        }

        this.runningTime = Math.min(++this.runningTime, MAX_RUNNING_TIME);

        if (!TreadmillBlock.isBack(state)) {
            RandomSource rand = world.getRandom();
            Direction face = state.getValue(TreadmillBlock.FACING);
            double cx = pos.getX() + 0.5;
            double cz = pos.getZ() + 0.5;
            double cy = pos.getY() + 0.75;
            for (int i = 0; i < 2; ++i) {
                double dx = (rand.nextDouble() - 0.5) * 0.4;
                double dz = (rand.nextDouble() - 0.5) * 0.4;
                world.addParticle(ParticleTypes.SMOKE,
                        cx + dx, cy + rand.nextDouble() * 0.1, cz + dz,
                        face.getStepX() * -0.05, 0.02, face.getStepZ() * -0.05);
            }
            if (this.isOverheated() && this.runningTime % 4 == 0) {
                world.addParticle(ParticleTypes.LARGE_SMOKE,
                        cx + (rand.nextDouble() - 0.5) * 0.3,
                        cy + 0.2,
                        cz + (rand.nextDouble() - 0.5) * 0.3,
                        0, 0.05, 0);
            }
        }
    }

    @Override
    public void onServerTick(Level world, BlockPos pos, BlockState state) {
        if (this.runnerUUID != null && world instanceof ServerLevel serverWorld) {
            this.setRunner(serverWorld.getEntity(this.runnerUUID));
            this.runnerUUID = null;
        }

        if (this.runner == null) {
            return;
        }

        Direction face = state.getValue(TreadmillBlock.FACING);
        Vec3 anchor = computeTreadmillPivot(pos, face);
        if (!isValidEntity(this.runner) || !isEntityNear(this.runner, anchor)) {
            this.setRunner(null);
            return;
        }

        if (!(this.runner instanceof Player)) {
            float yaw = face.toYRot();
            this.runner.snapTo(anchor.x, anchor.y, anchor.z, yaw, 0F);
            this.runner.setYHeadRot(yaw);
            this.runner.setYBodyRot(yaw);
            this.runner.setYRot(yaw);
            this.runner.yRotO = yaw;
        }

        if (this.runner instanceof LivingEntity livingEntity) {
            livingEntity.setSpeed(0.15F);
            livingEntity.setDeltaMovement(HORIZONTAL_MOTION[face.ordinal()]);

            if (livingEntity instanceof Mob mob) {
                mob.getNavigation().stop();
                mob.xxa = 0;
                mob.zza = 1;
            }

            livingEntity.setNoActionTime(0);
        }
        this.storedEnergy = this.producibleEnergyQuantity * (long)(1.0 + 0.5 * this.runningTime / MAX_RUNNING_TIME);
        this.transferEnergy(world, pos);
        if (this.runningTime < MAX_RUNNING_TIME) {
            ++this.runningTime;
            if (this.runningTime % 1000 == 0) {
                this.setChanged();
                this.sync();
            }
        }
    }

    public void onSteppedOn(BlockPos pos, BlockState state, Entity entity) {
        if (this.runner != null || !isEntityNear(entity, computeTreadmillPivot(pos, state.getValue(TreadmillBlock.FACING)))) {
            return;
        }

        Long energy = isValidEntity(entity) ? getOutputEnergyQuantityForEntity(entity, this) : null;
        if (energy != null) {
            this.setRunner(entity);
            this.producibleEnergyQuantity = energy;
        }
    }

    public boolean isOverheated() {
        return this.runner != null && this.runningTime >= MAX_RUNNING_TIME;
    }

    @Override
    public long getAmountAsLong() {
        TreadmillBlockEntity back = this.getBackPart();
        return back == null ? 0L : back.storedEnergy;
    }

    @Override
    public long getCapacityAsLong() {
        TreadmillBlockEntity back = this.getBackPart();
        if (back == null || back.runner == null) return 0L;
        return (long) (back.producibleEnergyQuantity * (1.0 + 0.5 * back.runningTime / MAX_RUNNING_TIME));
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TreadmillBlockEntity back = this.getBackPart();
        if (back == null) return 0;
        int extracted = (int) Math.min(back.storedEnergy, amount);
        if (extracted > 0) {
            back.storedEnergy -= extracted;
        }
        return extracted;
    }

    private void transferEnergy(Level world, BlockPos pos) {
        if (!(world instanceof ServerLevel serverLevel)) return;
        TreadmillBlockEntity back = this.getBackPart();
        if (back == null || back.storedEnergy <= 0) return;

        for (int i = 0; i < 2; ++i) {
            for (Direction direction : Direction.values()) {
                if (back.storedEnergy <= 0) return;
                BlockPos neighborPos = pos.relative(direction);
                EnergyHandler neighbor = serverLevel.getCapability(Capabilities.Energy.BLOCK,
                        neighborPos, direction.getOpposite());
                if (neighbor != null) {
                    try (Transaction tx = Transaction.openRoot()) {
                        int transferred = neighbor.insert((int) Math.min(back.storedEnergy, Integer.MAX_VALUE), tx);
                        if (transferred > 0) {
                            back.storedEnergy -= transferred;
                            tx.commit();
                        }
                    }
                }
            }
            pos = pos.relative(this.getBlockState().getValue(TreadmillBlock.FACING));
        }
    }

    @Override
    public DoubleBlockHalf getBlockType(BlockState state) {
        return TreadmillBlock.getTreadmillPart(state);
    }

    private TreadmillBlockEntity getBackPart() {
        if (this.cachedBackPart != null || this.level == null) {
            return this.cachedBackPart;
        }

        if (TreadmillBlock.isBack(this.getBlockState())) {
            this.cachedBackPart = this;
        } else {
            BlockPos backPartPos = this.worldPosition.relative(this.getBlockState().getValue(TreadmillBlock.FACING).getOpposite());
            BlockEntity be = this.level.getBlockEntity(backPartPos);
            this.cachedBackPart = be instanceof TreadmillBlockEntity ? (TreadmillBlockEntity) be : null;
        }
        return this.cachedBackPart;
    }

    protected void sync() {
        if (this.level instanceof ServerLevel serverWorld) {
            serverWorld.getChunkSource().blockChanged(this.worldPosition);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        this.saveCustomOnly(out);
        return out.buildResult();
    }


    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        this.runnerUUID = in.read("runner", UUIDUtil.CODEC).orElse(null);
        this.runnerId = in.getInt("runnerId").orElse(null);
        this.producibleEnergyQuantity = in.getLongOr("energy", 0L);
        this.runningTime = in.getIntOr("time", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        UUID runnerUuid = this.runnerUUID == null ? this.runner == null ? null : this.runner.getUUID() : this.runnerUUID;
        if (runnerUuid != null) {
            out.store("runner", UUIDUtil.CODEC, runnerUuid);
        }
        Integer runnerId = this.runner == null ? null : this.runner.getId();
        if (runnerId != null) {
            out.putInt("runnerId", runnerId);
        }
        out.putLong("energy", this.producibleEnergyQuantity);
        out.putInt("time", this.runningTime);
    }

    private static Long getOutputEnergyQuantityForEntity(Entity entity, EnergyHandler energyStorage) {
        return EntityFitnessEvents.MODIFY_OUTPUT_ENERGY_QUANTITY.invoker().modifyOutputEnergyQuantity(entity, energyStorage, ENERGY_MAP.get(entity.getType()));
    }

    private static boolean isValidEntity(Entity entity) {
        if (entity == null || !entity.isAlive()) {
            return false;
        }

        return (
                !entity.isSpectator() && !entity.isCrouching() && !entity.isInWater() &&
                        (!(entity instanceof LivingEntity livingEntity) || livingEntity.hurtTime <= 0 && !livingEntity.isBaby()) &&
                        (!(entity instanceof Mob mobEntity) || !mobEntity.isLeashed()) &&
                        (!(entity instanceof TamableAnimal tameableEntity) || !tameableEntity.isOrderedToSit())
        );
    }

    private static boolean isEntityNear(Entity entity, Vec3 pos) {
        return entity.distanceToSqr(pos) < MAX_SQUARED_DISTANCE;
    }

    private static Vec3 computeTreadmillPivot(BlockPos pos, Direction face) {
        double x = switch (face) {
            case WEST -> pos.getX();
            case EAST -> pos.getX() + 1;
            default -> pos.getX() + 0.5D;
        };
        double y = pos.getY() + 0.175;
        double z = switch (face) {
            case SOUTH -> pos.getZ() + 1;
            case NORTH -> pos.getZ();
            default -> pos.getZ() + 0.5D;
        };
        return new Vec3(x, y, z);
    }

    static {
        ENERGY_MAP = SyncConfig.getInstance().energyMap().stream()
                .collect(Collectors.toUnmodifiableMap(
                        SyncConfig.EnergyMapEntry::getEntityType,
                        SyncConfig.EnergyMapEntry::outputEnergyQuantity,
                        (a, b) -> a
                ));
    }
}