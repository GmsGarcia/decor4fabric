package net.gmsgarcia.decor4all.neoforge;

import java.util.Objects;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.blockentity.LogBenchBlockEntity;
import net.gmsgarcia.decor4all.content.DecorBlocks;
import net.gmsgarcia.decor4all.neoforge.client.LogBenchRenderer;
import net.gmsgarcia.decor4all.neoforge.client.SitEntityRenderer;
import net.gmsgarcia.decor4all.neoforge.client.CarpenterTableClientRecipes;
import net.gmsgarcia.decor4all.neoforge.client.CarpenterTableScreen;
import net.gmsgarcia.decor4all.net.CarpenterTableRecipesPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.minecraft.client.gui.screens.MenuScreens;

// NeoForge entrypoint: the only loader-specific part of the mod.
@Mod(Decor4All.MOD_ID)
public class NeoForgeDecor4All {

    public NeoForgeDecor4All(IEventBus eventBus) {
        NeoForgeContentRegistrar registrar = new NeoForgeContentRegistrar();
        Decor4All.init(registrar);
        registrar.attach(eventBus);

        eventBus.addListener(NeoForgeDecor4All::addCarpenterTableToVanillaTab);
        eventBus.addListener(NeoForgeDecor4All::onClientSetup);
        eventBus.addListener(NeoForgeDecor4All::registerRenderers);
        eventBus.addListener(NeoForgeDecor4All::registerPayloads);
    }

    // Puts the carpentry table item into minecraft:functional_blocks.
    private static void addCarpenterTableToVanillaTab(BuildCreativeModeTabContentsEvent event) {
        if (DecorBlocks.CARPENTER_TABLE_TAB.equals(event.getTabKey())) {
            event.accept(Decor4All.carpenterTableItem());
        }
    }

    // NeoForge's counterpart to Fabric's ClientModInitializer.
    private static void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(Decor4All.carpenterTableMenuType(), CarpenterTableScreen::new);
        Decor4All.LOGGER.info("Decor4All client ready");
    }

    // Teaches NeoForge how to decode the carpentry table's recipe-list payload.
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(Decor4All.MOD_ID)
                .playToClient(CarpenterTableRecipesPayload.TYPE, CarpenterTableRecipesPayload.CODEC,
                        (payload, context) -> CarpenterTableClientRecipes.accept(payload));
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                Objects.requireNonNull(Decor4All.sitEntityType(), "sit entity type not registered"),
                SitEntityRenderer::new);
        // Same registration as Fabric's, in NeoForge's spelling. The renderer is
        // a separate class per loader for the same reason the sit renderer is:
        // both are per-dist by definition.
        event.registerBlockEntityRenderer(LogBenchBlockEntity.type(), LogBenchRenderer::new);
    }
}
