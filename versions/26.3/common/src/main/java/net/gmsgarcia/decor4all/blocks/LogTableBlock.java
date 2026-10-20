package net.gmsgarcia.decor4all.blocks;

import com.mojang.serialization.MapCodec;
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

// 1.18.2's logTable: a full slab of a tabletop on four legs, where neighbouring tables merge by
// dropping the legs that meet.
public class LogTableBlock extends FenceBlock {
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
    public boolean connectsTo(BlockState state, boolean faceSolid, Direction direction) {
        return isTable(state);
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

    // Whether state is a table, i.e. whether a table should connect to it.
    private static boolean isTable(BlockState state) {
        return state.getBlock() instanceof LogTableBlock;
    }

    // 1.18.2's getFinalShape, transcribed case for case. Every branch keeps the top slab and adds
    // only the legs whose side has no neighbouring table, so a table ring closes into a single
    // continuous surface.
    private static VoxelShape tableShape(BlockState state, BlockGetter level, BlockPos pos) {
        boolean north = isTable(level.getBlockState(pos.north()));
        boolean east = isTable(level.getBlockState(pos.east()));
        boolean south = isTable(level.getBlockState(pos.south()));
        boolean west = isTable(level.getBlockState(pos.west()));

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
