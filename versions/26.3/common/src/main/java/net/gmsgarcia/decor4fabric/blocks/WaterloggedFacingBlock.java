package net.gmsgarcia.decor4fabric.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

/**
 * Shared plumbing for the horizontally-facing, waterloggable families that are
 * <em>not</em> block-entity providers: the chair, the high bench and the
 * carpentry table.
 *
 * <p>1.18.2 repeated this code in three block classes -- a {@code WATERLOGGED}
 * property, {@code getPlacementState} reading
 * {@code ctx.getPlayerFacing().getOpposite()}, a {@code getStateForNeighborUpdate}
 * that scheduled a water tick, a hand-rolled {@code tryFillWithFluid} and a
 * {@code getFluidState} override. 26.1 replaces {@code Waterloggable} with
 * {@link SimpleWaterloggedBlock}, which the block-state machinery and the bucket
 * both handle, so the two hand-written halves of that dance are gone and only the
 * fluid-state lookup and the placement facing survive here.
 *
 * <p>{@code SeatingContainerBlock} is the identical class for the three families
 * that do provide a block entity. There are two bases rather than one because
 * 26.1's {@code BaseEntityBlock} and {@code HorizontalDirectionalBlock} are
 * siblings, not a chain.
 *
 * <p>26.1 also renamed 1.18.2's {@code ItemPlacementContext} to
 * {@link BlockPlaceContext}, and {@code getPlayerFacing()} to
 * {@code getNearestLookingDirection()}. That last rename is not
 * behaviour-preserving for a horizontal property, which is why the facing is
 * resolved through {@link PlacementFacings} rather than read straight off the
 * context.
 */
public abstract class WaterloggedFacingBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {

    /** Same re-export {@code LadderBlock} uses, so subclasses can write {@code FACING}. */
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<Direction> FACING =
            HorizontalDirectionalBlock.FACING;

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected WaterloggedFacingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, PlacementFacings.horizontal(context).getOpposite())
                .setValue(WATERLOGGED,
                        context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    /** True for the two facings whose 1.18.2 shapes were the "NS" pair. */
    protected static boolean northSouth(Direction facing) {
        return facing.getAxis() == Direction.Axis.Z;
    }
}
