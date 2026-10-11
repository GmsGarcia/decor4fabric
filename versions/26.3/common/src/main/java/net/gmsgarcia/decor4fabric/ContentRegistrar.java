package net.gmsgarcia.decor4fabric;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
 * <p>This mirrors compress-em's seam with three additions: a
 * {@link #blockEntityType} method, because 1.18.2's two block entity types are
 * part of the frozen id list and have to move through the same seam as
 * everything else; its valid-block argument, because no supported target has a
 * usable {@code BlockEntityType.Builder}; and {@link #entityType}, added in
 * Phase 3, because the sit marker is the mod's only entity type and it has the
 * same two problems the block entity types had -- it must be registered under
 * its own id, and on NeoForge it cannot be constructed before the freeze. See
 * {@link VanillaRegistrar#blockEntityType} for a construction route that
 * survives every target, and {@link VanillaRegistrar#entityType} for the same
 * point about entity types.
 */
public interface ContentRegistrar {

    void block(String path, ResourceKey<Block> key, Supplier<Block> factory);

    void blockEntityType(String path, ResourceKey<BlockEntityType<?>> key, BlockEntitySupplier<?> factory,
            Supplier<Block[]> validBlocks);

    void blockItem(String path, ResourceKey<Item> key, Supplier<Item> factory);

    void tab(String path, ResourceKey<CreativeModeTab> key, Supplier<CreativeModeTab> factory);

    /**
     * Queues the carpentry table menu type, the first registry added in Phase 4.
     *
     * <p>Needs the seam for the same reason every other entry here does, and the
     * {@link Supplier} argument is load-bearing rather than ceremonial: a
     * {@code MenuType} has to be built around a factory, and on NeoForge that
     * build cannot happen while {@code init} is still running.
     *
     * <p>Wired after the tabs, before the entity type. The ordering is about when
     * things are <em>read</em> rather than dependencies: nothing here reads the
     * menu type during construction, but {@link Decor4Fabric#carpenterTableMenuType()}
     * is resolved by key by the block's use handler and by the screen
     * registration, and both of those are late.
     */
    void menuType(String path, ResourceKey<MenuType<?>> key, Supplier<MenuType<?>> factory);

    /**
     * Queues an entity type, the fourth registry the mod writes to.
     *
     * <p>Wired last in the ordering contract, after the tabs. That is not a
     * dependency -- an entity type reads nothing from the block registries at
     * construction, and the marker reads a block state at runtime rather than at
     * registration -- it is only that the entity is the one entry whose consumer
     * is a client renderer, so it wants the registries it might read to already
     * be settled.
     */
    void entityType(String path, ResourceKey<EntityType<?>> key, Supplier<EntityType<?>> factory);

    /**
     * Queues the carpentry table's recipe type, the fifth registry the mod
     * writes to.
     *
     * <p>Same deferral rationale as every other entry here -- on NeoForge
     * nothing may be built or registered while {@code init} runs -- and it bit
     * harder than most. The type used to register itself from its own static
     * initialiser, and {@code init} forced that initialisation to run by reading
     * the type. On NeoForge the read happened inside the {@code @Mod}
     * constructor, after the freeze, and the self-registration threw
     * {@code "Registry is already frozen"}. Moving the {@code Registry.register}
     * here, and making the owning class's field a bare construction, is what
     * defers it to {@code RegisterEvent}.
     */
    void recipeType(String path, ResourceKey<RecipeType<?>> key, Supplier<RecipeType<?>> factory);

    /**
     * Queues the carpentry table recipe's serializer, the sixth registry the mod
     * writes to.
     *
     * <p>Same as {@link #recipeType}; the serializer is the thing the datapack
     * recipes actually resolve against, so missing it produces the same
     * "no serializer" failure {@link #recipeType} documents for the type.
     */
    void recipeSerializer(String path, ResourceKey<RecipeSerializer<?>> key, Supplier<RecipeSerializer<?>> factory);
}
