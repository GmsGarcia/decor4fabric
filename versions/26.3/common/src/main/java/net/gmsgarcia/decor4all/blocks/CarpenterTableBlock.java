package net.gmsgarcia.decor4all.blocks;

import net.gmsgarcia.decor4all.menu.CarpenterTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// 1.18.2's carpenterTable: a full slab on four legs with four cross-supports, and the only block
// in the mod that was not made of Material.WOOD.
public class CarpenterTableBlock extends WaterloggedFacingBlock {
    /* CARPENTER_TABLE BASE */
    private static final VoxelShape CARPENTER_TABLE_BASE = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    private static final VoxelShape LEG_NORTH_WEST = Block.box(1.0D, 0.0D, 1.0D, 4.0D, 14.0D, 4.0D);
    private static final VoxelShape LEG_NORTH_EAST = Block.box(12.0D, 0.0D, 1.0D, 15.0D, 14.0D, 4.0D);
    private static final VoxelShape LEG_SOUTH_WEST = Block.box(1.0D, 0.0D, 12.0D, 4.0D, 14.0D, 15.0D);
    private static final VoxelShape LEG_SOUTH_EAST = Block.box(12.0D, 0.0D, 12.0D, 15.0D, 14.0D, 15.0D);

    /* CARPENTER_TABLE LEG SUPPORT */
    private static final VoxelShape NORTH_SUPP = Block.box(4.0D, 8.0D, 2.0D, 12.0D, 10.0D, 4.0D);
    private static final VoxelShape SOUTH_SUPP = Block.box(4.0D, 8.0D, 12.0D, 12.0D, 10.0D, 14.0D);
    private static final VoxelShape WEST_SUPP = Block.box(2.0D, 8.0D, 4.0D, 4.0D, 10.0D, 12.0D);
    private static final VoxelShape EAST_SUPP = Block.box(12.0D, 8.0D, 4.0D, 14.0D, 10.0D, 12.0D);

    private static final VoxelShape CARPENTER_TABLE_SHAPE = Shapes.or(CARPENTER_TABLE_BASE,
            LEG_NORTH_EAST, LEG_NORTH_WEST, LEG_SOUTH_EAST, LEG_SOUTH_WEST,
            NORTH_SUPP, SOUTH_SUPP, WEST_SUPP, EAST_SUPP);

    public CarpenterTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CARPENTER_TABLE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return CARPENTER_TABLE_SHAPE;
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return CARPENTER_TABLE_SHAPE;
    }

    // Opens the carpentry table menu.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        player.openMenu(new Provider(pos));
        return InteractionResult.CONSUME;
    }

    // Carries the block position into CarpenterTableMenu's factory.
    private record Provider(BlockPos pos) implements MenuProvider {

        @Override
        public Component getDisplayName() {
            return Component.translatable("container.decor4all.carpenter_table");
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            ContainerLevelAccess access = player.level().isClientSide()
                    ? ContainerLevelAccess.NULL
                    : ContainerLevelAccess.create(player.level(), pos);
            CarpenterTableMenu menu = new CarpenterTableMenu(containerId, playerInventory, access);

            // The menu needs a reference to the viewer to send its recipe list to.
            // Only a server has one; the client copy is built by the MenuType
            // factory and never sends anything.
            if (player instanceof ServerPlayer serverPlayer) {
                menu.setViewer(serverPlayer);
            }
            return menu;
        }
    }
}
