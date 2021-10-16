package net.gmsgarcia.decor4fabric.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class simple_logChair2 extends HorizontalFacingBlock {

    /* CHAIR LEGS */
    private static final VoxelShape FIRST_LEG = Block.createCuboidShape(3.0D, 0.0D,  3.0D, 5.0D, 7.0D, 5.0D);
    private static final VoxelShape SECOND_LEG = Block.createCuboidShape(11.0D, 0.0D,  3.0D, 13.0D, 7.0D, 5.0D);
    private static final VoxelShape THIRD_LEG = Block.createCuboidShape(3.0D, 0.0D,  11.0D, 5.0D, 7.0D, 13.0D);
    private static final VoxelShape FOURTH_LEG = Block.createCuboidShape(11.0D, 0.0D,  11.0D, 13.0D, 7.0D, 13.0D);

    /* CHAIR SIT */
    private static final VoxelShape CHAIR_SIT = Block.createCuboidShape(2.0D, 7.0D,  2.0D, 14.0D, 9.0D, 14.0D);

    /* DIRECTIONS */
    private static final VoxelShape CHAIR_NORTH = Block.createCuboidShape(2.0D, 9.0D,  12.0D, 14.0D, 20.0D, 14.0D);
    private static final VoxelShape CHAIR_NORTH_SUPPORT_1 = Block.createCuboidShape(3.0D, 9.0D,  3.0D, 4.0D, 12.0D, 5.0D);
    private static final VoxelShape CHAIR_NORTH_SUPPORT_2 = Block.createCuboidShape(12.0D, 9.0D,  3.0D, 13.0D, 12.0D, 5.0D);
    private static final VoxelShape CHAIR_NORTH_SUPPORT_TOP_1 = Block.createCuboidShape(2.0D, 12.0D,  3.0D, 4.0D, 13.0D, 12.0D);
    private static final VoxelShape CHAIR_NORTH_SUPPORT_TOP_2 = Block.createCuboidShape(12.0D, 12.0D,  3.0D, 14.0D, 13.0D, 12.0D);

    private static final VoxelShape CHAIR_SOUTH = Block.createCuboidShape(2.0D, 9.0D,  2.0D, 14.0D, 20.0D, 4.0D);
    private static final VoxelShape CHAIR_SOUTH_SUPPORT_1 = Block.createCuboidShape(3.0D, 9.0D,  11.0D, 4.0D, 12.0D, 13.0D);
    private static final VoxelShape CHAIR_SOUTH_SUPPORT_2 = Block.createCuboidShape(12.0D, 9.0D,  11.0D, 13.0D, 12.0D, 13.0D);
    private static final VoxelShape CHAIR_SOUTH_SUPPORT_TOP_1 = Block.createCuboidShape(2.0D, 12.0D,  4.0D, 4.0D, 13.0D, 13.0D);
    private static final VoxelShape CHAIR_SOUTH_SUPPORT_TOP_2 = Block.createCuboidShape(12.0D, 12.0D,  4.0D, 14.0D, 13.0D, 13.0D);

    private static final VoxelShape CHAIR_EAST = Block.createCuboidShape(2.0D, 9.0D,  2.0D, 4.0D, 20.0D, 14.0D);
    private static final VoxelShape CHAIR_EAST_SUPPORT_1 = Block.createCuboidShape(11.0D, 9.0D,  3.0D, 13.0D, 12.0D, 4.0D);
    private static final VoxelShape CHAIR_EAST_SUPPORT_2 = Block.createCuboidShape(11.0D, 9.0D,  12.0D, 13.0D, 12.0D, 13.0D);
    private static final VoxelShape CHAIR_EAST_SUPPORT_TOP_1 = Block.createCuboidShape(4.0D, 12.0D,  2.0D, 13.0D, 13.0D, 4.0D);
    private static final VoxelShape CHAIR_EAST_SUPPORT_TOP_2 = Block.createCuboidShape(4.0D, 12.0D,  12.0D, 13.0D, 13.0D, 14.0D);

    private static final VoxelShape CHAIR_WEST = Block.createCuboidShape(12.0D, 9.0D,  2.0D, 14.0D, 20.0D, 14.0D);
    private static final VoxelShape CHAIR_WEST_SUPPORT_1 = Block.createCuboidShape(3.0D, 9.0D,  3.0D, 5.0D, 12.0D, 4.0D);
    private static final VoxelShape CHAIR_WEST_SUPPORT_2 = Block.createCuboidShape(3.0D, 9.0D,  12.0D, 5.0D, 12.0D, 13.0D);
    private static final VoxelShape CHAIR_WEST_SUPPORT_TOP_1 = Block.createCuboidShape(3.0D, 12.0D,  2.0D, 12.0D, 13.0D, 4.0D);
    private static final VoxelShape CHAIR_WEST_SUPPORT_TOP_2 = Block.createCuboidShape(3.0D, 12.0D,  12.0D, 12.0D, 13.0D, 14.0D);

    /* VOXEL GROUPS */
    private static final VoxelShape CHAIR_VOXELSHAPE = VoxelShapes.union(FIRST_LEG, SECOND_LEG, THIRD_LEG, FOURTH_LEG, CHAIR_SIT);

    private static final VoxelShape FACING_NORTH = VoxelShapes.union
    (CHAIR_VOXELSHAPE, CHAIR_NORTH, CHAIR_NORTH_SUPPORT_1, CHAIR_NORTH_SUPPORT_2, CHAIR_NORTH_SUPPORT_TOP_1, CHAIR_NORTH_SUPPORT_TOP_2);

    private static final VoxelShape FACING_SOUTH = VoxelShapes.union
    (CHAIR_VOXELSHAPE, CHAIR_SOUTH, CHAIR_SOUTH_SUPPORT_1, CHAIR_SOUTH_SUPPORT_2, CHAIR_SOUTH_SUPPORT_TOP_1, CHAIR_SOUTH_SUPPORT_TOP_2);

    private static final VoxelShape FACING_EAST = VoxelShapes.union
    (CHAIR_VOXELSHAPE, CHAIR_EAST, CHAIR_EAST_SUPPORT_1, CHAIR_EAST_SUPPORT_2, CHAIR_EAST_SUPPORT_TOP_1, CHAIR_EAST_SUPPORT_TOP_2);

    private static final VoxelShape FACING_WEST = VoxelShapes.union
    (CHAIR_VOXELSHAPE, CHAIR_WEST, CHAIR_WEST_SUPPORT_1, CHAIR_WEST_SUPPORT_2, CHAIR_WEST_SUPPORT_TOP_1, CHAIR_WEST_SUPPORT_TOP_2);

    public simple_logChair2() {
        super(FabricBlockSettings.of(Material.WOOD)
                .resistance(3f)
                .hardness(2f)
                .breakByTool(FabricToolTags.AXES, 0)
                .sounds(BlockSoundGroup.WOOD));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        Direction dir = state.get(FACING);
        return switch (dir) {
            case NORTH -> FACING_NORTH;
            case SOUTH -> FACING_SOUTH;
            case EAST -> FACING_EAST;
            case WEST -> FACING_WEST;
            default -> VoxelShapes.fullCube();
        };
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
    }
}
