package net.gmsgarcia.decor4fabric.registry;

import net.gmsgarcia.decor4fabric.recipes.workBenchRecipe;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class recipeRegistry {

    public static RecipeSerializer<workBenchRecipe> WORKBENCH_SERIALIZER;
    public static RecipeType<workBenchRecipe> WORKBENCH_TYPE;

    public static void registerRecipes() {

        WORKBENCH_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier("decor4fabric", "workbench"), workBenchRecipe.Serializer.INSTANCE);
        WORKBENCH_TYPE = Registry.register(Registry.RECIPE_TYPE, new Identifier("decor4fabric", "workbench"), workBenchRecipe.Type.INSTANCE);

    }
}
