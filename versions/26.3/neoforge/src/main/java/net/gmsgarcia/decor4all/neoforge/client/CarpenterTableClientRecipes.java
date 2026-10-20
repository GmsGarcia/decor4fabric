package net.gmsgarcia.decor4all.neoforge.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.gmsgarcia.decor4all.net.CarpenterTableRecipesPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

// The client's copy of the carpentry table's recipe list.
public final class CarpenterTableClientRecipes {

    private static final Map<Integer, List<ItemStack>> BY_MENU = new ConcurrentHashMap<>();

    private CarpenterTableClientRecipes() {
    }

    // Receives one payload and stores its results against the menu's id.
    public static void accept(CarpenterTableRecipesPayload payload) {
        Minecraft.getInstance().execute(() -> BY_MENU.put(payload.menuId(), payload.results()));
    }

    // The results for one open menu, or an empty list.
    public static List<ItemStack> forMenu(int menuId) {
        return BY_MENU.getOrDefault(menuId, List.of());
    }

    // Drops a menu's entry. Called when the menu closes.
    public static void forget(int menuId) {
        BY_MENU.remove(menuId);
    }
}