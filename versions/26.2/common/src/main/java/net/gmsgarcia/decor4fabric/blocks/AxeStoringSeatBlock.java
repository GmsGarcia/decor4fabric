package net.gmsgarcia.decor4fabric.blocks;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.gmsgarcia.decor4fabric.blockentity.LogBenchBlockEntity;
import net.gmsgarcia.decor4fabric.content.BlockFamilies;
import net.gmsgarcia.decor4fabric.sit.Sit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * The behaviour 1.18.2 wrote out twice, once in {@code logBench} and once in
 * {@code logBench2}, byte for byte apart from the voxel shapes.
 *
 * <p>A stored axe is a display trick rather than a container: putting one in sets
 * {@link BlockFamilies#AXE_TYPE}, which the blockstates use to swap in the
 * matching model, and takes it out of the player's hand. Using a bench that
 * already holds an axe with an empty hand takes the axe back, rotates the bench
 * half a turn and resets {@code AXE_TYPE} to {@code 0}.
 *
 * <p>The six-tier ladder is looked up by item identity rather than by tool tier.
 * 1.18.2 wrote {@code player.isHolding(Items.NETHERITE_AXE)} and friends, and
 * 26.1 offers no equivalent to preserve: {@code Tier} was replaced by
 * {@code ToolMaterial} and {@code AxeItem} no longer exposes one. Matching the
 * six items 1.18.2 named also keeps 26.1's copper axe out of the mapping, which
 * has no {@code axe_type} value to land on.
 *
 * <p>Three 1.18.2 mechanisms are deliberately absent:
 *
 * <ul>
 *   <li>{@code Sit.sitMain()}, which registered a fresh {@code UseBlockCallback}
 *       on every right-click. Phase 3 puts the sit branch in
 *       {@link #useWithoutItem} instead, where the block already knows it is a
 *       seat.
 *   <li>{@code onBreak}, which discarded the passenger {@code SitEntity} and
 *       cleared the static occupancy map. Phase 3 keys occupancy off
 *       {@link BlockFamilies#OCCUPIED} and lets the marker clear it from its own
 *       removal path, which also covers logout, chunk unload and dimension
 *       change -- none of which {@code onBreak} ever saw.
 *   <li>{@code onStateReplaced} plus {@code ItemScatterer.spawn}, now handled by
 *       {@link BlockEntity#preRemoveSideEffects} for any block entity implementing
 *       {@code Container}.
 * </ul>
 */
public abstract class AxeStoringSeatBlock extends SeatingContainerBlock {

    /**
     * {@code axe_type} values 1..6, in 1.18.2's order. Index 0 is absent on
     * purpose: {@code 0} is the "no axe" sentinel, not a material.
     */
    private static final List<Entry> AXES = List.of(
            new Entry(Items.WOODEN_AXE, 1),
            new Entry(Items.STONE_AXE, 2),
            new Entry(Items.IRON_AXE, 3),
            new Entry(Items.GOLDEN_AXE, 4),
            new Entry(Items.DIAMOND_AXE, 5),
            new Entry(Items.NETHERITE_AXE, 6));

    private record Entry(Item item, int axeType) {
    }

    /**
     * Whether taking the axe back also flips the block's {@code FACING}.
     *
     * <p>1.18.2's two benches disagree and the difference is not a typo:
     * {@code logBench.onUse} wrote
     * {@code state.with(AXE_TYPE, 0).with(HORIZONTAL_FACING, current.getOpposite())}
     * while {@code logBench2.onUse} wrote only {@code state.with(AXE_TYPE, 0)}.
     * So storing an axe snapped the bench to the player's axis, and only the short
     * bench spun round again on retrieval. {@link LogBenchBlock} opts in;
     * {@link LogBench2Block} keeps the default.
     */
    private boolean flipsFacingOnTakeAxe;

    protected AxeStoringSeatBlock(Properties properties) {
        super(properties);
    }

    /**
     * Called from a subclass constructor. It cannot be a constructor parameter
     * because {@code BlockBehaviour.simpleCodec} needs a single
     * {@code (Properties)} constructor for {@link #codec()}.
     */
    protected final void setFlipsFacingOnTakeAxe(boolean flipsFacingOnTakeAxe) {
        this.flipsFacingOnTakeAxe = flipsFacingOnTakeAxe;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockFamilies.AXE_TYPE, BlockFamilies.OCCUPIED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(BlockFamilies.AXE_TYPE, 0)
                .setValue(BlockFamilies.OCCUPIED, false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LogBenchBlockEntity(pos, state);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        // An empty hand has no axe and no axe type, so without this it would fall
        // through to the `axeType == 0` branch below and answer SUCCESS, which
        // consumes the click. The sit branch lives in useWithoutItem, and the
        // game mode only consults that after a TRY_WITH_EMPTY_HAND from here, so
        // a plain SUCCESS would make the bench permanently unsittable. Reaching
        // the axe branches with an empty hand was impossible in 1.18.2 because
        // its single onUse branched on the hand first.
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        // 1.18.2's second branch was "hand not empty and AXE_TYPE == 0", with no
        // sneak check and no item check, and it ended in a bare SUCCESS. So
        // right-clicking a bare bench with a stick -- or with a block, which also
        // lands here -- consumed the click and did nothing. Keeping SUCCESS
        // matters: returning PASS would let the click through and place the block
        // instead, which is a different, arguably better, and certainly new
        // behaviour. Sneaking was never consulted, so it is not consulted here.
        if (state.getValue(BlockFamilies.AXE_TYPE) != 0) {
            return InteractionResult.PASS;
        }
        int axeType = axeTypeOf(stack.getItem());
        if (axeType == 0) {
            return InteractionResult.SUCCESS;
        }
        storeAxe(state, level, pos, player, hand, stack, axeType);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (state.getValue(BlockFamilies.AXE_TYPE) == 0) {
            // 1.18.2: `+ 0.17D` for both logBench and logBench2. Reached there
            // from the global callback by tag; reached here because the block is
            // the one asking.
            return Sit.trySit(player, level, pos, Sit.BENCH_HEIGHT);
        }
        takeAxeBack(state, level, pos, player);
        return InteractionResult.SUCCESS;
    }

    /**
     * 1.18.2's {@code storeAxe} plus the two lines that followed it in
     * {@code onUse}.
     *
     * <p>Two bugs are corrected rather than reproduced, because both destroy
     * items and neither is plausibly intended:
     *
     * <ul>
     *   <li>{@code onUse} did {@code blockEntity.setStack(0, hand.copy())}
     *       followed by {@code hand.setCount(0)}, so putting a stack of five axes
     *       on a bench swallowed all five and stored all five. Here one axe moves.
     *   <li>{@code storeAxe} matched on {@code player.getHorizontalFacing()},
     *       which is where the player is looking -- {@code Entity.getDirection()}
     *       in 26.1, with no {@code getOpposite()}.
     * </ul>
     *
     * <p>The sound placement is faithful: 1.18.2 played it inside {@code storeAxe},
     * so it fires only on the branches that actually changed the facing.
     */
    private void storeAxe(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, ItemStack stack, int axeType) {
        Direction current = state.getValue(FACING);
        Direction playerFacing = player.getDirection();
        // 1.18.2 snapped the bench onto the player's own axis, and onto the
        // bench's canonical facing when the two disagreed.
        boolean sameAxis = playerFacing.getAxis() == current.getAxis();
        level.setBlock(pos, state.setValue(BlockFamilies.AXE_TYPE, axeType)
                .setValue(FACING, sameAxis ? playerFacing
                        : (current.getAxis() == Direction.Axis.Z ? Direction.NORTH : Direction.EAST)),
                Block.UPDATE_ALL);
        player.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
        if (level.getBlockEntity(pos) instanceof LogBenchBlockEntity bench) {
            bench.setItem(0, stack.copyWithCount(1));
        }
        player.getItemInHand(hand).shrink(1);
    }

    /**
     * 1.18.2's third branch, which cleared {@code AXE_TYPE} and played its sound
     * unconditionally and only handed the axe back if the slot was non-empty.
     * Gating the whole branch on the slot being non-empty -- as the first draft of
     * this class did -- would leave {@code AXE_TYPE} stuck on a value the player
     * can never clear once the item entity despawns.
     */
    private void takeAxeBack(BlockState state, Level level, BlockPos pos, Player player) {
        Direction facing = state.getValue(FACING);
        level.setBlock(pos, state.setValue(BlockFamilies.AXE_TYPE, 0)
                .setValue(FACING, this.flipsFacingOnTakeAxe ? facing.getOpposite() : facing),
                Block.UPDATE_ALL);
        player.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
        if (level.getBlockEntity(pos) instanceof LogBenchBlockEntity bench) {
            ItemStack axe = bench.removeItemNoUpdate(0);
            if (!axe.isEmpty() && !player.getInventory().add(axe)) {
                player.drop(axe, false);
            }
        }
    }

    private static int axeTypeOf(Item item) {
        for (Entry entry : AXES) {
            if (entry.item() == item) {
                return entry.axeType();
            }
        }
        return 0;
    }
}
