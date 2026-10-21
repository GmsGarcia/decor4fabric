package net.gmsgarcia.decor4all.menu;

import java.util.ArrayList;
import java.util.List;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.content.DecorBlocks;
import net.gmsgarcia.decor4all.net.CarpenterTableRecipesPayload;
import net.gmsgarcia.decor4all.recipe.CarpenterTableRecipe;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

// The carpentry table's menu: one input slot on the left, one result on the right, and a
// server-side list of everything the current input could become.
public class CarpenterTableMenu extends AbstractContainerMenu {

    // The single ingredient slot. 1.18.2's first slot; still index 0.
    public static final int INPUT_SLOT = 0;

    // The crafted-output slot. 1.18.2's second slot; still index 1.
    public static final int RESULT_SLOT = 1;

    // First player-inventory slot, i.e. one past #RESULT_SLOT.
    private static final int PLAYER_SLOT_START = 2;

    // One past the last player-inventory slot.
    private static final int PLAYER_SLOT_END = 38;

    // Where the player's first inventory row starts, for the shift-click swap.
    private static final int PLAYER_MAIN_END = PLAYER_SLOT_START + 27;

    // Whether the take-result sound has already been played this game tick.
    private long lastTakeTime = Long.MIN_VALUE;

    private final ContainerLevelAccess access;

    private final SimpleContainer input;

    private final ResultContainer output;

    final Slot inputSlot;

    final Slot resultSlot;

    // The server's filtered list. Empty on the client, and never populated there.
    private List<CarpenterTableRecipe> visibleRecipes = List.of();

    // Selection, mirrored to the client through #selectedRecipeSlot.
    private int selectedIndex = -1;

    // The selection, as a data slot so the client can highlight it.
    private final DataSlot selectedRecipeSlot = new DataSlot() {
        @Override
        public int get() {
            return selectedIndex;
        }

        @Override
        public void set(int value) {
            selectedIndex = value;
        }
    };

    // The input this menu last acted on, by item.
    private ItemStack lastInputItem = ItemStack.EMPTY;

    // The player this menu belongs to, or null on the client.
    private ServerPlayer viewer;

    public CarpenterTableMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    // The server-side constructor, reached through the MenuType factory.
    public CarpenterTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(Decor4All.carpenterTableMenuType(), containerId);
        this.access = access;

        this.input = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                CarpenterTableMenu.this.slotsChanged(this);
            }
        };

        this.output = new ResultContainer();

        this.inputSlot = addSlot(new Slot(this.input, 0, 20, 33));
        this.resultSlot = addSlot(new ResultSlot(this.output, 143, 33));

        // 1.18.2's player inventory: three rows at y=84, hotbar at y=142.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        addDataSlot(selectedRecipeSlot);
    }

    // Called by the carpentry table block entity; see #viewer.
    public void setViewer(ServerPlayer player) {
        this.viewer = player;
    }

    // The current selection, which on both sides is the same number.
    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Decor4All.block(DecorBlocks.CARPENTER_TABLE_ENTRY));
    }

    // Selects a recipe by index.
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (isValidSelection(id)) {
            selectedIndex = id;
            populateResult();
        }
        return true;
    }

    // Rebuilds the recipe list when the input's item changes.
    @Override
    public void slotsChanged(Container container) {
        if (container != this.input) {
            return;
        }
        ItemStack stack = this.inputSlot.getItem();
        if (stack.getItem() == lastInputItem.getItem()) {
            return;
        }
        lastInputItem = stack.copy();
        refreshRecipes();
        syncRecipeList();
    }

    // Replaces the list with every recipe whose ingredient takes the input.
    private void refreshRecipes() {
        visibleRecipes = List.of();
        selectedIndex = -1;
        this.resultSlot.set(ItemStack.EMPTY);

        Level level = level();
        if (level == null || !(level.recipeAccess() instanceof RecipeManager manager)) {
            return;
        }

        ItemStack stack = this.inputSlot.getItem();
        if (stack.isEmpty()) {
            return;
        }

        SingleRecipeInput recipeInput = new SingleRecipeInput(stack);
        List<CarpenterTableRecipe> matches = new ArrayList<>();
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value() instanceof CarpenterTableRecipe recipe && recipe.matches(recipeInput, level)) {
                matches.add(recipe);
            }
        }
        visibleRecipes = List.copyOf(matches);
    }

    // Puts the selected recipe's output in the result slot.
    private void populateResult() {
        Level level = level();
        if (isValidSelection(selectedIndex) && level != null) {
            CarpenterTableRecipe recipe = visibleRecipes.get(selectedIndex);
            this.resultSlot.set(recipe.craft(this.inputSlot.getItem(), level.registryAccess()));
        } else {
            this.resultSlot.set(ItemStack.EMPTY);
        }
        broadcastFullState();
    }

    private boolean isValidSelection(int index) {
        return index >= 0 && index < visibleRecipes.size();
    }

    // The level the carpentry table stands in, or null on the client.
    private Level level() {
        return access.evaluate((level, pos) -> level, (Level) null);
    }

    // Pushes the current list to the viewer.
    private void syncRecipeList() {
        if (viewer == null || !viewer.connection.isAcceptingMessages()) {
            return;
        }
        viewer.connection.send(new ClientboundCustomPayloadPacket(
                new CarpenterTableRecipesPayload(containerId, resultsForClient())));
    }

    // The finished stacks the client draws.
    private List<ItemStack> resultsForClient() {
        Level level = level();
        if (level == null) {
            return List.of();
        }
        ItemStack input = this.inputSlot.getItem();
        List<ItemStack> stacks = new ArrayList<>(visibleRecipes.size());
        for (CarpenterTableRecipe recipe : visibleRecipes) {
            stacks.add(recipe.craft(input, level.registryAccess()));
        }
        return stacks;
    }

    // 1.18.2 refused pick-all into the result slot; so does this.
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != output;
    }

    // Shift-click transfer, following 1.18.2's rules unchanged.
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index == RESULT_SLOT) {
            stack.onCraftedBy(player, stack.getCount());
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(original, stack);
        } else if (index == INPUT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (level() == null) {
            return ItemStack.EMPTY;
        } else if (isIngredient(stack)) {
            if (!moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_SLOT_START && index < PLAYER_MAIN_END) {
            if (!moveItemStackTo(stack, PLAYER_MAIN_END, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_MAIN_END && index < PLAYER_SLOT_END) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_MAIN_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        }
        slot.setChanged();
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        broadcastFullState();
        return original;
    }

    // Whether a stack is something the carpentry table would accept.
    private boolean isIngredient(ItemStack stack) {
        Level level = level();
        if (level == null || stack.isEmpty() || !(level.recipeAccess() instanceof RecipeManager manager)) {
            return false;
        }
        SingleRecipeInput recipeInput = new SingleRecipeInput(stack);
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value() instanceof CarpenterTableRecipe recipe && recipe.matches(recipeInput, level)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.output.clearContent();
        this.viewer = null;
        access.execute((level, pos) -> clearContainer(player, this.input));
    }

    // The result slot.
    private class ResultSlot extends Slot {

        ResultSlot(ResultContainer container, int x, int y) {
            super(container, 1, x, y);
        }

        @Override
        public boolean isFake() {
            return true;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack remove(int amount) {
            return getItem().copyWithCount(amount);
        }

        @Override
        protected void checkTakeAchievements(ItemStack stack) {
            // No recipe statistics: the carpentry table never records which recipe was
            // used, because 1.18.2's ResultContainer was a bare CraftingResultInventory
            // and nothing in the mod read its used slot back.
        }

        @Override
        public void onTake(Player player, ItemStack taken) {
            ItemStack input = inputSlot.remove(1);
            if (!input.isEmpty()) {
                populateResult();
            }

            taken.onCraftedBy(player, taken.getCount());

            access.execute((level, pos) -> {
                long time = level.getGameTime();
                if (lastTakeTime != time) {
                    level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                    lastTakeTime = time;
                }
            });
        }
    }
}