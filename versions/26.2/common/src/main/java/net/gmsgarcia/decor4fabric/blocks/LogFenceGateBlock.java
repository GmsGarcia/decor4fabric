package net.gmsgarcia.decor4fabric.blocks;

import com.mojang.serialization.MapCodec;
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
 * <p>The codec is declared as {@code MapCodec<FenceGateBlock>} rather than
 * {@code MapCodec<? extends FenceGateBlock>} because {@code FenceGateBlock.codec()}
 * is declared with that concrete parameter type and generics are invariant, so a
 * narrower override does not compile. See {@link LogFenceBlock} for the full
 * explanation.
 *
 * <p><b>Known limitation, deliberately kept:</b> a {@code Block} is
 * deserialised from its codec and its registry id alone, so a single codec
 * cannot recover which of the eleven woods a given gate was. Vanilla has the same
 * limitation for its six gates and solves it by giving each wood its own subclass
 * with its own codec. Here the eleven gates share one subclass, so a gate
 * deserialises with the {@link WoodType} baked into whichever instance the codec
 * holds. Placement and interaction are unaffected -- the block in the world is the
 * registered instance -- but a gate that has been round-tripped through a codec
 * may carry the wrong wood's sounds. Fixing it properly means one subclass per
 * wood, which is a Phase 3 change; it is noted here rather than silently
 * papered over.
 */
public class LogFenceGateBlock extends FenceGateBlock {

    public static final MapCodec<FenceGateBlock> CODEC =
            BlockBehaviour.simpleCodec(properties -> new LogFenceGateBlock(WoodType.OAK, properties));

    public LogFenceGateBlock(WoodType woodType, BlockBehaviour.Properties properties) {
        super(woodType, properties);
    }

    @Override
    public MapCodec<FenceGateBlock> codec() {
        return CODEC;
    }
}
