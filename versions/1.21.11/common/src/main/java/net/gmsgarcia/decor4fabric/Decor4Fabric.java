package net.gmsgarcia.decor4fabric;

import java.util.ArrayList;
import java.util.List;
import net.gmsgarcia.decor4fabric.content.DecorBlocks;
import net.gmsgarcia.decor4fabric.content.DecorBlocks.BlockEntityEntry;
import net.gmsgarcia.decor4fabric.content.DecorBlocks.Entry;
import net.gmsgarcia.decor4fabric.menu.WorkBenchMenu;
import net.gmsgarcia.decor4fabric.recipe.WorkBenchRecipe;
import net.gmsgarcia.decor4fabric.sit.SitEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The loader-neutral bootstrap.
 *
 * <p>Everything both loaders do identically lives here; the only loader-specific
 * code in the mod is the entrypoint pair. Fabric's {@code ModInitializer} runs
 * before the registry freeze, whereas NeoForge freezes every registry
 * <em>before</em> it constructs the {@code @Mod} class, so from Phase 2 on this
 * class hands each loader a registrar instead of building objects itself.
 *
 * <p>1.18.2's equivalent was {@code mainDecor}, which was Fabric-specific and
 * called the registration classes directly.
 *
 * <p><b>This method is called before anything is registered on either loader</b>
 * -- on Fabric it is early enough to register inline, on NeoForge the queued
 * suppliers will not run until {@code RegisterEvent}. So nothing here may touch
 * a registered object. Every reference to a {@link Block} is written as
 * {@link #block(Entry)} <em>inside</em> a supplier, and the block entity
 * valid-block sets are handed over as a supplier too. That is what lets the same
 * common code serve both loaders, and why the counts logged at the end are
 * catalogue sizes rather than live registry sizes.
 */
public final class Decor4Fabric {

    public static final String MOD_ID = "decor4fabric";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * The sit marker, by id.
     *
     * <p>1.18.2's id was {@code entity_sit}, and it is kept: the type is written
     * into every sitting player's save data, so a different id would orphan the
     * markers in any existing world. {@code EntityType#saveAsPassenger} writes
     * the id, and an unknown id deserialises to nothing.
     */
    public static final String SIT_ENTITY_PATH = "entity_sit";

    public static final ResourceKey<EntityType<?>> SIT_ENTITY_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, DecorBlocks.id(SIT_ENTITY_PATH));

    /** The workbench menu type path, i.e. {@code decor4fabric:workbench}. */
    public static final String WORKBENCH_MENU_PATH = "workbench";

    /**
     * The workbench's menu type, by id.
     *
     * <p>Its path is the same {@code workbench} as the block and item, in a
     * different registry, which is 1.18.2's arrangement: {@code screenRegistry}
     * registered the handler under the same id the block and item used rather
     * than under a name of its own. Keeping it means the id in a player's saved
     * menu state still resolves.
     */
    public static final ResourceKey<MenuType<?>> WORKBENCH_MENU_KEY =
            ResourceKey.create(Registries.MENU, DecorBlocks.id(WORKBENCH_MENU_PATH));

    private static boolean initialised;

    private Decor4Fabric() {
    }

    /**
     * Runs once per launch.
     *
     * <p>Guarded because a second call would try to re-register the 166 block ids
     * and throw deep inside the registry freeze, which is a far worse error
     * message than this one.
     *
     * @param registrar the loader's registration seam
     */
    public static void init(ContentRegistrar registrar) {
        if (initialised) {
            throw new IllegalStateException(MOD_ID + " was initialised twice");
        }
        initialised = true;

        int blocks = registerBlocks(registrar);
        int blockEntities = registerBlockEntityTypes(registrar);
        int items = registerItems(registrar);
        int tabs = registerTabs(registrar);
        int menuTypes = registerMenuTypes(registrar);
        int entityTypes = registerEntityTypes(registrar);

        // Referenced so the class initialises and RecipeType.register runs. A
        // recipe type registers itself from its own static initialiser rather
        // than through the ContentRegistrar, because one registry entry is all a
        // recipe type is and RecipeType.register is what every vanilla recipe
        // type uses. The cost of that idiom is that nothing else forces the class
        // to initialise -- instance creation is what normally does it, and
        // instances cannot exist until the datapack is read, which is too late.
        // Without this read, decor4fabric:workbench would be missing from
        // RECIPE_TYPE at world load and every workbench recipe would fail with a
        // "no serializer" error there rather than at mod load. Reading TYPE as a
        // logging argument is what forces initialisation while also putting the
        // resolved type where a developer can see it.
        LOGGER.debug("Workbench recipe type: {}", WorkBenchRecipe.TYPE);

        LOGGER.info("Queued {} blocks, {} block items, {} block entity types, {} creative tabs, {}"
                + " menu types and {} entity types", blocks, items, blockEntities, tabs, menuTypes, entityTypes);
    }

/**
 * The mod's one menu type, the workbench's.
 *
 * <p>Built inside a supplier like every other entry, and registered exactly once
 * by {@link VanillaRegistrar#menuType}.
 *
 * <p><b>The supplier constructs; it must not register.</b> It used to call
 * {@code MenuType.register(WORKBENCH_MENU_PATH, ...)}, which was wrong twice
 * over. {@code MenuType.register} is not a constructor -- javap shows it is
 * {@code new MenuType(supplier, FeatureFlags.VANILLA_SET)} followed by
 * {@code Registry.register(BuiltInRegistries.MENU, name, menuType)}, so it
 * performed a registry write of its own. That write is
 *
 * <ol>
 *   <li>under the wrong id: it is the {@code Registry.register} overload taking a
 *       {@code String}, which routes through {@code withDefaultNamespace}, so the
 *       type landed as {@code minecraft:workbench_menu} while
 *       {@link #WORKBENCH_MENU_KEY} says {@code decor4fabric:...}; and</li>
 *   <li>a duplicate: {@link VanillaRegistrar#menuType} then registered the very
 *       same instance again under the correct key, which throws
 *       {@code IllegalStateException: Adding duplicate value ... to registry}
 *       during mod init. That is where the game died on the first
 *       {@code runClient}.</li>
 * </ol>
 *
 * <p>So the type is built with the public constructor and handed to the
 * registrar, which is the single registry write. {@code FeatureFlags.VANILLA_SET}
 * is not a choice: it is the exact set vanilla's own {@code register} passes, and
 * the alternative would be a menu type with subtly different feature gating.
 *
 * <p>Both the constructor and {@code MenuType.MenuSupplier} are public on
 * 1.21.11, 26.1, 26.2 and 26.3, verified with {@code javap -p} on each, so this
 * needs no access widening at all -- the two {@code MenuType} entries that used
 * to sit in the classtweaker are gone.
 *
 * <p>The factory is a {@code MenuSupplier}, so the two constructors that differ
 * between logical sides -- the client's two-argument
 * {@code WorkBenchMenu(int, Inventory)} and the server's three-argument one with
 * its {@code ContainerLevelAccess} -- are both reachable from the one registered
 * type.
 */
private static int registerMenuTypes(ContentRegistrar registrar) {
    registrar.menuType(WORKBENCH_MENU_PATH, WORKBENCH_MENU_KEY,
            () -> new MenuType<>((id, inventory) -> new WorkBenchMenu(id, inventory), FeatureFlags.VANILLA_SET));
    return 1;
}

    private static int registerBlocks(ContentRegistrar registrar) {
        for (Entry entry : DecorBlocks.ALL) {
            registrar.block(entry.path(), entry.key(), () -> entry.spec().create(entry.key()));
        }
        return DecorBlocks.ALL.size();
    }

    /**
     * The valid-block sets are supplied, not computed here: 16 and 22 blocks
     * respectively, and on NeoForge none of them exist yet at this point.
     */
    private static int registerBlockEntityTypes(ContentRegistrar registrar) {
        for (BlockEntityEntry type : DecorBlocks.BLOCK_ENTITIES) {
            registrar.blockEntityType(type.path(), type.key(), type.factory(), () -> validBlocksFor(type));
        }
        return DecorBlocks.BLOCK_ENTITIES.size();
    }

    private static Block[] validBlocksFor(BlockEntityEntry type) {
        List<Block> blocks = new ArrayList<>();
        for (Entry entry : DecorBlocks.ALL) {
            if (type.accepts().test(entry)) {
                blocks.add(block(entry));
            }
        }
        return blocks.toArray(Block[]::new);
    }

    private static int registerItems(ContentRegistrar registrar) {
        for (Entry entry : DecorBlocks.ALL) {
            // The lookup is inside the factory so it happens when the block is
            // already registered, not now.
            //
            // setId is not optional. The Item constructor calls
            // Properties.effectiveDescriptionId(), which calls itemIdOrThrow(),
            // so a bare `new Item.Properties()` throws
            // NullPointerException("Item id not set") the moment the item is
            // constructed. Vanilla never hits this because every vanilla item
            // goes through Items.registerItem, which fills the id in for you.
            // useBlockDescriptionPrefix matches what that helper does for
            // block items, so the lang key is `block.decor4fabric.<path>` and
            // not `item.decor4fabric.<path>`.
            registrar.blockItem(entry.path(), entry.itemKey(),
                    () -> new BlockItem(block(entry), new Item.Properties()
                            .setId(entry.itemKey())
                            .useBlockDescriptionPrefix()));
        }
        return DecorBlocks.ALL.size();
    }

    private static int registerTabs(ContentRegistrar registrar) {
        for (DecorBlocks.TabEntry tab : DecorBlocks.TABS) {
            registrar.tab(tab.path(), tab.key(), () -> DecorBlocks.buildTab(tab, Decor4Fabric::block));
        }
        return DecorBlocks.TABS.size();
    }

    /**
     * The mod's one entity type: the sit marker, and nothing else.
     *
     * <p>Built inside a supplier for the same reason every block is, and here
     * that is not a formality: {@link EntityType.Builder#build} calls
     * {@code Registry.register} itself, so on NeoForge building this during
     * {@code init} throws {@code "Registry is already frozen"}. The builder is
     * therefore assembled inside the supplier and only the finished type crosses
     * into the registrar.
     *
     * <p>Every option here has a reason, and the ones that look like noise are
     * the ones that stop the marker being noticeable:
     *
     * <ul>
     *   <li>{@code sized(0.001F, 0.001F)} is 1.18.2's
     *       {@code EntityDimensions.fixed(0.001F, 0.001F)}, kept. The marker has
     *       no collision box of its own to be any size; this is the smallest
     *       legal value, which is what keeps it from being a target.
     *   <li>{@code noSummon()} blocks {@code /summon} and the spawn eggs, of
     *       which it would otherwise have one for free. A hand-summoned marker
     *       with no seat would clear its own block on the first tick, or worse,
     *       ride nobody.
     *   <li>{@code updateInterval(1)} makes it tick every tick. The default is
     *       3, which would leave a freed seat marked occupied for up to 150ms
     *       after a dismount -- long enough for a second player to be told the
     *       bench is taken when it is not.
     *   <li>{@code clientTrackingRange(5)} is the vanilla default for a mob of
     *       this size and is stated here only so the choice is on the record.
     *   <li>{@code MobCategory.MISC} is 1.18.2's {@code SpawnGroup.MISC}, renamed
     *       in 1.21. Every target here spells it that way.
     * </ul>
     */
    private static int registerEntityTypes(ContentRegistrar registrar) {
        registrar.entityType(SIT_ENTITY_PATH, SIT_ENTITY_KEY, () -> EntityType.Builder
                .of(SitEntity::new, MobCategory.MISC)
                .sized(0.001F, 0.001F)
                .noSummon()
                .updateInterval(1)
                .clientTrackingRange(5)
                .build(SIT_ENTITY_KEY));
        return 1;
    }

    /** The registered block for a catalogue entry. */
    public static Block block(Entry entry) {
        return BuiltInRegistries.BLOCK.getValue(entry.key());
    }

    public static Block block(String path) {
        return BuiltInRegistries.BLOCK.getValue(blockKey(path));
    }

    public static Item item(Entry entry) {
        return BuiltInRegistries.ITEM.getValue(entry.itemKey());
    }

    public static BlockEntityType<?> blockEntityType(BlockEntityEntry type) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(type.key());
    }

    /**
     * The sit marker, or null if it has not been registered yet.
     *
     * <p>Resolved by key on every use rather than cached, matching
     * {@link #block(Entry)}: on NeoForge the instance does not exist while
     * {@code init} runs, so a field written there would be null forever. The only
     * callers are a right-click and a client renderer registration, both of which
     * are long after every register pass.
     *
     * <p>Typed rather than {@code EntityType<?>} because every caller needs
     * {@code EntityType<SitEntity>}: {@code EntityType} is invariant in its
     * entity parameter, so {@code EntityType<? extends Entity>} would not infer
     * from a {@code EntityRendererProvider<SitEntity>} argument. The unchecked
     * cast is safe because {@link #SIT_ENTITY_KEY} is bound only to the type
     * built by {@code SitEntity::new} in {@link #registerEntityTypes}, and
     * {@code EntityType.Builder} is invariant in its factory argument.
     */
    @SuppressWarnings("unchecked")
    public static @Nullable EntityType<SitEntity> sitEntityType() {
        return (EntityType<SitEntity>) (EntityType<?>) BuiltInRegistries.ENTITY_TYPE.getValue(SIT_ENTITY_KEY);
    }

    private static ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(Registries.BLOCK, DecorBlocks.id(path));
    }

    /**
     * The workbench item, for the loader-specific injection into
     * {@link DecorBlocks#WORKBENCH_TAB}.
     *
     * <p>Every other item in the mod reaches a tab through its {@code Entry}'s
     * {@code tab} field and {@link DecorBlocks#buildTab}, which is common code.
     * The workbench cannot: it belongs to a <em>vanilla</em> tab, and adding to a
     * vanilla tab is an event subscription, which is a different API on each
     * loader. So this one item is resolved by the loaders instead.
     *
     * <p>Safe to call from inside a tab-contents callback on both loaders: the
     * callbacks run long after every registration pass has completed.
     */
    public static Item workbenchItem() {
        return item(DecorBlocks.WORKBENCH_ENTRY);
    }

    /**
     * The workbench menu type, resolved by key on every use.
     *
     * <p>Same reasoning as {@link #block(Entry)}: on NeoForge the instance does
     * not exist while {@code init} runs, so a field written there would be null
     * forever. The two callers are {@link WorkBenchMenu}'s constructor and the
     * block's use handler, both long after every register pass.
     *
     * <p>Typed rather than {@code MenuType<?>}, because {@code MenuType} is
     * invariant in its menu parameter and the constructor calls
     * {@code super(MenuType, int)} on {@link WorkBenchMenu}, which needs exactly
     * {@code MenuType<WorkBenchMenu>}. The cast is safe because
     * {@link #WORKBENCH_MENU_KEY} is bound only to the type built in
     * {@link #registerMenuTypes}.
     */
    @SuppressWarnings("unchecked")
    public static MenuType<WorkBenchMenu> workbenchMenuType() {
        return (MenuType<WorkBenchMenu>) BuiltInRegistries.MENU.getValue(WORKBENCH_MENU_KEY);
    }
}
