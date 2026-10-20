package net.gmsgarcia.decor4all.blocks;

import net.gmsgarcia.decor4all.blockentity.LogBenchBlockEntity;
import net.gmsgarcia.decor4all.content.BlockFamilies;
import net.gmsgarcia.decor4all.sit.Sit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

// The behaviour 1.18.2 wrote out twice, once in logBench and once in logBench2, byte for byte
// apart from the voxel shapes.
public abstract class AxeStoringSeatBlock extends SeatingContainerBlock {

    // Whether taking the axe back also flips the block's FACING.
    protected AxeStoringSeatBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockFamilies.OCCUPIED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(BlockFamilies.OCCUPIED, false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LogBenchBlockEntity(pos, state);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        // An empty hand carries no axe, so it would fall through to the
        // "already stored" test below and answer PASS whenever the bench holds
        // an axe -- and PASS never reaches useWithoutItem, where taking the axe
        // back lives. TRY_WITH_EMPTY_HAND is the only value the game mode
        // forwards to useWithoutItem (and only for the main hand), so the empty
        // hand has to claim nothing here. Reaching the axe branches with an
        // empty hand was impossible in 1.18.2 because its single onUse branched
        // on the hand first.
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        // 1.18.2 read "already occupied" from AXE_TYPE; here it is read from the
        // slot, which is the same fact and cannot disagree with what is drawn.
        // A bench holding an axe refuses a held item with PASS: the axe comes
        // back on an empty hand only (see useWithoutItem below), and a player
        // holding something may not sit down until it does. So the full-hand sit
        // is reserved for a bench whose slot is free.
        LogBenchBlockEntity bench = benchAt(level, pos);
        if (bench != null && !bench.isEmpty()) {
            return InteractionResult.PASS;
        }
        // 1.18.2's second branch was "hand not empty and AXE_TYPE == 0", with no
        // sneak check and no item check, and it ended in a bare SUCCESS. So
        // right-clicking a bare bench with a stick -- or with a block, which also
        // lands here -- consumed the click and did nothing, and the bench could
        // not be sat on while holding anything because useWithoutItem, where the
        // sit branch lives, is only consulted after TRY_WITH_EMPTY_HAND. An axe
        // still never reaches trySit: it is stored right here. Sneaking was
        // never consulted, so it is not consulted here -- though a sneaking
        // player with an item never reaches this method at all: the game mode
        // skips block interaction for that click.
        if (!isAxe(stack)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        storeAxe(state, level, pos, player, hand, stack);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        LogBenchBlockEntity bench = benchAt(level, pos);
        if (bench != null && !bench.isEmpty()) {
            takeAxeBack(state, level, pos, player);
            return InteractionResult.SUCCESS;
        }
        // 1.18.2: `+ 0.17D` for both logBench and logBench2. Reached there
        // from the global callback by tag; reached here because the block is
        // the one asking.
        return Sit.trySit(player, level, pos, Sit.BENCH_HEIGHT);
    }

    // 1.18.2's storeAxe plus the two lines that followed it in onUse.
    private void storeAxe(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, ItemStack stack) {
        player.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
        if (level.getBlockEntity(pos) instanceof LogBenchBlockEntity stored) {
            // Before the slot write, so the one update packet setItem sends
            // carries the direction as well as the axe.
            stored.setAxeFacing(axeFacingFor(pos, state, player));
            stored.setItem(0, stack.copyWithCount(1));
        }
        player.getItemInHand(hand).shrink(1);
    }

    // Which way an axe stored by player should point. It never latches along the log.
    private static Direction axeFacingFor(BlockPos pos, BlockState state, Player player) {
        Direction facing = state.getValue(FACING);
        double dx = player.getX() - (pos.getX() + 0.5D);
        double dz = player.getZ() - (pos.getZ() + 0.5D);
        // Equal magnitudes are the diagonal, which cannot happen for a player
        // who clicked this block's own hitbox, so either side of the tie-break
        // is unreachable and the >= is only there to pick one deterministically.
        Direction towardPlayer;
        if (Math.abs(dx) >= Math.abs(dz)) {
            towardPlayer = dx > 0.0D ? Direction.EAST : Direction.WEST;
        } else {
            towardPlayer = dz > 0.0D ? Direction.SOUTH : Direction.NORTH;
        }
        if (towardPlayer.getAxis() == facing.getAxis()) {
            // Same half-turn mirror as the end branch below: on the
            // east/west-facing benches the sprite composes the other way, so
            // the axe reads at the player with towardPlayer itself there,
            // where the north/south-facing benches read
            // towardPlayer.getOpposite().
            return facing.getAxis() == Direction.Axis.X
                    ? towardPlayer
                    : towardPlayer.getOpposite();
        }
        // An end click latches the nearest not-end face so the axe still reads
        // toward whichever half of the log the player is on. The signed
        // distance from the log's centre line along the block's own axis --
        // positive on the side FACING points to -- picks the half the player
        // is on. Which latched value reads toward it flips with the log's
        // axis: the sprite's half turns compose the same way on the
        // north/south-facing benches but mirror on the east/west-facing ones,
        // so FACING itself does there what FACING.getOpposite() does here.
        // Dead centre, exactly no side, falls through to FACING on the
        // north/south-facing benches and to FACING.getOpposite() on the
        // east/west-facing ones; the "even slightly to one side" case is what
        // decides the direction.
        double across = facing.getStepX() * dx + facing.getStepZ() * dz;
        boolean readsFacing = (across > 0.0D) == (facing.getAxis() == Direction.Axis.X);
        return readsFacing ? facing : facing.getOpposite();
    }

    // 1.18.2's third branch, which cleared AXE_TYPE and played its sound unconditionally and only
    // handed the axe back if the slot was non-empty.
    private void takeAxeBack(BlockState state, Level level, BlockPos pos, Player player) {
        player.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
        if (level.getBlockEntity(pos) instanceof LogBenchBlockEntity bench) {
            ItemStack axe = bench.removeItemNoUpdate(0);
            if (!axe.isEmpty()) {
                // ServerPlayerGameMode.useItemOn only reaches useWithoutItem when
                // the block answered TryEmptyHandInteraction *and* hand ==
                // MAIN_HAND, and a bench holding an axe answers PASS to any
                // non-empty stack (see useItemOn) -- so the main-hand slot is
                // empty here by the time this runs. The axe goes straight into
                // it; there is nothing to fall back from, and no reason to route
                // it through the inventory.
                player.setItemInHand(InteractionHand.MAIN_HAND, axe);
            }
        }
    }

    // Whether a stack should be accepted onto a bench.
    private static boolean isAxe(ItemStack stack) {
        return stack.is(ItemTags.AXES);
    }

    private static @Nullable LogBenchBlockEntity benchAt(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof LogBenchBlockEntity bench ? bench : null;
    }
}