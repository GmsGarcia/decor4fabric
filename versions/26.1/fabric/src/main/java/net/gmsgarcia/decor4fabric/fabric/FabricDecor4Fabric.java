package net.gmsgarcia.decor4fabric.fabric;

import net.fabricmc.api.ModInitializer;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.VanillaRegistrar;

/**
 * Fabric entrypoint: the only loader-specific part of the mod.
 *
 * <p>{@code ModInitializer} runs before the registry freeze, so
 * {@link VanillaRegistrar} can build each object inline and
 * {@link Decor4Fabric#init} logs real counts when it returns. NeoForge is handed
 * the identical set of factories but has to defer them, so it logs later.
 */
public class FabricDecor4Fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Decor4Fabric.init(new VanillaRegistrar());
    }
}
