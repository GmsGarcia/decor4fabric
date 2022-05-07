package net.gmsgarcia.decor4fabric.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import javax.swing.plaf.TableHeaderUI;

import static net.gmsgarcia.decor4fabric.sitOnStuff.Sit.sitMain;

public class logChair extends HorizontalFacingBlock {

    /* CHAIR LEGS */
    private static final VoxelShape FIRST_LEG = Block.createCuboidShape(3.0D, 0.0D,  3.0D, 16.0D, 5.0D, 5.0D);
    private static final VoxelShape SECOND_LEG = Block.createCuboidShape(11.0D, 0.0D,  3.0D, 13.0D, 7.0D, 5.0D);
    private static final VoxelShape THIRD_LEG = Block.createCuboidShape(3.0D, 0.0D,  11.0D, 16.0D, 7.0D, 13.0D);
    private static final VoxelShape FOURTH_LEG = Block.createCuboidShape(11.0D, 0.0D,  3.0D, 13.0D, 7.0D, 5.0D);

    /* FACING NORTH OR SOUTH */
    private static final VoxelShape TOP_NS = Block.createCuboidShape(0.0D, 4.0D,  3.0D, 16.0D, 6.0D, 13.0D);
    private static final VoxelShape MID_NS = Block.createCuboidShape(0.0D, 3.0D,  4.0D, 16.0D, 4.0D, 12.0D);
    private static final VoxelShape BOTTOM_NS = Block.createCuboidShape(0.0D, 2.0D,  5.0D, 16.0D, 3.0D, 11.0D);

    private static final VoxelShape FIRST_LEG_NS = Block.createCuboidShape(2.0D, 0.0D,  6.0D, 6.0D, 2.0D, 10.0D);
    private static final VoxelShape SECOND_LEG_NS = Block.createCuboidShape(10.0D, 0.0D,  6.0D, 14.0D, 2.0D, 10.0D);

    private static final VoxelShape AROUND_1LEG_NORTH_NS = Block.createCuboidShape(1.0D, 0.0D,  6.0D, 2.0D, 2.0D, 10.0D);
    private static final VoxelShape AROUND_1LEG_SOUTH_NS = Block.createCuboidShape(6.0D, 0.0D,  6.0D, 7.0D, 2.0D, 10.0D);
    private static final VoxelShape AROUND_1LEG_WEST_NS = Block.createCuboidShape(2.0D, 0.0D,  5.0D, 6.0D, 2.0D, 6.0D);
    private static final VoxelShape AROUND_1LEG_EAST_NS = Block.createCuboidShape(2.0D, 0.0D,  10.0D, 6.0D, 2.0D, 11.0D);

    private static final VoxelShape AROUND_2LEG_NORTH_NS = Block.createCuboidShape(9.0D, 0.0D,  6.0D, 10.0D, 2.0D, 10.0D);
    private static final VoxelShape AROUND_2LEG_SOUTH_NS = Block.createCuboidShape(14.0D, 0.0D,  6.0D, 15.0D, 2.0D, 10.0D);
    private static final VoxelShape AROUND_2LEG_WEST_NS = Block.createCuboidShape(10.0D, 0.0D,  5.0D, 14.0D, 2.0D, 6.0D);
    private static final VoxelShape AROUND_2LEG_EAST_NS = Block.createCuboidShape(10.0D, 0.0D,  10.0D, 14.0D, 2.0D, 11.0D);

    /* FACING WEST & EAST */
    private static final VoxelShape TOP_WE = Block.createCuboidShape(3.0D, 4.0D, 0.0D, 13.0D, 6.0D, 16.0D);
    private static final VoxelShape MID_WE = Block.createCuboidShape(4.0D, 3.0D,  0.0D, 12.0D, 4.0D, 16.0D);
    private static final VoxelShape BOTTOM_WE = Block.createCuboidShape(5.0D, 2.0D,  0.0D, 11.0D, 3.0D, 16.0D);

    private static final VoxelShape FIRST_LEG_WE = Block.createCuboidShape(6.0D, 0.0D,  2.0D, 10.0D, 2.0D, 6.0D);
    private static final VoxelShape SECOND_LEG_WE = Block.createCuboidShape(6.0D, 0.0D,  10.0D, 10.0D, 2.0D, 14.0D);

    private static final VoxelShape AROUND_1LEG_NORTH_WE = Block.createCuboidShape(6.0D, 0.0D,  1.0D, 10.0D, 2.0D, 2.0D);
    private static final VoxelShape AROUND_1LEG_SOUTH_WE = Block.createCuboidShape(6.0D, 0.0D,  6.0D, 10.0D, 2.0D, 7.0D);
    private static final VoxelShape AROUND_1LEG_WEST_WE = Block.createCuboidShape(5.0D, 0.0D,  2.0D, 6.0D, 2.0D, 6.0D);
    private static final VoxelShape AROUND_1LEG_EAST_WE = Block.createCuboidShape(10.0D, 0.0D,  2.0D, 11.0D, 2.0D, 6.0D);

    private static final VoxelShape AROUND_2LEG_NORTH_WE = Block.createCuboidShape(6.0D, 0.0D,  9.0D, 10.0D, 2.0D, 10.0D);
    private static final VoxelShape AROUND_2LEG_SOUTH_WE = Block.createCuboidShape(6.0D, 0.0D,  14.0D, 10.0D, 2.0D, 15.0D);
    private static final VoxelShape AROUND_2LEG_WEST_WE = Block.createCuboidShape(5.0D, 0.0D,  10.0D, 6.0D, 2.0D, 14.0D);
    private static final VoxelShape AROUND_2LEG_EAST_WE = Block.createCuboidShape(10.0D, 0.0D,  10.0D, 11.0D, 2.0D, 14.0D);

    private static final VoxelShape FACING_NS = VoxelShapes.union(TOP_NS, MID_NS, BOTTOM_NS, FIRST_LEG_NS, SECOND_LEG_NS, AROUND_1LEG_NORTH_NS, AROUND_1LEG_SOUTH_NS, AROUND_1LEG_WEST_NS, AROUND_1LEG_EAST_NS, AROUND_2LEG_NORTH_NS, AROUND_2LEG_SOUTH_NS, AROUND_2LEG_WEST_NS, AROUND_2LEG_EAST_NS);
    private static final VoxelShape FACING_WE = VoxelShapes.union(TOP_WE, MID_WE, BOTTOM_WE, FIRST_LEG_WE, SECOND_LEG_WE, AROUND_1LEG_NORTH_WE, AROUND_1LEG_SOUTH_WE, AROUND_1LEG_WEST_WE ,AROUND_1LEG_EAST_WE, AROUND_2LEG_NORTH_WE, AROUND_2LEG_SOUTH_WE, AROUND_2LEG_WEST_WE ,AROUND_2LEG_EAST_WE);

    private static final VoxelShape FACING_NORTH = VoxelShapes.union(FIRST_LEG, SECOND_LEG, THIRD_LEG, FOURTH_LEG);

    public logChair(){
        super(FabricBlockSettings.of(Material.WOOD)
                .resistance(3f)
                .hardness(2f)
                .sounds(BlockSoundGroup.WOOD));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        Direction dir = state.get(FACING);
        return switch (dir) {
            case NORTH, SOUTH, EAST, WEST -> FACING_NORTH;
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

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        sitMain();
        return ActionResult.SUCCESS;
    }
}