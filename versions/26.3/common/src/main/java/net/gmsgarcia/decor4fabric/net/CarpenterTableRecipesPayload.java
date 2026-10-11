package net.gmsgarcia.decor4fabric.net;

import java.util.List;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Ships the client the list of outputs it cannot derive on its own.
 *
 * <p>This payload exists because modern Minecraft stopped exposing per-type
 * recipe lists to the client. On 1.18.2 the carpentry table screen called
 * {@code getAvailableRecipes()} on its handler and got the whole
 * {@code List<carpenterTableRecipe>}, then drew {@code recipe.getOutput()} for each
 * one. Since 1.20.5 that is impossible: {@code Level.recipeAccess()} returns a
 * {@code RecipeAccess}, whose entire surface is {@code propertySet()} and
 * {@code stonecutterRecipes()}, and the client's own implementation --
 * {@code ClientRecipeContainer} -- implements those same two methods and nothing
 * else. Vanilla syncs recipe lists per type and has only ever done it for the
 * stonecutter. A custom recipe type is simply not on the client.
 *
 * <p>So the carpentry table's own menu has to send it. The menu is still fully
 * server-authoritative: the client never filters, never assembles and never
 * decides anything. It receives finished {@link ItemStack}s purely to draw
 * icons, which is why this carries results rather than recipes. Sending recipes
 * would mean syncing a {@code RecipeHolder} per entry and then decoding ingredient
 * predicates the client has no use for, so the client would pay for a codec it
 * would never call. The one stack that is actually consumed -- the crafted
 * result -- already reaches the client the ordinary way, through the result slot.
 *
 * <p>Keyed by container id because the payload has no other way to say which
 * open menu it belongs to, and the server cannot assume only one carpentry table menu
 * is open: two players, or one player who opened and abandoned one, is enough.
 * The client cache is keyed the same way and entries are dropped when the menu
 * closes, so a stale payload can only ever land on an id that is no longer open.
 *
 * <p>Registered once per loader and never on the server. See
 * {@code ContentRegistrar#payloadType} for why registration is a seam but sending
 * is not.
 */
public record CarpenterTableRecipesPayload(int menuId, List<ItemStack> results) implements CustomPacketPayload {

    /**
     * {@code decor4fabric:carpenter_table_recipes}.
     *
     * <p>Bound once at class load. {@link net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type}
     * is a record wrapper around an {@link Identifier} with no registration
     * side effect, so unlike a block or a recipe type there is nothing to defer
     * and this needs no seam.
     */
    public static final CustomPacketPayload.Type<CarpenterTableRecipesPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(Decor4Fabric.MOD_ID, "carpenter_table_recipes"));

    /**
     * Upper bound on synced entries, matching the grid's capacity.
     *
     * <p>The screen only ever draws a page of 12 at a time, and
     * {@code CarpenterTableMenu} refreshes the list whenever the input changes, so a
     * pathological ingredient that matched hundreds of recipes would cost the
     * client a lot of bytes to render a page it cannot reach. The cap is
     * enforced on write as well as on decode, so the client cannot be made to
     * allocate an oversized list by a hostile server either.
     */
    public static final int MAX_RESULTS = 256;

    /**
     * {@link ItemStack#OPTIONAL_LIST_STREAM_CODEC} rather than
     * {@code STREAM_CODEC.apply(list())}: the entries are already known non-empty
     * on the sending side, and the optional variant is the one already typed as
     * a list, which keeps the composite to two fields.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, CarpenterTableRecipesPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, CarpenterTableRecipesPayload::menuId,
                    ItemStack.OPTIONAL_LIST_STREAM_CODEC, CarpenterTableRecipesPayload::results,
                    CarpenterTableRecipesPayload::new);

    /**
     * The payload's channel identifier.
     *
     * <p>An explicit override rather than an accessor the record generates,
     * because {@link CustomPacketPayload} declares {@code type()} abstract and a
     * record only auto-generates it if a component is named {@code type}.
     */
    @Override
    public CustomPacketPayload.Type<CarpenterTableRecipesPayload> type() {
        return TYPE;
    }

    /**
     * Truncates to {@link #MAX_RESULTS} and copies.
     *
     * <p>An explicit canonical constructor, so the bound is applied in the one
     * place every instance passes through. {@link #CODEC} decodes through this
     * constructor, which is what makes the cap a real defence rather than a
     * convention: a hostile or buggy server cannot hand the client a list long
     * enough to matter, because the list is already truncated by the time the
     * instance exists. Doing it at the call site instead would leave decode
     * uncapped and the guarantee worth nothing.
     *
     * <p>The copy is for the same reason. {@code ItemStack} is mutable, so a
     * caller holding the list it passed in could mutate a payload after the fact,
     * and the cache hands these lists straight to the screen.
     */
    public CarpenterTableRecipesPayload(int menuId, List<ItemStack> results) {
        this.menuId = menuId;
        this.results = results.size() <= MAX_RESULTS
                ? List.copyOf(results)
                : List.copyOf(results.subList(0, MAX_RESULTS));
    }
}