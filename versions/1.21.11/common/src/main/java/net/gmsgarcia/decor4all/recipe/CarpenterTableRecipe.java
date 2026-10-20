package net.gmsgarcia.decor4all.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gmsgarcia.decor4all.Decor4All;
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

// The carpentry table's recipe: one ingredient in, one stack out, crafted 1x1.
public class CarpenterTableRecipe extends SingleItemRecipe {

    // The recipe type, registered as decor4all:carpenter_table.
    public static final RecipeType<CarpenterTableRecipe> TYPE =
            new RecipeType<CarpenterTableRecipe>() { };

    // JSON shape: ingredient + result.
    public static final MapCodec<CarpenterTableRecipe> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(CarpenterTableRecipe::input),
                    ItemStack.CODEC.fieldOf("result").forGetter(CarpenterTableRecipe::result)
            ).apply(instance, (ingredient, result) -> new CarpenterTableRecipe("", ingredient, result)));

    // Network shape, mirroring #MAP_CODEC field for field.
    public static final StreamCodec<RegistryFriendlyByteBuf, CarpenterTableRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, CarpenterTableRecipe::input,
                    ItemStack.STREAM_CODEC, CarpenterTableRecipe::result,
                    (ingredient, result) -> new CarpenterTableRecipe("", ingredient, result)
            );

// The serializer, registered as decor4all:carpenter_table -- the same id as #TYPE, which is the
// convention: the recipe manager resolves a recipe file's "type" field against RECIPE_SERIALIZER,
// not against RECIPE_TYPE.
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

    // CRAFTING_MISC, not STONECUTTER.
    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    // The one seam the rest of the codebase may rely on across targets.
    public ItemStack craft(ItemStack input, HolderLookup.Provider registries) {
        return assemble(new SingleRecipeInput(input), registries);
    }
}
