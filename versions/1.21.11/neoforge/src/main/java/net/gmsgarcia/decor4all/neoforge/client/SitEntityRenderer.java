package net.gmsgarcia.decor4all.neoforge.client;

import net.gmsgarcia.decor4all.sit.SitEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

// Draws nothing, and is the point.
public class SitEntityRenderer extends EntityRenderer<SitEntity, EntityRenderState> {

    // Public because Fabric and NeoForge both hold this as a EntityRendererProvider, which is
    // invoked as a method reference.
    public SitEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        // A 0.001F marker inside a 0.001F-tall shadow is a dot of block, and the
        // vanilla default of 0.5F would paint a full half-block shadow under a
        // bench that has already got one.
        this.shadowRadius = 0.0F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    // Always false, so the marker is skipped before the dispatcher does any work at all.
    @Override
    public boolean shouldRender(SitEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return false;
    }
}
