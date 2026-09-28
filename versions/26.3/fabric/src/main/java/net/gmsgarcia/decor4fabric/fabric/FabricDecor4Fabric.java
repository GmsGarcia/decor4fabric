package net.gmsgarcia.decor4fabric.fabric;

import net.fabricmc.api.ModInitializer;
import net.gmsgarcia.decor4fabric.Decor4Fabric;

/**
 * Fabric entrypoint: the only loader-specific part of the mod.
 *
 * <p>{@code ModInitializer} runs before the registry freeze, so from Phase 2 on
 * this side registers eagerly and can log real counts immediately. NeoForge is
 * handed the identical set of factories but has to defer them, and therefore
 * logs a moment later.
 */
public class FabricDecor4Fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Decor4Fabric.init();
        Decor4Fabric.LOGGER.info("Decor4Fabric loaded (skeleton, 0 blocks)");
    }
}