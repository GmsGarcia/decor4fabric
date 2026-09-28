package net.gmsgarcia.decor4fabric.neoforge;

import java.util.function.Supplier;
import net.gmsgarcia.decor4fabric.ContentRegistrar;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The NeoForge {@link ContentRegistrar}: four {@link DeferredRegister}s and the
 * order they fire in.
 *
 * <p>NeoForge freezes every registry before it constructs the {@code @Mod} class,
 * so nothing here builds an object. Every factory is queued on a
 * {@code DeferredRegister} and invoked later, during {@code RegisterEvent}, when
 * the registry is writable again. That is why this class exists at all; see
 * {@link ContentRegistrar} for the full argument.
 *
 * <p>Blocks and items share an id space -- {@code oak_log_bench} names both --
 * but they are separate registries, so they get separate {@code DeferredRegister}s.
 * {@link #attach} is what fixes their order: a {@code DeferredRegister} fires in
 * the order it was attached to the bus, so attaching {@link #blocks} before
 * {@link #blockEntityTypes} before {@link #items} is what guarantees every
 * {@code BlockItem} can find its block and every block entity type can validate
 * its valid-block set. Tabs attach last; they only need suppliers, so they are not
 * on the critical path at all.
 *
 * <p>Block entity types are wrapped rather than passed straight through:
 * {@code Decor4Fabric} hands over a supplier for the valid-block array precisely
 * because that array cannot be built until the blocks are in. Here it is resolved
 * at {@code RegisterEvent} time and wrapped again so the array is built once, not
 * once per call.
 */
final class NeoForgeContentRegistrar implements ContentRegistrar {

    private final DeferredRegister<Block> blocks =
            DeferredRegister.create(Registries.BLOCK, Decor4Fabric.MOD_ID);
    private final DeferredRegister<BlockEntityType<?>> blockEntityTypes =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Decor4Fabric.MOD_ID);
    private final DeferredRegister<Item> items =
            DeferredRegister.create(Registries.ITEM, Decor4Fabric.MOD_ID);
    private final DeferredRegister<CreativeModeTab> tabs =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Decor4Fabric.MOD_ID);

    @Override
    public void block(String path, ResourceKey<Block> key, Supplier<Block> factory) {
        blocks.register(path, factory);
    }

    @Override
    public void blockEntityType(String path, BlockEntitySupplier<?> factory, Supplier<Block[]> validBlocks) {
        blockEntityTypes.register(path, () -> {
            // 26.1 dropped BlockEntityType.Builder; the only construction route is
            // the private static register(String, BlockEntitySupplier, Block...),
            // which decor4fabric.classtweaker makes public.
            return BlockEntityType.register(path, factory, validBlocks.get());
        });
    }

    @Override
    public void blockItem(String path, ResourceKey<Item> key, Supplier<Item> factory) {
        items.register(path, factory);
    }

    @Override
    public void tab(String path, ResourceKey<CreativeModeTab> key, Supplier<CreativeModeTab> factory) {
        tabs.register(path, factory);
    }

    /**
     * Binds the four registers to the mod event bus, in the order given.
     *
     * <p>Must run after {@link Decor4Fabric#init(ContentRegistrar)}, which is what
     * queues the factories in the first place.
     */
    void attach(IEventBus eventBus) {
        blocks.register(eventBus);
        blockEntityTypes.register(eventBus);
        items.register(eventBus);
        tabs.register(eventBus);
    }
}
