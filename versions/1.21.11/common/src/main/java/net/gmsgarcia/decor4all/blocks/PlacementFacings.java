package net.gmsgarcia.decor4all.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;

// Resolves the FACING of a horizontally-facing block at placement.
final class PlacementFacings {

    private PlacementFacings() {
    }

    // The horizontal direction the placed block should face, always one of north, south, east or west.
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
