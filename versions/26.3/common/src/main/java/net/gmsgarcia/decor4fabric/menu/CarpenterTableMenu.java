package net.gmsgarcia.decor4fabric.menu;

import java.util.ArrayList;
import java.util.List;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.content.DecorBlocks;
import net.gmsgarcia.decor4fabric.net.CarpenterTableRecipesPayload;
import net.gmsgarcia.decor4fabric.recipe.CarpenterTableRecipe;
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

/**
 * The carpentry table's menu: one input slot on the left, one result on the right, and
 * a server-side list of everything the current input could become.
 *
 * <p>The slot layout, positions and every rule below are 1.18.2's
 * {@code carpenterTableScreenHandler}, unchanged. What changed is how the recipe list
 * is found and how it reaches the client, and both are consequences of modern
 * Minecraft rather than choices:
 *
 * <ul>
 *   <li>1.18.2 asked {@code world.getRecipeManager().getAllMatches(type, input, world)},
 *       which filtered by type and by ingredient in one step. That method is
 *       gone. {@link #refreshRecipes()} enumerates {@link RecipeManager#getRecipes()}
 *       and filters, which is the shape its replacement forces.
 *   <li>1.18.2 let the screen call {@code getAvailableRecipes()} and draw the list
 *       itself. The client has no access to a custom recipe type at all now, so
 *       the list goes out over {@link CarpenterTableRecipesPayload} and the screen
 *       reads it from a client-side cache.
 * </ul>
 *
 * <p><b>The menu never trusts the client.</b> The client may ask to select any
 * index, and {@link #selectedRecipeSlot} is writable by anything that can open a
 * packet, so both the selection handler and {@link #populateResult()} bounds-check
 * against the server's own list rather than trusting either.
 */
public class CarpenterTableMenu extends AbstractContainerMenu {

    /** The single ingredient slot. 1.18.2's first slot; still index 0. */
    public static final int INPUT_SLOT = 0;

    /** The crafted-output slot. 1.18.2's second slot; still index 1. */
    public static final int RESULT_SLOT = 1;

    /** First player-inventory slot, i.e. one past {@link #RESULT_SLOT}. */
    private static final int PLAYER_SLOT_START = 2;

    /** One past the last player-inventory slot. */
    private static final int PLAYER_SLOT_END = 38;

    /** Where the player's first inventory row starts, for the shift-click swap. */
    private static final int PLAYER_MAIN_END = PLAYER_SLOT_START + 27;

    /**
     * Whether the take-result sound has already been played this game tick.
     *
     * <p>1.18.2's {@code lastTakeTime} guard, kept for the reason it was written:
     * the sound fires from {@link ResultSlot#onTake}, and a player shift-clicking
     * the result repeatedly would otherwise stack one sound per click.
     */
    private long lastTakeTime = Long.MIN_VALUE;

    private final ContainerLevelAccess access;

    private final SimpleContainer input;

    private final ResultContainer output;

    final Slot inputSlot;

    final Slot resultSlot;

    /**
     * The server's filtered list. Empty on the client, and never populated there.
     *
     * <p>Not synced as recipes; see the class comment. The client holds only the
     * rendered {@link ItemStack}s.
     */
    private List<CarpenterTableRecipe> visibleRecipes = List.of();

    /** Selection, mirrored to the client through {@link #selectedRecipeSlot}. */
    private int selectedIndex = -1;

    /**
     * The selection, as a data slot so the client can highlight it.
     *
     * <p>1.18.2 used a {@code Property}, which was a two-argument factory taking
     * an int supplier and consumer and doing exactly this. Modern
     * {@code AbstractContainerMenu} kept only the older {@code DataSlot} shape,
     * so the anonymous subclass is the direct translation.
     */
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

    /**
     * The input this menu last acted on, by item.
     *
     * <p>1.18.2 compared item identity rather than stack contents, so that
     * splitting a stack, changing its count or picking it up did not rebuild the
     * list. Kept for the same reason, and it is what keeps
     * {@link CarpenterTableRecipesPayload} off the wire every tick: a list can only
     * change when the input's item does.
     *
     * <p>Starts empty rather than null, and {@link ItemStack#EMPTY}'s item is
     * {@code Items.AIR} -- the same thing an empty slot reports -- so the first
     * call to {@link #slotsChanged} short-circuits instead of sending an empty
     * payload the client does not need.
     */
    private ItemStack lastInputItem = ItemStack.EMPTY;

    /**
     * The player this menu belongs to, or null on the client.
     *
     * <p>{@link AbstractContainerMenu} does not retain its player, so something
     * has to. The carpentry table block entity sets it when it opens the menu and
     * {@link #removed(Player)} clears it: a menu outliving its player while
     * holding a strong reference would pin a whole player object for as long as
     * the menu stayed reachable.
     */
    private ServerPlayer viewer;

    public CarpenterTableMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    /**
     * The server-side constructor, reached through the {@code MenuType} factory.
     *
     * <p>The input container is an anonymous subclass purely to get 1.18.2's
     * notification: its {@code setChanged} override is what turns a click into a
     * {@link #slotsChanged} call. Without it a stack moving between the slot and
     * the cursor never marks the container dirty, because {@code Slot#set} on its
     * own does not.
     */
    public CarpenterTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(Decor4Fabric.carpenterTableMenuType(), containerId);
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

    /** Called by the carpentry table block entity; see {@link #viewer}. */
    public void setViewer(ServerPlayer player) {
        this.viewer = player;
    }

    /**
     * The server's filtered recipe list, for the tests and the debug tooling.
     *
     * <p>Server-side only, and deliberately so: this is the authoritative list
     * {@link #clickMenuButton} bounds-checks against, and the client cannot read
     * it because it is empty there.
     */
    public List<CarpenterTableRecipe> getVisibleRecipes() {
        return visibleRecipes;
    }

    /** The current selection, which on both sides is the same number. */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /** 1.18.2's {@code canCraft}: an input is present and some recipe takes it. */
    public boolean canCraft() {
        return this.inputSlot.hasItem() && !visibleRecipes.isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Decor4Fabric.block(DecorBlocks.CARPENTER_TABLE_ENTRY));
    }

    /**
     * Selects a recipe by index.
     *
     * <p>Returns 1.18.2's unconditional {@code true}: the handler consumed the
     * click either way. That is deliberate, and it is why the bounds check below
     * has no observable effect on the return value -- a client that could tell
     * valid indices from invalid ones would be able to probe the list length one
     * click at a time.
     */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (isValidSelection(id)) {
            selectedIndex = id;
            populateResult();
        }
        return true;
    }

    /**
     * Rebuilds the recipe list when the input's <em>item</em> changes.
     *
     * <p>Filtered to {@link #input} because the player inventory fires this too;
     * reacting to those would refresh the list on every unrelated pickup.
     */
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

    /**
     * Replaces the list with every recipe whose ingredient takes the input.
     *
     * <p>This is 1.18.2's {@code getAllMatches} call, rebuilt. The old method
     * applied the type filter and the ingredient test together against an
     * indexed structure; the replacement offers neither, so both happen here.
     * The cost is that every recipe in the pack is examined on each input change.
     * That is the price of the modern API, acceptable for a menu that changes
     * only when the player changes what is in the slot, and the reason
     * {@link #slotsChanged} gates on item identity rather than reacting to the
     * container being marked dirty.
     *
     * <p>The selection resets to -1 rather than being preserved. 1.18.2 did the
     * same, and keeping it would be actively wrong: index 3 of the old list bears
     * no relation to index 3 of the new one, so a retained selection would quietly
     * craft something other than what the player last clicked.
     */
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

    /**
     * Puts the selected recipe's output in the result slot.
     *
     * <p>An out-of-range selection yields an empty result rather than an
     * exception, because {@link #selectedRecipeSlot} is client-writable and a
     * crafted packet can set it to any integer.
     */
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

    /** The level the carpentry table stands in, or null on the client. */
    private Level level() {
        return access.evaluate((level, pos) -> level, (Level) null);
    }

    /**
     * Pushes the current list to the viewer.
     *
     * <p>Sent on every input change and nowhere else. The carpentry table stores no
     * input, so a menu always opens over an empty slot and there is no
     * open-time case to cover: the player has to put something in before there is
     * anything to send.
     */
    private void syncRecipeList() {
        if (viewer == null || !viewer.connection.isAcceptingMessages()) {
            return;
        }
        viewer.connection.send(new ClientboundCustomPayloadPacket(
                new CarpenterTableRecipesPayload(containerId, resultsForClient())));
    }

    /**
     * The finished stacks the client draws.
     *
     * <p>Assembled server-side and truncated by
     * {@link CarpenterTableRecipesPayload}'s constructor, so the bound is applied to
     * what goes on the wire rather than to what the server happens to hold.
     */
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

    /** 1.18.2 refused pick-all into the result slot; so does this. */
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != output;
    }

    /**
     * Shift-click transfer, following 1.18.2's rules unchanged.
     *
     * <p>The interesting branch pulls an ingredient out of the player's inventory
     * and drops it in the input slot, testing membership with
     * {@link #isIngredient} -- which needs a {@link Level} the client does not
     * have. So the client's copy must not guess: {@code doClick} runs this
     * {@code QUICK_MOVE} locally too, and a wrong guess is a visible move into
     * the player inventory that the server's authoritative result then corrects
     * into the input slot (the shift-click flicker). Player-inventory indices
     * are therefore refused on the client; the deterministic RESULT and INPUT
     * branches are safe to predict either way.
     */
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

    /**
     * Whether a stack is something the carpentry table would accept.
     *
     * <p>1.18.2 asked the recipe manager for a first match against a synthetic
     * one-slot inventory. No such call exists now, so this asks the question the
     * recipe list is itself built from: does any carpentry table recipe take it?
     * Deliberately wider than {@link #canCraft} -- it has to answer while the
     * input slot is empty, which is exactly when pulling an item out of the
     * player's inventory into that slot is the point.
     */
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

    /**
     * The result slot.
     *
     * <p>1.18.2 wrote this as an anonymous subclass; it is a named nested class
     * only because it has to reach two private members of the enclosing menu,
     * {@link #populateResult()} and {@link #lastTakeTime}.
     *
     * <p>{@link #isFake()} is the one addition 1.18.2 did not need. Modern
     * {@code AbstractContainerMenu} asks it before treating a slot's contents as
     * a real container item, and every vanilla result slot answers true; a result
     * slot that claimed otherwise would let the vanilla click path try to pull a
     * stack out of the {@link ResultContainer} directly, bypassing the
     * {@link #onTake} below and so never consuming an input.
     */
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