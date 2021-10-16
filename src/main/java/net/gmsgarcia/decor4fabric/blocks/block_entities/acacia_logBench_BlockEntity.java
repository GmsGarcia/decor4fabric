package net.gmsgarcia.decor4fabric.blocks.block_entities;

import net.gmsgarcia.decor4fabric.blocks.inventories.itemStand_Inv;
import net.gmsgarcia.decor4fabric.registry.blockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class acacia_logBench_BlockEntity extends BlockEntity implements itemStand_Inv {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public acacia_logBench_BlockEntity(BlockPos pos, BlockState state) {
        super(blockRegistry.ACACIA_LOG_BENCH_BLOCK_ENTITY, pos, state);
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
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        return nbt;
    }
}
