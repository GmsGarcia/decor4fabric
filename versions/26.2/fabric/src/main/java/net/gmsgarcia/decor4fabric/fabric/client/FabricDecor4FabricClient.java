package net.gmsgarcia.decor4fabric.fabric.client;

import java.util.Objects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.blockentity.LogBenchBlockEntity;

/**
 * Fabric client entrypoint, replacing 1.18.2's {@code clientDecor}.
 *
 * <p>That class registered the cutout render layers and render types for the
 * leaves, glass and lantern blocks, and registered the sit entity's renderer.
 * Phase 3 brings the second half back: the entity type itself is registered from
 * common code, but a renderer is per-dist by definition, so it cannot be.
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
        Decor4Fabric.LOGGER.info("Decor4Fabric client ready");
    }
}