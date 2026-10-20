package net.gmsgarcia.decor4all.recipe;

import com.mojang.serialization.MapCodec;
import net.gmsgarcia.decor4all.Decor4All;
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

// The carpentry table's recipe: one ingredient in, one stack out, crafted 1x1.
public class CarpenterTableRecipe extends SingleItemRecipe {

    // The recipe type, registered as decor4all:carpenter_table.
    public static final RecipeType<CarpenterTableRecipe> TYPE =
            new RecipeType<CarpenterTableRecipe>() { };

    // JSON shape: ingredient + result.
    public static final MapCodec<CarpenterTableRecipe> MAP_CODEC =
            SingleItemRecipe.simpleMapCodec(CarpenterTableRecipe::new);

    // Network shape, mirroring #MAP_CODEC field for field.
    public static final StreamCodec<RegistryFriendlyByteBuf, CarpenterTableRecipe> STREAM_CODEC =
            SingleItemRecipe.simpleStreamCodec(CarpenterTableRecipe::new);

    // The serializer, registered as decor4all:carpenter_table -- the same id as #TYPE, which is
    // the convention: the recipe manager resolves a recipe file's "type" field against
    // RECIPE_SERIALIZER, not against RECIPE_TYPE.
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

    // Always empty; see the class comment. Required because Recipe#group() is abstract and
    // Recipe.CommonInfo does not carry it.
    @Override
    public String group() {
        return "";
    }

    // The one place this file is allowed to differ from its 1.21.11 twin.
    public ItemStack craft(ItemStack input, HolderLookup.Provider registries) {
        return assemble(new SingleRecipeInput(input));
    }

    // CRAFTING_MISC, not STONECUTTER.
    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
