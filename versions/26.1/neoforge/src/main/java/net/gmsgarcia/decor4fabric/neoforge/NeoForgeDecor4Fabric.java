package net.gmsgarcia.decor4fabric.neoforge;

import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * NeoForge entrypoint: the only loader-specific part of the mod.
 *
 * <p>NeoForge freezes every registry before it constructs this class, so from
 * Phase 2 on nothing here may build an object. Blocks, block entity types, items
 * and creative tabs are queued as factories on a {@code DeferredRegister} and
 * built later, during {@code RegisterEvent}. That is why the registration calls
 * cannot live in the common class, and why the two loaders need different code
 * here.
 *
 * <p>{@link #attach} has to run after {@link Decor4Fabric#init} rather than
 * before it: init is what fills the deferred registers, and attaching to the bus
 * earlier would fire an empty register pass.
 */
@Mod(Decor4Fabric.MOD_ID)
public class NeoForgeDecor4Fabric {

    public NeoForgeDecor4Fabric(IEventBus eventBus) {
        NeoForgeContentRegistrar registrar = new NeoForgeContentRegistrar();
        Decor4Fabric.init(registrar);
        registrar.attach(eventBus);
    }
}
