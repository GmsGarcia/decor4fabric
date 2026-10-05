package net.gmsgarcia.decor4fabric.fabric.client;

import java.util.Objects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.blockentity.LogBenchBlockEntity;
import net.gmsgarcia.decor4fabric.net.WorkBenchRecipesPayload;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * Fabric client entrypoint, replacing 1.18.2's {@code clientDecor}.
 *
 * <p>That class registered the cutout render layers and render types for the
 * leaves, glass and lantern blocks, and registered the sit entity's renderer.
 * Phase 3 brings the second half back: the entity type itself is registered from
 * common code, but a renderer is per-dist by definition, so it cannot be. Phase 4
 * adds the same asymmetry's other half -- the workbench screen and the payload
 * that feeds it, neither of which the common tree can register.
 *
 * <p>Both of those are loader-neutral on this target despite the file living here.
 * Screen registration is vanilla's {@code MenuScreens} on every version this port
 * supports, and the payload's type is registered through Fabric's API only because
 * sending it is not: the menu sends a plain
 * {@code ClientboundCustomPayloadPacket} from common code. See
 * {@link WorkBenchRecipesPayload}.
 */
public class FabricDecor4FabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // The type instance, not the key: EntityRendererRegistry.register takes
        // an EntityType on every supported Fabric API version. Client
        // initialisers run after mod initialisers, so by now the common
        // entrypoint has built and registered it; requireNonNull turns the
        // alternative into a named failure rather than a bare NPE if the
        // ordering ever changes.
        EntityRendererRegistry.register(
                Objects.requireNonNull(Decor4Fabric.sitEntityType(), "sit entity type not registered"),
                SitEntityRenderer::new);
        // The bench's own axe, drawn from the bench's own slot. Resolved from the
        // registry for the same reason LogBenchBlockEntity resolves its own type
        // there: there is no static field to hand out, on either loader.
        BlockEntityRendererRegistry.register(LogBenchBlockEntity.type(), LogBenchRenderer::new);
        // Client-side only: never reaches the server and needs no permission.
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, buildContext) -> AxePoseCommand.register(dispatcher));

        MenuScreens.register(Decor4Fabric.workbenchMenuType(), WorkBenchScreen::new);

        // The recipe list is server-only data the client cannot derive, so the type
        // has to be known here or the packet is undecodable on arrival. The codec
        // is the sending side's, which is what makes the two agree.
        //
        // {@code playS2C} is this target's spelling of the registry; 26.x renamed it
        // to {@code clientboundPlay}. The accessor is the only line in this file
        // that differs across versions for that reason.
        //
        // The context parameter is discarded rather than used: it carries the sending
        // player, which for a server-driven list this size is not something the
        // client needs to authorise anything.
        PayloadTypeRegistry.playS2C().register(WorkBenchRecipesPayload.TYPE, WorkBenchRecipesPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(WorkBenchRecipesPayload.TYPE,
                (payload, context) -> WorkBenchClientRecipes.accept(payload));

        Decor4Fabric.LOGGER.info("Decor4Fabric client ready");
    }
}