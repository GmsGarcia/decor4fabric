package net.gmsgarcia.decor4fabric.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.18.2's {@code workBench}: a full slab on four legs with four cross-supports,
 * and the only block in the mod that was not made of {@code Material.WOOD}.
 *
 * <p>{@code Material.METAL} survives as a grey map colour and as the 3.5/3.5
 * strength pair, but 1.18.2 also passed {@code .sounds(BlockSoundGroup.WOOD)},
 * so the block sounds like wood. That contradiction is preserved rather than
 * resolved: see {@link net.gmsgarcia.decor4fabric.content.BlockFamilies#WORKBENCH}.
 *
 * <p>No {@code onUse} override here yet. 1.18.2 opened
 * {@code workBenchScreenHandler}, which is Phase 4's codec-based
 * {@code Recipe<SingleRecipeInput>} rewrite; opening a screen before the
 * recipes exist would hand the player an empty, unrecoverable grid.
 */
public class WorkBenchBlock extends WaterloggedFacingBlock {

    public static final MapCodec<WorkBenchBlock> CODEC = BlockBehaviour.simpleCodec(WorkBenchBlock::new);

    /* WORKBENCH BASE */
    private static final VoxelShape WORKBENCH_BASE = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    private static final VoxelShape LEG_NORTH_WEST = Block.box(1.0D, 0.0D, 1.0D, 4.0D, 14.0D, 4.0D);
    private static final VoxelShape LEG_NORTH_EAST = Block.box(12.0D, 0.0D, 1.0D, 15.0D, 14.0D, 4.0D);
    private static final VoxelShape LEG_SOUTH_WEST = Block.box(1.0D, 0.0D, 12.0D, 4.0D, 14.0D, 15.0D);
    private static final VoxelShape LEG_SOUTH_EAST = Block.box(12.0D, 0.0D, 12.0D, 15.0D, 14.0D, 15.0D);

    /* WORKBENCH LEG SUPPORT */
    private static final VoxelShape NORTH_SUPP = Block.box(4.0D, 8.0D, 2.0D, 12.0D, 10.0D, 4.0D);
    private static final VoxelShape SOUTH_SUPP = Block.box(4.0D, 8.0D, 12.0D, 12.0D, 10.0D, 14.0D);
    private static final VoxelShape WEST_SUPP = Block.box(2.0D, 8.0D, 4.0D, 4.0D, 10.0D, 12.0D);
    private static final VoxelShape EAST_SUPP = Block.box(12.0D, 8.0D, 4.0D, 14.0D, 10.0D, 12.0D);

    private static final VoxelShape WORKBENCH_TABLE = Shapes.or(WORKBENCH_BASE,
            LEG_NORTH_EAST, LEG_NORTH_WEST, LEG_SOUTH_EAST, LEG_SOUTH_WEST,
            NORTH_SUPP, SOUTH_SUPP, WEST_SUPP, EAST_SUPP);

    public WorkBenchBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends WorkBenchBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return WORKBENCH_TABLE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return WORKBENCH_TABLE;
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return WORKBENCH_TABLE;
    }
}
