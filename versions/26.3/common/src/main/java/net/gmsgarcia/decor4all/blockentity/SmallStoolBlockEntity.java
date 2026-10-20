package net.gmsgarcia.decor4all.blockentity;

import net.gmsgarcia.decor4all.content.DecorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

// The one-slot inventory behind 1.18.2's logSmallStool_BlockEntity, which holds a single carpet --
// see SingleSlotBlockEntity for everything this inherits.
public class SmallStoolBlockEntity extends SingleSlotBlockEntity {

    public SmallStoolBlockEntity(BlockPos pos, BlockState state) {
        super(BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(DecorBlocks.LOG_SMALL_STOOL.key()), pos, state);
    }
}
