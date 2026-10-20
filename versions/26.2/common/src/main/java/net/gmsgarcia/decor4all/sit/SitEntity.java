package net.gmsgarcia.decor4all.sit;

import java.util.Optional;
import net.gmsgarcia.decor4all.content.BlockFamilies;
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

// The mount a player rides while sitting, and nothing else.
public class SitEntity extends Entity {

    private static final String KEY_SEAT_X = "SeatX";
    private static final String KEY_SEAT_Y = "SeatY";
    private static final String KEY_SEAT_Z = "SeatZ";
    private static final String KEY_ANCHOR_X = "AnchorX";
    private static final String KEY_ANCHOR_Y = "AnchorY";
    private static final String KEY_ANCHOR_Z = "AnchorZ";

    // The block this marker occupies. Null only before #attach runs.
    private @Nullable BlockPos seatPos;

    // Where the player was standing when they sat down; where they get put back.
    private @Nullable BlockPos anchorPos;

    // Whether #releaseSeat has already run.
    private boolean released;

    // The only constructor, because EntityType.EntityFactory is a two-argument method reference
    // and nothing may build this any other way.
    public SitEntity(EntityType<? extends SitEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    // Records the two positions this marker needs.
    public void attach(BlockPos seat, BlockPos anchor) {
        this.seatPos = seat.immutable();
        this.anchorPos = anchor.immutable();
    }

    // The block this marker occupies, or null before #attach.
    public @Nullable BlockPos seatPos() {
        return this.seatPos;
    }

    // Releases the seat the moment any of its reasons to exist disappears.
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

    // Puts the passenger back where it started and gives the seat up.
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

    // Clears OCCUPIED, once, for every route out of this entity.
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

    // Saved, unlike 1.18.2's empty pair of methods.
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

    // Reads a position, or null when the key is absent.
    private static @Nullable BlockPos readPos(ValueInput input, String x, String y, String z) {
        Optional<Integer> px = input.getInt(x);
        Optional<Integer> py = input.getInt(y);
        Optional<Integer> pz = input.getInt(z);
        if (px.isEmpty() || py.isEmpty() || pz.isEmpty()) {
            return null;
        }
        return new BlockPos(px.get(), py.get(), pz.get());
    }

    // 1.18.2's empty body, kept because Entity still declares it abstract.
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    // Immortal.
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
