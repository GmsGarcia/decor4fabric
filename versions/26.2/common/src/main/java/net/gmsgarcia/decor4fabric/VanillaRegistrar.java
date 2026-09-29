package net.gmsgarcia.decor4fabric;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;

/**
 * The {@link ContentRegistrar} used on the loaders whose entrypoint runs before
 * the registry freeze, which is currently only Fabric. It builds each object the
 * moment it is offered and registers it immediately.
 *
 * <p>The {@code path} argument is ignored on this side: {@code Registry.register}
 * derives the id from the {@link ResourceKey}, and a key that disagrees with the
 * path would already have failed to resolve. NeoForge's deferred registrar is
 * the one that needs the bare path.
 */
public final class VanillaRegistrar implements ContentRegistrar {

    @Override
    public void block(String path, ResourceKey<Block> key, Supplier<Block> factory) {
        Registry.register(BuiltInRegistries.BLOCK, key, factory.get());
    }

    @Override
    public void blockEntityType(String path, ResourceKey<BlockEntityType<?>> key, BlockEntitySupplier<?> factory,
            Supplier<Block[]> validBlocks) {
        // Resolved now rather than inside the factory: everything is registered
        // already, and the constructor needs the array immediately.
        //
        // The constructor is the only construction route that works on every
        // target, which is what keeps this file byte-identical across the four
        // common trees as PORTING_PLAN.md section 10.1 requires. Neither
        // BlockEntityType.Builder nor the static
        // register(String, BlockEntitySupplier, Block...) qualifies: 26.1 still
        // has the static register, but 26.2 deleted it outright and published the
        // constructor instead. It is private on 26.1, so the classtweaker widens
        // it; on 26.2 and later it is already public and that entry is a no-op.
        //
        // Set.of rejects a duplicate, which is the one behavioural difference
        // worth naming. The static register it replaced logged a warning for an
        // empty array and said nothing about duplicates. No target here has a
        // duplicate valid block, and a duplicate would be a copy-paste error that
        // ought to fail loudly rather than silently register a short set.
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key,
                new BlockEntityType<>(factory, Set.of(validBlocks.get())));
    }

    @Override
    public void blockItem(String path, ResourceKey<Item> key, Supplier<Item> factory) {
        Registry.register(BuiltInRegistries.ITEM, key, factory.get());
    }

    @Override
    public void tab(String path, ResourceKey<CreativeModeTab> key, Supplier<CreativeModeTab> factory) {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, factory.get());
    }
}
