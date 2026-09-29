package net.gmsgarcia.decor4fabric.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;

/**
 * Resolves the {@code FACING} of a horizontally-facing block at placement.
 *
 * <p>1.18.2's {@code ItemPlacementContext.getPlayerFacing()} derived its result
 * from the player's yaw, so it could only ever return one of the four horizontal
 * directions. 26.1 renamed that to
 * {@link BlockPlaceContext#getNearestLookingDirection()}, which sounds like a
 * pure rename but is not: it reports the direction the crosshair points at, and
 * that is {@link Direction#UP} when the player looks straight down at the block.
 * Feeding it into {@link net.minecraft.world.level.block.HorizontalDirectionalBlock#FACING},
 * which only accepts the four horizontal values, throws
 * {@code IllegalArgumentException} from {@code StateHolder.setValueInternal} and
 * crashes placement. This is reachable in normal play: placing a stool or bench
 * while looking down at the target position.
 *
 * <p>The original yaw-derived semantics are reproduced here so that the facing
 * a player gets is the one 1.18.2 gave them, rather than an unrelated value
 * chosen only to be a legal enum constant.
 */
final class PlacementFacings {

    private PlacementFacings() {
    }

    /**
     * The horizontal direction the placed block should face, always one of
     * north, south, east or west.
     *
     * <p>Prefers the player's yaw, matching 1.18.2. Falls back to the first
     * horizontal entry of {@link BlockPlaceContext#getNearestLookingDirections()}
     * and finally to {@link Direction#NORTH} so the result is a legal property
     * value even when there is no player, as in structure and command placement.
     */
    static Direction horizontal(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            return Direction.fromYRot(player.getYRot());
        }
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                return direction;
            }
        }
        return Direction.NORTH;
    }
}
