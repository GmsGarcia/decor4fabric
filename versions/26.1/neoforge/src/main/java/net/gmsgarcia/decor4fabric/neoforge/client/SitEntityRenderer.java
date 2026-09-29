package net.gmsgarcia.decor4fabric.neoforge.client;

import net.gmsgarcia.decor4fabric.sit.SitEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Draws nothing, and is the point.
 *
 * <p>1.18.2 had a {@code SitEntityRenderer} too, and it had real content: it
 * iterated {@code passengers}, drew a name tag for each, and so on. All of that
 * is now vanilla. A riding player is drawn as a passenger of the vehicle by
 * {@code LivingEntityRenderer}, the marker's own {@code isInvisible} is never
 * consulted because {@link #shouldRender} short-circuits first, and the nametag
 * logic has no input to work with in any case.
 *
 * <p>So the correct renderer is one that refuses to draw, and the interesting
 * question is not what to remove but how little has to be left. A client that
 * registers nothing at all for an {@code EntityType} gets a crash -- the client
 * builds its own registry view from the server's and has no renderer to fall
 * back on -- so a do-nothing class is required, not optional.
 *
 * <p>The class is duplicated per loader rather than shared in
 * {@code common}: it imports {@code net.minecraft.client}, which the dedicated
 * server jar does not ship, and the common source set is compiled into both
 * loaders' jars. The two copies are identical apart from the package name, and
 * that is verified rather than assumed -- {@code EntityRenderer}'s abstract
 * surface is {@code createRenderState()} alone on all three targets.
 */
public class SitEntityRenderer extends EntityRenderer<SitEntity, EntityRenderState> {

    /**
     * Public because Fabric and NeoForge both hold this as a
     * {@code EntityRendererProvider}, which is invoked as a method reference.
     */
    public SitEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        // A 0.001F marker inside a 0.001F-tall shadow is a dot of block, and the
        // vanilla default of 0.5F would paint a full half-block shadow under a
        // bench that has already got one.
        this.shadowRadius = 0.0F;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Vanilla's {@code EntityRenderState} is concrete and already carries
     * every field the base class reads, so there is nothing to extend. The
     * alternative -- a nested subclass -- would only be needed if the marker
     * needed to carry state through extraction, and it carries none: it renders
     * nothing, so it has nothing to remember.
     */
    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    /**
     * Always false, so the marker is skipped before the dispatcher does any work
     * at all.
     *
     * <p>Overriding this is what makes the class genuinely inert rather than
     * merely empty. The base implementation compares the entity's bounding box
     * against the frustum, and a marker parked at seat height inside a bench
     * <em>would</em> pass that test whenever the bench itself is visible, so the
     * renderer would then run, and the default {@code submit} would then submit
     * the shadow. Refusing here is also the earliest point at which it can be
     * refused, which is why the 0.001F size is belt-and-braces rather than the
     * mechanism.
     */
    @Override
    public boolean shouldRender(SitEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return false;
    }
}
