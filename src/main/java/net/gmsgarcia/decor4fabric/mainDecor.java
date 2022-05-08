package net.gmsgarcia.decor4fabric;

import net.fabricmc.api.ModInitializer;

import static net.gmsgarcia.decor4fabric.registry.blockRegistry.*;
import static net.gmsgarcia.decor4fabric.registry.fuelRegistry.registerFuel;
import static net.gmsgarcia.decor4fabric.registry.recipeRegistry.registerRecipes;
import static net.gmsgarcia.decor4fabric.registry.screenRegistry.registerScreens;
import static net.gmsgarcia.decor4fabric.sitOnStuff.Sit.sitMain;

public class mainDecor implements ModInitializer {

    @Override
    public void onInitialize() {
        /* WorkBench */
        registerWorkBench();

        /* Blocks */
        registerBenches();
        registerChairs();
        registerStools();
        registerTables();
        //registerFences();

        registerBlockEntities();

        /* Items */
        //registerItems();

        /* Fuel */
        registerFuel();

        /* Recipes */
        registerRecipes();

        /* Screens */
        registerScreens();

        /* Sit Functions */
        sitMain();

    }
}