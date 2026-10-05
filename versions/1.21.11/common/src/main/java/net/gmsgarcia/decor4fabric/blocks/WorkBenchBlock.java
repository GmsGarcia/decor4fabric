package net.gmsgarcia.decor4fabric.blocks;

import com.mojang.serialization.MapCodec;
import net.gmsgarcia.decor4fabric.menu.WorkBenchMenu;
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

/**
 * 1.18.2's {@code workBench}: a full slab on four legs with four cross-supports,
 * and the only block in the mod that was not made of {@code Material.WOOD}.
 *
 * <p>{@code Material.METAL} survives as a grey map colour and as the 3.5/3.5
 * strength pair, but 1.18.2 also passed {@code .sounds(BlockSoundGroup.WOOD)},
 * so the block sounds like wood. That contradiction is preserved rather than
 * resolved: see {@link net.gmsgarcia.decor4fabric.content.BlockFamilies#WORKBENCH}.
 *
 * <p>The {@code onUse} that was held back through Phases 2 and 3 is
 * {@link #useWithoutItem}. It was not missing on purpose: opening the menu needs
 * {@link WorkBenchMenu}, and opening it before the recipe type was registered
 * would have handed the player an empty grid with no way to recover.
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

    /**
     * Opens the workbench menu.
     *
     * <p>Returns {@link InteractionResult#SUCCESS} on the client and
     * {@link InteractionResult#CONSUME} on the server. That asymmetry is
     * vanilla's: SUCCESS tells the client the swing animation should play and
     * arms its prediction, while CONSUME tells the server the interaction was
     * handled and suppresses the "nothing happened" feedback. Returning SUCCESS
     * on both sides makes the client predict an outcome the server never
     * produces.
     *
     * <p>The workbench has no block entity -- 1.18.2's
     * {@code workBenchBlock} had none either, because it stored nothing -- so
     * there is no {@code MenuProvider} to hang off. {@link Provider} exists to
     * carry the position instead, which the menu needs for its
     * {@link net.minecraft.world.inventory.ContainerLevelAccess} and therefore
     * for {@code stillValid}.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        player.openMenu(new Provider(pos));
        return InteractionResult.CONSUME;
    }

    /** Carries the block position into {@link WorkBenchMenu}'s factory. */
    private record Provider(BlockPos pos) implements MenuProvider {

        @Override
        public Component getDisplayName() {
            return Component.translatable("container.decor4fabric.workbench");
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            ContainerLevelAccess access = player.level().isClientSide()
                    ? ContainerLevelAccess.NULL
                    : ContainerLevelAccess.create(player.level(), pos);
            WorkBenchMenu menu = new WorkBenchMenu(containerId, playerInventory, access);

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
