package net.gmsgarcia.decor4all.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// 1.18.2's logBench: a slab-height seat. All of its behaviour lives in AxeStoringSeatBlock; this
// class is the shape and nothing else.
public class LogBenchBlock extends AxeStoringSeatBlock {

    public static final MapCodec<LogBenchBlock> CODEC = BlockBehaviour.simpleCodec(LogBenchBlock::new);

    private static final VoxelShape NORTH_SOUTH = Block.box(0.0D, 0.0D, 5.0D, 16.0D, 6.0D, 11.0D);
    private static final VoxelShape EAST_WEST = Block.box(5.0D, 0.0D, 0.0D, 11.0D, 6.0D, 16.0D);

    public LogBenchBlock(BlockBehaviour.Properties properties) {
        super(properties);
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
