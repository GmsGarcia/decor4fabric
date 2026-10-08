package net.gmsgarcia.decor4fabric.sit;

import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.content.BlockFamilies;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sitting down: the one entry point every seatable family shares.
 *
 * <p>1.18.2 put this in {@code Sit.sitMain}, a {@code UseBlockCallback} that ran
 * on <em>every</em> block right-click anywhere in the world, asked the block tag
 * whether the block was a seat, and branched to one of four hardcoded seat
 * heights. Three problems with that shape, all of which are structural rather
 * than cosmetic:
 *
 * <ul>
 *   <li>The event was registered once per call and {@code sitMain} was called
 *       from three different block classes, so the number of live handlers grew
 *       with the number of clicks and every click ran all of them.
 *   <li>It had to recognise seats by <em>tag</em>, because at that point no
 *       shared Java base class held the four families. They do now --
 *       {@code AxeStoringSeatBlock}, {@code LogBench2Block},
 *       {@code SmallStoolBlock}, {@code HighBenchBlock} and {@code ChairBlock} --
 *       so each one calls
 *       {@link #trySit} from its own {@code useWithoutItem} and the tag lookup
 *       disappears along with the event.
 *   <li>A callback that fires before block interaction cannot tell an empty hand
 *       from a full one <em>after</em> the block has had its turn, and the block
 *       is where the clicks that outrank a sit are actually decided: an axe
 *       going onto a bench, a carpet going onto a stool. Those branches now
 *       answer {@code TRY_WITH_EMPTY_HAND} for everything they do not claim, so
 *       the game mode hands the remainder of the click to the block's
 *       {@code useWithoutItem} and from there to this class; see
 *       the note on {@link #trySit}.
 * </ul>
 *
 * <p>What is kept from 1.18.2 is the rule that a seat is taken only by a player
 * who is not sneaking, and the four heights, which are the visible part of the
 * feature. The other half of 1.18.2's {@code sneakingAndEmpty} gate -- the empty
 * hand -- is deliberately dropped: holding an item does not stop a sit. The
 * clicks that must outrank sitting (an axe onto a bench, a carpet onto a stool)
 * are decided by the block before this class is called, not by what is in the
 * hand.
 */
public final class Sit {

    /**
     * {@code logBench} and {@code logBench2}: 1.18.2's {@code + 0.17D}, plus
     * {@code + 0.2D} because the player sat visibly below the plank.
     *
     * <p>The lowest of the four, which matches the geometry: both benches top out
     * at y=8, so the player's feet sit just above the plank.
     */
    public static final double BENCH_HEIGHT = 0.37D;

    /** {@code logBench3}: 1.18.2's {@code + 0.3D}, plus {@code + 0.2D}. */
    public static final double HIGH_BENCH_HEIGHT = 0.50D;

    /** {@code logChair} and {@code logChair2}: 1.18.2's {@code + 0.35D}, plus {@code + 0.2D}. */
    public static final double CHAIR_HEIGHT = 0.55D;

    /** {@code logSmallStool}: the same as a chair, 1.18.2's {@code + 0.35D} plus {@code + 0.2D}. */
    public static final double STOOL_HEIGHT = 0.55D;

    private Sit() {
    }

    /**
     * Sits {@code player} on the seat at {@code pos}, if they are allowed to.
     *
     * <p>Called from a block's {@code useWithoutItem}, so the caller has already
     * decided this block is a seat and has already rejected the interactions
     * that take priority over sitting -- storing an axe, laying a carpet.
     *
     * <h2>Why the block has to answer {@code TRY_WITH_EMPTY_HAND}</h2>
     *
     * <p>{@code ServerPlayerGameMode#useItemOn} runs a block's
     * {@code useItemOn} first, and only falls through to {@code useWithoutItem}
     * when the first call returned {@link InteractionResult#TRY_WITH_EMPTY_HAND}
     * and the interacting hand is the main hand. Every seatable block here
     * therefore has to route the clicks it does not claim -- everything that is
     * not an axe, not a carpet, not already handled -- to
     * {@code TRY_WITH_EMPTY_HAND} rather than consuming them, or the sit branch
     * is unreachable and the seat cannot be used while holding anything. That is
     * easy to get wrong because {@code SUCCESS} is what 1.18.2's own blocks
     * returned, and returning it still looks correct -- it consumes the click, so
     * nothing falls through to the block behind the seat.
     *
     * <p>Once there, the hand is no longer asked about. Only the sneaking half
     * of 1.18.2's gate is left, plus the permission and occupancy checks below.
     *
     * <h2>Server authority</h2>
     *
     * <p>Does the work only on the server, as 1.18.2 did, and hands the client a
     * consuming {@link InteractionResult#SUCCESS} so that nothing is predicted
     * locally. A {@code PASS} here was the right answer only while a seat could
     * be reached with an empty hand: the client's item-use path then held an
     * empty stack, which does nothing, and the server's state change -- the
     * block, the marker, the ride -- replicated back. With an item in hand that
     * same path would predict a block placement the server is about to refuse,
     * because on the server this method sits instead, and the divergence would
     * show as a phantom block until the next correction. Consuming skips the
     * item-use path and the off-hand attempt on the client, and the click
     * reaches the server either way -- the client's prediction wrapper sends
     * {@code ServerboundUseItemOnPacket} unconditionally -- so the server's
     * seat, or its {@code PASS} when the seat is taken or the player is
     * sneaking, still replicates back. Running the sit itself client-side would
     * put a client-authored {@code OCCUPIED} write into the loop for no
     * benefit, since the seat's availability is the one thing here that is not
     * already client-known.
     *
     * @param player the player attempting to sit
     * @param level the level of the seat; the work happens only when this is a
     *     server level
     * @param pos the seat block's position
     * @param height how far above the block's base the player sits
     * @return {@link InteractionResult#SUCCESS} on the server once the player is
     *     seated, and on the client for every click that reaches this method so
     *     that the client claims it without predicting; {@link InteractionResult#PASS}
     *     on the server when the player is not seated, which leaves the click to
     *     the rest of the block
     */
    public static InteractionResult trySit(Player player, Level level, BlockPos pos, double height) {
        if (level.isClientSide()) {
            // Consume, do not pass: see the server-authority note above. The
            // packet goes out regardless, and a pass would drop a held item
            // into the client's item-use path, predicting placement of whatever
            // the player is holding onto the seat the server is about to sit
            // them on.
            return InteractionResult.SUCCESS;
        }
        // The sneaking half of 1.18.2's `sneakingAndEmpty`. The other half --
        // an empty main hand -- is deliberately dropped: sitting with an item
        // in hand is the point of this branch. What still outranks a sit (an
        // axe onto a bench, a carpet onto a stool) was decided by the block
        // before useWithoutItem was ever called.
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        // 1.18.2's `canPlayerModifyAt`. Renamed in 1.20.5; the check did not
        // change, and it is what stops a claim-protected region from being sat
        // in, which would otherwise move a player without moving a block.
        if (!level.mayInteract(player, pos)) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(pos);
        // 1.18.2 asked its static map, which was keyed on the block's corner and
        // therefore wrong in the ways its javadoc lists. The block's own
        // property is the question itself, and it survives a restart.
        if (!state.hasProperty(BlockFamilies.OCCUPIED) || state.getValue(BlockFamilies.OCCUPIED)) {
            return InteractionResult.PASS;
        }
        EntityType<SitEntity> type = sitEntityType();
        if (type == null) {
            return InteractionResult.PASS;
        }
        SitEntity sit = type.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (sit == null) {
            return InteractionResult.PASS;
        }

        // Order matters. The marker is spawned and ridden before the block is
        // marked, so a failure anywhere in here leaves an unoccupied block rather
        // than an occupied one with nothing holding it. The marker's own tick
        // cannot run in between -- it is a later tick by definition -- so it
        // never sees the block free.
        sit.attach(pos, player.blockPosition());
        sit.setPos(pos.getX() + 0.5D, pos.getY() + height, pos.getZ() + 0.5D);
        if (!level.addFreshEntity(sit)) {
            return InteractionResult.PASS;
        }
        // force = true: the marker is purpose-built for exactly one passenger and
        // has no reason to refuse. ignoreCamera = true: a log bench is not a
        // camera, and without this the ride would take the player's view with it.
        if (!player.startRiding(sit, true, true)) {
            sit.discard();
            return InteractionResult.PASS;
        }
        level.setBlock(pos, state.setValue(BlockFamilies.OCCUPIED, true), Block.UPDATE_ALL);
        return InteractionResult.SUCCESS;
    }

    /**
     * The registered marker type, or null before registration has happened.
     *
     * <p>Resolved by key out of the registry rather than held in a static field,
     * for the reason the rest of the mod does it: on NeoForge the object does not
     * exist yet while the common bootstrap runs, and this method is only ever
     * called from a right-click, long after every registration pass.
     */
    private static EntityType<SitEntity> sitEntityType() {
        return Decor4Fabric.sitEntityType();
    }
}
