package net.gmsgarcia.decor4fabric.blockentity;

import net.gmsgarcia.decor4fabric.content.DecorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The one-slot inventory behind 1.18.2's {@code logBench_BlockEntity}, shared
 * by both bench families.
 *
 * <p>1.18.2 named the class after the block that spawned it even though
 * {@code logBench2} used the same entity, and declared its
 * {@code impl_Inventory} marker interface purely to expose
 * {@code getItems()}. 26.1 has {@code Container} in the vanilla namespace with a
 * real contract, so the marker interface and the abstract base that came with
 * it are both gone.
 *
 * <p>The type is resolved from the registry on construction rather than cached in
 * a {@code public static BlockEntityType} field. A static field would have to be
 * assigned by the registrar, which works on Fabric but not on NeoForge: there
 * {@code Decor4Fabric.init} runs before {@code RegisterEvent}, so the type has
 * not been built yet and there is nothing to assign. The constructor is only ever
 * reached once the type <em>is</em> in the registry -- a block entity cannot exist
 * before that -- so the lookup is always populated, and it is correct on both
 * loaders.
 */
public class LogBenchBlockEntity extends SingleSlotBlockEntity {

    public LogBenchBlockEntity(BlockPos pos, BlockState state) {
        super(BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(DecorBlocks.LOG_BENCH.key()), pos, state);
    }
}
