package net.gmsgarcia.decor4fabric.recipe;

import com.mojang.serialization.MapCodec;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;

/**
 * The carpentry table's recipe: one ingredient in, one stack out, crafted 1x1.
 *
 * <p>1.18.2 declared {@code carpenterTableRecipe extends CuttingRecipe}, and the
 * {@code CuttingRecipe} superclass was what made that a two-line class --
 * {@code matches} and {@code assemble} came for free and 1.18.2 only had to
 * override {@code matches} to narrow it to a single slot.
 * {@code CuttingRecipe} was added in 1.19.3 for the mace and removed in 1.20, so
 * the superclass has to change. {@link SingleItemRecipe} is what took its place
 * for exactly this shape: it is {@code Recipe<SingleRecipeInput>} with an
 * {@link Ingredient}, a result, and {@code matches}/{@code assemble}/
 * {@code placementInfo()} already implemented.
 *
 * <p>So this class is now almost empty, and that is the point. What is left is
 * only what is genuinely specific to the carpentry table: the two registry handles and
 * the two categorisation answers. Nothing here reimplements crafting logic.
 *
 * <h2>Codec shape</h2>
 *
 * <p>{@link #MAP_CODEC} is built by {@link SingleItemRecipe#simpleMapCodec},
 * which fixes the JSON field names at {@code ingredient} and {@code result} and
 * wraps the pair in a {@link Recipe.CommonInfo}. The result is an
 * {@link ItemStackTemplate} rather than an {@link ItemStack}, which is how the
 * count survives into the JSON as {@code {"id": ..., "count": 3}} -- the
 * {@code count} was a separate top-level integer in 1.18.2.
 *
 * <p>{@code group} is gone. 1.18.2's serializer read a {@code group} string and
 * 1.18.2's {@code group()} returned it, but modern {@link Recipe.CommonInfo}
 * carries only {@code showNotification} and {@code group()} is a separate
 * abstract method with nothing reading it for a non-crafting-table recipe.
 * {@link #group()} therefore returns the empty string, which is what
 * {@code StonecutterRecipe} does for the same reason -- see the {@code group()}
 * body in the vanilla class. Generated recipes therefore do not carry a
 * {@code group} field, and old hand-written ones that do are ignored rather than
 * rejected, since the codec simply has no field to map them to.
 */
public class CarpenterTableRecipe extends SingleItemRecipe {

    /**
     * The recipe type, registered as {@code decor4fabric:carpenter_table}.
     *
     * <p><b>Not</b> registered through {@link RecipeType#register}, despite that
     * being how every vanilla recipe type is made. That helper is
     * {@code Registry.register(RECIPE_TYPE, Identifier.withDefaultNamespace(name), type)},
     * and {@code withDefaultNamespace} hard-codes {@code minecraft} -- so
     * {@code RecipeType.register("carpenter_table")} lands in the registry as
     * {@code minecraft:carpenter_table}. Nothing rejects it, it compiles, and the jars
     * look right; it only misbehaves at datapack load, when
     * {@code decor4fabric:carpenter_table} resolves to no type and every one of the
     * 165 generated recipes is dropped as an unknown recipe type. Handing the
     * registry an explicit {@link Identifier} is the fix, and it is the same
     * idiom {@link net.gmsgarcia.decor4fabric.VanillaRegistrar} uses for every
     * other entry.
     *
     * <p>The field itself is only a construction. With {@code RecipeType}'s
     * anonymous subclass there is no self-registering {@code register} to call
     * from a static initialiser: the write is performed by the
     * {@code ContentRegistrar} seam, which on NeoForge defers it to
     * {@code RegisterEvent}. The old form of this field did the write inline,
     * which worked on Fabric only -- on NeoForge the class initialiser ran from
     * inside {@code Decor4Fabric#init}, i.e. after the registry was frozen, and
     * the write threw {@code "Registry is already frozen"}. See
     * {@link net.gmsgarcia.decor4fabric.ContentRegistrar#recipeType} for the
     * fix.
     */
    public static final RecipeType<CarpenterTableRecipe> TYPE =
            new RecipeType<CarpenterTableRecipe>() { };

    /** JSON shape: {@code ingredient} + {@code result}. */
    public static final MapCodec<CarpenterTableRecipe> MAP_CODEC =
            SingleItemRecipe.simpleMapCodec(CarpenterTableRecipe::new);

    /** Network shape, mirroring {@link #MAP_CODEC} field for field. */
    public static final StreamCodec<RegistryFriendlyByteBuf, CarpenterTableRecipe> STREAM_CODEC =
            SingleItemRecipe.simpleStreamCodec(CarpenterTableRecipe::new);

    /**
     * The serializer, registered as {@code decor4fabric:carpenter_table} -- the same
     * id as {@link #TYPE}, which is the convention: the recipe manager resolves
     * a recipe file's {@code "type"} field against {@code RECIPE_SERIALIZER},
     * not against {@code RECIPE_TYPE}.
     *
     * <p>Registration is not optional and is easy to miss. Nothing references
     * this field except {@link #getSerializer()}, so leaving it unregistered
     * compiles cleanly, passes every jar-content check, and fails only at
     * datapack load, where all 165 carpentry table recipes report an unknown
     * serializer and the carpentry table opens to an empty grid.
     *
     * <p>A {@link RecipeSerializer} is now a record of a codec and a stream
     * codec -- there is no {@code read}/{@code write} pair to implement and no
     * {@code fromNetwork} to forget. Both halves are the ones above. Like
     * {@link #TYPE}, the field is a bare construction and the
     * {@code Registry.register} happens through the {@code ContentRegistrar}
     * seam, deferred to {@code RegisterEvent} on NeoForge.
     */
    public static final RecipeSerializer<CarpenterTableRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public CarpenterTableRecipe(Recipe.CommonInfo commonInfo, Ingredient ingredient, ItemStackTemplate result) {
        super(commonInfo, ingredient, result);
    }

    @Override
    public RecipeType<? extends SingleItemRecipe> getType() {
        return TYPE;
    }

    @Override
    public RecipeSerializer<? extends SingleItemRecipe> getSerializer() {
        return SERIALIZER;
    }

    /**
     * Always empty; see the class comment. Required because
     * {@link Recipe#group()} is abstract and {@link Recipe.CommonInfo} does not
     * carry it.
     */
    @Override
    public String group() {
        return "";
    }

    /**
     * The one place this file is allowed to differ from its 1.21.11 twin.
     *
     * <p>{@link Recipe#assemble} changed arity between the targets. On this one it
     * is {@code assemble(RecipeInput)} -- the {@link
     * net.minecraft.core.HolderLookup.Provider} parameter was dropped -- while
     * 1.21.11 still takes {@code assemble(RecipeInput, HolderLookup.Provider)}.
     * That is a signature change on a method inherited from vanilla, so no
     * classtweaker entry or access-widener line can paper over it: the callers
     * would each need a different argument list.
     *
     * <p>Rather than sanction two divergent files, this shim absorbs the
     * difference. The 1.21.11 copy of this class implements the same method with
     * the same name and the same {@code (ItemStack, HolderLookup.Provider)}
     * signature, forwarding to its own two-argument {@code assemble} and
     * ignoring the provider exactly as vanilla's own 1.21.11 recipes do. The menu
     * then has one call site, {@code recipe.craft(stack, level.registryAccess())},
     * that compiles everywhere.
     *
     * @param input      the single ingredient stack handed in by the input slot
     * @param registries unused on this target, and required on 1.21.11; passed
     *                   unconditionally so the call sites match
     * @return the assembled result stack
     */
    public ItemStack craft(ItemStack input, HolderLookup.Provider registries) {
        return assemble(new SingleRecipeInput(input));
    }

    /**
     * {@code CRAFTING_MISC}, not {@code STONECUTTER}.
     *
     * <p>This decides where the recipe is filed in the recipe book and in the
     * recipe-book screen's category tabs. {@code STONECUTTER} would file a bench
     * recipe under the stonecutter's tab, which is wrong on its face -- and on
     * this mod specifically it would be actively misleading, because the
     * carpentry table is a different machine from the stonecutter even though 1.18.2
     * borrowed its GUI texture.
     */
    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}