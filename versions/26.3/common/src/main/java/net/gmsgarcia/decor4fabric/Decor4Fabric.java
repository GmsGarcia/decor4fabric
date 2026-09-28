package net.gmsgarcia.decor4fabric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The loader-neutral bootstrap.
 *
 * <p>Everything both loaders do identically lives here; the only
 * loader-specific code in the mod is the entrypoint pair. Fabric's
 * {@code ModInitializer} still runs before the registry freeze, whereas
 * NeoForge freezes every registry <em>before</em> it constructs the
 * {@code @Mod} class, so from Phase 2 on this class hands each loader a
 * registrar instead of building objects itself.
 *
 * <p>1.18.2's equivalent was {@code mainDecor}, which was Fabric-specific and
 * called the registration classes directly.
 */
public final class Decor4Fabric {

    public static final String MOD_ID = "decor4fabric";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean initialised;

    private Decor4Fabric() {
    }

    /**
     * Runs once per launch.
     *
     * <p>Guarded because a second call would try to re-register the 166 block
     * ids and throw deep inside the registry freeze, which is a much worse
     * error message than this one.
     */
    public static void init() {
        if (initialised) {
            throw new IllegalStateException(MOD_ID + " was initialised twice");
        }
        initialised = true;
        // Phase 2 registers blocks, block items, items and the four creative
        // tabs through a ContentRegistrar, so both loaders share one id order.
    }
}