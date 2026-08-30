package com.zavidvi.voidmod.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GravePartBlock extends Block {
    public static final MapCodec<GravePartBlock> CODEC = simpleCodec(GravePartBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final IntegerProperty SIDE = IntegerProperty.create("side", 0, 2);

    public static final BooleanProperty UPPER = BooleanProperty.create("upper");

    private static final int SIDE_ZERO = 1;

    private static final VoxelShape[] SHAPES = buildShapes();

    public GravePartBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SIDE, SIDE_ZERO)
                .setValue(UPPER, true));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SIDE, UPPER);
    }

    public static BlockState stateFor(BlockState grave, int side, boolean upper) {
        Direction facing = grave.hasProperty(GraveBlock.FACING)
                ? grave.getValue(GraveBlock.FACING)
                : Direction.NORTH;

        return com.zavidvi.voidmod.registry.ModBlocks.GRAVE_PART.get().defaultBlockState()
                .setValue(FACING, facing)
                .setValue(SIDE, side + SIDE_ZERO)
                .setValue(UPPER, upper);
    }

    public static BlockPos gravePos(BlockState state, BlockPos pos) {
        Direction side = state.getValue(FACING).getClockWise();
        int steps = state.getValue(SIDE) - SIDE_ZERO;
        return pos.offset(-side.getStepX() * steps,
                state.getValue(UPPER) ? -1 : 0,
                -side.getStepZ() * steps);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[shapeIndex(state)];
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, net.minecraft.world.InteractionHand hand,
                                          BlockHitResult hit) {
        return GraveBlock.useBlackBone(stack, level, gravePos(state, pos), player);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos,
                                     BlockState neighbourState, RandomSource random) {
        BlockPos grave = gravePos(state, pos);
        return level.getBlockState(grave).getBlock() instanceof GraveBlock
                ? state
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    private static int shapeIndex(BlockState state) {
        return shapeIndex(state.getValue(FACING), state.getValue(SIDE) - SIDE_ZERO, state.getValue(UPPER));
    }

    private static int shapeIndex(Direction facing, int side, boolean upper) {
        return ((facing.get2DDataValue() * 3) + (side + SIDE_ZERO)) * 2 + (upper ? 1 : 0);
    }

    private static VoxelShape[] buildShapes() {
        VoxelShape[] shapes = new VoxelShape[Direction.Plane.HORIZONTAL.length() * 3 * 2];

        for (Direction facing : Direction.Plane.HORIZONTAL) {
            Direction side = facing.getClockWise();
            for (int offset = -1; offset <= 1; offset++) {
                for (int upper = 0; upper <= 1; upper++) {
                    VoxelShape moved = GraveBlock.shapeFor(facing).move(
                            -side.getStepX() * offset, -upper, -side.getStepZ() * offset);

                    shapes[shapeIndex(facing, offset, upper == 1)] = Shapes
                            .joinUnoptimized(moved, Shapes.block(), BooleanOp.AND)
                            .optimize();
                }
            }
        }

        return shapes;
    }
}
