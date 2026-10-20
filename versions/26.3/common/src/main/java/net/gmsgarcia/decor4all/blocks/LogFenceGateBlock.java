package net.gmsgarcia.decor4all.blocks;

import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;

// 1.18.2's logFenceGate: a FenceGateBlock with wood properties and no overrides, plus the WoodType
// argument 26.1 added.
public class LogFenceGateBlock extends FenceGateBlock {

    public LogFenceGateBlock(WoodType woodType, BlockBehaviour.Properties properties) {
        super(woodType, properties);
    }
}
