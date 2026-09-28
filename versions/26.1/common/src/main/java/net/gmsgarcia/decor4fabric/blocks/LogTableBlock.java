package net.gmsgarcia.decor4fabric.blocks;

import com.mojang.serialization.MapCodec;
import net.gmsgarcia.decor4fabric.registry.DecorTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.18.2's {@code logTable}: a full slab of a tabletop on four legs, where
 * neighbouring tables merge by dropping the legs that meet.
 *
 * <p>Extending {@code FenceBlock} is 1.18.2's trick for getting the connection
 * state for free, and it comes with a price: the four side booleans are public
 * state that 1.18.2 never set, and the block ignored them entirely. It overrides
 * the connection test to consult the {@code decor4fabric:tables} block tag, then
 * computes its own shape from the same tag.
 *
 * <p>Two 26.1 renames apply here: {@code canConnect} is now {@code connectsTo},
 * and {@code Shapes.union} is now {@code Shapes.or}.
 *
 * <p>The codec is declared as {@code MapCodec<FenceBlock>> rather than
 * {@code MapCodec<? extends LogTableBlock>} because {@code FenceBlock.codec()}
 * is declared with that concrete parameter type and generics are invariant, so a
 * narrower override does not compile. See {@link LogFenceBlock}.
 *
 * <p>26.1's {@code FenceBlock} builds its shapes from four hard-coded numbers
 * (post 4..12, height to 24) rather than accepting them, so the inherited
 * fence shape stays whatever vanilla's is. 1.18.2 overrode the collision and
 * outline shapes to the table shape and left the occlusion shape alone, and that
 * is reproduced exactly: the overrides below cover outline, collision and visual
 * shape, and {@code getOcclusionShape} is deliberately not touched.
 */
public class LogTableBlock extends FenceBlock {

    public static final MapCodec<FenceBlock> CODEC = BlockBehaviour.simpleCodec(LogTableBlock::new);

    /* TABLE BASE */
    private static final VoxelShape TABLE_BASE = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    private static final VoxelShape LEG_NORTH_WEST = Block.box(1.0D, 0.0D, 1.0D, 4.0D, 14.0D, 4.0D);
    private static final VoxelShape LEG_NORTH_EAST = Block.box(12.0D, 0.0D, 1.0D, 15.0D, 14.0D, 4.0D);
    private static final VoxelShape LEG_SOUTH_WEST = Block.box(1.0D, 0.0D, 12.0D, 4.0D, 14.0D, 15.0D);
    private static final VoxelShape LEG_SOUTH_EAST = Block.box(12.0D, 0.0D, 12.0D, 15.0D, 14.0D, 15.0D);

    private static final VoxelShape ALL_LEGS = Shapes.or(
            LEG_NORTH_EAST, LEG_NORTH_WEST, LEG_SOUTH_EAST, LEG_SOUTH_WEST);

    public LogTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FenceBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean connectsTo(BlockState state, boolean faceSolid, Direction direction) {
        return state.is(DecorTags.Blocks.TABLES);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return tableShape(state, level, pos);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return tableShape(state, level, pos);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return tableShape(state, level, pos);
    }

    /**
     * 1.18.2's {@code getFinalShape}, transcribed case for case. Every branch
     * keeps the top slab and adds only the legs whose side has no neighbouring
     * table, so a table ring closes into a single continuous surface.
     */
    private static VoxelShape tableShape(BlockState state, BlockGetter level, BlockPos pos) {
        boolean north = level.getBlockState(pos.north()).is(DecorTags.Blocks.TABLES);
        boolean east = level.getBlockState(pos.east()).is(DecorTags.Blocks.TABLES);
        boolean south = level.getBlockState(pos.south()).is(DecorTags.Blocks.TABLES);
        boolean west = level.getBlockState(pos.west()).is(DecorTags.Blocks.TABLES);

        if (!north && !east && !south && !west) {
            return Shapes.or(TABLE_BASE, ALL_LEGS);
        }
        if (north && east && !south && !west) {
            return Shapes.or(TABLE_BASE, LEG_SOUTH_WEST);
        }
        if (north && !east && !south && west) {
            return Shapes.or(TABLE_BASE, LEG_SOUTH_EAST);
        }
        if (!north && east && south && !west) {
            return Shapes.or(TABLE_BASE, LEG_NORTH_WEST);
        }
        if (!north && !east && south && west) {
            return Shapes.or(TABLE_BASE, LEG_NORTH_EAST);
        }
        if (north && !east && !south && !west) {
            return Shapes.or(TABLE_BASE, LEG_SOUTH_EAST, LEG_SOUTH_WEST);
        }
        if (!north && east && !south && !west) {
            return Shapes.or(TABLE_BASE, LEG_NORTH_WEST, LEG_SOUTH_WEST);
        }
        if (!north && !east && south && !west) {
            return Shapes.or(TABLE_BASE, LEG_NORTH_EAST, LEG_NORTH_WEST);
        }
        if (!north && !east && !south && west) {
            return Shapes.or(TABLE_BASE, LEG_NORTH_EAST, LEG_SOUTH_EAST);
        }
        return TABLE_BASE;
    }
}
