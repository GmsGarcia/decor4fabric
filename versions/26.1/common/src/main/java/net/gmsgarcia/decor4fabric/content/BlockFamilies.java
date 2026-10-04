package net.gmsgarcia.decor4fabric.content;

import net.gmsgarcia.decor4fabric.blocks.ChairBlock;
import net.gmsgarcia.decor4fabric.blocks.HighBenchBlock;
import net.gmsgarcia.decor4fabric.blocks.LogBench2Block;
import net.gmsgarcia.decor4fabric.blocks.LogBenchBlock;
import net.gmsgarcia.decor4fabric.blocks.LogFenceBlock;
import net.gmsgarcia.decor4fabric.blocks.LogFenceGateBlock;
import net.gmsgarcia.decor4fabric.blocks.LogTableBlock;
import net.gmsgarcia.decor4fabric.blocks.SmallStoolBlock;
import net.gmsgarcia.decor4fabric.blocks.WorkBenchBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

/**
 * The ten block families, and the state properties they share.
 *
 * <p>1.18.2 declared {@code AXE_TYPE} and {@code WOOL_COLOR} twice -- once on
 * {@code logBench} and again on {@code logBench2}, once on
 * {@code logSmallStool} and again on {@code logTable} -- as four independent
 * {@code IntProperty} instances with the same name and range. Minecraft compares
 * properties by name, so the duplicates were a latent bug rather than a working
 * feature: the "high bench" family stores no axe at all, so it gets no
 * property. One shared constant per property retires that coincidence.
 *
 * <p>{@code AXE_TYPE} itself is now gone entirely. It only ever existed to pick
 * one of six generated axe models, and that display is now drawn from the stored
 * {@code ItemStack} by a block entity renderer, which means the axe is no longer
 * a blockstate concern at all -- it never appears in a blockstate key, is not
 * part of any model, and can hold any axe rather than the six 1.18.2 named. What
 * the bench holds is read from {@code LogBenchBlockEntity} instead, and pushed to
 * clients through the block entity's update packet.
 *
 * <p>{@code OCCUPIED} has no 1.18.2 equivalent. It is introduced now so every
 * seatable family is ready for Phase 3's sit entity, which will use a blockstate
 * instead of the old static {@code Map<Vec3d, Boolean>}. Existing blockstates
 * still resolve because the default is {@code false}.
 */
public final class BlockFamilies {

    private BlockFamilies() {
    }

    /**
     * {@code 0} is "no carpet"; {@code 1..16} follow {@code DyeColor} order, so
     * {@code value == DyeColor.byId(value).getId() + 1}. The offset preserves
     * 1.18.2's sentinel.
     */
    public static final IntegerProperty WOOL_COLOR = IntegerProperty.create("wool_color", 0, 16);

    /** Reserved for Phase 3's sit entity. Not yet read by any logic. */
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    /** 1.18.2: {@code Material.WOOD}, hardness 2, resistance 3, wood sounds. */
    public static final BlockSpec LOG_BENCH = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            LogBenchBlock::new);

    public static final BlockSpec LOG_BENCH_2 = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            LogBench2Block::new);

    /** 1.18.2's {@code logBench3}: taller legs, no inventory, no axe slot. */
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

    /**
     * 1.18.2: {@code Material.METAL}, hardness 3.5, resistance 3.5, but still
     * wood sounds.
     *
     * <p>{@code Material} is gone and only its map colour survived, as
     * {@code MapColor.METAL}. There is no {@code MapColor.GRAY} in 26.1 -- the
     * grey constants are {@code LIGHT_GRAY} and {@code GRAY} is not among them --
     * and {@code METAL} is the direct mapping of the 1.18.2 material, so
     * substituting it keeps the map rendering the same tone it had.
     */
    public static final BlockSpec WORKBENCH = new BlockSpec(MapColor.METAL, SoundType.WOOD, 3.5F, 3.5F,
            WorkBenchBlock::new);

    /**
     * Fence gates are the one family that cannot share a single spec.
     *
     * <p>26.1 changed {@code FenceGateBlock}'s constructor from
     * {@code (Properties)} to {@code (WoodType, Properties)}, and the argument is
     * not decorative: the constructor does
     * {@code super(properties.sound(type.soundType()))} and then uses {@code type}
     * for the gate open/close sounds. 1.18.2 had no such parameter, so every
     * variant shared one wood sound set and {@code BLOCK_FENCE_GATE_OPEN} /
     * {@code _CLOSE}.
     *
     * <p>Passing the matching {@link WoodType} therefore changes audible
     * behaviour relative to 1.18.2 for the variants whose {@code BlockSetType}
     * has its own gate sounds. That is a deliberate deviation, recorded in
     * PORTING_PLAN.md: the alternative is an oak-sounding cherry gate, and the
     * new woods have no 1.18.2 behaviour to preserve in the first place.
     */
    public static BlockSpec logFenceGate(WoodType woodType) {
        return new BlockSpec(MapColor.WOOD, woodType.soundType(), 2.0F, 3.0F,
                properties -> new LogFenceGateBlock(woodType, properties));
    }
}
