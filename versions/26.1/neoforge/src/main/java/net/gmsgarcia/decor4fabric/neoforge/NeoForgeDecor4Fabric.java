package net.gmsgarcia.decor4fabric.neoforge;

import java.util.Objects;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.blockentity.LogBenchBlockEntity;
import net.gmsgarcia.decor4fabric.content.DecorBlocks;
import net.gmsgarcia.decor4fabric.neoforge.client.AxePoseCommand;
import net.gmsgarcia.decor4fabric.neoforge.client.LogBenchRenderer;
import net.gmsgarcia.decor4fabric.neoforge.client.SitEntityRenderer;
import net.gmsgarcia.decor4fabric.neoforge.client.WorkBenchClientRecipes;
import net.gmsgarcia.decor4fabric.neoforge.client.WorkBenchScreen;
import net.gmsgarcia.decor4fabric.net.WorkBenchRecipesPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * NeoForge entrypoint: the only loader-specific part of the mod.
 *
 * <p>NeoForge freezes every registry before it constructs this class, so from
 * Phase 2 on nothing here may build an object. Blocks, block entity types, items,
 * creative tabs and the one entity type are queued as factories on a
 * {@code DeferredRegister} and built later, during {@code RegisterEvent}. That is
 * why the registration calls cannot live in the common class, and why the two
 * loaders need different code here.
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
        eventBus.addListener(NeoForgeDecor4Fabric::registerRenderers);
        eventBus.addListener(NeoForgeDecor4Fabric::registerClientCommands);
        eventBus.addListener(NeoForgeDecor4Fabric::registerPayloads);
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
     * <p>Phase 1 shipped this as a log line, because at that point there was no
     * client content to attach it to. Phase 3 gave it one -- see
     * {@link #registerRenderers} -- and Phase 4 gives it the other: the workbench
     * screen. The log stays for parity.
     *
     * <p>{@code FMLClientSetupEvent} is the right place for it rather than the
     * constructor because the menu type has to exist first. {@code MenuType} is a
     * registry object, so on NeoForge it is built during the {@code RegisterEvent}
     * pass that {@link NeoForgeContentRegistrar#attach} hooked -- and that pass
     * fires before client setup. Reading the type any earlier would see null.
     *
     * <p>{@code MenuScreens} here is vanilla's, not NeoForge's. It became public
     * API in 26.x and {@code register} is reached through the classtweaker entry
     * that also widens {@code MenuType.register}; that is what lets this one call
     * be identical on both loaders.
     */
    private static void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(Decor4Fabric.workbenchMenuType(), WorkBenchScreen::new);
        Decor4Fabric.LOGGER.info("Decor4Fabric client ready");
    }

    /**
     * Teaches NeoForge how to decode the workbench's recipe-list payload.
     *
     * <p>Fabric's equivalent is two lines in the client initialiser; NeoForge wants
     * an event, and the event fires on the <em>mod</em> bus -- the same bus the
     * listeners above use. It also fires early, before registries freeze, which is
     * what makes it the only place this can be registered at all.
     *
     * <p>The codec is the sending side's, so the two halves cannot drift: a
     * mismatch would fail the handshake rather than mis-decode.
     *
     * <p>{@code HandlerThread.MAIN} is the default and is left alone deliberately.
     * {@link WorkBenchClientRecipes} hops to the client thread itself, because on
     * Fabric the receiving thread is the loader's business rather than this mod's,
     * and one implementation that is correct on both loaders beats one that is
     * correct on one and accidentally correct on the other.
     */
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(Decor4Fabric.MOD_ID)
                .playToClient(WorkBenchRecipesPayload.TYPE, WorkBenchRecipesPayload.CODEC,
                        (payload, context) -> WorkBenchClientRecipes.accept(payload));
    }

    /**
     * Gives the sit marker a renderer.
     *
     * <p>{@code RegisterRenderers} is an {@code IModBusEvent}, so it goes on the
     * mod bus -- the same bus the two listeners above already use. It is fired
     * only on a client, and that is what makes the unguarded registration here
     * safe: a HotSpot method is verified when it is first invoked, not when its
     * owner is loaded, so {@code SitEntityRenderer} and the
     * {@code net.minecraft.client} tree it imports are never resolved on a
     * dedicated server. {@code onClientSetup} above relies on the same property.
     *
     * <p>Registered by type instance rather than by key, because that is what
     * {@code registerEntityRenderer} takes on every supported NeoForge version.
     * {@code RegisterRenderers} fires after the registry events the
     * {@code DeferredRegister} attached in {@link #attach} hooks, so the lookup
     * succeeds.
     */
    /**
     * Adds {@code /decor4fabric axe ...}, the development command that tunes the
     * stored axe's pose live.
     *
     * <p>{@code RegisterClientCommandsEvent} is a NeoForge bus event that fires
     * only on a client and carries the client-side dispatcher, which is what
     * keeps the command off the server entirely.
     */
    private static void registerClientCommands(RegisterClientCommandsEvent event) {
        AxePoseCommand.register(event.getDispatcher());
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                Objects.requireNonNull(Decor4Fabric.sitEntityType(), "sit entity type not registered"),
                SitEntityRenderer::new);
        // Same registration as Fabric's, in NeoForge's spelling. The renderer is
        // a separate class per loader for the same reason the sit renderer is:
        // both are per-dist by definition.
        event.registerBlockEntityRenderer(LogBenchBlockEntity.type(), LogBenchRenderer::new);
    }
}
