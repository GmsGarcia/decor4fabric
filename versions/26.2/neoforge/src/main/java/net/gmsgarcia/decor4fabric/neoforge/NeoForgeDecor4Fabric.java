package net.gmsgarcia.decor4fabric.neoforge;

import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.content.DecorBlocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

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

        eventBus.addListener(NeoForgeDecor4Fabric::addWorkbenchToVanillaTab);
        eventBus.addListener(NeoForgeDecor4Fabric::onClientSetup);
    }

    /**
     * Puts the workbench item into {@code minecraft:functional_blocks}.
     *
     * <p>1.18.2 put it in {@code ItemGroup.DECORATIONS}, which no longer exists;
     * {@link DecorBlocks#WORKBENCH_TAB} records which surviving tab replaced it.
     * See {@code FabricDecor4Fabric} for the full explanation of why this one
     * item needs a loader-specific hook while the other 165 do not.
     *
     * <p>{@code BuildCreativeModeTabContentsEvent} is itself a
     * {@code CreativeModeTab.Output}, so {@code accept} is the vanilla method.
     * NeoForge's patched {@code CreativeModeTab#buildContents} is what fires it,
     * which is why this reaches vanilla tabs as well as mod ones.
     *
     * <p>There is deliberately no {@code Item.Properties#tab(...)} alternative:
     * NeoForge's {@code IItemPropertiesExtensions} on 26.1 exposes only
     * {@code component(Supplier, T)} and no tab assignment at all, and the item is
     * built in common code where a NeoForge-only method could not be called.
     */
    private static void addWorkbenchToVanillaTab(BuildCreativeModeTabContentsEvent event) {
        if (DecorBlocks.WORKBENCH_TAB.equals(event.getTabKey())) {
            event.accept(Decor4Fabric.workbenchItem());
        }
    }

    /**
     * NeoForge's counterpart to Fabric's {@code ClientModInitializer}.
     *
     * <p>Phase 1 shipped a Fabric client entrypoint but no NeoForge one, because
     * at that point there was no client content to attach it to. The mod still has
     * none -- the workbench screen and handler are Phase 4 -- so this logs for
     * parity and is where client registration will go.
     */
    private static void onClientSetup(FMLClientSetupEvent event) {
        Decor4Fabric.LOGGER.info("Decor4Fabric client ready");
    }
}
