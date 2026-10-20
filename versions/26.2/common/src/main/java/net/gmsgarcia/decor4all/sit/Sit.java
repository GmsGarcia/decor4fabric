package net.gmsgarcia.decor4all.sit;

import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.content.BlockFamilies;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

// Sitting down: the one entry point every seatable family shares.
public final class Sit {

    // logBench and logBench2: 1.18.2's + 0.17D, plus + 0.2D because the player sat visibly below the plank.
    public static final double BENCH_HEIGHT = 0.37D;

    // logBench3: 1.18.2's + 0.3D, plus + 0.2D.
    public static final double HIGH_BENCH_HEIGHT = 0.50D;

    // logChair and logChair2: 1.18.2's + 0.35D, plus + 0.2D.
    public static final double CHAIR_HEIGHT = 0.55D;

    // logSmallStool: the same as a chair, 1.18.2's + 0.35D plus + 0.2D.
    public static final double STOOL_HEIGHT = 0.55D;

    private Sit() {
    }

    // Sits player on the seat at pos, if they are allowed to.
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

    // The registered marker type, or null before registration has happened.
    private static EntityType<SitEntity> sitEntityType() {
        return Decor4All.sitEntityType();
    }
}
