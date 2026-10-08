package net.gmsgarcia.decor4fabric.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.gmsgarcia.decor4fabric.blockentity.LogBenchBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the axe a log bench is holding, from the bench's own slot.
 *
 * <p>1.18.2 had no renderer at all. Storing an axe set {@code AXE_TYPE} and the
 * generated blockstate swapped in one of six axe models, so which axe was
 * visible was decided by which of six {@code Items.*} constants matched and
 * nothing else could ever be stored. Both halves of that are gone: the axe is
 * whatever is in {@link LogBenchBlockEntity}'s slot, and it is drawn here.
 *
 * <p>The rendering pipeline is not the one 1.18.2 or most tutorials assume. There
 * is no {@code BakedModel} to resolve quads from -- the class does not exist on
 * any supported target -- and {@code ItemRenderer} is gone on 26.x. Instead the
 * render state is filled by {@link net.minecraft.client.renderer.item.ItemModelResolver},
 * the same two calls {@code BrushableBlockRenderer} makes for the brush on a
 * brushable block, which is the closest vanilla analogue: an item held against a
 * block rather than in a hand or an inventory slot.
 *
 * <p>{@code updateForTopItem} takes an {@code ItemOwner}, which is
 * {@code null} here and in every vanilla caller of this overload -- the variant
 * exists precisely for items that have no owning entity.
 */
public class LogBenchRenderer implements BlockEntityRenderer<LogBenchBlockEntity, LogBenchRenderer.State> {

/**
 * The pose: an axe lying across the log and sunk into it, and where it sits
 * relative to the block's centre.
 *
 * <p>These constants are the only visual judgement in the class and the only
 * part that cannot be derived from the API. They are gathered here rather than
 * inline so that tuning the display in game is a one-line change and does not
 * disturb the state plumbing below.
 *
 * <p>There are two rotations, about the two different axes, answering
 * different questions. {@link #AXE_SPIN_DEGREES} turns the axe about the
 * vertical, so it decides which way <em>along</em> the bench the axe lies;
 * {@link #AXE_LEAN_DEGREES} turns it about the remaining horizontal axis,
 * deciding which way it stands within that bearing. Neither replaces the
 * facing yaw -- both compose with it -- which is why only quarter-turn
 * multiples mean anything here.
 *
 * <p>The spin being {@code 90} is what puts the axe <em>across</em> the log
 * rather than along it. The log is drawn by the blockstate, which rotates it by
 * {@code y} = 0/90/180/270 for north/east/south/west, and {@link #yawFor}
 * supplies exactly those values -- so at a spin of zero the two agree and the
 * axe reads as a stick laid on top of the log.
 *
 * <p>All six values below were found by eye, with {@code /decor4fabric axe set}
 * and a block entity renderer in the game, rather than derived. That is worth
 * saying plainly because it inverts the usual expectation: the numbers are not
 * measurements of anything and there is no formula behind them. The one
 * constraint that <em>is</em> structural is the scale, below.
 *
 * <p>Both rotations have a plausible-looking wrong answer nearby, and both
 * wrong answers were tried first. A spin of {@code 45} composes with the facing
 * yaw instead of replacing it, leaving every facing at an odd diagonal; and a
 * lean about {@code x} rather than {@code z} rolls the axe over end to end
 * instead of standing it up, which is unguessable from the axis names because
 * {@code x} is the axis the log itself lies along.
 *
 * <p>The scale is the one number that is easy to get wrong from first
 * principles, and the reason is not discoverable from the API. Vanilla's
 * {@code item/generated} parent <em>does</em> define a {@code fixed} display
 * entry -- {@code rotation [0, 180, 0]}, {@code scale [1, 1, 1]} -- so
 * selecting {@link ItemDisplayContext#FIXED} applies a half turn and no
 * shrinking at all. The sprite arrives <em>a full block across</em>, and
 * {@code 1.0} was drawing a block-sized axe; {@code 0.65} is the eyeballed
 * proportion against the 6-unit-tall log.
 *
     * <p>That vanilla half turn is already accounted for: it is baked into the
     * sprite before we see it, and {@link #yawFor} supplies the quarter turns on
     * top. The two compose, so {@code yawFor(d)} draws the axe along {@code -d}
     * -- an axe latched to the block's facing points out of the back of the
     * bench, which is how 1.18.2's axe models were baked into the rotated
     * blockstate. Anything aiming the axe at a specific direction has to
     * express it in that inverted frame; see
     * {@link net.gmsgarcia.decor4fabric.blocks.AxeStoringSeatBlock}.
 *
 * <p>These are the <em>compiled-in</em> pose, and {@link AxePose} starts from
 * exactly these values. They are package-private rather than private for that
 * reason: {@link AxePose#reset()} reads them, so a session tuned with
 * {@code /decor4fabric axe ...} and then reset lands back on the numbers below
 * rather than on a second copy of them that could drift.
 */
    static final float AXE_SPIN_DEGREES = 90.0F;

    /**
     * The second rotation, about the remaining <em>horizontal</em> axis.
     *
     * <p>Distinct from {@link #AXE_SPIN_DEGREES} on purpose: the spin turns the
     * axe about the vertical, deciding which way along the bench it lies, while
     * this tips it about the other horizontal axis. Applied after the offset
     * translate rather than with the other rotations, so it pivots the axe about
     * its own resting point instead of swinging it around the block centre.
     *
     * <p>{@code z}, not {@code x}; see the class comment for why that is not
     * guessable from the axis names.
     */
    static final float AXE_LEAN_DEGREES = 270.0F;

    /**
     * Where the axe rests, in blocks relative to the block centre, applied
     * after the rotations and so in the axe's own rotated frame.
     *
     * <p>Eyeballed, not derived. An earlier version of this comment worked
     * {@code -0.3125} out of the log's geometry --
     * {@code log_bench_model.json} is two elements spanning {@code x 0..16},
     * {@code y 0..6}, {@code z 5..11}, so the log's vertical middle is 3/16
     * and the block centre is 0.5 -- and that arithmetic was correct but the
     * pose wrong, because it centres the sprite's bounding box and the axe
     * does not read as centred that way. {@code offsetX} is negative even
     * though the log is already centred east-west, which is the same point.
     */
    static final float AXE_OFFSET_X = -0.3F;
    static final float AXE_OFFSET_Y = -0.15F;
    static final float AXE_OFFSET_Z = 0.0F;

    static final float AXE_SCALE = 0.65F;

    /**
     * Both loaders build renderers through a factory that hands over the shared
     * render context, so the constructor has to accept it even though nothing
     * here needs it: the item pipeline is reached through
     * {@link Minecraft#getInstance()} rather than through anything the context
     * provides.
     */
    public LogBenchRenderer(BlockEntityRendererProvider.Context context) {
    }

    /**
     * Per-frame state, rebuilt by {@link #extractRenderState} and reused by
     * {@link #submit}.
     *
     * <p>Nothing is carried in a field on the renderer: one renderer instance
     * serves every bench in the world, so per-block data has to travel through
     * the render state. The facing-derived yaw is stored here rather than read
     * from {@code blockState} in {@code submit} because 26.x removed that field
     * from {@link BlockEntityRenderState}, so a subclass cannot rely on it.
     */
    public static final class State extends BlockEntityRenderState {

        public final ItemStackRenderState axe = new ItemStackRenderState();

        /**
         * Clockwise quarter turns about {@code y} for the direction the axe
         * points, which is the bench's facing unless an axe was stored from one
         * of the log's long sides.
         */
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
        poseStack.mulPose(Axis.YP.rotationDegrees(AxePose.spinDegrees()));
        poseStack.translate(AxePose.offsetX(), AxePose.offsetY(), AxePose.offsetZ());
        // The lean goes last so it pivots the axe about its own resting point
        // rather than swinging it in an arc around the block centre.
        poseStack.mulPose(Axis.ZP.rotationDegrees(AxePose.leanDegrees()));
        float s = AxePose.scale();
        poseStack.scale(s, s, s);
        state.axe.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    /**
     * The {@code y} rotation an axe authored facing north needs for a bench
     * facing each direction.
     *
     * <p>North 0, east 90, south 180, west 270 -- clockwise quarter turns, the
     * same mapping the generated blockstates use, so an axe that used to be baked
     * into the bench model keeps the orientation it had.
     */
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