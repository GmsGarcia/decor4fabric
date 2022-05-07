package net.gmsgarcia.decor4fabric.registry;

import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import static net.gmsgarcia.decor4fabric.registry.blockRegistry.*;

public class fuelRegistry {
    public static void registerFuel()
    {
        FuelRegistryImpl.INSTANCE.add(OAK_LOG_SMALL_STOOL, 300);
    }
}
