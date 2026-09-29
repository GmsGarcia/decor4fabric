package net.gmsgarcia.decor4fabric.blocks;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.gmsgarcia.decor4fabric.blockentity.SmallStoolBlockEntity;
import net.gmsgarcia.decor4fabric.content.BlockFamilies;
import net.gmsgarcia.decor4fabric.sit.Sit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * 1.18.2's {@code logSmallStool}: a seat that holds one carpet.
 *
 * <p>Like the bench's axe, the carpet is a display trick. Placing one sets
 * {@link BlockFamilies#WOOL_COLOR}, which the blockstates use to swap in the
 * matching model, and takes one carpet from the player's stack. Sneaking with an
 * empty hand takes it back.
 *
 * <p>{@code WOOL_COLOR} is {@code 0} for "no carpet" and {@code 1..16} in
 * {@link DyeColor} order, so the two ends of the range are a sentinel and a
 * lookup rather than two separate concepts. 1.18.2 spelled that out as sixteen
 * near-identical branches of an if-else chain; a table over the sixteen carpet
 * items gives the same mapping with one branch.
 */
public class SmallStoolBlock extends SeatingContainerBlock {

    public static final MapCodec<SmallStoolBlock> CODEC = BlockBehaviour.simpleCodec(SmallStoolBlock::new);

    private static final VoxelShape SIT = Block.box(3.0D, 6.0D, 3.0D, 13.0D, 9.0D, 13.0D);
    private static final VoxelShape FIRST_LEG = Block.box(4.0D, 0.0D, 4.0D, 6.0D, 6.0D, 6.0D);
    private static final VoxelShape SECOND_LEG = Block.box(4.0D, 0.0D, 10.0D, 6.0D, 6.0D, 12.0D);
    private static final VoxelShape THIRD_LEG = Block.box(10.0D, 0.0D, 4.0D, 12.0D, 6.0D, 6.0D);
    private static final VoxelShape FOURTH_LEG = Block.box(10.0D, 0.0D, 10.0D, 12.0D, 6.0D, 12.0D);

    private static final VoxelShape SHAPE = Shapes.or(SIT, FIRST_LEG, SECOND_LEG, THIRD_LEG, FOURTH_LEG);

    /**
     * {@code wool_color} {@code 1..16} against the carpet that produces it.
     * Indexed by {@code woolColor - 1}.
     *
     * <p>Looked up by id rather than written as {@code Items.WHITE_CARPET} and
     * friends, because those sixteen constants stopped existing in 26.2, which
     * collapsed them into a {@code ColorCollection<Item> Items.CARPET} indexed
     * by {@code DyeColor}. {@code ColorCollection} does not exist at all on 26.1,
     * so neither spelling compiles everywhere, and the two forms cannot be
     * reconciled without a per-target file -- which PORTING_PLAN.md 10.1 forbids.
     *
     * <p>The id is therefore spelled out. {@code getName()} on {@link DyeColor}
     * would generate the same strings but is itself a per-target risk, and a
     * missing carpet now fails at class init with a message naming the id rather
     * than silently colouring the stool wrong.
     */
    private static final List<Item> CARPETS = List.of(
            carpet("white"),
            carpet("orange"),
            carpet("magenta"),
            carpet("light_blue"),
            carpet("yellow"),
            carpet("lime"),
            carpet("pink"),
            carpet("gray"),
            carpet("light_gray"),
            carpet("cyan"),
            carpet("purple"),
            carpet("blue"),
            carpet("brown"),
            carpet("green"),
            carpet("red"),
            carpet("black"));

    /**
     * Resolves {@code minecraft:<color>_carpet}, failing loudly if it is absent.
     *
     * <p>{@code getValue} rather than {@code get}: the latter answers with an
     * {@code Optional<Holder.Reference<Item>>} on every target, so it would need
     * unwrapping, and {@code Registry.get} used to answer with air for an unknown
     * key, which would quietly turn into "colour 0". {@code getValue} is the
     * nullable accessor, so the missing-carpet case is an explicit null here.
     */
    private static Item carpet(String color) {
        Identifier id = Identifier.withDefaultNamespace(color + "_carpet");
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null) {
            throw new IllegalStateException("no carpet item registered under " + id);
        }
        return item;
    }

    public SmallStoolBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends SmallStoolBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockFamilies.WOOL_COLOR, BlockFamilies.OCCUPIED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(BlockFamilies.WOOL_COLOR, 0)
                .setValue(BlockFamilies.OCCUPIED, false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmallStoolBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return SHAPE;
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
        // An empty hand has no carpet, so it would otherwise reach the
        // `woolColor == 0` branch below and answer SUCCESS, consuming the click
        // and leaving useWithoutItem -- where the sit branch lives -- unconsulted.
        // The stool would be decoration. TRY_WITH_EMPTY_HAND is the same value
        // BlockBehaviour.useItemOn returns by default, which is why 1.18.2's
        // single onUse never needed it: it branched on the hand itself.
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        // 1.18.2's second branch: any held item, but only while no carpet is on
        // the stool. It tested sixteen isHolding() calls and then returned SUCCESS
        // unconditionally, so right-clicking with a stick on a bare stool consumed
        // the click without doing anything. Reproduced rather than tidied, because
        // "eats the click" is observable: without it the click falls through to
        // whatever is behind the stool.
        if (state.getValue(BlockFamilies.WOOL_COLOR) != 0) {
            return InteractionResult.PASS;
        }
        int woolColor = woolColorOf(stack.getItem());
        if (woolColor == 0) {
            return InteractionResult.SUCCESS;
        }
        setWoolColor(state, level, pos, player, woolColor);
        if (level.getBlockEntity(pos) instanceof SmallStoolBlockEntity stool) {
            stool.setItem(0, stack.copyWithCount(1));
        }
        player.getItemInHand(hand).shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        // 1.18.2 branched on the hand, not on WOOL_COLOR, and the order mattered.
        // Its first branch was "empty hand and not sneaking" -> SUCCESS, with no
        // carpet check at all, so a stool with a carpet on it could still be sat
        // on; the carpet came off only via the third branch, which required
        // sneaking. Checking the carpet first, as a natural rewrite would, would
        // make a carpeted stool unsittable and is not what shipped. The order is
        // therefore kept, and the sneak test is left in place even though
        // Sit.trySit re-checks it: the two branches need different outcomes, and
        // only the caller knows which.
        if (!player.isSecondaryUseActive()) {
            // 1.18.2: `+ 0.35D`, the same height as a chair.
            return Sit.trySit(player, level, pos, Sit.STOOL_HEIGHT);
        }
        if (state.getValue(BlockFamilies.WOOL_COLOR) != 0) {
            takeCarpetBack(state, level, pos, player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * 1.18.2's {@code placeCarpet}, used for both directions: it played
     * {@code BLOCK_WOOL_PLACE} and set {@code WOOL_COLOR}, keeping {@code FACING}
     * as it was. The take-back path passed {@code 0}, so removing a carpet made the
     * same sound as placing one.
     */
    private void setWoolColor(BlockState state, Level level, BlockPos pos, Player player, int woolColor) {
        player.playSound(SoundEvents.WOOL_PLACE, 1.0F, 1.0F);
        level.setBlock(pos, state.setValue(BlockFamilies.WOOL_COLOR, woolColor), Block.UPDATE_ALL);
    }

    private void takeCarpetBack(BlockState state, Level level, BlockPos pos, Player player) {
        setWoolColor(state, level, pos, player, 0);
        if (level.getBlockEntity(pos) instanceof SmallStoolBlockEntity stool) {
            ItemStack carpet = stool.removeItemNoUpdate(0);
            if (!carpet.isEmpty() && !player.getInventory().add(carpet)) {
                player.drop(carpet, false);
            }
        }
    }

    /**
     * {@code wool_color} for a carpet item, or {@code 0} if it is not a carpet.
     * The table is positional, so this is equivalent to
     * {@code DyeColor.byId(...).getId() + 1} without assuming the two lists stay
     * in step.
     */
    private static int woolColorOf(Item item) {
        if (!(item instanceof BlockItem blockItem)) {
            return 0;
        }
        for (int index = 0; index < CARPETS.size(); index++) {
            if (CARPETS.get(index) == blockItem) {
                return index + 1;
            }
        }
        return 0;
    }

    /** Exposed so Phase 3's model generator can emit one variant per colour. */
    public static DyeColor dyeColorOf(int woolColor) {
        return woolColor == 0 ? null : DyeColor.byId(woolColor - 1);
    }
}
