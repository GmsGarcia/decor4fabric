package net.gmsgarcia.decor4fabric.sit;

import java.util.Optional;
import net.gmsgarcia.decor4fabric.content.BlockFamilies;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The mount a player rides while sitting, and nothing else.
 *
 * <p>1.18.2's {@code SitEntity} existed for the same reason this one does: there
 * is no way to tell Minecraft that a player is sitting, so the player is made a
 * passenger of something and that something is placed on the seat. What 1.18.2
 * got wrong is the bookkeeping around it, and every one of those defects is
 * fixed here rather than reproduced:
 *
 * <ul>
 *   <li><b>Occupancy lived in a static {@code HashMap}.</b> It was per-JVM, not
 *       per-level and not per-save, so a seat occupied in one world was occupied
 *       in every world, the state did not survive a restart, and the map leaked
 *       an entry per sit forever. Here the seat is the block's
 *       {@link BlockFamilies#OCCUPIED} property, which is per-position, saved
 *       with the chunk, and freed by the block itself. The marker only has to
 *       know which block it belongs to.
 *   <li><b>Cleanup used the wrong key.</b> {@code sit} stored the
 *       <em>player's</em> {@code BlockPos} as the map value and the
 *       <em>block's</em> corner as the key, then {@code remove} looked itself up
 *       by {@code getPos()}, which is a {@code Vec3d} of the seat, not a
 *       {@code BlockPos} at all. So the map entry was never removed, and the
 *       block stayed occupied for the rest of the session.
 *   <li><b>Removal was not idempotent.</b> {@code updatePassengerForDismount}
 *       called {@code remove} and {@code remove} also ran on discard, so a seat
 *       could be released twice. {@link #released} makes release happen once.
 *   <li><b>{@code sitMain} registered a fresh {@code UseBlockCallback} on every
 *       right-click,</b> so the number of handlers grew with the number of
 *       clicks. Sitting is now a method call from the block's own
 *       {@code useWithoutItem}, and no event exists.
 * </ul>
 *
 * <p><b>Why an entity at all, rather than freezing the player in place.</b>
 * Being a passenger is what makes dismounting work for free: sneak and jump
 * dismount in vanilla, {@code LivingEntity} applies the dismount location, the
 * client knows to draw the player sitting, and every other mod that inspects
 * {@code player.getVehicle()} sees something sensible. Reimplementing that means
 * subscribing to movement and input packets, which the loader seam would then
 * have to cover twice.
 *
 * <p>The entity is deliberately inert: no physics, no gravity, not pickable, not
 * pushable, immune to damage, and it never submits anything to the renderer. It
 * exists to be ridden and to hold a {@code BlockPos}.
 */
public class SitEntity extends Entity {

    private static final String KEY_SEAT_X = "SeatX";
    private static final String KEY_SEAT_Y = "SeatY";
    private static final String KEY_SEAT_Z = "SeatZ";
    private static final String KEY_ANCHOR_X = "AnchorX";
    private static final String KEY_ANCHOR_Y = "AnchorY";
    private static final String KEY_ANCHOR_Z = "AnchorZ";

    /** The block this marker occupies. Null only before {@link #attach} runs. */
    private @Nullable BlockPos seatPos;

    /** Where the player was standing when they sat down; where they get put back. */
    private @Nullable BlockPos anchorPos;

    /**
     * Whether {@link #releaseSeat} has already run.
     *
     * <p>Necessary rather than defensive: dismounting runs
     * {@link #getDismountLocationForPassenger} and then removes the entity, and
     * an entity can also be discarded by a chunk unload or by a world save in
     * the same tick. All of those funnel through {@link #remove}, so without
     * this the block would be written twice and the second write would be the
     * one that resurrects a block that has since been replaced.
     */
    private boolean released;

    /**
     * The only constructor, because {@link EntityType.EntityFactory} is a
     * two-argument method reference and nothing may build this any other way.
     */
    public SitEntity(EntityType<? extends SitEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /**
     * Records the two positions this marker needs.
     *
     * @param seat the block being occupied
     * @param anchor the block the player was standing in, for the dismount
     */
    public void attach(BlockPos seat, BlockPos anchor) {
        this.seatPos = seat.immutable();
        this.anchorPos = anchor.immutable();
    }

    /** The block this marker occupies, or null before {@link #attach}. */
    public @Nullable BlockPos seatPos() {
        return this.seatPos;
    }

    /**
     * Releases the seat the moment any of its reasons to exist disappears.
     *
     * <p>This is the whole cleanup story, and it is a tick rather than a set of
     * event handlers. A marker outlives its seat in four ways, all of which end
     * with the block no longer being occupied:
     *
     * <ul>
     *   <li>the player dismounts -- vanilla's own dismount removes the vehicle;
     *   <li>the player logs out or dies -- the passenger goes, so does the ride;
     *   <li>the block is broken or replaced -- {@link #OCCUPIED} is gone with it;
     *   <li>the chunk unloads mid-sit -- the player is saved off the vehicle.
     * </ul>
     *
     * <p>Rather than listen for each of those, this asks the one question whose
     * answer is authoritative: is the block still marked occupied? If not, the
     * marker is stale and removing it is the correct repair. That also means a
     * seat freed by any other means -- a future command, another mod -- is picked
     * up here without this class knowing it happened.
     *
     * <p>Client side there is nothing to check: the server owns {@code OCCUPIED}
     * and replicates it, and the client's own copy of the marker is removed by
     * the server's removal packet.
     */
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        // getPassengers().isEmpty() rather than hasPassenger(...): the
        // no-argument overload was removed, and the question is "is anyone at
        // all aboard" rather than "is this particular entity aboard".
        if (this.seatPos == null || this.getPassengers().isEmpty()) {
            this.discard();
            return;
        }
        BlockState state = this.level().getBlockState(this.seatPos);
        if (!state.hasProperty(BlockFamilies.OCCUPIED) || !state.getValue(BlockFamilies.OCCUPIED)) {
            this.discard();
        }
    }

    /**
     * Puts the passenger back where it started and gives the seat up.
     *
     * <p>Replaces 1.18.2's {@code updatePassengerForDismount}, which vanilla
     * deleted; {@code Entity#getDismountLocationForPassenger} is the hook now, and
     * it is only asked where to put the passenger, so the removal has to be done
     * here as well.
     *
     * <p>1.18.2 returned {@code new Vec3d(pos.getX(), pos.getY(), pos.getZ())}
     * for the block the player was standing in -- the block's <em>corner</em>.
     * That is a legal position and a bad one: it puts the player flush against
     * the edge of whatever they were standing next to, and inside a block if the
     * floor was a slab or a stair. The block's centre is returned instead, which
     * is what every vanilla dismount location does.
     *
     * <p>The marker is discarded before returning rather than left to the caller.
     * Vanilla's dismount does not remove the vehicle -- a boat stays a boat -- so
     * a seat that relied on that would stay occupied forever.
     */
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        BlockPos anchor = this.anchorPos;
        this.discard();
        if (passenger instanceof Player && anchor != null) {
            // Vec3.atCenterOf rather than BlockPos#getCenter: 26.2 dropped the
            // accessor, and this is the spelling that exists on all three
            // targets.
            return Vec3.atCenterOf(anchor);
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    /**
     * Clears {@code OCCUPIED}, once, for every route out of this entity.
     *
     * <p>Overridden rather than handled per call site because the removal reasons
     * are not enumerable from here: {@code DISCARDED}, {@code KILLED},
     * {@code UNLOADED_TO_CHUNK} and a chunk unload all reach this, and only the
     * last two are ones the mod chose to cause.
     */
    @Override
    public void remove(RemovalReason reason) {
        this.releaseSeat();
        super.remove(reason);
    }

    private void releaseSeat() {
        if (this.released) {
            return;
        }
        this.released = true;
        BlockPos pos = this.seatPos;
        Level level = this.level();
        if (pos == null || level == null) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        // Only writes when the property is set, so a marker whose block has
        // already been replaced with something that has no OCCUPIED at all --
        // or with air -- does not resurrect it.
        if (state.hasProperty(BlockFamilies.OCCUPIED) && state.getValue(BlockFamilies.OCCUPIED)) {
            level.setBlock(pos, state.setValue(BlockFamilies.OCCUPIED, false), Block.UPDATE_ALL);
        }
    }

    /**
     * Saved, unlike 1.18.2's empty pair of methods.
     *
     * <p>A seat that cannot be written to disk is a seat that can be lost: a
     * {@code noSave} marker disappears when its chunk unloads while the player
     * is still sitting, and the block is left {@code OCCUPIED} with nothing to
     * clear it, so that bench can never be sat on again until it is broken and
     * replaced. Persisting {@link #seatPos} keeps the marker and the block in
     * agreement across a restart; the tick above is what repairs the two cases
     * where they can still disagree, namely a block that was broken while the
     * chunk was unloaded, and a player who was not saved riding.
     */
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.seatPos != null) {
            output.putInt(KEY_SEAT_X, this.seatPos.getX());
            output.putInt(KEY_SEAT_Y, this.seatPos.getY());
            output.putInt(KEY_SEAT_Z, this.seatPos.getZ());
        }
        if (this.anchorPos != null) {
            output.putInt(KEY_ANCHOR_X, this.anchorPos.getX());
            output.putInt(KEY_ANCHOR_Y, this.anchorPos.getY());
            output.putInt(KEY_ANCHOR_Z, this.anchorPos.getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.seatPos = readPos(input, KEY_SEAT_X, KEY_SEAT_Y, KEY_SEAT_Z);
        this.anchorPos = readPos(input, KEY_ANCHOR_X, KEY_ANCHOR_Y, KEY_ANCHOR_Z);
    }

    /**
     * Reads a position, or null when the key is absent.
     *
     * <p>{@code getIntOr(..., 0)} would answer (0,0,0) for missing data, which is
     * a perfectly valid block and would put a marker on the world spawn's
     * corner. All three components have to be present or the position is not a
     * position.
     */
    private static @Nullable BlockPos readPos(ValueInput input, String x, String y, String z) {
        Optional<Integer> px = input.getInt(x);
        Optional<Integer> py = input.getInt(y);
        Optional<Integer> pz = input.getInt(z);
        if (px.isEmpty() || py.isEmpty() || pz.isEmpty()) {
            return null;
        }
        return new BlockPos(px.get(), py.get(), pz.get());
    }

    /** 1.18.2's empty body, kept because {@code Entity} still declares it abstract. */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    /**
     * Immortal.
     *
     * <p>{@code Entity#hurt} is no longer abstract, but the server-side path is,
     * and a seat that can be destroyed by a cactus leaves a block occupied with
     * nothing riding it until the next tick -- one tick of a bench that refuses
     * to be sat on, for no benefit.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
