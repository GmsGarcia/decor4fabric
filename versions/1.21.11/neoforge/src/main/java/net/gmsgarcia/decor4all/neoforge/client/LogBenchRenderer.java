package net.gmsgarcia.decor4all.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.gmsgarcia.decor4all.blockentity.LogBenchBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

// Draws the axe a log bench is holding, from the bench's own slot.
public class LogBenchRenderer implements BlockEntityRenderer<LogBenchBlockEntity, LogBenchRenderer.State> {

// The pose: an axe lying across the log and sunk into it, and where it sits relative to the block's centre.
    static final float AXE_SPIN_DEGREES = 90.0F;

    // The second rotation, about the remaining horizontal axis.
    static final float AXE_LEAN_DEGREES = 270.0F;

    // Where the axe rests, in blocks relative to the block centre, applied after the rotations and
    // so in the axe's own rotated frame.
    static final float AXE_OFFSET_X = -0.3F;
    static final float AXE_OFFSET_Y = -0.15F;
    static final float AXE_OFFSET_Z = 0.0F;

    static final float AXE_SCALE = 0.65F;

    // Both loaders build renderers through a factory that hands over the shared render context, so
    // the constructor has to accept it even though nothing here needs it: the item pipeline is
    // reached through Minecraft#getInstance() rather than through anything the context provides.
    public LogBenchRenderer(BlockEntityRendererProvider.Context context) {
    }

    // Per-frame state, rebuilt by #extractRenderState and reused by #submit.
    public static final class State extends BlockEntityRenderState {

        public final ItemStackRenderState axe = new ItemStackRenderState();

        // Clockwise quarter turns about y for the direction the axe points, which is the bench's
        // facing unless an axe was stored from one of the log's long sides.
        public float yaw;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LogBenchBlockEntity bench, State state, float partialTick,
            Vec3 cameraPos, CrumblingOverlay crumblingOverlay) {
        // Fills blockPos, lightCoords and breakProgress. lightCoords is what
        // submit() hands to the item, so skipping this would render the axe at
        // full brightness in the dark.
        BlockEntityRenderer.super.extractRenderState(bench, state, partialTick, cameraPos, crumblingOverlay);

        ItemStack stack = bench.getItem(0);
        state.axe.clear();
        if (stack.isEmpty()) {
            return;
        }
        // The latched direction wins over the blockstate: it records which way
        // the axe was pointed when it was stored. It is never null for an axe
        // placed since the end-latching rule -- an end click now rounds to the
        // nearest not-end facing -- so the blockstate fallback only covers axes
        // stored before that and is otherwise dead.
        Direction facing = bench.axeFacing() != null
                ? bench.axeFacing()
                : bench.getBlockState()
                        .getOptionalValue(BlockStateProperties.HORIZONTAL_FACING)
                        .orElse(Direction.NORTH);
        state.yaw = yawFor(facing);
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(
                state.axe, stack, ItemDisplayContext.FIXED, bench.getLevel(), null, 0);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState camera) {
        // No super call: submit is abstract on the interface, so unlike
        // extractRenderState there is no default to inherit. The consequence is
        // that the axe does not get the breaking crack overlay vanilla applies to
        // most block entity models, which is an acceptable trade for a decoration
        // that disappears with the block anyway.
        if (state.axe.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        // Block entity renderers are posed at the block's minimum corner, not its
        // centre, which is why every one of them opens with this translation.
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw));
        poseStack.mulPose(Axis.YP.rotationDegrees(AXE_SPIN_DEGREES));
        poseStack.translate(AXE_OFFSET_X, AXE_OFFSET_Y, AXE_OFFSET_Z);
        // The lean goes last so it pivots the axe about its own resting point
        // rather than swinging it in an arc around the block centre.
        poseStack.mulPose(Axis.ZP.rotationDegrees(AXE_LEAN_DEGREES));
        float s = AXE_SCALE;
        poseStack.scale(s, s, s);
        state.axe.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    // The y rotation an axe authored facing north needs for a bench facing each direction.
    private static float yawFor(Direction facing) {
        return switch (facing) {
            case NORTH -> 0.0F;
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}