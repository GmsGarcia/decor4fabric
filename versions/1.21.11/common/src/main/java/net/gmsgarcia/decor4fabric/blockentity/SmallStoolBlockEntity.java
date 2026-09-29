package net.gmsgarcia.decor4fabric.blockentity;

import net.gmsgarcia.decor4fabric.content.DecorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The one-slot inventory behind 1.18.2's {@code logSmallStool_BlockEntity}, which
 * holds a single carpet -- see {@link SingleSlotBlockEntity} for everything this
 * inherits.
 *
 * <p>The type is looked up by key rather than held in a static field; see
 * {@link LogBenchBlockEntity} for why.
 */
public class SmallStoolBlockEntity extends SingleSlotBlockEntity {

    public SmallStoolBlockEntity(BlockPos pos, BlockState state) {
        super(BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(DecorBlocks.LOG_SMALL_STOOL.key()), pos, state);
    }
}
