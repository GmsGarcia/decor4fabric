package net.gmsgarcia.decor4all.blockentity;

import net.gmsgarcia.decor4all.content.DecorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

// The one-slot inventory behind 1.18.2's logBench_BlockEntity, which 1.18.2 shared with logBench2.
public class LogBenchBlockEntity extends SingleSlotBlockEntity {

    // NBT key for the latched axe direction. A Direction's own string name would do, but the 2D
    // data value is what Direction#from2DDataValue(int) round-trips and it is a single byte rather
    // than a string comparison on load.
    private static final String KEY_AXE_FACING = "AxeFacing";

    // Which way the stored axe points, latched when it was put there.
    private @Nullable Direction axeFacing;

    public LogBenchBlockEntity(BlockPos pos, BlockState state) {
        super(type(), pos, state);
    }

    // The direction the axe should point, or null to follow the block.
    public @Nullable Direction axeFacing() {
        return this.axeFacing;
    }

    // Latches the direction the axe points.
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

    // The bench's block entity type, looked up by key and narrowed to this class.
    @SuppressWarnings("unchecked")
    public static BlockEntityType<LogBenchBlockEntity> type() {
        return (BlockEntityType<LogBenchBlockEntity>) (BlockEntityType<?>)
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(DecorBlocks.LOG_BENCH.key());
    }
}
