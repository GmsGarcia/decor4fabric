package net.gmsgarcia.decor4fabric.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 1.18.2's {@code logFence}: a {@code FenceBlock} with wood properties and no
 * overrides at all.
 *
 * <p>That is the whole class. Its connection behaviour, the {@code minecraft:fences}
 * and {@code minecraft:wooden_fences} tag membership in the data files, and the
 * waterlogging come from {@code FenceBlock} itself.
 *
 * <p>Two API details that are easy to get wrong:
 *
 * <ul>
 *   <li>The import is {@code ...block.state.BlockBehaviour}, not
 *       {@code ...block.BlockBehaviour}.
 *   <li>{@code FenceBlock.codec()} is declared as {@code MapCodec<FenceBlock>} --
 *       a concrete parameter type, not {@code MapCodec<? extends Block>}. A
 *       subclass therefore cannot narrow the return to
 *       {@code MapCodec<LogFenceBlock>}, because generics are invariant and that
 *       is not a subtype of {@code MapCodec<FenceBlock>}; the compiler rejects it
 *       with "cannot override codec() in FenceBlock". The declared type has to
 *       match the superclass exactly. Type inference then does the rest:
 *       {@code simpleCodec} infers its {@code B} from the field's target type, and
 *       {@code LogFenceBlock::new} is accepted as a
 *       {@code Function<Properties, FenceBlock>} because {@code Function}'s
 *       return type is covariant.
 * </ul>
 */
public class LogFenceBlock extends FenceBlock {

    public static final MapCodec<FenceBlock> CODEC = BlockBehaviour.simpleCodec(LogFenceBlock::new);

    public LogFenceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FenceBlock> codec() {
        return CODEC;
    }
}
