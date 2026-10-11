package net.gmsgarcia.decor4fabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Stops the sitting feature from being reported as a build-height violation.
 *
 * <p>The bug: {@code ServerPlayer.startRiding} teleports the rider on every mount
 * of anything, which sets {@code awaitingPositionFromClient} until the client
 * acknowledges it. While that field is non-null the guard in
 * {@code handleUseItemOn} fails, and control reaches a final {@code else} that
 * calls {@code sendBuildLimitMessage(true, level.getMaxY())}. That branch is the
 * only one of the six call sites with no height test at all -- it passes
 * {@code getMaxY()} as a display argument -- so the overlay quotes the
 * dimension's build ceiling, 319 in the overworld and 255 in the Nether, no
 * matter where the player or the bench actually is. The message is lying about
 * the reason; the click really was refused for the unrelated
 * {@code awaitingPositionFromClient} half of the condition.
 *
 * <p>This is not specific to this mod. Any mount started from inside a
 * {@code UseItemOn} packet races the same way, because vanilla's own mounts are
 * client-initiated and therefore do not land the teleport while the packet
 * handler is still on the stack.
 *
 * <p>Why a mixin at all: the alternative would be to clear the field
 * reflectively, which desyncs a client that is mid-handshake. Deferring the
 * mount to the next server tick does not work either -- the field is set by
 * {@code startRiding} regardless of when that runs, and the rejection happens on
 * a <em>later</em> packet, not the one that sat you down.
 *
 * <p>Why conditional rather than the blanket no-op the sibling mod at
 * {@code ../Sit} uses: that version swallows the call unconditionally, which
 * also silences the five genuine height checks and the claim-protection refusal.
 * Gating on {@code awaitingPositionFromClient == null} keeps those intact and
 * scopes suppression to the mount race alone.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    /**
     * The pending teleport-ack position, or null when the client is in sync.
     *
     * <p>Read through the mixin rather than cached: it is mutated by the packet
     * handlers on the same object, and the value at the moment this runs is
     * exactly the condition that selected the branch we are standing in.
     */
    @Shadow
    private Vec3 awaitingPositionFromClient;

    /**
     * The one call site in {@code handleUseItemOn} with no height test before it.
     *
     * <p>Verified with {@code javap -c} against the named vanilla jar, per
     * target, rather than copied from the sibling mod. All of 26.1, 26.2 and
     * 26.3 have exactly six call sites; ordinals 0 through 4 are each preceded
     * by a real {@code BlockPos.getY()} comparison against the build bounds,
     * and ordinal 5 is the trailing {@code else}, reached only by the
     * {@code awaitingPositionFromClient} and {@code mayInteract} branches and
     * preceded by nothing but {@code iconst_1; iload 12}.
     *
     * <p>That makes this ordinal load-bearing in a way {@code defaultRequire}
     * cannot fully protect: it catches a changed call-site <em>count</em>, but
     * if Mojang reorders the six while keeping six, ordinal 5 silently re-targets
     * and can land on a genuine height check. Re-run the javap on every
     * Minecraft update, not only when one breaks. The one-line check is
     * {@code sendBuildLimitMessage} call count and the bytecode preceding the
     * last one, in {@code handleUseItemOn}.
     *
     * <p>1.21.11 deliberately has no counterpart. {@code sendBuildLimitMessage}
     * does not exist on that target -- the message is inlined as
     * {@code Component.translatable("build.tooHigh", maxY)} -- and its
     * {@code awaitingPositionFromClient} branch jumps straight to the block
     * resync with no message, so the bug does not exist there to fix.
     */
    @Redirect(
            method = "handleUseItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;sendBuildLimitMessage(ZI)V",
                    ordinal = 5))
    private void decor4fabric$suppressFalseBuildLimitOnMount(
            ServerPlayer player, boolean isTooHigh, int limit) {
        if (this.awaitingPositionFromClient == null) {
            player.sendBuildLimitMessage(isTooHigh, limit);
        }
    }
}
