package net.gmsgarcia.decor4all.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

// 1.18.2's logFence: a FenceBlock with wood properties and no overrides at all.
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
