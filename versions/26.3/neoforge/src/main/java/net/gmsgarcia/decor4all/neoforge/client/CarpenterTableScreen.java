package net.gmsgarcia.decor4all.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import java.util.List;
import net.gmsgarcia.decor4all.menu.CarpenterTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

// The carpentry table screen: 1.18.2's layout, drawn with modern render primitives.
public class CarpenterTableScreen extends AbstractContainerScreen<CarpenterTableMenu> {

    // The stonecutter's own textures, by vanilla id.
    private static final Identifier BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/container/stonecutter.png");

    private static final Identifier SCROLLER =
            Identifier.withDefaultNamespace("container/stonecutter/scroller");

    private static final Identifier SCROLLER_DISABLED =
            Identifier.withDefaultNamespace("container/stonecutter/scroller_disabled");

    private static final Identifier RECIPE =
            Identifier.withDefaultNamespace("container/stonecutter/recipe");

    private static final Identifier RECIPE_HIGHLIGHTED =
            Identifier.withDefaultNamespace("container/stonecutter/recipe_highlighted");

    private static final Identifier RECIPE_SELECTED =
            Identifier.withDefaultNamespace("container/stonecutter/recipe_selected");

    // 4 columns, 3 rows: 1.18.2's NUM_COLUMNS/NUM_ROWS.
    private static final int COLUMNS = 4;
    private static final int ROWS = 3;

    // Results per page, i.e. one full grid.
    private static final int PER_PAGE = COLUMNS * ROWS;

    private static final int CELL_WIDTH = 16;
    private static final int CELL_HEIGHT = 18;

    // A cell is 18 tall but an item is 16, so the frame sprite and the item inside it are inset by
    // different amounts -- vanilla draws the frame at y + 1 and the item at y + 2. The grid
    // therefore starts one pixel lower than #GRID_Y, which is also where hit-testing begins.
    // Drawing both at y is what put the list a pixel high.
    private static final int CELL_FRAME_INSET = 1;
    private static final int CELL_ITEM_INSET = 2;

    // Top-left of the grid, relative to #leftPos/#topPos.
    private static final int GRID_X = 52;
    private static final int GRID_Y = 14;

    private static final int SCROLLBAR_X = 119;
    private static final int SCROLLBAR_TOP = 15;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;

    // The scrollbar's track height, not its thumb.
    private static final int SCROLLBAR_TRACK = 54;

    // The track's top, for hit-testing only. Vanilla's scrollbar grabs from topPos + 9 while it
    // draws the thumb at #SCROLLBAR_TOP and drags against #SCROLLBAR_DRAG_TOP. Three different
    // numbers, all read off javap; they are not meant to agree, so do not collapse them into one.
    private static final int SCROLLBAR_HIT_TOP = 9;

    // The track's top for drag arithmetic: topPos + 14.
    private static final int SCROLLBAR_DRAG_TOP = 14;

    // How far the thumb travels, as vanilla's literal 41.0F in extractBackground. Not
    // SCROLLBAR_TRACK - SCROLLBAR_HEIGHT, which is 39: the thumb overhangs the track by a pixel at
    // each end.
    private static final int SCROLLBAR_TRAVEL = 41;

    // Scroll position, normalised to 0..1.
    private float scroll;

    // Whether the scrollbar thumb is being dragged.
    private boolean dragging;

    // Length of the list last rendered, to spot the input item changing.
    private int lastResultCount = -1;

    public CarpenterTableScreen(CarpenterTableMenu menu, Inventory playerInventory, Component title) {
        // The stonecutter's own size. Passed explicitly rather than inherited from
        // the 3-argument constructor so the grid offsets below are readable against
        // something: these numbers are meaningless unless imageWidth is 176.
        super(menu, playerInventory, title, 176, 166);
        // The base class draws the title at (titleLabelX, titleLabelY) = (8, 6)
        // against the panel's top-edge origin; this rises it a touch.
        this.titleLabelY = 5;
    }

    // The background, the grid and the scrollbar.
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        // Before the thumb is drawn: a list that changed this frame must not
        // spend that frame drawing the scroll position it used to have.
        syncScrollToList();

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, scrollbarSprite(),
                scrollbarX(), scrollbarY(), SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);

        // Vanilla's cursor rect starts at the thumb's own origin (+15) rather
        // than at the click hit test's +9, and is 54 tall. It also reports
        // not-allowed over a bar that cannot move, which is the only thing that
        // tells the player not to try: the disabled sprite alone does not.
        if (isHovering(SCROLLBAR_X, SCROLLBAR_TOP, SCROLLBAR_WIDTH, SCROLLBAR_TRACK, mouseX, mouseY)) {
            graphics.requestCursor(isScrollBarActive()
                    ? (this.dragging ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND)
                    : CursorTypes.NOT_ALLOWED);
        }

        List<ItemStack> results = results();
        int first = firstVisible();
        int originX = leftPos + GRID_X;
        int originY = topPos + GRID_Y;
        int selected = this.menu.getSelectedIndex();

        for (int cell = 0; cell < PER_PAGE; cell++) {
            int index = first + cell;
            if (index >= results.size()) {
                break;
            }
            int x = originX + cell % COLUMNS * CELL_WIDTH;
            int y = originY + cell / COLUMNS * CELL_HEIGHT;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, cellSprite(index, selected, cellAt(mouseX, mouseY)),
                    x, y + CELL_FRAME_INSET, CELL_WIDTH, CELL_HEIGHT);
            graphics.item(results.get(index), x, y + CELL_ITEM_INSET);
        }
    }

    // Tooltips, for the grid and for everything the base class covers.
    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        int cell = cellAt(mouseX, mouseY);
        List<ItemStack> results = results();
        int index = firstVisible() + cell;
        if (cell >= 0 && index < results.size()) {
            graphics.setTooltipForNextFrame(this.font, results.get(index), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {

        // Only the left button selects. The base class would have thrown a right-click
        // away anyway, and swallowing it here would eat the vanilla "close on outside
        // click" gesture for a cell that was never really clicked.
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(event, doubleClick);
        }

        if (isOverScrollbar(event.x(), event.y())) {
            // Vanilla only raises its scrolling flag here and falls through to
            // super: the thumb does not move on the grab, and mouseDragged below
            // gates on isScrollBarActive, so a bar with nothing to scroll cannot
            // be moved at all. Snapping the thumb to the cursor here -- with an
            // early `return true` that also hid the click from the base class --
            // parked a dead thumb wherever the player clicked, on a list that
            // could never scroll it back.
            this.dragging = true;
        }

        int cell = cellAt(event.x(), event.y());
        List<ItemStack> results = results();
        int index = firstVisible() + cell;
        if (cell >= 0 && index < results.size()) {
            this.minecraft.getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, index);
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.dragging && isScrollBarActive()) {
            // The second half of the same guard as vanilla: a short list has
            // nothing to scroll, so the thumb must not move even mid-drag --
            // otherwise it slides down a dead track.
            scrollToPointer(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.dragging = false;
        return super.mouseReleased(event);
    }

    // Scrolls by whole notches rather than pixels.
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Vanilla lets the base class have first refusal, so the wheel still
        // reaches the hotbar before the recipe list.
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        int rows = offscreenRows();
        if (rows <= 0) {
            return false;
        }
        this.scroll = Mth.clamp(this.scroll - (float) scrollY / rows, 0.0F, 1.0F);
        return true;
    }

    // Drops this menu's cached recipe list.
    @Override
    public void removed() {
        CarpenterTableClientRecipes.forget(this.menu.containerId);
        super.removed();
    }

    private List<ItemStack> results() {
        return CarpenterTableClientRecipes.forMenu(this.menu.containerId);
    }

    // How many whole rows sit past the visible three, i.e. vanilla's getOffscreenRows: ceil(count
    // / 4) - 3. Scrolling is per row, not per item, so a page always moves by one line of
    // results.
    private int offscreenRows() {
        int rows = (results().size() + COLUMNS - 1) / COLUMNS;
        return Math.max(0, rows - ROWS);
    }

    // Whether there is anything to scroll, i.e. vanilla's isScrollBarActive.
    private boolean isScrollBarActive() {
        return results().size() > PER_PAGE;
    }

    // The absolute index drawn in the grid's first cell.
    private int firstVisible() {
        return Mth.clamp((int) (this.scroll * offscreenRows() + 0.5F), 0, offscreenRows()) * COLUMNS;
    }

    // Rewinds the view when the payload swaps in a different-length list.
    private void syncScrollToList() {
        int size = results().size();
        if (size != this.lastResultCount) {
            this.lastResultCount = size;
            this.scroll = 0.0F;
        }
    }

    // The grid cell under the cursor, or -1.
    private int cellAt(double mouseX, double mouseY) {
        if (!isHovering(GRID_X, GRID_Y + CELL_ITEM_INSET, COLUMNS * CELL_WIDTH, ROWS * CELL_HEIGHT,
                mouseX, mouseY)) {
            return -1;
        }
        int column = Mth.clamp((int) (mouseX - leftPos - GRID_X) / CELL_WIDTH, 0, COLUMNS - 1);
        int row = Mth.clamp((int) (mouseY - topPos - GRID_Y - CELL_ITEM_INSET) / CELL_HEIGHT, 0, ROWS - 1);
        return row * COLUMNS + column;
    }

    // The cell frame for one result: selected beats hovered beats plain.
    private Identifier cellSprite(int index, int selected, int hovered) {
        if (index == selected) {
            return RECIPE_SELECTED;
        }
        // `hovered` is -1 when the cursor is off the grid, and firstVisible() is 0
        // more than the hovered cell's index only when the list is scrolled -- so
        // without this guard a scrolled grid highlights the last visible cell
        // whenever the mouse leaves it.
        if (hovered >= 0 && index == firstVisible() + hovered) {
            return RECIPE_HIGHLIGHTED;
        }
        return RECIPE;
    }

    // Dimmed when the list fits on one page, matching the stonecutter.
    private Identifier scrollbarSprite() {
        return isScrollBarActive() ? SCROLLER : SCROLLER_DISABLED;
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        return isHovering(SCROLLBAR_X, SCROLLBAR_HIT_TOP, SCROLLBAR_WIDTH, SCROLLBAR_TRACK, mouseX, mouseY);
    }

    private int scrollbarX() {
        return leftPos + SCROLLBAR_X;
    }

    private int scrollbarY() {
        return topPos + SCROLLBAR_TOP + (int) (SCROLLBAR_TRAVEL * this.scroll);
    }


    // Maps a cursor y to a scroll position, centring the thumb on it.
    private void scrollToPointer(double mouseY) {
        int trackTop = topPos + SCROLLBAR_DRAG_TOP;
        this.scroll = Mth.clamp(((float) mouseY - trackTop - SCROLLBAR_HEIGHT / 2.0F)
                / (SCROLLBAR_TRACK - SCROLLBAR_HEIGHT), 0.0F, 1.0F);
    }
}
