package net.gmsgarcia.decor4fabric.blocks;

import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * 1.18.2's {@code logFenceGate}: a {@code FenceGateBlock} with wood properties
 * and no overrides, plus the {@link WoodType} argument 26.1 added.
 *
 * <p>26.1 changed the superclass constructor to {@code (WoodType, Properties)}
 * and the argument is load-bearing -- it does
 * {@code super(properties.sound(type.soundType()))} and then supplies the gate
 * open/close sounds. {@link net.gmsgarcia.decor4fabric.content.BlockFamilies#logFenceGate}
 * builds one of these per wood.
 *
 * <p><b>No {@code codec()} override, on purpose.</b> This class used to declare
 * {@code simpleCodec(properties -> new LogFenceGateBlock(WoodType.OAK, properties))}
 * on the reasoning that one shared codec cannot know which of the eleven woods a
 * given gate was, so the fix was a subclass per wood. That reasoning was wrong,
 * and the {@code WoodType.OAK} it hardcoded was not merely lossy: it was the
 * wrong wood for ten of the eleven gates.
 *
 * <p>Vanilla already solved this in this very superclass. {@code FenceGateBlock}
 * carries a {@code wood_type} field in its own codec
 * ({@code WoodType.CODEC.fieldOf("wood_type")}), so a gate round-trips through
 * serialisation with the sounds it was built with. Vanilla therefore needs no
 * per-wood subclass, and neither do we: inheriting {@code FenceGateBlock.codec()}
 * writes this block's wood and reads back a gate that carries it.
 *
 * <p>Overriding it to build a {@code LogFenceGateBlock} specifically would also
 * mean holding a second copy of the wood type, since the superclass's is private
 * with no accessor -- two sources of truth for one fact, which is how the bench
 * and bench_2 models came to disagree about which way a model faced. Vanilla
 * deserialises a plain {@code FenceGateBlock} for its own six gates, and blocks
 * are resolved by registry id rather than by codec type, so that is the
 * behaviour to copy.
 */
public class LogFenceGateBlock extends FenceGateBlock {

    public LogFenceGateBlock(WoodType woodType, BlockBehaviour.Properties properties) {
        super(woodType, properties);
    }
}
