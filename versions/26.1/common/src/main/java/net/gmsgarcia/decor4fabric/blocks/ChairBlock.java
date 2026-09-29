package net.gmsgarcia.decor4fabric.blocks;

import com.mojang.serialization.MapCodec;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.content.BlockFamilies;
import net.gmsgarcia.decor4fabric.sit.Sit;
import net.minecraft.core.BlockPos;
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
 * 1.18.2's {@code logChair}, which covered both the armless and the
 * arm-resting variant behind a {@code boolean hasArmRests} constructor flag and
 * two ids each: {@code ..._log_chair} / {@code ..._log_chair_2}.
 *
 * <p>The flag becomes an enum because {@code MapCodec.simpleCodec} has to name a
 * single constructor, and {@code (Properties)} cannot express the variant. The
 * ids are unchanged.
 *
 * <p>This is the only seat with no hand-rolled {@code useWithoutItem} in the
 * 1.18.2 port: sitting lived entirely in the global callback, so there was
 * nothing to hang a sit branch off. Phase 3 adds one.
 */
public class ChairBlock extends WaterloggedFacingBlock {

    /** Which of 1.18.2's two shapes to use. */
    public enum Arms {
        /** {@code logChair(false)}: {@code ..._log_chair}. */
        NONE,
        /** {@code logChair(true)}: {@code ..._log_chair_2}. */
        PRESENT
    }

    public static final MapCodec<ChairBlock> CODEC =
            BlockBehaviour.simpleCodec(properties -> new ChairBlock(Arms.NONE, properties));

    public static final MapCodec<ChairBlock> ARMCHAIR_CODEC =
            BlockBehaviour.simpleCodec(properties -> new ChairBlock(Arms.PRESENT, properties));

    /* CHAIR LEGS */
    private static final VoxelShape FIRST_LEG = Block.box(3.0D, 0.0D, 3.0D, 5.0D, 7.0D, 5.0D);
    private static final VoxelShape SECOND_LEG = Block.box(11.0D, 0.0D, 3.0D, 13.0D, 7.0D, 5.0D);
    private static final VoxelShape THIRD_LEG = Block.box(3.0D, 0.0D, 11.0D, 5.0D, 7.0D, 13.0D);
    private static final VoxelShape FOURTH_LEG = Block.box(11.0D, 0.0D, 11.0D, 13.0D, 7.0D, 13.0D);

    /* CHAIR SIT */
    private static final VoxelShape CHAIR_SIT = Block.box(2.0D, 7.0D, 2.0D, 14.0D, 9.0D, 14.0D);

    /* BACKREST, PER FACING */
    private static final VoxelShape BACK_NORTH = Block.box(2.0D, 9.0D, 12.0D, 14.0D, 20.0D, 14.0D);
    private static final VoxelShape BACK_SOUTH = Block.box(2.0D, 9.0D, 2.0D, 14.0D, 20.0D, 4.0D);
    private static final VoxelShape BACK_EAST = Block.box(2.0D, 9.0D, 2.0D, 4.0D, 20.0D, 14.0D);
    private static final VoxelShape BACK_WEST = Block.box(12.0D, 9.0D, 2.0D, 14.0D, 20.0D, 14.0D);

    /* ARM SUPPORTS, PER FACING */
    private static final VoxelShape ARM_NORTH_1 = Block.box(3.0D, 9.0D, 3.0D, 4.0D, 12.0D, 5.0D);
    private static final VoxelShape ARM_NORTH_2 = Block.box(12.0D, 9.0D, 3.0D, 13.0D, 12.0D, 5.0D);
    private static final VoxelShape ARM_NORTH_3 = Block.box(2.0D, 12.0D, 3.0D, 4.0D, 13.0D, 12.0D);
    private static final VoxelShape ARM_NORTH_4 = Block.box(12.0D, 12.0D, 3.0D, 14.0D, 13.0D, 12.0D);

    private static final VoxelShape ARM_SOUTH_1 = Block.box(3.0D, 9.0D, 11.0D, 4.0D, 12.0D, 13.0D);
    private static final VoxelShape ARM_SOUTH_2 = Block.box(12.0D, 9.0D, 11.0D, 13.0D, 12.0D, 13.0D);
    private static final VoxelShape ARM_SOUTH_3 = Block.box(2.0D, 12.0D, 4.0D, 4.0D, 13.0D, 13.0D);
    private static final VoxelShape ARM_SOUTH_4 = Block.box(12.0D, 12.0D, 4.0D, 14.0D, 13.0D, 13.0D);

    private static final VoxelShape ARM_EAST_1 = Block.box(11.0D, 9.0D, 3.0D, 13.0D, 12.0D, 4.0D);
    private static final VoxelShape ARM_EAST_2 = Block.box(11.0D, 9.0D, 12.0D, 13.0D, 12.0D, 13.0D);
    private static final VoxelShape ARM_EAST_3 = Block.box(4.0D, 12.0D, 2.0D, 13.0D, 13.0D, 4.0D);
    private static final VoxelShape ARM_EAST_4 = Block.box(4.0D, 12.0D, 12.0D, 13.0D, 13.0D, 14.0D);

    private static final VoxelShape ARM_WEST_1 = Block.box(3.0D, 9.0D, 3.0D, 5.0D, 12.0D, 4.0D);
    private static final VoxelShape ARM_WEST_2 = Block.box(3.0D, 9.0D, 12.0D, 5.0D, 12.0D, 13.0D);
    private static final VoxelShape ARM_WEST_3 = Block.box(3.0D, 12.0D, 2.0D, 12.0D, 13.0D, 4.0D);
    private static final VoxelShape ARM_WEST_4 = Block.box(3.0D, 12.0D, 12.0D, 12.0D, 13.0D, 14.0D);

    private static final VoxelShape BODY = Shapes.or(
            FIRST_LEG, SECOND_LEG, THIRD_LEG, FOURTH_LEG, CHAIR_SIT);

    private static final VoxelShape PLAIN_NORTH = Shapes.or(BODY, BACK_NORTH);
    private static final VoxelShape PLAIN_SOUTH = Shapes.or(BODY, BACK_SOUTH);
    private static final VoxelShape PLAIN_EAST = Shapes.or(BODY, BACK_EAST);
    private static final VoxelShape PLAIN_WEST = Shapes.or(BODY, BACK_WEST);

    private static final VoxelShape ARMS_NORTH = Shapes.or(PLAIN_NORTH, ARM_NORTH_1, ARM_NORTH_2,
            ARM_NORTH_3, ARM_NORTH_4);
    private static final VoxelShape ARMS_SOUTH = Shapes.or(PLAIN_SOUTH, ARM_SOUTH_1, ARM_SOUTH_2,
            ARM_SOUTH_3, ARM_SOUTH_4);
    private static final VoxelShape ARMS_EAST = Shapes.or(PLAIN_EAST, ARM_EAST_1, ARM_EAST_2,
            ARM_EAST_3, ARM_EAST_4);
    private static final VoxelShape ARMS_WEST = Shapes.or(PLAIN_WEST, ARM_WEST_1, ARM_WEST_2,
            ARM_WEST_3, ARM_WEST_4);

    private final Arms arms;

    public ChairBlock(Arms arms, BlockBehaviour.Properties properties) {
        super(properties);
        this.arms = arms;
    }

    @Override
    protected MapCodec<? extends ChairBlock> codec() {
        return this.arms == Arms.PRESENT ? ARMCHAIR_CODEC : CODEC;
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

    /**
     * 1.18.2 picked the shape with a four-way if-chain over the horizontal
     * facings. The switch is a transcription, with one addition: the
     * {@code UP}/{@code DOWN} arm.
     *
     * <p>{@code FACING} is {@code BlockStateProperties.HORIZONTAL_FACING}, and
     * 26.1 builds that property from a <em>predicate</em> --
     * {@code EnumProperty.create("facing", Direction.class, Plane.HORIZONTAL::test)}
     * -- rather than from a fixed value set. javac cannot see through the
     * predicate, so it treats the property as possibly holding all six
     * {@link Direction} constants and rejects a four-arm switch with "the switch
     * expression does not cover all possible input values". {@code UP} and
     * {@code DOWN} are unreachable; the explicit arm says so instead of hiding
     * the problem behind a {@code default} that would quietly render a wrong
     * shape.
     */
    private VoxelShape outline(BlockState state) {
        boolean arms = this.arms == Arms.PRESENT;
        return switch (state.getValue(FACING)) {
            case NORTH -> arms ? ARMS_NORTH : PLAIN_NORTH;
            case SOUTH -> arms ? ARMS_SOUTH : PLAIN_SOUTH;
            case EAST -> arms ? ARMS_EAST : PLAIN_EAST;
            case WEST -> arms ? ARMS_WEST : PLAIN_WEST;
            case UP, DOWN -> throw new IllegalStateException(
                    Decor4Fabric.MOD_ID + " chair got a non-horizontal facing: " + state.getValue(FACING));
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        // 1.18.2's `+ 0.35D` for both ids, reached from Sit.sitMain by tag. The
        // arm-resting variant is the same height -- the arms go up, not the seat.
        return Sit.trySit(player, level, pos, Sit.CHAIR_HEIGHT);
    }
}
