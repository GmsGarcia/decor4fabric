package net.gmsgarcia.decor4all.blocks;

import com.mojang.serialization.MapCodec;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.content.BlockFamilies;
import net.gmsgarcia.decor4all.sit.Sit;
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

// 1.18.2's logChair, which covered both the armless and the arm-resting variant behind a boolean
// hasArmRests constructor flag and two ids each: ..._log_chair / ..._log_chair_2.
public class ChairBlock extends WaterloggedFacingBlock {

    // Which of 1.18.2's two shapes to use.
    public enum Arms {
        // logChair(false): ..._log_chair.
        NONE,
        // logChair(true): ..._log_chair_2.
        PRESENT
    }
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

    // 1.18.2 picked the shape with a four-way if-chain over the horizontal facings. The switch is
    // a transcription, with one addition: the UP/DOWN arm.
    private VoxelShape outline(BlockState state) {
        boolean arms = this.arms == Arms.PRESENT;
        return switch (state.getValue(FACING)) {
            case NORTH -> arms ? ARMS_NORTH : PLAIN_NORTH;
            case SOUTH -> arms ? ARMS_SOUTH : PLAIN_SOUTH;
            case EAST -> arms ? ARMS_EAST : PLAIN_EAST;
            case WEST -> arms ? ARMS_WEST : PLAIN_WEST;
            case UP, DOWN -> throw new IllegalStateException(
                    Decor4All.MOD_ID + " chair got a non-horizontal facing: " + state.getValue(FACING));
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
