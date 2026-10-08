package net.gmsgarcia.decor4fabric.blockentity;

import net.gmsgarcia.decor4fabric.content.DecorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * The one-slot inventory behind 1.18.2's {@code logBench_BlockEntity}, which
 * 1.18.2 shared with {@code logBench2}.
 *
 * <p>Since Phase 4 only {@code logBench} has the axe: {@code logBench2} is a
 * pure seat with no block entity, so this class is exclusive to bench 1.
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

    /**
     * NBT key for the latched axe direction. A {@link Direction}'s own string
     * name would do, but the 2D data value is what
     * {@link Direction#from2DDataValue(int)} round-trips and it is a single
     * byte rather than a string comparison on load.
     */
    private static final String KEY_AXE_FACING = "AxeFacing";

    /**
     * Which way the stored axe points, latched when it was put there.
     *
     * <p>{@code null} means "follow the block", which is the pre-existing
     * behaviour and the fallback for every case
     * {@link #axeFacingFor(BlockPos, BlockState, net.minecraft.world.entity.player.Player, net.minecraft.world.phys.BlockHitResult)}
     * declines to latch. It is also the state of a bench saved before this
     * field existed, so {@code null} has to keep working rather than throw.
     *
     * <p>This is deliberately latched rather than recomputed per frame. The
     * alternative -- reading the nearest player at submit time -- would make
     * the axe swivel as somebody walked past a bench nobody was using, and
     * would need the renderer to search for players, which is a client-side
     * guess that two clients could disagree about. A latched value is one
     * number on the server, replicated by the existing block entity update.
     */
    private @Nullable Direction axeFacing;

    public LogBenchBlockEntity(BlockPos pos, BlockState state) {
        super(type(), pos, state);
    }

    /**
     * The direction the axe should point, or {@code null} to follow the block.
     *
     * <p>Read by the renderer in preference to the blockstate's {@code FACING},
     * which is still the fallback so that a bench placed and never used, or
     * saved by an older version, renders exactly as it did before.
     */
    public @Nullable Direction axeFacing() {
        return this.axeFacing;
    }

    /**
     * Latches the direction the axe points.
     *
     * <p>Called before the slot is written, so the
     * {@link net.minecraft.world.level.block.entity.BlockEntity#getUpdatePacket()}
     * that {@link SingleSlotBlockEntity#setItem} sends already carries this
     * value and the client learns about both in one packet. Assigning after
     * would need a second sync for no benefit.
     *
     * <p>Not persisted on its own behalf when the slot is empty: an empty bench
     * draws nothing, so a stale direction is invisible and is overwritten by
     * the next {@code storeAxe} either way.
     */
    public void setAxeFacing(@Nullable Direction facing) {
        this.axeFacing = facing;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.axeFacing = input.getInt(KEY_AXE_FACING).map(Direction::from2DDataValue).orElse(null);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.axeFacing != null) {
            output.putInt(KEY_AXE_FACING, this.axeFacing.get2DDataValue());
        }
    }

    /**
     * The bench's block entity type, looked up by key and narrowed to this class.
     *
     * <p>The registry lookup returns {@code BlockEntityType<?>}, which is enough
     * for the constructor above but not for registering a renderer: both loaders
     * infer the renderer factory's type argument from the type passed in, and an
     * unbounded wildcard gives them nothing to infer from. Casting once here
     * rather than at each of the four registration sites is what keeps the
     * unchecked part to a single documented line.
     *
     * <p>The key is authoritative, so the cast is as safe as the constructor's own
     * lookup: only this class is ever built with this type.
     */
    @SuppressWarnings("unchecked")
    public static BlockEntityType<LogBenchBlockEntity> type() {
        return (BlockEntityType<LogBenchBlockEntity>) (BlockEntityType<?>)
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(DecorBlocks.LOG_BENCH.key());
    }
}
