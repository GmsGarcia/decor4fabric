package net.gmsgarcia.decor4fabric;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;

/**
 * The platform seam between {@link Decor4Fabric}'s content tables and whichever
 * loader is running.
 *
 * <p>Every method takes a {@link Supplier} rather than an already-built
 * instance, and that is the entire point of the interface. The two loaders
 * disagree about <em>when</em> a registry entry may be created:
 *
 * <ul>
 *   <li>Fabric's {@code ModInitializer} runs before the registry freeze, so
 *       {@link VanillaRegistrar} can build and register each object inline.
 *   <li>NeoForge freezes the registries <em>before</em> it constructs the
 *       {@code @Mod} class, so building a block there fails outright:
 *       {@code Block}'s constructor claims an intrusive registry holder and
 *       {@code MappedRegistry.validateWrite} throws
 *       {@code IllegalStateException: Registry is already frozen}. Its
 *       {@code DeferredRegister}s have to construct the object later, during
 *       {@code RegisterEvent}.
 * </ul>
 *
 * <p>Deferring <em>construction</em>, not merely the {@code Registry.register}
 * call, is what lets one common bootstrap serve both loaders.
 *
 * <p><b>Ordering contract:</b> implementations must construct all blocks before
 * any block entity type, then all items, then the creative tabs.
 *
 * <ul>
 *   <li>A block entity type validates its valid-block set at construction, so it
 *       has to come after every block.
 *   <li>A block item is built around its block.
 *   <li>A tab's icon and contents are read lazily, after everything is
 *       registered.
 * </ul>
 *
 * <p>That last list is a contract about <em>invocation</em> order, not about the
 * order the methods are called in. {@link Decor4Fabric} calls all of them up
 * front, while nothing is registered yet; what matters is that a deferred
 * implementation resolves its suppliers in the order above when the event fires.
 *
 * <p>Implementations do not return the registered instance and the caller does
 * not try to keep one: {@link Decor4Fabric} resolves blocks back out of
 * {@code BuiltInRegistries} by key, which works on NeoForge too, where the
 * instances do not exist yet while {@code init} runs.
 *
 * <p>That is also why {@link #blockEntityType} takes a
 * {@code Supplier<Block[]>} rather than a {@code Block[]}. The valid-block sets
 * are 16 and 22 blocks and have to be read out of the block registry, which on
 * NeoForge is still empty when {@code init} returns. A supplier lets Fabric
 * resolve it immediately and NeoForge resolve it during {@code RegisterEvent},
 * from identical common code.
 *
 * <p>This mirrors compress-em's seam with two additions: a
 * {@link #blockEntityType} method, because 1.18.2's two block entity types are
 * part of the frozen id list and have to move through the same seam as
 * everything else, and its valid-block argument, because 26.1 dropped
 * {@code BlockEntityType.Builder} and the only remaining construction route is the
 * static {@code BlockEntityType.register(String, BlockEntitySupplier, Block...)}.
 */
public interface ContentRegistrar {

    void block(String path, ResourceKey<Block> key, Supplier<Block> factory);

    void blockEntityType(String path, BlockEntitySupplier<?> factory, Supplier<Block[]> validBlocks);

    void blockItem(String path, ResourceKey<Item> key, Supplier<Item> factory);

    void tab(String path, ResourceKey<CreativeModeTab> key, Supplier<CreativeModeTab> factory);
}
