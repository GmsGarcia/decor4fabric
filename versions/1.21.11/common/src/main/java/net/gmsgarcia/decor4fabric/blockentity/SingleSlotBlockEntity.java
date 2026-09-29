package net.gmsgarcia.decor4fabric.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The one-slot inventory 1.18.2 duplicated across
 * {@code logBench_BlockEntity} and {@code logSmallStool_BlockEntity}. The two
 * classes were identical apart from the {@code super(...)} type argument, so they
 * collapse into one base here and two thin subclasses that only carry the type.
 *
 * <p>Three 1.18.2 mechanisms disappear with it:
 *
 * <ul>
 *   <li>{@code impl_Inventory} and its {@code getItems()} accessor, replaced by
 *       {@link Container}'s {@code getItem}/{@code setItem} contract.
 *   <li>{@code Inventories.readNbt}/{@code writeNbt}, replaced by
 *       {@link ContainerHelper}'s {@link ValueInput}/{@link ValueOutput} pair.
 *   <li>{@code onStateReplaced} + {@code ItemScatterer.spawn(world, pos, this)}
 *       in every one of the three owning block classes, replaced by
 *       {@link BlockEntity#preRemoveSideEffects}, which fires the drop for any
 *       block entity implementing {@link Container}. That is why no block here
 *       overrides a state-replacement hook.
 * </ul>
 */
public abstract class SingleSlotBlockEntity extends BlockEntity implements Container {

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    protected SingleSlotBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.items.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(this.items, slot, count);
        if (!removed.isEmpty()) {
            this.setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(this.items, slot);
        this.setChanged();
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        this.setChanged();
    }

    /**
     * Abstract on {@link Container} since 1.19.3. For a one-slot container this
     * is "empty the slot": the stack is dropped rather than deleted, which is what
     * vanilla's own single-slot containers do via
     * {@code ContainerHelper.clearContainer}.
     */
    @Override
    public void clearContent() {
        this.items.set(0, ItemStack.EMPTY);
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
}
