package net.gmsgarcia.decor4fabric.blocks.block_entities.logBench2;

import net.gmsgarcia.decor4fabric.blocks.inventories.itemStand_Inv;
import net.gmsgarcia.decor4fabric.registry.blockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class jungle_logBench2_BlockEntity extends BlockEntity implements itemStand_Inv {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public jungle_logBench2_BlockEntity(BlockPos pos, BlockState state) {
        super(blockRegistry.JUNGLE_LOG_BENCH_2_BLOCK_ENTITY, pos, state);
    }
    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
    }
}
