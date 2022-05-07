package net.gmsgarcia.decor4fabric.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
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

import static net.gmsgarcia.decor4fabric.sitOnStuff.Sit.sitMain;

public class logBench3 extends HorizontalFacingBlock {

    /* FACING NORTH OR SOUTH */
    private static final VoxelShape TOP_NS = Block.createCuboidShape(0.0D, 6.0D,  3.0D, 16.0D, 8.0D, 13.0D);
    private static final VoxelShape MID_NS = Block.createCuboidShape(0.0D, 5.0D,  4.0D, 16.0D, 6.0D, 12.0D);
    private static final VoxelShape BOTTOM_NS = Block.createCuboidShape(0.0D, 4.0D,  5.0D, 16.0D, 5.0D, 11.0D);

    private static final VoxelShape FIRST_LEG_SUPPORT_1_NS = Block.createCuboidShape(2.0D, 3.0D,  6.0D, 4.0D, 4.0D, 10.0D);
    private static final VoxelShape FIRST_LEG_SUPPORT_2_NS = Block.createCuboidShape(2.0D, 2.0D,  5.0D, 4.0D, 3.0D, 11.0D);

    private static final VoxelShape FIRST_LEG_WEST_NS = Block.createCuboidShape(2.0D, 0.0D,  5.0D, 4.0D, 2.0D, 7.0D);
    private static final VoxelShape FIRST_LEG_EAST_NS = Block.createCuboidShape(2.0D, 0.0D,  9.0D, 4.0D, 2.0D, 11.0D);

    private static final VoxelShape SECOND_LEG_SUPPORT_1_NS = Block.createCuboidShape(12.0D, 3.0D,  6.0D, 14.0D, 4.0D, 10.0D);
    private static final VoxelShape SECOND_LEG_SUPPORT_2_NS = Block.createCuboidShape(12.0D, 2.0D,  5.0D, 14.0D, 3.0D, 11.0D);

    private static final VoxelShape SECOND_LEG_WEST_NS = Block.createCuboidShape(12.0D, 0.0D,  5.0D, 14.0D, 2.0D, 7.0D);
    private static final VoxelShape SECOND_LEG_EAST_NS = Block.createCuboidShape(12.0D, 0.0D,  9.0D, 14.0D, 2.0D, 11.0D);

    /* FACING WEST & EAST */
    private static final VoxelShape TOP_WE = Block.createCuboidShape(3.0D, 6.0D, 0.0D, 13.0D, 8.0D, 16.0D);
    private static final VoxelShape MID_WE = Block.createCuboidShape(4.0D, 5.0D,  0.0D, 12.0D, 6.0D, 16.0D);
    private static final VoxelShape BOTTOM_WE = Block.createCuboidShape(5.0D, 4.0D,  0.0D, 11.0D, 5.0D, 16.0D);

    private static final VoxelShape FIRST_LEG_SUPPORT_1_WE = Block.createCuboidShape(6.0D, 3.0D,  2.0D, 10.0D, 4.0D, 4.0D);
    private static final VoxelShape FIRST_LEG_SUPPORT_2_WE = Block.createCuboidShape(5.0D, 2.0D,  2.0D, 11.0D, 3.0D, 4.0D);

    private static final VoxelShape FIRST_LEG_WEST_WE = Block.createCuboidShape(5.0D, 0.0D,  2.0D, 7.0D, 2.0D, 4.0D);
    private static final VoxelShape FIRST_LEG_EAST_WE = Block.createCuboidShape(9.0D, 0.0D,  2.0D, 11.0D, 2.0D, 4.0D);

    private static final VoxelShape SECOND_LEG_SUPPORT_1_WE = Block.createCuboidShape(6.0D, 3.0D,  12.0D, 10.0D, 4.0D, 14.0D);
    private static final VoxelShape SECOND_LEG_SUPPORT_2_WE = Block.createCuboidShape(5.0D, 2.0D,  12.0D, 11.0D, 3.0D, 14.0D);

    private static final VoxelShape SECOND_LEG_WEST_WE = Block.createCuboidShape(5.0D, 0.0D,  12.0D, 7.0D, 2.0D, 14.0D);
    private static final VoxelShape SECOND_LEG_EAST_WE = Block.createCuboidShape(9.0D, 0.0D,  12.0D, 11.0D, 2.0D, 14.0D);


    private static final VoxelShape FACING_NS = VoxelShapes.union(TOP_NS, MID_NS, BOTTOM_NS, FIRST_LEG_SUPPORT_1_NS, FIRST_LEG_SUPPORT_2_NS, FIRST_LEG_WEST_NS, FIRST_LEG_EAST_NS, SECOND_LEG_SUPPORT_1_NS, SECOND_LEG_SUPPORT_2_NS, SECOND_LEG_WEST_NS, SECOND_LEG_EAST_NS);
    private static final VoxelShape FACING_WE = VoxelShapes.union(TOP_WE, MID_WE, BOTTOM_WE, FIRST_LEG_SUPPORT_1_WE, FIRST_LEG_SUPPORT_2_WE, FIRST_LEG_WEST_WE, FIRST_LEG_EAST_WE, SECOND_LEG_SUPPORT_1_WE, SECOND_LEG_SUPPORT_2_WE, SECOND_LEG_WEST_WE, SECOND_LEG_EAST_WE);

    public logBench3() {
        super(FabricBlockSettings.of(Material.WOOD)
                .resistance(3f)
                .hardness(2f)
                .sounds(BlockSoundGroup.WOOD));
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        Direction dir = state.get(FACING);
        switch(dir) {
            case NORTH:
            case SOUTH:
                return FACING_NS;
            case EAST:
            case WEST:
                return FACING_WE;
            default:
                return VoxelShapes.fullCube();
        }
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
