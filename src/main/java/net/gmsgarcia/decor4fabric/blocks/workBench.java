package net.gmsgarcia.decor4fabric.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.gmsgarcia.decor4fabric.screen.workBenchScreenHandler;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class workBench extends HorizontalFacingBlock {

    /* WORKBENCH BASE */
    private static final VoxelShape WORKBENCH_BASE = Block.createCuboidShape(0.0D, 12.0D,  0.0D, 16.0D, 16.0D, 16.0D);

    /* WORKBENCH BASE */
    private static final VoxelShape LEG_NORTH_WEST = Block.createCuboidShape(1.0D, 0.0D,  1.0D, 4.0D, 14.0D, 4.0D);
    private static final VoxelShape LEG_NORTH_EAST = Block.createCuboidShape(12.0D, 0.0D,  1.0D, 15.0D, 14.0D, 4.0D);
    private static final VoxelShape LEG_SOUTH_WEST = Block.createCuboidShape(1.0D, 0.0D,  12.0D, 4.0D, 14.0D, 15.0D);
    private static final VoxelShape LEG_SOUTH_EAST = Block.createCuboidShape(12.0D, 0.0D,  12.0D, 15.0D, 14.0D, 15.0D);

    /* WORKBENCH LEG SUPPORT */
    private static final VoxelShape NORTH_SUPP = Block.createCuboidShape(4.0D, 8.0D,  2.0D, 12.0D, 10.0D, 4.0D);
    private static final VoxelShape SOUTH_SUPP = Block.createCuboidShape(4.0D, 8.0D,  12.0D, 12.0D, 10.0D, 14.0D);
    private static final VoxelShape WEST_SUPP = Block.createCuboidShape(2.0D, 8.0D,  4.0D, 4.0D, 10.0D, 12.0D);
    private static final VoxelShape EAST_SUPP = Block.createCuboidShape(12.0D, 8.0D,  4.0D, 14.0D, 10.0D, 12.0D);

    private static final VoxelShape WORKBENCH_TABLE = VoxelShapes.union(WORKBENCH_BASE, LEG_NORTH_EAST, LEG_NORTH_WEST, LEG_SOUTH_EAST, LEG_SOUTH_WEST, NORTH_SUPP, SOUTH_SUPP, WEST_SUPP, EAST_SUPP);

    public workBench() {
        super(FabricBlockSettings.of(Material.METAL)
                .resistance(3.5f)
                .hardness(3.5f)
                .sounds(BlockSoundGroup.WOOD));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) player.openHandledScreen(new Factory(pos));
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        return WORKBENCH_TABLE;
    }

    private record Factory(BlockPos pos) implements NamedScreenHandlerFactory {

        @Override
        public Text getDisplayName() {
            return new TranslatableText("Workbench");
        }

        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            return new workBenchScreenHandler(syncId, inv, ScreenHandlerContext.create(player.world, pos));
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
    }

}
