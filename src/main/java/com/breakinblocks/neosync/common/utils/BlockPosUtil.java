package com.breakinblocks.neosync.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class BlockPosUtil {
    public static Optional<Direction> getHorizontalFacing(BlockPos pos, BlockGetter blockView) {
        BlockState state = blockView.getBlockState(pos);
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return Optional.of(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        return Optional.empty();
    }

    public static boolean hasPlayerInside(BlockPos pos, EntityGetter world) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        return world.getNearestPlayer(x, y, z, 1, false) != null;
    }

    public static boolean isEntityInside(Entity entity, BlockPos pos) {
        double dX = Math.abs((pos.getX() + 0.5) - entity.getX());
        double dZ = Math.abs((pos.getZ() + 0.5) - entity.getZ());
        final double MAX_DELTA = 0.01;
        return dX < MAX_DELTA && dZ < MAX_DELTA;
    }

    public static void moveEntity(Entity entity, BlockPos target, Direction facing, boolean inside) {
        Direction targetDirection = facing.getOpposite();
        Vec3 currentPos = entity.position();
        double targetX = target.getX() + 0.5;
        double targetZ = target.getZ() + 0.5;
        if (!inside) {
            targetX += targetDirection.getStepX();
            targetZ += targetDirection.getStepZ();
        }
        double currentX = currentPos.x;
        double currentZ = currentPos.z;
        final double MAX_SPEED = 0.33;
        double velocityX = getMinVelocity(targetX - currentX, MAX_SPEED);
        double velocityZ = getMinVelocity(targetZ - currentZ, MAX_SPEED);
        float yaw = targetDirection.toYRot();

        entity.setDeltaMovement(velocityX, 0, velocityZ);
        entity.setXRot(0);
        entity.setYRot(yaw);
        entity.setYHeadRot(yaw);
        entity.setYBodyRot(yaw);
        entity.yRotO = yaw;
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.yBodyRotO = yaw;
            livingEntity.yHeadRotO = yaw;
        }
    }

    private static double getMinVelocity(double velocity, double absLimit) {
        return Math.abs(velocity) < absLimit ? velocity : absLimit * Math.signum(velocity);
    }
}