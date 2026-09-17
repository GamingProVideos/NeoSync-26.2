    package com.breakinblocks.neosync.common.block;

    import com.mojang.serialization.MapCodec;
    import net.minecraft.core.BlockPos;
    import net.minecraft.core.Direction;
    import net.minecraft.core.particles.ParticleTypes;
    import net.minecraft.util.RandomSource;
    import net.minecraft.util.StringRepresentable;
    import net.minecraft.world.entity.Entity;
    import net.minecraft.world.entity.LivingEntity;
    import net.minecraft.world.entity.player.Player;
    import net.minecraft.world.item.ItemStack;
    import net.minecraft.world.item.context.BlockPlaceContext;
    import net.minecraft.world.level.BlockGetter;
    import net.minecraft.world.level.Level;
    import net.minecraft.world.level.LevelAccessor;
    import net.minecraft.world.level.block.*;
    import net.minecraft.world.level.block.entity.BlockEntity;
    import net.minecraft.world.level.block.entity.BlockEntityTicker;
    import net.minecraft.world.level.block.entity.BlockEntityType;
    import net.minecraft.world.level.block.state.BlockState;
    import net.minecraft.world.level.block.state.StateDefinition;
    import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
    import net.minecraft.world.level.block.state.properties.EnumProperty;
    import net.minecraft.world.level.pathfinder.PathComputationType;
    import net.minecraft.world.phys.shapes.CollisionContext;
    import net.minecraft.world.phys.shapes.Shapes;
    import net.minecraft.world.phys.shapes.VoxelShape;
    import com.breakinblocks.neosync.common.block.entity.SyncBlockEntities;
    import com.breakinblocks.neosync.common.block.entity.TickableBlockEntity;
    import com.breakinblocks.neosync.common.block.entity.TreadmillBlockEntity;
    import org.jetbrains.annotations.Nullable;

    @SuppressWarnings("deprecation")
    public class TreadmillBlock extends HorizontalDirectionalBlock implements EntityBlock {
        public static final MapCodec<TreadmillBlock> CODEC = simpleCodec(TreadmillBlock::new);
        public static final EnumProperty<Part> PART = EnumProperty.create("treadmill_part", Part.class);

        @Override
        protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
            return CODEC;
        }


        private static final VoxelShape NORTH_SHAPE_BACK;
        private static final VoxelShape NORTH_SHAPE_FRONT;
        private static final VoxelShape SOUTH_SHAPE_BACK;
        private static final VoxelShape SOUTH_SHAPE_FRONT;
        private static final VoxelShape EAST_SHAPE_BACK;
        private static final VoxelShape EAST_SHAPE_FRONT;
        private static final VoxelShape WEST_SHAPE_BACK;
        private static final VoxelShape WEST_SHAPE_FRONT;

        public TreadmillBlock(Properties properties) {
            super(properties);
            this.registerDefaultState(this.stateDefinition.any().setValue(PART, Part.BACK));
        }

        public static boolean isBack(BlockState state) {
            Part part = state.getValue(PART);
            return part == Part.BACK;
        }

        public static DoubleBlockHalf getTreadmillPart(BlockState state) {
            Part part = state.getValue(PART);
            return part == Part.BACK ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;
        }

        @Override
        @Nullable
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new TreadmillBlockEntity(pos, state);
        }

        @Override
        public RenderShape getRenderShape(BlockState state) {
            return RenderShape.MODEL;
        }

        @Override
        public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
            Part part = state.getValue(PART);
            Direction facing = state.getValue(FACING);
            BlockEntity first = world.getBlockEntity(pos);
            BlockEntity second = world.getBlockEntity(pos.relative(getDirectionTowardsOtherPart(part, facing)));
            if (!(first instanceof TreadmillBlockEntity firstTreadmill) || !(second instanceof TreadmillBlockEntity secondTreadmill)) {
                return;
            }
            TreadmillBlockEntity front = part == Part.BACK ? secondTreadmill : firstTreadmill;
            TreadmillBlockEntity back = part == Part.BACK ? firstTreadmill : secondTreadmill;

            if (back.isOverheated()) {
                double x = front.getBlockPos().getX() + random.nextDouble();
                double y = front.getBlockPos().getY() + 0.4;
                double z = front.getBlockPos().getZ() + random.nextDouble();
                world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0.1, 0);
            }
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING, PART);
        }


        @Override
        public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            Direction direction = ctx.getHorizontalDirection();
            BlockPos blockPos = ctx.getClickedPos();
            BlockPos blockPos2 = blockPos.relative(direction);
            return ctx.getLevel().getBlockState(blockPos2).canBeReplaced(ctx) ? this.defaultBlockState().setValue(FACING, direction) : null;
        }

        @Override
        public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
            super.setPlacedBy(world, pos, state, placer, itemStack);
            if (!world.isClientSide()) {
                BlockPos blockPos = pos.relative(state.getValue(FACING));
                world.setBlock(blockPos, state.setValue(PART, Part.FRONT), 3);
                world.updateNeighborsAt(pos, Blocks.AIR);
                state.updateNeighbourShapes(world, pos, 3);
            }
        }

        @Override
        public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
            if (!world.isClientSide() && player.isCreative()) {
                Part part = state.getValue(PART);
                if (part == Part.FRONT) {
                    BlockPos blockPos = pos.relative(getDirectionTowardsOtherPart(part, state.getValue(FACING)));
                    BlockState blockState = world.getBlockState(blockPos);
                    if (blockState.getBlock() == this && blockState.getValue(PART) == Part.BACK) {
                        world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 35);
                        world.levelEvent(player, 2001, blockPos, Block.getId(blockState));
                    }
                }
            }
            return super.playerWillDestroy(world, pos, state, player);
        }

        @Override
        protected boolean isPathfindable(BlockState state, PathComputationType type) {
            return true;
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
            Direction direction = state.getValue(FACING);
            boolean isBack = isBack(state);

            return switch (direction) {
                case NORTH -> isBack ? NORTH_SHAPE_BACK : NORTH_SHAPE_FRONT;
                case SOUTH -> isBack ? SOUTH_SHAPE_BACK : SOUTH_SHAPE_FRONT;
                case EAST -> isBack ? EAST_SHAPE_BACK : EAST_SHAPE_FRONT;
                case WEST -> isBack ? WEST_SHAPE_BACK : WEST_SHAPE_FRONT;
                default -> NORTH_SHAPE_FRONT;
            };
        }

        @Override
        public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
            if (!world.isClientSide() && world.getBlockEntity(pos) instanceof TreadmillBlockEntity treadmillBlockEntity) {
                treadmillBlockEntity.onSteppedOn(pos, state, entity);
            }
        }

        @Override
        @Nullable
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
            if (type != SyncBlockEntities.TREADMILL.get() || !isBack(state)) {
                return null;
            }

            return TickableBlockEntity.getTicker(world);
        }

        public static Direction getDirectionTowardsOtherPart(Part part, Direction direction) {
            return part == Part.BACK ? direction : direction.getOpposite();
        }

        public enum Part implements StringRepresentable {
            FRONT("front"),
            BACK("back");

            private final String name;

            Part(String name) {
                this.name = name;
            }

            @Override
            public String toString() {
                return this.name;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }

        static {
            final VoxelShape TRACK_NORTH_SOUTH = Block.box(1.5, 0, 0, 14.5, 4, 16);
            final VoxelShape TRACK_EAST_WEST = Block.box(0, 0, 1.5, 16, 4, 14.5);

            final VoxelShape LEFT_SIDE_GUARD_NORTH_BACK = Block.box(1.5, 0, 0, 1.6, 8.3, 7.4);
            final VoxelShape RIGHT_SIDE_GUARD_NORTH_BACK = Block.box(14.4, 0, 0, 14.5, 8.3, 7.4);

            final VoxelShape LEFT_SIDE_GUARD_NORTH_FRONT = Block.box(1.5, 0, 7.5, 1.6, 8.7, 16);
            final VoxelShape RIGHT_SIDE_GUARD_NORTH_FRONT = Block.box(14.4, 0, 7.5, 14.5, 8.7, 16);
            final VoxelShape DASHBOARD_NORTH_FRONT = Block.box(1.5, 0, 2, 14.5, 8.5, 3);

            final VoxelShape LEFT_SIDE_GUARD_SOUTH_BACK = Block.box(1.5, 0, 8.6, 1.6, 8.3, 16);
            final VoxelShape RIGHT_SIDE_GUARD_SOUTH_BACK = Block.box(14.4, 0, 8.6, 14.5, 8.3, 16);

            final VoxelShape LEFT_SIDE_GUARD_SOUTH_FRONT = Block.box(1.5, 0, 0, 1.6, 8.7, 8.5);
            final VoxelShape RIGHT_SIDE_GUARD_SOUTH_FRONT = Block.box(14.4, 0, 0, 14.5, 8.7, 8.5);
            final VoxelShape DASHBOARD_SOUTH_FRONT = Block.box(1.5, 0, 13, 14.5, 8.5, 14);

            final VoxelShape LEFT_SIDE_GUARD_EAST_BACK = Block.box(8.6, 0, 1.5, 16, 8.3, 1.6);
            final VoxelShape RIGHT_SIDE_GUARD_EAST_BACK = Block.box(8.6, 0, 14.4, 16, 8.3, 14.5);

            final VoxelShape LEFT_SIDE_GUARD_EAST_FRONT = Block.box(0, 0, 1.5, 8.5, 8.7, 1.6);
            final VoxelShape RIGHT_SIDE_GUARD_EAST_FRONT = Block.box(0, 0, 14.4, 8.5, 8.7, 14.5);
            final VoxelShape DASHBOARD_EAST_FRONT = Block.box(13, 0, 1.5, 14, 8.5, 14.5);

            final VoxelShape LEFT_SIDE_GUARD_WEST_BACK = Block.box(0, 0, 1.5, 7.4, 8.3, 1.6);
            final VoxelShape RIGHT_SIDE_GUARD_WEST_BACK = Block.box(0, 0, 14.4, 7.4, 8.3, 14.5);

            final VoxelShape LEFT_SIDE_GUARD_WEST_FRONT = Block.box(7.5, 0, 1.5, 16, 8.7, 1.6);
            final VoxelShape RIGHT_SIDE_GUARD_WEST_FRONT = Block.box(7.5, 0, 14.4, 16, 8.7, 14.5);
            final VoxelShape DASHBOARD_WEST_FRONT = Block.box(2, 0, 1.5, 3, 8.5, 14.5);

            NORTH_SHAPE_BACK = Shapes.or(TRACK_NORTH_SOUTH, LEFT_SIDE_GUARD_NORTH_BACK, RIGHT_SIDE_GUARD_NORTH_BACK).optimize();
            NORTH_SHAPE_FRONT = Shapes.or(TRACK_NORTH_SOUTH, LEFT_SIDE_GUARD_NORTH_FRONT, RIGHT_SIDE_GUARD_NORTH_FRONT, DASHBOARD_NORTH_FRONT).optimize();

            SOUTH_SHAPE_BACK = Shapes.or(TRACK_NORTH_SOUTH, LEFT_SIDE_GUARD_SOUTH_BACK, RIGHT_SIDE_GUARD_SOUTH_BACK).optimize();
            SOUTH_SHAPE_FRONT = Shapes.or(TRACK_NORTH_SOUTH, LEFT_SIDE_GUARD_SOUTH_FRONT, RIGHT_SIDE_GUARD_SOUTH_FRONT, DASHBOARD_SOUTH_FRONT).optimize();

            EAST_SHAPE_BACK = Shapes.or(TRACK_EAST_WEST, LEFT_SIDE_GUARD_EAST_BACK, RIGHT_SIDE_GUARD_EAST_BACK).optimize();
            EAST_SHAPE_FRONT = Shapes.or(TRACK_EAST_WEST, LEFT_SIDE_GUARD_EAST_FRONT, RIGHT_SIDE_GUARD_EAST_FRONT, DASHBOARD_EAST_FRONT).optimize();

            WEST_SHAPE_BACK = Shapes.or(TRACK_EAST_WEST, LEFT_SIDE_GUARD_WEST_BACK, RIGHT_SIDE_GUARD_WEST_BACK).optimize();
            WEST_SHAPE_FRONT = Shapes.or(TRACK_EAST_WEST, LEFT_SIDE_GUARD_WEST_FRONT, RIGHT_SIDE_GUARD_WEST_FRONT, DASHBOARD_WEST_FRONT).optimize();
        }
    }