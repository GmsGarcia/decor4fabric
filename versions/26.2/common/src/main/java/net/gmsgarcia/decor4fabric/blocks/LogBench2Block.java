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
 * 1.18.2's {@code logBench2}: a four-legged table-height seat with a slatted
 * top.
 *
 * <p>1.18.2 shared the axe-storing behaviour of {@code logBench}, but that is
 * gone here: an axe slot is now a block entity, and a second bench holding one
 * was a copy of a feature nobody asked for twice. This bench is a plain seat
 * like {@link HighBenchBlock} -- sit on it with anything in hand (an axe sits
 * too; nothing is stored). Only {@code logBench} keeps the axe.
 *
 * <p>The shapes are transcribed one-for-one from 1.18.2, which wrote both
 * facings out as separate unions of thirteen boxes rather than rotating one:
 * {@code FACING_NS} for north and south, {@code FACING_WE} for east and west.
 */
public class LogBench2Block extends WaterloggedFacingBlock {

    public static final MapCodec<LogBench2Block> CODEC = BlockBehaviour.simpleCodec(LogBench2Block::new);

    /* FACING NORTH OR SOUTH */
    private static final VoxelShape TOP_NS = Block.box(0.0D, 4.0D, 3.0D, 16.0D, 6.0D, 13.0D);
    private static final VoxelShape MID_NS = Block.box(0.0D, 3.0D, 4.0D, 16.0D, 4.0D, 12.0D);
    private static final VoxelShape BOTTOM_NS = Block.box(0.0D, 2.0D, 5.0D, 16.0D, 3.0D, 11.0D);

    private static final VoxelShape FIRST_LEG_NS = Block.box(2.0D, 0.0D, 6.0D, 6.0D, 2.0D, 10.0D);
    private static final VoxelShape SECOND_LEG_NS = Block.box(10.0D, 0.0D, 6.0D, 14.0D, 2.0D, 10.0D);

    private static final VoxelShape AROUND_1LEG_NORTH_NS = Block.box(1.0D, 0.0D, 6.0D, 2.0D, 2.0D, 10.0D);
    private static final VoxelShape AROUND_1LEG_SOUTH_NS = Block.box(6.0D, 0.0D, 6.0D, 7.0D, 2.0D, 10.0D);
    private static final VoxelShape AROUND_1LEG_WEST_NS = Block.box(2.0D, 0.0D, 5.0D, 6.0D, 2.0D, 6.0D);
    private static final VoxelShape AROUND_1LEG_EAST_NS = Block.box(2.0D, 0.0D, 10.0D, 6.0D, 2.0D, 11.0D);

    private static final VoxelShape AROUND_2LEG_NORTH_NS = Block.box(9.0D, 0.0D, 6.0D, 10.0D, 2.0D, 10.0D);
    private static final VoxelShape AROUND_2LEG_SOUTH_NS = Block.box(14.0D, 0.0D, 6.0D, 15.0D, 2.0D, 10.0D);
    private static final VoxelShape AROUND_2LEG_WEST_NS = Block.box(10.0D, 0.0D, 5.0D, 14.0D, 2.0D, 6.0D);
    private static final VoxelShape AROUND_2LEG_EAST_NS = Block.box(10.0D, 0.0D, 10.0D, 14.0D, 2.0D, 11.0D);

    /* FACING WEST & EAST */
    private static final VoxelShape TOP_WE = Block.box(3.0D, 4.0D, 0.0D, 13.0D, 6.0D, 16.0D);
    private static final VoxelShape MID_WE = Block.box(4.0D, 3.0D, 0.0D, 12.0D, 4.0D, 16.0D);
    private static final VoxelShape BOTTOM_WE = Block.box(5.0D, 2.0D, 0.0D, 11.0D, 3.0D, 16.0D);

    private static final VoxelShape FIRST_LEG_WE = Block.box(6.0D, 0.0D, 2.0D, 10.0D, 2.0D, 6.0D);
    private static final VoxelShape SECOND_LEG_WE = Block.box(6.0D, 0.0D, 10.0D, 10.0D, 2.0D, 14.0D);

    private static final VoxelShape AROUND_1LEG_NORTH_WE = Block.box(6.0D, 0.0D, 1.0D, 10.0D, 2.0D, 2.0D);
    private static final VoxelShape AROUND_1LEG_SOUTH_WE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 2.0D, 7.0D);
    private static final VoxelShape AROUND_1LEG_WEST_WE = Block.box(5.0D, 0.0D, 2.0D, 6.0D, 2.0D, 6.0D);
    private static final VoxelShape AROUND_1LEG_EAST_WE = Block.box(10.0D, 0.0D, 2.0D, 11.0D, 2.0D, 6.0D);

    private static final VoxelShape AROUND_2LEG_NORTH_WE = Block.box(6.0D, 0.0D, 9.0D, 10.0D, 2.0D, 10.0D);
    private static final VoxelShape AROUND_2LEG_SOUTH_WE = Block.box(6.0D, 0.0D, 14.0D, 10.0D, 2.0D, 15.0D);
    private static final VoxelShape AROUND_2LEG_WEST_WE = Block.box(5.0D, 0.0D, 10.0D, 6.0D, 2.0D, 14.0D);
    private static final VoxelShape AROUND_2LEG_EAST_WE = Block.box(10.0D, 0.0D, 10.0D, 11.0D, 2.0D, 14.0D);

    private static final VoxelShape FACING_NS = Shapes.or(TOP_NS, MID_NS, BOTTOM_NS,
            FIRST_LEG_NS, SECOND_LEG_NS,
            AROUND_1LEG_NORTH_NS, AROUND_1LEG_SOUTH_NS, AROUND_1LEG_WEST_NS, AROUND_1LEG_EAST_NS,
            AROUND_2LEG_NORTH_NS, AROUND_2LEG_SOUTH_NS, AROUND_2LEG_WEST_NS, AROUND_2LEG_EAST_NS);

    private static final VoxelShape FACING_WE = Shapes.or(TOP_WE, MID_WE, BOTTOM_WE,
            FIRST_LEG_WE, SECOND_LEG_WE,
            AROUND_1LEG_NORTH_WE, AROUND_1LEG_SOUTH_WE, AROUND_1LEG_WEST_WE, AROUND_1LEG_EAST_WE,
            AROUND_2LEG_NORTH_WE, AROUND_2LEG_SOUTH_WE, AROUND_2LEG_WEST_WE, AROUND_2LEG_EAST_WE);

    public LogBench2Block(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends LogBench2Block> codec() {
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        // 1.18.2 handled this from Sit.sitMain, which asked a block tag and then
        // picked one of four heights; this is the branch that belonged here. The
        // return value is whatever actually happened, so a seat that is taken or
        // a player who is sneaking passes the click on instead of swallowing it.
        // `useItemOn` is not overridden: BlockBehaviour's default answers
        // TRY_WITH_EMPTY_HAND for any non-empty stack, so an axe in hand reaches
        // the sit instead of being stored -- nothing here stores axes any more.
        return Sit.trySit(player, level, pos, Sit.BENCH_HEIGHT);
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
}
