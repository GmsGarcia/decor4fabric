package net.gmsgarcia.decor4fabric.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.18.2's {@code logBench}: a slab-height seat. All of its behaviour lives in
 * {@link AxeStoringSeatBlock}; this class is the shape and nothing else.
 *
 * <p>1.18.2 wrote the shape in 16ths of a block via {@code VoxelShapes.cuboid} and
 * overrode only {@code getOutlineShape}: north/south
 * {@code cuboid(0, 0, 0.3125, 1, 0.375, 0.6875)}, east/west the same box rotated
 * onto the other axis. Translated to {@link Block#box}, that is the two constants
 * below.
 *
 * <p>The three shape overrides are not redundant. In 1.18.2
 * {@code AbstractBlock.getCollisionShape} returned {@code getOutlineShape(...)}
 * and {@code AbstractBlock.getOutlineShape} returned {@code state.getShape(...)},
 * so overriding the outline set collision and picking too. 26.1 gives collision,
 * outline and picking three independent methods that each default to the full
 * cube, so a port that overrides only one of them silently turns the block solid.
 */
public class LogBenchBlock extends AxeStoringSeatBlock {

    public static final MapCodec<LogBenchBlock> CODEC = BlockBehaviour.simpleCodec(LogBenchBlock::new);

    private static final VoxelShape NORTH_SOUTH = Block.box(0.0D, 0.0D, 5.0D, 16.0D, 6.0D, 11.0D);
    private static final VoxelShape EAST_WEST = Block.box(5.0D, 0.0D, 0.0D, 11.0D, 6.0D, 16.0D);

    public LogBenchBlock(BlockBehaviour.Properties properties) {
        super(properties);
        setFlipsFacingOnTakeAxe(true);
    }

    @Override
    protected MapCodec<? extends LogBenchBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return outline(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return outline(state);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return outline(state);
    }

    private static VoxelShape outline(BlockState state) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? NORTH_SOUTH : EAST_WEST;
    }
}
