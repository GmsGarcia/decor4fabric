package net.gmsgarcia.decor4fabric.blocks;

import com.mojang.serialization.MapCodec;
import net.gmsgarcia.decor4fabric.content.BlockFamilies;
import net.gmsgarcia.decor4fabric.sit.Sit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * 1.18.2's {@code logBench3}: a bar-height bench, tagged
 * {@code decor4fabric:high_benches}.
 *
 * <p>It is the one seating family with no {@code axe_type} property and no block
 * entity -- it never held anything in 1.18.2, and {@code mainDecor} excluded it
 * from the {@code log_bench} block entity's valid-block set.
 *
 * <p>{@code OCCUPIED} arrives in Phase 3, together with the sit behaviour that
 * reads it. It is declared here rather than in {@link WaterloggedFacingBlock},
 * where it would also land on the carpentry table, which is not a seat.
 */
public class HighBenchBlock extends WaterloggedFacingBlock {

    public static final MapCodec<HighBenchBlock> CODEC = BlockBehaviour.simpleCodec(HighBenchBlock::new);

    /* FACING NORTH OR SOUTH */
    private static final VoxelShape TOP_NS = Block.box(0.0D, 6.0D, 3.0D, 16.0D, 8.0D, 13.0D);
    private static final VoxelShape MID_NS = Block.box(0.0D, 5.0D, 4.0D, 16.0D, 6.0D, 12.0D);
    private static final VoxelShape BOTTOM_NS = Block.box(0.0D, 4.0D, 5.0D, 16.0D, 5.0D, 11.0D);

    private static final VoxelShape FIRST_LEG_SUPPORT_1_NS = Block.box(2.0D, 3.0D, 6.0D, 4.0D, 4.0D, 10.0D);
    private static final VoxelShape FIRST_LEG_SUPPORT_2_NS = Block.box(2.0D, 2.0D, 5.0D, 4.0D, 3.0D, 11.0D);
    private static final VoxelShape FIRST_LEG_WEST_NS = Block.box(2.0D, 0.0D, 5.0D, 4.0D, 2.0D, 7.0D);
    private static final VoxelShape FIRST_LEG_EAST_NS = Block.box(2.0D, 0.0D, 9.0D, 4.0D, 2.0D, 11.0D);

    private static final VoxelShape SECOND_LEG_SUPPORT_1_NS = Block.box(12.0D, 3.0D, 6.0D, 14.0D, 4.0D, 10.0D);
    private static final VoxelShape SECOND_LEG_SUPPORT_2_NS = Block.box(12.0D, 2.0D, 5.0D, 14.0D, 3.0D, 11.0D);
    private static final VoxelShape SECOND_LEG_WEST_NS = Block.box(12.0D, 0.0D, 5.0D, 14.0D, 2.0D, 7.0D);
    private static final VoxelShape SECOND_LEG_EAST_NS = Block.box(12.0D, 0.0D, 9.0D, 14.0D, 2.0D, 11.0D);

    /* FACING WEST & EAST */
    private static final VoxelShape TOP_WE = Block.box(3.0D, 6.0D, 0.0D, 13.0D, 8.0D, 16.0D);
    private static final VoxelShape MID_WE = Block.box(4.0D, 5.0D, 0.0D, 12.0D, 6.0D, 16.0D);
    private static final VoxelShape BOTTOM_WE = Block.box(5.0D, 4.0D, 0.0D, 11.0D, 5.0D, 16.0D);

    private static final VoxelShape FIRST_LEG_SUPPORT_1_WE = Block.box(6.0D, 3.0D, 2.0D, 10.0D, 4.0D, 4.0D);
    private static final VoxelShape FIRST_LEG_SUPPORT_2_WE = Block.box(5.0D, 2.0D, 2.0D, 11.0D, 3.0D, 4.0D);
    private static final VoxelShape FIRST_LEG_WEST_WE = Block.box(5.0D, 0.0D, 2.0D, 7.0D, 2.0D, 4.0D);
    private static final VoxelShape FIRST_LEG_EAST_WE = Block.box(9.0D, 0.0D, 2.0D, 11.0D, 2.0D, 4.0D);

    private static final VoxelShape SECOND_LEG_SUPPORT_1_WE = Block.box(6.0D, 3.0D, 12.0D, 10.0D, 4.0D, 14.0D);
    private static final VoxelShape SECOND_LEG_SUPPORT_2_WE = Block.box(5.0D, 2.0D, 12.0D, 11.0D, 3.0D, 14.0D);
    private static final VoxelShape SECOND_LEG_WEST_WE = Block.box(5.0D, 0.0D, 12.0D, 7.0D, 2.0D, 14.0D);
    private static final VoxelShape SECOND_LEG_EAST_WE = Block.box(9.0D, 0.0D, 12.0D, 11.0D, 2.0D, 14.0D);

    private static final VoxelShape FACING_NS = Shapes.or(TOP_NS, MID_NS, BOTTOM_NS,
            FIRST_LEG_SUPPORT_1_NS, FIRST_LEG_SUPPORT_2_NS, FIRST_LEG_WEST_NS, FIRST_LEG_EAST_NS,
            SECOND_LEG_SUPPORT_1_NS, SECOND_LEG_SUPPORT_2_NS, SECOND_LEG_WEST_NS, SECOND_LEG_EAST_NS);

    private static final VoxelShape FACING_WE = Shapes.or(TOP_WE, MID_WE, BOTTOM_WE,
            FIRST_LEG_SUPPORT_1_WE, FIRST_LEG_SUPPORT_2_WE, FIRST_LEG_WEST_WE, FIRST_LEG_EAST_WE,
            SECOND_LEG_SUPPORT_1_WE, SECOND_LEG_SUPPORT_2_WE, SECOND_LEG_WEST_WE, SECOND_LEG_EAST_WE);

    public HighBenchBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HighBenchBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockFamilies.OCCUPIED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(BlockFamilies.OCCUPIED, false);
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
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? FACING_NS : FACING_WE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        // 1.18.2 handled this from Sit.sitMain, which asked a block tag and then
        // picked one of four heights; this is the branch that belonged here. The
        // return value is no longer the placeholder SUCCESS -- it is whatever
        // actually happened, so a seat that is taken or a player who is sneaking
        // passes the click on instead of swallowing it.
        return Sit.trySit(player, level, pos, Sit.HIGH_BENCH_HEIGHT);
    }
}
