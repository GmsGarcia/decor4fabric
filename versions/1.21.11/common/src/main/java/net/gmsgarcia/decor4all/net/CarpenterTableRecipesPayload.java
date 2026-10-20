package net.gmsgarcia.decor4all.net;

import java.util.List;
import net.gmsgarcia.decor4all.Decor4All;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

// Ships the client the list of outputs it cannot derive on its own.
public record CarpenterTableRecipesPayload(int menuId, List<ItemStack> results) implements CustomPacketPayload {

    // decor4all:carpenter_table_recipes.
    public static final CustomPacketPayload.Type<CarpenterTableRecipesPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(Decor4All.MOD_ID, "carpenter_table_recipes"));

    // Upper bound on synced entries, matching the grid's capacity.
    public static final int MAX_RESULTS = 256;

    // ItemStack#OPTIONAL_LIST_STREAM_CODEC rather than STREAM_CODEC.apply(list()): the entries are
    // already known non-empty on the sending side, and the optional variant is the one already
    // typed as a list, which keeps the composite to two fields.
    public static final StreamCodec<RegistryFriendlyByteBuf, CarpenterTableRecipesPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, CarpenterTableRecipesPayload::menuId,
                    ItemStack.OPTIONAL_LIST_STREAM_CODEC, CarpenterTableRecipesPayload::results,
                    CarpenterTableRecipesPayload::new);

    // The payload's channel identifier.
    @Override
    public CustomPacketPayload.Type<CarpenterTableRecipesPayload> type() {
        return TYPE;
    }

    // Truncates to #MAX_RESULTS and copies.
    public CarpenterTableRecipesPayload(int menuId, List<ItemStack> results) {
        this.menuId = menuId;
        this.results = results.size() <= MAX_RESULTS
                ? List.copyOf(results)
                : List.copyOf(results.subList(0, MAX_RESULTS));
    }
}