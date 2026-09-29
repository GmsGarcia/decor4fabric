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
 *       {@code AxeStoringSeatBlock}, {@code SmallStoolBlock},
 *       {@code HighBenchBlock} and {@code ChairBlock} -- so each one calls
 *       {@link #trySit} from its own {@code useWithoutItem} and the tag lookup
 *       disappears along with the event.
 *   <li>A callback that fires before block interaction cannot tell an empty hand
 *       from a full one <em>after</em> the block has had its turn. The axe and
 *       carpet behaviours each end in a bare {@code SUCCESS} for items they do
 *       not recognise, which is what a sit needs to bypass; see the note on
 *       {@code TRY_WITH_EMPTY_HAND} in {@link #trySit}.
 * </ul>
 *
 * <p>What is kept from 1.18.2 is the rule that a seat is taken only by an empty
 * hand on a player who is not sneaking, and the four heights, which are the
 * visible part of the feature.
 */
public final class Sit {

    /**
     * {@code logBench} and {@code logBench2}: 1.18.2's {@code + 0.17D}.
     *
     * <p>The lowest of the four, which matches the geometry: both benches top out
     * at y=8, so the player's feet sit just above the plank.
     */
    public static final double BENCH_HEIGHT = 0.17D;

    /** {@code logBench3}: 1.18.2's {@code + 0.3D}. */
    public static final double HIGH_BENCH_HEIGHT = 0.30D;

    /** {@code logChair} and {@code logChair2}: 1.18.2's {@code + 0.35D}. */
    public static final double CHAIR_HEIGHT = 0.35D;

    /** {@code logSmallStool}: 1.18.2's {@code + 0.35D}, the same as a chair. */
    public static final double STOOL_HEIGHT = 0.35D;

    private Sit() {
    }

    /**
     * Sits {@code player} on the seat at {@code pos}, if they are allowed to.
     *
     * <p>Called from a block's {@code useWithoutItem}, so the caller has already
     * decided this block is a seat and has already rejected the interactions
     * that take priority over sitting -- storing an axe, laying a carpet.
     *
     * <h2>Why the empty-hand half of the rule is checked twice</h2>
     *
     * <p>{@code ServerPlayerGameMode#useItemOn} runs a block's
     * {@code useItemOn} first, and only falls through to {@code useWithoutItem}
     * when the first call returned {@link InteractionResult#TRY_WITH_EMPTY_HAND}
     * and the interacting hand is the main hand. Every seatable block here
     * therefore has to answer an empty main hand with {@code TRY_WITH_EMPTY_HAND}
     * rather than with {@code SUCCESS}, or the sit branch is unreachable and the
     * seat can never be used. That is easy to get wrong because {@code SUCCESS}
     * is what 1.18.2's own blocks returned, and returning it still looks
     * correct -- it consumes the click, so nothing falls through to the block
     * behind the bench.
     *
     * <p>So the hand check is repeated here as well. It is redundant while the
     * only caller is a main-hand-only dispatch, and it is what makes
     * {@code trySit} safe to call from anywhere: a caller holding a carpet can
     * never seat itself by accident.
     *
     * <h2>Server authority</h2>
     *
     * <p>Returns {@link InteractionResult#PASS} on the client and does the work
     * only on the server, as 1.18.2 did. The client passes, then reaches the
     * item-use path with an empty hand, which does nothing; the server's state
     * change -- the block, the marker, the ride -- replicates back and the
     * player is seated. Predicted locally instead would put a client-authored
     * {@code OCCUPIED} write into the loop for no benefit, since the seat's
     * availability is the one thing here that is not already client-known.
     *
     * @param player the player attempting to sit
     * @param level the level of the seat; the work happens only when this is a
     *     server level
     * @param pos the seat block's position
     * @param height how far above the block's base the player sits
     * @return {@link InteractionResult#SUCCESS} if the player is now seated,
     *     {@link InteractionResult#PASS} if they are not, which leaves the
     *     click to the rest of the block
     */
    public static InteractionResult trySit(Player player, Level level, BlockPos pos, double height) {
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        // 1.18.2's `sneakingAndEmpty`, spelled out rather than assumed.
        if (player.isSecondaryUseActive() || !player.getMainHandItem().isEmpty()) {
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
