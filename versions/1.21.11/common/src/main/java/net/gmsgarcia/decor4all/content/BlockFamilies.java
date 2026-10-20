package net.gmsgarcia.decor4all.content;

import net.gmsgarcia.decor4all.blocks.ChairBlock;
import net.gmsgarcia.decor4all.blocks.HighBenchBlock;
import net.gmsgarcia.decor4all.blocks.LogBench2Block;
import net.gmsgarcia.decor4all.blocks.LogBenchBlock;
import net.gmsgarcia.decor4all.blocks.LogFenceBlock;
import net.gmsgarcia.decor4all.blocks.LogFenceGateBlock;
import net.gmsgarcia.decor4all.blocks.LogTableBlock;
import net.gmsgarcia.decor4all.blocks.SmallStoolBlock;
import net.gmsgarcia.decor4all.blocks.CarpenterTableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

// The ten block families, and the state properties they share.
public final class BlockFamilies {

    private BlockFamilies() {
    }

    // 0 is "no carpet"; 1..16 follow DyeColor order, so value == DyeColor.byId(value).getId() + 1.
    // The offset preserves 1.18.2's sentinel.
    public static final IntegerProperty WOOL_COLOR = IntegerProperty.create("wool_color", 0, 16);

    // Whether a sit marker currently occupies the seat; read by Sit.trySit.
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    // 1.18.2: Material.WOOD, hardness 2, resistance 3, wood sounds.
    public static final BlockSpec LOG_BENCH = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            LogBenchBlock::new);

    public static final BlockSpec LOG_BENCH_2 = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            LogBench2Block::new);

    // 1.18.2's logBench3: taller legs, no inventory, no axe slot.
    public static final BlockSpec HIGH_BENCH = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            HighBenchBlock::new);

    public static final BlockSpec SMALL_STOOL = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            SmallStoolBlock::new);

    public static final BlockSpec CHAIR = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            properties -> new ChairBlock(ChairBlock.Arms.NONE, properties));

    public static final BlockSpec ARMCHAIR = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            properties -> new ChairBlock(ChairBlock.Arms.PRESENT, properties));

    public static final BlockSpec LOG_TABLE = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            LogTableBlock::new);

    public static final BlockSpec LOG_FENCE = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            LogFenceBlock::new);

    // 1.18.2: Material.METAL, hardness 3.5, resistance 3.5, but still wood sounds.
    public static final BlockSpec CARPENTER_TABLE = new BlockSpec(MapColor.METAL, SoundType.WOOD, 3.5F, 3.5F,
            CarpenterTableBlock::new);

    // Fence gates are the one family that cannot share a single spec.
    public static BlockSpec logFenceGate(WoodType woodType) {
        return new BlockSpec(MapColor.WOOD, woodType.soundType(), 2.0F, 3.0F,
                properties -> new LogFenceGateBlock(woodType, properties));
    }
}
