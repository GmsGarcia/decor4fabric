package net.gmsgarcia.decor4fabric.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
 * <h2>This file is the sanctioned divergence</h2>
 *
 * <p>Unlike its 26.x twin this class cannot be byte-identical, and the reason is
 * {@link SingleItemRecipe} itself, which was reworked between the targets:
 *
 * <ul>
 *   <li>the result is an {@link ItemStack} here and an
 *       {@code ItemStackTemplate} there, so {@code count} rides inside the result
 *       field on this target;</li>
 *   <li>there is no {@code Recipe.CommonInfo} here -- the equivalent state is the
 *       bare {@code group} constructor parameter;</li>
 *   <li>there is no {@code SingleItemRecipe.simpleMapCodec} /
 *       {@code simpleStreamCodec} here, so both codecs are written out by hand
 *       below rather than derived;</li>
 *   <li>{@code RecipeSerializer} is an interface with {@code codec()} and
 *       {@code streamCodec()} accessors here, not a record of two codec fields.</li>
 * </ul>
 *
 * <p>The only difference the <em>rest</em> of the codebase may observe is
 * {@link #craft(ItemStack, HolderLookup.Provider)}, which exists here and on 26.x
 * with the same name and the same signature precisely so that
 * {@code CarpenterTableMenu} -- the file that actually uses recipes -- stays
 * identical. {@code diff -r} therefore names this one file in its
 * {@code --exclude} list.
 *
 * <h2>Codec shape</h2>
 *
 * <p>JSON field names are fixed at {@code ingredient} and {@code result}, and
 * the emitted result is {@code {"id": ..., "count": 3}} -- the {@code count} was a
 * separate top-level integer in 1.18.2. The 26.x codec produces the same JSON, so
 * one recipe file serves every target.
 *
 * <p>{@code group} is not read. 1.18.2's serializer read a {@code group} string,
 * but nothing reads it for a non-crafting-table recipe, and the emitted JSON does
 * not carry it, so the superclass is handed {@code ""} and
 * {@link #group()} inherits it. Old hand-written files that do carry a
 * {@code group} field are ignored rather than rejected, since the codec simply has
 * no field to map them to.
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

    /**
     * JSON shape: {@code ingredient} + {@code result}.
     *
     * <p>Hand-written where 26.x derives this from
     * {@code SingleItemRecipe.simpleMapCodec}, because that helper does not exist
     * on this target. {@link Ingredient#CODEC} is the list-shaped codec the
     * 1.20.5+ JSON format uses, and {@link ItemStack#CODEC} carries the count,
     * so {@code 1 log -> 3 chairs} needs no separate field.
     */
    public static final MapCodec<CarpenterTableRecipe> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(CarpenterTableRecipe::input),
                    ItemStack.CODEC.fieldOf("result").forGetter(CarpenterTableRecipe::result)
            ).apply(instance, (ingredient, result) -> new CarpenterTableRecipe("", ingredient, result)));

    /** Network shape, mirroring {@link #MAP_CODEC} field for field. */
    public static final StreamCodec<RegistryFriendlyByteBuf, CarpenterTableRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, CarpenterTableRecipe::input,
                    ItemStack.STREAM_CODEC, CarpenterTableRecipe::result,
                    (ingredient, result) -> new CarpenterTableRecipe("", ingredient, result)
            );

/**
 * The serializer, registered as {@code decor4fabric:carpenter_table} -- the same
 * id as {@link #TYPE}, which is the convention: the recipe manager resolves
 * a recipe file's {@code "type"} field against {@code RECIPE_SERIALIZER},
 * not against {@code RECIPE_TYPE}.
 *
 * <p>Registration is not optional and is easy to miss. Nothing references
 * this field except {@link #getSerializer()}, so leaving it unregistered
 * compiles cleanly, passes every jar-content check, and fails only at datapack
 * load, where all 165 carpentry table recipes report an unknown serializer and the
 * carpentry table opens to an empty grid.
 *
* <p>A {@link RecipeSerializer} is an interface of a codec and a stream codec on
     * this target -- there is no {@code read}/{@code write} pair to implement and
     * no {@code fromNetwork} to forget, but there is also no record constructor to
     * call, so the two accessors are implemented by hand. Both return the fields
     * above. Like {@link #TYPE}, the field is a bare construction and the
     * {@code Registry.register} happens through the {@code ContentRegistrar}
     * seam, deferred to {@code RegisterEvent} on NeoForge.
     */
    public static final RecipeSerializer<CarpenterTableRecipe> SERIALIZER = new RecipeSerializer<>() {
            @Override
            public MapCodec<CarpenterTableRecipe> codec() {
                return MAP_CODEC;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, CarpenterTableRecipe> streamCodec() {
                return STREAM_CODEC;
            }
        };

    /**
     * @param group    always {@code ""}; see the class comment
     * @param ingredient the accepted input
     * @param result   the crafted output, count included
     */
    public CarpenterTableRecipe(String group, Ingredient ingredient, ItemStack result) {
        super(group, ingredient, result);
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

    /**
     * The one seam the rest of the codebase may rely on across targets.
     *
     * <p>{@link SingleItemRecipe#assemble} takes {@code (RecipeInput,
     * HolderLookup.Provider)} on this target and only {@code (RecipeInput)} on
     * 26.x, so the two copies of {@code CarpenterTableMenu} would each need a
     * different argument list. This shim absorbs the difference instead: the
     * 26.x copy has the same method with the same signature and simply ignores
     * the provider, so the menu has one call site that compiles everywhere.
     *
     * <p>{@code registries} is unused here only in the sense that this recipe
     * does no registry lookups of its own; it is threaded through because the
     * inherited {@code assemble} requires it.
     *
     * @param input      the single ingredient stack handed in by the input slot
     * @param registries the level's registries, required by {@code assemble} here
     * @return the assembled result stack
     */
    public ItemStack craft(ItemStack input, HolderLookup.Provider registries) {
        return assemble(new SingleRecipeInput(input), registries);
    }
}