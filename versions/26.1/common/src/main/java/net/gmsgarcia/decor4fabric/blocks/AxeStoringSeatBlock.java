package net.gmsgarcia.decor4fabric.blocks;

import com.mojang.serialization.MapCodec;
import net.gmsgarcia.decor4fabric.blockentity.LogBenchBlockEntity;
import net.gmsgarcia.decor4fabric.content.BlockFamilies;
import net.gmsgarcia.decor4fabric.sit.Sit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
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
 * <p>1.18.2 stored the axe as a display trick rather than as an inventory:
 * putting one in set {@code AXE_TYPE}, which the blockstates used to swap in the
 * matching generated model, and took it out of the player's hand. That is gone.
 * The axe now lives only in {@link LogBenchBlockEntity}'s one slot and is drawn
 * from there by a block entity renderer, so no axe is a blockstate concern, no
 * generated model encodes one, and any axe can be stored rather than the six
 * 1.18.2 named.
 *
 * <p>Membership is therefore a question about the item rather than about a
 * ladder of six tiers: a stack is an axe if it is in {@link ItemTags#AXES} or if
 * its item is an {@link AxeItem}. The tag covers any axe a mod registers there,
 * which is the contract mods actually use, and the class check covers the ones
 * that do not. 1.18.2 matched on item identity against six constants --
 * {@code player.isHolding(Items.NETHERITE_AXE)} and friends -- so a modded axe
 * was silently refused and, worse, right-clicking with one consumed the click
 * and stored nothing.
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
     * Whether taking the axe back also flips the block's {@code FACING}.
     *
     * <p>Removed. 1.18.2's two benches disagreed here and the difference was not a
     * typo: {@code logBench.onUse} wrote
     * {@code state.with(AXE_TYPE, 0).with(HORIZONTAL_FACING, current.getOpposite())}
     * while {@code logBench2.onUse} wrote only {@code state.with(AXE_TYPE, 0)}. So
     * storing an axe snapped the bench onto the player's own axis, and only the
     * short bench spun round again on retrieval.
     *
     * <p>Both halves are gone because both are wrong now. {@code FACING} picks the
     * bench's model variant out of the blockstate, so a bench that rotated when an
     * axe went in visibly changed shape as a side effect of storing one -- the log
     * swung round to a different facing while the axe appeared on it. An axe is no
     * longer a blockstate concern at all, and nothing about putting one in or
     * taking one out may rotate the bench.
     */
    protected AxeStoringSeatBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockFamilies.OCCUPIED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
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
        // An empty hand carries no axe, so it would fall through to the
        // "already stored" test below and answer PASS whenever the bench holds
        // an axe -- and PASS never reaches useWithoutItem, where taking the axe
        // back lives. TRY_WITH_EMPTY_HAND is the only value the game mode
        // forwards to useWithoutItem (and only for the main hand), so the empty
        // hand has to claim nothing here. Reaching the axe branches with an
        // empty hand was impossible in 1.18.2 because its single onUse branched
        // on the hand first.
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        // 1.18.2 read "already occupied" from AXE_TYPE; here it is read from the
        // slot, which is the same fact and cannot disagree with what is drawn.
        // A bench holding an axe refuses a held item with PASS: the axe comes
        // back on an empty hand only (see useWithoutItem below), and a player
        // holding something may not sit down until it does. So the full-hand sit
        // is reserved for a bench whose slot is free.
        LogBenchBlockEntity bench = benchAt(level, pos);
        if (bench != null && !bench.isEmpty()) {
            return InteractionResult.PASS;
        }
        // 1.18.2's second branch was "hand not empty and AXE_TYPE == 0", with no
        // sneak check and no item check, and it ended in a bare SUCCESS. So
        // right-clicking a bare bench with a stick -- or with a block, which also
        // lands here -- consumed the click and did nothing, and the bench could
        // not be sat on while holding anything because useWithoutItem, where the
        // sit branch lives, is only consulted after TRY_WITH_EMPTY_HAND. An axe
        // still never reaches trySit: it is stored right here. Sneaking was
        // never consulted, so it is not consulted here -- though a sneaking
        // player with an item never reaches this method at all: the game mode
        // skips block interaction for that click.
        if (!isAxe(stack)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        storeAxe(state, level, pos, player, hand, stack);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        LogBenchBlockEntity bench = benchAt(level, pos);
        if (bench != null && !bench.isEmpty()) {
            takeAxeBack(state, level, pos, player);
            return InteractionResult.SUCCESS;
        }
        // 1.18.2: `+ 0.17D` for both logBench and logBench2. Reached there
        // from the global callback by tag; reached here because the block is
        // the one asking.
        return Sit.trySit(player, level, pos, Sit.BENCH_HEIGHT);
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
     *
     * <p>Storing touches no blockstate at all. It used to snap the bench onto the
     * player's own axis, which rotated the log to a different model variant as a
     * side effect of putting an axe away; {@code FACING} is now changed only by
     * placement. The axe itself reaches clients through
     * {@link LogBenchBlockEntity#setItem}, which pushes a block entity update,
     * and so does the direction it latches for
     * {@link #axeFacingFor(BlockPos, BlockState, Player)}.
     */
    private void storeAxe(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, ItemStack stack) {
        player.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
        if (level.getBlockEntity(pos) instanceof LogBenchBlockEntity stored) {
            // Before the slot write, so the one update packet setItem sends
            // carries the direction as well as the axe.
            stored.setAxeFacing(axeFacingFor(pos, state, player));
            stored.setItem(0, stack.copyWithCount(1));
        }
        player.getItemInHand(hand).shrink(1);
    }

    /**
     * Which way an axe stored by {@code player} should point. It never latches
     * along the log.
     *
     * <p>The axe points back at whoever put it there, but only from the log's
     * two long sides; from an end of the log it cannot follow the player,
     * because pointing along the log is the one direction the tuned pose does
     * not survive. It latches the nearest not-end face instead so it still
     * reads toward the player: the signed position across the log says which
     * half the player is on, and the axe is set to the facing of that same
     * half, so standing even a hair to one side of the centre line flips which
     * way it reads.
     *
     * <p>Which of the two sides a given player is on is decided by the log's
     * axis, not the block's. The two are perpendicular in world space -- the
     * block's {@code FACING} runs across the log, and both bench families'
     * blockstates rotate the model to keep it that way -- so a player is on a
     * long side exactly when the direction from the bench to the player shares
     * an axis with {@code FACING}. Testing the model's geometry directly would
     * be wrong here anyway, because {@code log_bench_2} authors its log along
     * {@code z} and relies on a {@code y: 90} blockstate offset, so the same
     * block's model and its world axes disagree by a quarter turn.
     *
     * <p>Position is used, not {@code player.getDirection()}. The player is
     * normally looking at the bench while clicking it, so their facing is
     * nearly opposite the direction from bench to player, and using it would
     * point the axe away from them in the common case. The horizontal offset
     * from the block centre is what "facing the player" actually means.
     *
     * <h2>Why the result is the opposite of the player</h2>
     *
     * <p>The return is {@code towardPlayer.getOpposite()}, which looks like a
     * bug and is not. {@code yawFor} hands the renderer the same quarter turns
     * the blockstate uses for the bench model -- north 0, east 90, south 180,
     * west 270 -- and {@code ItemDisplayContext.FIXED} already applies vanilla's
     * own {@code rotation [0, 180, 0]} to the sprite before the renderer sees
     * it. The two half turns compose, so {@code yawFor(d)} draws the axe along
     * {@code -d}: an axe latched to the block's facing points out of the
     * <em>back</em> of the bench, which is how 1.18.2's axe models were baked
     * into the rotated blockstate and why the bench reads as having an axe
     * stuck in it behind the sitter's back.
     *
     * <p>So "point at the player" is expressed in that inverted frame. An earlier
     * version of this method returned {@code towardPlayer} and pointed the axe
     * directly away from whoever had just stored it; inverting here puts both
     * long sides back into the frame {@link LogBenchBlockEntity#axeFacing()}
     * and the renderer already agree on.
     */
    private static Direction axeFacingFor(BlockPos pos, BlockState state, Player player) {
        Direction facing = state.getValue(FACING);
        double dx = player.getX() - (pos.getX() + 0.5D);
        double dz = player.getZ() - (pos.getZ() + 0.5D);
        // Equal magnitudes are the diagonal, which cannot happen for a player
        // who clicked this block's own hitbox, so either side of the tie-break
        // is unreachable and the >= is only there to pick one deterministically.
        Direction towardPlayer;
        if (Math.abs(dx) >= Math.abs(dz)) {
            towardPlayer = dx > 0.0D ? Direction.EAST : Direction.WEST;
        } else {
            towardPlayer = dz > 0.0D ? Direction.SOUTH : Direction.NORTH;
        }
        if (towardPlayer.getAxis() == facing.getAxis()) {
            return towardPlayer.getOpposite();
        }
        // An end click latches the nearest not-end face, on the player's own
        // half of the log: the signed distance from the log's centre line
        // along the block's own axis -- positive on the side FACING points to
        // -- picks the half the player is on, and the axe is set to that
        // half's facing. Dead centre, exactly no side, falls through to
        // FACING.getOpposite(); the "even slightly to one side" case is what
        // decides the direction.
        double across = facing.getStepX() * dx + facing.getStepZ() * dz;
        return across < 0.0D ? facing : facing.getOpposite();
    }

    /**
     * 1.18.2's third branch, which cleared {@code AXE_TYPE} and played its sound
     * unconditionally and only handed the axe back if the slot was non-empty.
     *
     * <p>The unreachable half of that is now structurally impossible rather than
     * merely avoided. 1.18.2 gated this branch on {@code AXE_TYPE != 0} while
     * taking the item out of the slot, so a value that survived its item -- the
     * dropped axe despawnping, say -- left the bench permanently un-sittable with
     * no way to clear it. Here the same slot both gates the branch and supplies
     * the item, so the two cannot disagree.
     */
    private void takeAxeBack(BlockState state, Level level, BlockPos pos, Player player) {
        player.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
        if (level.getBlockEntity(pos) instanceof LogBenchBlockEntity bench) {
            ItemStack axe = bench.removeItemNoUpdate(0);
            if (!axe.isEmpty() && !player.getInventory().add(axe)) {
                player.drop(axe, false);
            }
        }
    }

    /**
     * Whether a stack should be accepted onto a bench.
     *
     * <p>Either test alone would be wrong. {@link ItemTags#AXES} is what a mod is
     * expected to join, but joining a tag is opt-in and nothing forces it; an
     * {@link AxeItem} subclass outside the tag would be invisible to the tag test.
     * Conversely a mod may put its axe in the tag without it extending
     * {@code AxeItem}, and the class test would miss that. Together they cover
     * both conventions, and the {@code AxeItem} fallback also keeps an axe that
     * simply forgot to register itself working.
     */
    private static boolean isAxe(ItemStack stack) {
        return stack.is(ItemTags.AXES) || stack.getItem() instanceof AxeItem;
    }

    private static @Nullable LogBenchBlockEntity benchAt(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof LogBenchBlockEntity bench ? bench : null;
    }
}