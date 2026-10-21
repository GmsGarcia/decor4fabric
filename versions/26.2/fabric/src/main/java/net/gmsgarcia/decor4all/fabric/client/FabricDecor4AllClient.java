package net.gmsgarcia.decor4all.fabric.client;

import java.util.Objects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.blockentity.LogBenchBlockEntity;
import net.gmsgarcia.decor4all.net.CarpenterTableRecipesPayload;
import net.minecraft.client.gui.screens.MenuScreens;

// Fabric client entrypoint, replacing 1.18.2's clientDecor.
public class FabricDecor4AllClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // The type instance, not the key: EntityRendererRegistry.register takes
        // an EntityType on every supported Fabric API version. Client
        // initialisers run after mod initialisers, so by now the common
        // entrypoint has built and registered it; requireNonNull turns the
        // alternative into a named failure rather than a bare NPE if the
        // ordering ever changes.
        EntityRendererRegistry.register(
                Objects.requireNonNull(Decor4All.sitEntityType(), "sit entity type not registered"),
                SitEntityRenderer::new);
        // The bench's own axe, drawn from the bench's own slot. Resolved from the
        // registry for the same reason LogBenchBlockEntity resolves its own type
        // there: there is no static field to hand out, on either loader.
        BlockEntityRendererRegistry.register(LogBenchBlockEntity.type(), LogBenchRenderer::new);
        MenuScreens.register(Decor4All.carpenterTableMenuType(), CarpenterTableScreen::new);

        // The recipe list is server-only data the client cannot derive, so the type
        // has to be known here or the packet is undecodable on arrival. The codec
        // is the sending side's, which is what makes the two agree.
        //
        // The context parameter is discarded rather than used: it carries the sending
        // player, which for a server-driven list this size is not something the
        // client needs to authorise anything.
        PayloadTypeRegistry.clientboundPlay().register(CarpenterTableRecipesPayload.TYPE, CarpenterTableRecipesPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(CarpenterTableRecipesPayload.TYPE,
                (payload, context) -> CarpenterTableClientRecipes.accept(payload));

        Decor4All.LOGGER.info("Decor4All client ready");
    }
}