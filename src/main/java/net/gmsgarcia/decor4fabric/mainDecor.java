package net.gmsgarcia.decor4fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

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
        registerFences();
        registerFencesGates();

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

    public static final String MOD_ID = "decor4fabric";

    public static float getModVersion()
    {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(MOD_ID);

        if(modContainer.isPresent())
        {
            try
            {
                return Float.parseFloat(String.valueOf(modContainer.get().getMetadata().getVersion()));
            }
            catch(NumberFormatException e) {}
        }

        return 0;
    }
}