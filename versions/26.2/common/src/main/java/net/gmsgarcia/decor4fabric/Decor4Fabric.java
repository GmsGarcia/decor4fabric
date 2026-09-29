package net.gmsgarcia.decor4fabric;

import java.util.ArrayList;
import java.util.List;
import net.gmsgarcia.decor4fabric.content.DecorBlocks;
import net.gmsgarcia.decor4fabric.content.DecorBlocks.BlockEntityEntry;
import net.gmsgarcia.decor4fabric.content.DecorBlocks.Entry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

        LOGGER.info("Queued {} blocks, {} block items, {} block entity types and {} creative tabs",
                blocks, items, blockEntities, tabs);
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
}
