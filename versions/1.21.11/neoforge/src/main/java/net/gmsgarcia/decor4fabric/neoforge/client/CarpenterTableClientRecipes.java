package net.gmsgarcia.decor4fabric.neoforge.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.gmsgarcia.decor4fabric.net.CarpenterTableRecipesPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * The client's copy of the carpentry table's recipe list.
 *
 * <p>Exists because the client has no way to work it out. See
 * {@link CarpenterTableRecipesPayload} for the long version; the short version is that
 * {@code ClientRecipeContainer} implements only {@code propertySet()} and
 * {@code stonecutterRecipes()}, so a custom recipe type is simply not queryable
 * from the client and the server has to send the answers.
 *
 * <p>Keyed by container id, and only ever read by
 * {@link CarpenterTableScreen}. Two carpentry table menus can be open at once -- a player who
 * walks away from one and opens another -- so the key is the menu's identity, not
 * a single slot. {@link #forget(int)} is called when a menu closes, which is what
 * bounds the map: without it an id would stay resident for the rest of the
 * session, holding assembled item stacks for a menu nobody can reach.
 *
 * <p>This is client-side state touched by a network thread, so the payload is
 * handed to {@link Minecraft#execute} rather than applied directly. The map itself
 * is concurrent so a late payload arriving after {@link #forget(int)} cannot
 * resurrect the entry it was meant to clear.
 */
public final class CarpenterTableClientRecipes {

    private static final Map<Integer, List<ItemStack>> BY_MENU = new ConcurrentHashMap<>();

    private CarpenterTableClientRecipes() {
    }

    /** Receives one payload and stores its results against the menu's id. */
    public static void accept(CarpenterTableRecipesPayload payload) {
        Minecraft.getInstance().execute(() -> BY_MENU.put(payload.menuId(), payload.results()));
    }

    /**
     * The results for one open menu, or an empty list.
     *
     * <p>Empty rather than null so the screen has nothing to guard: a menu that
     * has not been sent a list yet -- the common case, since the carpentry table stores
     * no input and so always opens over an empty slot -- has nothing to draw, and
     * the list only arrives once the player puts something in.
     */
    public static List<ItemStack> forMenu(int menuId) {
        return BY_MENU.getOrDefault(menuId, List.of());
    }

    /** Drops a menu's entry. Called when the menu closes. */
    public static void forget(int menuId) {
        BY_MENU.remove(menuId);
    }
}