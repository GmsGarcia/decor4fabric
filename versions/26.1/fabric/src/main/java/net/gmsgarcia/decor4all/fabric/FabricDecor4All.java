package net.gmsgarcia.decor4all.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.VanillaRegistrar;
import net.gmsgarcia.decor4all.content.DecorBlocks;

// Fabric entrypoint: the only loader-specific part of the mod.
public class FabricDecor4All implements ModInitializer {

    @Override
    public void onInitialize() {
        Decor4All.init(new VanillaRegistrar());
        addCarpenterTableToVanillaTab();
    }

    // Puts the carpentry table item into minecraft:functional_blocks.
    private static void addCarpenterTableToVanillaTab() {
        CreativeModeTabEvents.modifyOutputEvent(DecorBlocks.CARPENTER_TABLE_TAB)
                .register(output -> output.accept(Decor4All.carpenterTableItem()));
    }
}
