package net.gmsgarcia.decor4fabric;

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
    public void blockEntityType(String path, BlockEntitySupplier<?> factory, Supplier<Block[]> validBlocks) {
        // Resolved now rather than inside the factory: everything is registered
        // already, and BlockEntityType.register needs the array immediately.
        //
        // 26.1 has no BlockEntityType.Builder; the only construction route is the
        // private static register(String, BlockEntitySupplier, Block...), which
        // decor4fabric.classtweaker makes public.
        BlockEntityType.register(path, factory, validBlocks.get());
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
