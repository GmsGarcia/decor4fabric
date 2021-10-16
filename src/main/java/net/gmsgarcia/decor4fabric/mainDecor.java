package net.gmsgarcia.decor4fabric;

import net.fabricmc.api.ModInitializer;

import static net.gmsgarcia.decor4fabric.registry.blockRegistry.*;
import static net.gmsgarcia.decor4fabric.registry.itemRegistry.registerItems;
import static net.gmsgarcia.decor4fabric.sitOnStuff.Sit.sitMain;

public class mainDecor implements ModInitializer {

    @Override
    public void onInitialize() {
        /* Blocks */
        registerBenches();
        registerChairs();
        /*registerTables();*/

        /* Items */
        registerItems();

        /* Entities */
        sitMain();
    }

}