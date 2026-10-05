package net.gmsgarcia.decor4fabric.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;

/**
 * The workbench's recipe: one ingredient in, one stack out, crafted 1x1.
 *
 * <p>1.18.2 declared {@code workBenchRecipe extends CuttingRecipe}, and the
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
 * only what is genuinely specific to the workbench: the two registry handles and
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
 * {@code WorkBenchMenu} -- the file that actually uses recipes -- stays
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
public class WorkBenchRecipe extends SingleItemRecipe {

    /**
     * The recipe type, registered as {@code decor4fabric:workbench}.
     *
     * <p><b>Not</b> registered through {@link RecipeType#register}, despite that
     * being how every vanilla recipe type is made. That helper is
     * {@code Registry.register(RECIPE_TYPE, Identifier.withDefaultNamespace(name), type)},
     * and {@code withDefaultNamespace} hard-codes {@code minecraft} -- so
     * {@code RecipeType.register("workbench")} lands in the registry as
     * {@code minecraft:workbench}. Nothing rejects it, it compiles, and the jars
     * look right; it only misbehaves at datapack load, when
     * {@code decor4fabric:workbench} resolves to no type and every one of the
     * 165 generated recipes is dropped as an unknown recipe type. Handing the
     * registry an explicit {@link Identifier} is the fix, and it is the same
     * idiom {@link net.gmsgarcia.decor4fabric.VanillaRegistrar} uses for every
     * other entry.
     *
     * <p>Eager rather than deferred through the {@code ContentRegistrar} seam:
     * that seam exists because blocks and items need their
     * {@code DeferredRegister}s resolved before the registry is frozen, and a
     * recipe type has no such dependency -- one registry entry is all it is.
     * {@link net.gmsgarcia.decor4fabric.Decor4Fabric} logs {@link #TYPE} during
     * construction so the class is initialised before the datapack load reads
     * the registry.
     */
    public static final RecipeType<WorkBenchRecipe> TYPE = Registry.register(
            BuiltInRegistries.RECIPE_TYPE,
            Identifier.fromNamespaceAndPath(Decor4Fabric.MOD_ID, "workbench"),
            new RecipeType<WorkBenchRecipe>() { });

    /**
     * JSON shape: {@code ingredient} + {@code result}.
     *
     * <p>Hand-written where 26.x derives this from
     * {@code SingleItemRecipe.simpleMapCodec}, because that helper does not exist
     * on this target. {@link Ingredient#CODEC} is the list-shaped codec the
     * 1.20.5+ JSON format uses, and {@link ItemStack#CODEC} carries the count,
     * so {@code 1 log -> 3 chairs} needs no separate field.
     */
    public static final MapCodec<WorkBenchRecipe> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(WorkBenchRecipe::input),
                    ItemStack.CODEC.fieldOf("result").forGetter(WorkBenchRecipe::result)
            ).apply(instance, (ingredient, result) -> new WorkBenchRecipe("", ingredient, result)));

    /** Network shape, mirroring {@link #MAP_CODEC} field for field. */
    public static final StreamCodec<RegistryFriendlyByteBuf, WorkBenchRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, WorkBenchRecipe::input,
                    ItemStack.STREAM_CODEC, WorkBenchRecipe::result,
                    (ingredient, result) -> new WorkBenchRecipe("", ingredient, result)
            );

/**
 * The serializer, registered as {@code decor4fabric:workbench} -- the same
 * id as {@link #TYPE}, which is the convention: the recipe manager resolves
 * a recipe file's {@code "type"} field against {@code RECIPE_SERIALIZER},
 * not against {@code RECIPE_TYPE}.
 *
 * <p>Registration is not optional and is easy to miss. Nothing references
 * this field except {@link #getSerializer()}, so leaving it unregistered
 * compiles cleanly, passes every jar-content check, and fails only at datapack
 * load, where all 165 workbench recipes report an unknown serializer and the
 * workbench opens to an empty grid.
 *
 * <p>A {@link RecipeSerializer} is an interface of a codec and a stream codec on
 * this target -- there is no {@code read}/{@code write} pair to implement and
 * no {@code fromNetwork} to forget, but there is also no record constructor to
 * call, so the two accessors are implemented by hand. Both return the fields
 * above. {@code RecipeSerializer.register} does exist here but resolves the
 * bare name the same way {@link RecipeType#register} does, so this goes
 * through {@link Registry#register} like every other entry.
 */
public static final RecipeSerializer<WorkBenchRecipe> SERIALIZER = Registry.register(
        BuiltInRegistries.RECIPE_SERIALIZER,
        Identifier.fromNamespaceAndPath(Decor4Fabric.MOD_ID, "workbench"),
        new RecipeSerializer<>() {
            @Override
            public MapCodec<WorkBenchRecipe> codec() {
                return MAP_CODEC;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, WorkBenchRecipe> streamCodec() {
                return STREAM_CODEC;
            }
        });

    /**
     * @param group    always {@code ""}; see the class comment
     * @param ingredient the accepted input
     * @param result   the crafted output, count included
     */
    public WorkBenchRecipe(String group, Ingredient ingredient, ItemStack result) {
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
     * workbench is a different machine from the stonecutter even though 1.18.2
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
     * 26.x, so the two copies of {@code WorkBenchMenu} would each need a
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