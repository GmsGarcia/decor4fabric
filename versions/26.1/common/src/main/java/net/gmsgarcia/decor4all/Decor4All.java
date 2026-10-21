package net.gmsgarcia.decor4all;

import java.util.ArrayList;
import java.util.List;
import net.gmsgarcia.decor4all.content.DecorBlocks;
import net.gmsgarcia.decor4all.content.DecorBlocks.BlockEntityEntry;
import net.gmsgarcia.decor4all.content.DecorBlocks.Entry;
import net.gmsgarcia.decor4all.menu.CarpenterTableMenu;
import net.gmsgarcia.decor4all.recipe.CarpenterTableRecipe;
import net.gmsgarcia.decor4all.sit.SitEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// The loader-neutral bootstrap.
public final class Decor4All {

    public static final String MOD_ID = "decor4all";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // The sit marker, by id.
    public static final String SIT_ENTITY_PATH = "entity_sit";

    public static final ResourceKey<EntityType<?>> SIT_ENTITY_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, DecorBlocks.id(SIT_ENTITY_PATH));

    // The carpentry table menu type path, i.e. decor4all:carpenter_table.
    public static final String CARPENTER_TABLE_MENU_PATH = "carpenter_table";

    // The carpentry table's menu type, by id.
    public static final ResourceKey<MenuType<?>> CARPENTER_TABLE_MENU_KEY =
            ResourceKey.create(Registries.MENU, DecorBlocks.id(CARPENTER_TABLE_MENU_PATH));

    // The carpentry table recipe type path, i.e. decor4all:carpenter_table.
    public static final String CARPENTER_TABLE_RECIPE_PATH = "carpenter_table";

    // The carpentry table recipe type, by id.
    public static final ResourceKey<RecipeType<?>> CARPENTER_TABLE_RECIPE_KEY =
            ResourceKey.create(Registries.RECIPE_TYPE, DecorBlocks.id(CARPENTER_TABLE_RECIPE_PATH));

    // The carpentry table recipe serializer, by id.
    public static final ResourceKey<RecipeSerializer<?>> CARPENTER_TABLE_RECIPE_SERIALIZER_KEY =
            ResourceKey.create(Registries.RECIPE_SERIALIZER, DecorBlocks.id(CARPENTER_TABLE_RECIPE_PATH));

    private static boolean initialised;

    private Decor4All() {
    }

    // Runs once per launch.
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
        int recipeTypes = registerRecipes(registrar);

        LOGGER.info("Queued {} blocks, {} block items, {} block entity types, {} creative tabs, {}"
                + " menu types, {} entity types and {} recipe entries", blocks, items, blockEntities, tabs,
                menuTypes, entityTypes, recipeTypes);
    }

// The mod's one menu type, the carpentry table's.
    private static int registerMenuTypes(ContentRegistrar registrar) {
        registrar.menuType(CARPENTER_TABLE_MENU_PATH, CARPENTER_TABLE_MENU_KEY,
                () -> new MenuType<>((id, inventory) -> new CarpenterTableMenu(id, inventory), FeatureFlags.VANILLA_SET));
        return 1;
    }

    private static int registerBlocks(ContentRegistrar registrar) {
        for (Entry entry : DecorBlocks.ALL) {
            registrar.block(entry.path(), entry.key(), () -> entry.spec().create(entry.key()));
        }
        return DecorBlocks.ALL.size();
    }

    // The valid-block sets are supplied, not computed here: 11 and 22 blocks respectively, and on
    // NeoForge none of them exist yet at this point.
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
            // block items, so the lang key is `block.decor4all.<path>` and
            // not `item.decor4all.<path>`.
            registrar.blockItem(entry.path(), entry.itemKey(),
                    () -> new BlockItem(block(entry), new Item.Properties()
                            .setId(entry.itemKey())
                            .useBlockDescriptionPrefix()));
        }
        return DecorBlocks.ALL.size();
    }

    private static int registerTabs(ContentRegistrar registrar) {
        for (DecorBlocks.TabEntry tab : DecorBlocks.TABS) {
            registrar.tab(tab.path(), tab.key(), () -> DecorBlocks.buildTab(tab, Decor4All::block));
        }
        return DecorBlocks.TABS.size();
    }

    // The mod's one entity type: the sit marker, and nothing else.
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

    // The registered block for a catalogue entry.
    public static Block block(Entry entry) {
        return BuiltInRegistries.BLOCK.getValue(entry.key());
    }

    // The carpentry table recipe type and serializer, by id.
    private static int registerRecipes(ContentRegistrar registrar) {
        registrar.recipeType(CARPENTER_TABLE_RECIPE_PATH, CARPENTER_TABLE_RECIPE_KEY,
                () -> CarpenterTableRecipe.TYPE);
        registrar.recipeSerializer(CARPENTER_TABLE_RECIPE_PATH, CARPENTER_TABLE_RECIPE_SERIALIZER_KEY,
                () -> CarpenterTableRecipe.SERIALIZER);
        return 2;
    }

    public static Block block(String path) {
        return BuiltInRegistries.BLOCK.getValue(blockKey(path));
    }

    public static Item item(Entry entry) {
        return BuiltInRegistries.ITEM.getValue(entry.itemKey());
    }

    // The sit marker, or null if it has not been registered yet.
    @SuppressWarnings("unchecked")
    public static @Nullable EntityType<SitEntity> sitEntityType() {
        return (EntityType<SitEntity>) (EntityType<?>) BuiltInRegistries.ENTITY_TYPE.getValue(SIT_ENTITY_KEY);
    }

    private static ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(Registries.BLOCK, DecorBlocks.id(path));
    }

    // The carpentry table item, for the loader-specific injection into DecorBlocks#CARPENTER_TABLE_TAB.
    public static Item carpenterTableItem() {
        return item(DecorBlocks.CARPENTER_TABLE_ENTRY);
    }

    // The carpentry table menu type, resolved by key on every use.
    @SuppressWarnings("unchecked")
    public static MenuType<CarpenterTableMenu> carpenterTableMenuType() {
        return (MenuType<CarpenterTableMenu>) BuiltInRegistries.MENU.getValue(CARPENTER_TABLE_MENU_KEY);
    }
}
