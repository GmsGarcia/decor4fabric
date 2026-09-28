package net.gmsgarcia.decor4fabric.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.gmsgarcia.decor4fabric.Decor4Fabric;

/**
 * Fabric client entrypoint, replacing 1.18.2's {@code clientDecor}.
 *
 * <p>That class registered the cutout render layers and render types for the
 * leaves, glass and lantern blocks. They are the only client-exclusive
 * registrations in the mod, which is why the common bootstrap has no client
 * counterpart.
 */
public class FabricDecor4FabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Decor4Fabric.LOGGER.info("Decor4Fabric client ready");
    }
}