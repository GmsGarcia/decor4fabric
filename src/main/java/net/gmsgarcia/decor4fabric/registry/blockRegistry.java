package net.gmsgarcia.decor4fabric.registry;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

// IMPORT MY STUFF
import net.gmsgarcia.decor4fabric.blocks.*;

import net.gmsgarcia.decor4fabric.blocks.repetitive_block_classes.logBench.*;
import net.gmsgarcia.decor4fabric.blocks.repetitive_block_classes.logBench2.*;
import net.gmsgarcia.decor4fabric.blocks.repetitive_block_classes.logSmallStool.*;
import net.gmsgarcia.decor4fabric.blocks.repetitive_block_classes.strippedLogSmallStool.*;

import net.gmsgarcia.decor4fabric.blocks.block_entities.logBench.*;
import net.gmsgarcia.decor4fabric.blocks.block_entities.logBench2.*;
import net.gmsgarcia.decor4fabric.blocks.block_entities.logSmallStool.*;
import net.gmsgarcia.decor4fabric.blocks.block_entities.strippedLogSmallStool.*;


public class blockRegistry {

    public static final String MOD_ID = "decor4fabric";

    /////////////
    // BENCHES //
    /////////////
    public static final Block OAK_LOG_BENCH = new oak_logBench();
    public static final Block BIRCH_LOG_BENCH = new birch_logBench();
    public static final Block SPRUCE_LOG_BENCH = new spruce_logBench();
    public static final Block DARK_OAK_LOG_BENCH = new dark_oak_logBench();
    public static final Block ACACIA_LOG_BENCH = new acacia_logBench();
    public static final Block JUNGLE_LOG_BENCH = new jungle_logBench();
    public static final Block CRIMSON_STEM_BENCH = new crimson_stem_logBench();
    public static final Block WARPED_STEM_BENCH = new warped_stem_logBench();

    public static BlockEntityType<oak_logBench_BlockEntity> OAK_LOG_BENCH_BLOCK_ENTITY;
    public static BlockEntityType<birch_logBench_BlockEntity> BIRCH_LOG_BENCH_BLOCK_ENTITY;
    public static BlockEntityType<spruce_logBench_BlockEntity> SPRUCE_LOG_BENCH_BLOCK_ENTITY;
    public static BlockEntityType<dark_oak_logBench_BlockEntity> DARK_OAK_LOG_BENCH_BLOCK_ENTITY;
    public static BlockEntityType<acacia_logBench_BlockEntity> ACACIA_LOG_BENCH_BLOCK_ENTITY;
    public static BlockEntityType<jungle_logBench_BlockEntity> JUNGLE_LOG_BENCH_BLOCK_ENTITY;
    public static BlockEntityType<crimson_stem_logBench_BlockEntity> CRIMSON_STEM_BENCH_BLOCK_ENTITY;
    public static BlockEntityType<warped_stem_logBench_BlockEntity> WARPED_STEM_BENCH_BLOCK_ENTITY;

    public static final Block OAK_LOG_BENCH_2 = new oak_logBench2();
    public static final Block BIRCH_LOG_BENCH_2 = new birch_logBench2();
    public static final Block SPRUCE_LOG_BENCH_2 = new spruce_logBench2();
    public static final Block DARK_OAK_LOG_BENCH_2 = new dark_oak_logBench2();
    public static final Block ACACIA_LOG_BENCH_2 = new acacia_logBench2();
    public static final Block JUNGLE_LOG_BENCH_2 = new jungle_logBench2();
    public static final Block CRIMSON_STEM_BENCH_2 = new crimson_stem_logBench2();
    public static final Block WARPED_STEM_BENCH_2 = new warped_stem_logBench2();

    public static BlockEntityType<oak_logBench2_BlockEntity> OAK_LOG_BENCH_2_BLOCK_ENTITY;
    public static BlockEntityType<birch_logBench2_BlockEntity> BIRCH_LOG_BENCH_2_BLOCK_ENTITY;
    public static BlockEntityType<spruce_logBench2_BlockEntity> SPRUCE_LOG_BENCH_2_BLOCK_ENTITY;
    public static BlockEntityType<dark_oak_logBench2_BlockEntity> DARK_OAK_LOG_BENCH_2_BLOCK_ENTITY;
    public static BlockEntityType<acacia_logBench2_BlockEntity> ACACIA_LOG_BENCH_2_BLOCK_ENTITY;
    public static BlockEntityType<jungle_logBench2_BlockEntity> JUNGLE_LOG_BENCH_2_BLOCK_ENTITY;
    public static BlockEntityType<crimson_stem_logBench2_BlockEntity> CRIMSON_STEM_BENCH_2_BLOCK_ENTITY;
    public static BlockEntityType<warped_stem_logBench2_BlockEntity> WARPED_STEM_BENCH_2_BLOCK_ENTITY;

    public static final Block OAK_LOG_BENCH_3 = new logBench3();
    public static final Block BIRCH_LOG_BENCH_3 = new logBench3();
    public static final Block SPRUCE_LOG_BENCH_3 = new logBench3();
    public static final Block DARK_OAK_LOG_BENCH_3 = new logBench3();
    public static final Block ACACIA_LOG_BENCH_3 = new logBench3();
    public static final Block JUNGLE_LOG_BENCH_3 = new logBench3();
    public static final Block CRIMSON_STEM_BENCH_3 = new logBench3();
    public static final Block WARPED_STEM_BENCH_3 = new logBench3();

    ////////////
    // STOOLS //
    ////////////
    public static final Block OAK_LOG_SMALL_STOOL = new acacia_logSmallStool();
    public static final Block BIRCH_LOG_SMALL_STOOL = new acacia_logSmallStool();
    public static final Block SPRUCE_LOG_SMALL_STOOL = new acacia_logSmallStool();
    public static final Block DARK_OAK_LOG_SMALL_STOOL = new acacia_logSmallStool();
    public static final Block ACACIA_LOG_SMALL_STOOL = new acacia_logSmallStool();
    public static final Block JUNGLE_LOG_SMALL_STOOL = new acacia_logSmallStool();
    public static final Block CRIMSON_STEM_SMALL_STOOL = new acacia_logSmallStool();
    public static final Block WARPED_STEM_SMALL_STOOL = new acacia_logSmallStool();

    public static BlockEntityType<oak_logSmallStool_BlockEntity> OAK_LOG_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<birch_logSmallStool_BlockEntity> BIRCH_LOG_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<spruce_logSmallStool_BlockEntity> SPRUCE_LOG_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<dark_oak_logSmallStool_BlockEntity> DARK_OAK_LOG_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<acacia_logSmallStool_BlockEntity> ACACIA_LOG_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<jungle_logSmallStool_BlockEntity> JUNGLE_LOG_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<crimson_stem_logSmallStool_BlockEntity> CRIMSON_STEM_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<warped_stem_logSmallStool_BlockEntity> WARPED_STEM_SMALL_STOOL_BLOCK_ENTITY;

    public static final Block STRIPPED_OAK_LOG_SMALL_STOOL = new stripped_oak_logSmallStool();
    public static final Block STRIPPED_BIRCH_LOG_SMALL_STOOL = new stripped_birch_logSmallStool();
    public static final Block STRIPPED_SPRUCE_LOG_SMALL_STOOL = new stripped_spruce_logSmallStool();
    public static final Block STRIPPED_DARK_OAK_LOG_SMALL_STOOL = new stripped_dark_oak_logSmallStool();
    public static final Block STRIPPED_ACACIA_LOG_SMALL_STOOL = new stripped_acacia_logSmallStool();
    public static final Block STRIPPED_JUNGLE_LOG_SMALL_STOOL = new stripped_jungle_logSmallStool();
    public static final Block STRIPPED_CRIMSON_STEM_SMALL_STOOL = new stripped_crimson_stem_logSmallStool();
    public static final Block STRIPPED_WARPED_STEM_SMALL_STOOL = new stripped_warped_stem_logSmallStool();

    public static BlockEntityType<stripped_oak_logSmallStool_BlockEntity> STRIPPED_OAK_LOG_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<stripped_birch_logSmallStool_BlockEntity> STRIPPED_BIRCH_LOG_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<stripped_spruce_logSmallStool_BlockEntity> STRIPPED_SPRUCE_LOG_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<stripped_dark_oak_logSmallStool_BlockEntity> STRIPPED_DARK_OAK_LOG_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<stripped_acacia_logSmallStool_BlockEntity> STRIPPED_ACACIA_LOG_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<stripped_jungle_logSmallStool_BlockEntity> STRIPPED_JUNGLE_LOG_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<stripped_crimson_stem_logSmallStool_BlockEntity> STRIPPED_CRIMSON_STEM_SMALL_STOOL_BLOCK_ENTITY;
    public static BlockEntityType<stripped_warped_stem_logSmallStool_BlockEntity> STRIPPED_WARPED_STEM_SMALL_STOOL_BLOCK_ENTITY;

    ////////////
    // CHAIRS //
    ////////////
    public static final Block SIMPLE_OAK_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_BIRCH_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_SPRUCE_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_DARK_OAK_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_ACACIA_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_JUNGLE_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_CRIMSON_STEM_CHAIR = new simple_logChair();
    public static final Block SIMPLE_WARPED_STEM_CHAIR = new simple_logChair();

    public static final Block SIMPLE_OAK_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_BIRCH_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_SPRUCE_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_DARK_OAK_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_ACACIA_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_JUNGLE_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_CRIMSON_STEM_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_WARPED_STEM_CHAIR_2 = new simple_logChair2();

    public static final Block SIMPLE_STRIPPED_OAK_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_BIRCH_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_ACACIA_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_WARPED_STEM_CHAIR = new simple_logChair();

    public static final Block SIMPLE_STRIPPED_OAK_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_BIRCH_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_ACACIA_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_WARPED_STEM_CHAIR_2 = new simple_logChair2();

    ////////////
    // TABLES //
    ////////////
    public static final Block OAK_LOG_TABLE = new logTable();
    public static final Block BIRCH_LOG_TABLE = new logTable();
    public static final Block SPRUCE_LOG_TABLE = new logTable();
    public static final Block DARK_OAK_LOG_TABLE = new logTable();
    public static final Block ACACIA_LOG_TABLE = new logTable();
    public static final Block JUNGLE_LOG_TABLE = new logTable();
    public static final Block CRIMSON_STEM_TABLE = new logTable();
    public static final Block WARPED_STEM_TABLE = new logTable();

    public static final Block STRIPPED_OAK_LOG_TABLE = new logTable();
    public static final Block STRIPPED_BIRCH_LOG_TABLE = new logTable();
    public static final Block STRIPPED_SPRUCE_LOG_TABLE = new logTable();
    public static final Block STRIPPED_DARK_OAK_LOG_TABLE = new logTable();
    public static final Block STRIPPED_ACACIA_LOG_TABLE = new logTable();
    public static final Block STRIPPED_JUNGLE_LOG_TABLE = new logTable();
    public static final Block STRIPPED_CRIMSON_STEM_TABLE = new logTable();
    public static final Block STRIPPED_WARPED_STEM_TABLE = new logTable();

    ////////////
    // FENCES //
    ////////////
    /*public static final Block OAK_LOG_FENCE = new logFence();
    public static final Block BIRCH_LOG_FENCE = new logFence();
    public static final Block SPRUCE_LOG_FENCE = new logFence();
    public static final Block DARK_OAK_LOG_FENCE = new logFence();
    public static final Block ACACIA_LOG_FENCE = new logFence();
    public static final Block JUNGLE_LOG_FENCE = new logFence();
    public static final Block CRIMSON_STEM_FENCE = new logFence();
    public static final Block WARPED_STEM_FENCE = new logFence();

    public static final Block STRIPPED_OAK_LOG_FENCE = new logFence();
    public static final Block STRIPPED_BIRCH_LOG_FENCE = new logFence();
    public static final Block STRIPPED_SPRUCE_LOG_FENCE = new logFence();
    public static final Block STRIPPED_DARK_OAK_LOG_FENCE = new logFence();
    public static final Block STRIPPED_ACACIA_LOG_FENCE = new logFence();
    public static final Block STRIPPED_JUNGLE_LOG_FENCE = new logFence();
    public static final Block STRIPPED_CRIMSON_STEM_FENCE = new logFence();
    public static final Block STRIPPED_WARPED_STEM_FENCE = new logFence();*/

    public static final ItemGroup seatsItemGroup = FabricItemGroupBuilder.build(new Identifier("decor4fabric", "seats"), () -> new ItemStack(SIMPLE_STRIPPED_OAK_LOG_CHAIR));
    public static final ItemGroup tablesItemGroup = FabricItemGroupBuilder.build(new Identifier("decor4fabric", "tables"), () -> new ItemStack(OAK_LOG_TABLE));
    //public static final ItemGroup fencesItemGroup = FabricItemGroupBuilder.build(new Identifier("decor4fabric", "fences"), () -> new ItemStack(OAK_LOG_FENCE));

    public static final Block WORKBENCH = new workBench();

    public static void registerWorkBench() {
        /* WORKBENCH */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "workbench"), WORKBENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "workbench"), new BlockItem(WORKBENCH, new Item.Settings().group(ItemGroup.DECORATIONS)));

    }

    public static void registerBenches() {

        /* LOG BENCHES 1 */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "oak_log_bench"), OAK_LOG_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "oak_log_bench"), new BlockItem(OAK_LOG_BENCH, new Item.Settings().group(seatsItemGroup)));
        OAK_LOG_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:oak_log_bench", FabricBlockEntityTypeBuilder.create(oak_logBench_BlockEntity::new, OAK_LOG_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "birch_log_bench"), BIRCH_LOG_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "birch_log_bench"), new BlockItem(BIRCH_LOG_BENCH, new Item.Settings().group(seatsItemGroup)));
        BIRCH_LOG_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:birch_log_bench", FabricBlockEntityTypeBuilder.create(birch_logBench_BlockEntity::new, BIRCH_LOG_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "spruce_log_bench"), SPRUCE_LOG_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "spruce_log_bench"), new BlockItem(SPRUCE_LOG_BENCH, new Item.Settings().group(seatsItemGroup)));
        SPRUCE_LOG_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:spruce_log_bench", FabricBlockEntityTypeBuilder.create(spruce_logBench_BlockEntity::new, SPRUCE_LOG_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "dark_oak_log_bench"), DARK_OAK_LOG_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "dark_oak_log_bench"), new BlockItem(DARK_OAK_LOG_BENCH, new Item.Settings().group(seatsItemGroup)));
        DARK_OAK_LOG_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:dark_oak_log_bench", FabricBlockEntityTypeBuilder.create(dark_oak_logBench_BlockEntity::new, DARK_OAK_LOG_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "acacia_log_bench"), ACACIA_LOG_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "acacia_log_bench"), new BlockItem(ACACIA_LOG_BENCH, new Item.Settings().group(seatsItemGroup)));
        ACACIA_LOG_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:acacia_log_bench", FabricBlockEntityTypeBuilder.create(acacia_logBench_BlockEntity::new, ACACIA_LOG_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "jungle_log_bench"), JUNGLE_LOG_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "jungle_log_bench"), new BlockItem(JUNGLE_LOG_BENCH, new Item.Settings().group(seatsItemGroup)));
        JUNGLE_LOG_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:jungle_log_bench", FabricBlockEntityTypeBuilder.create(jungle_logBench_BlockEntity::new, JUNGLE_LOG_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "crimson_stem_bench"), CRIMSON_STEM_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "crimson_stem_bench"), new BlockItem(CRIMSON_STEM_BENCH, new Item.Settings().group(seatsItemGroup)));
        CRIMSON_STEM_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:crimson_stem_bench", FabricBlockEntityTypeBuilder.create(crimson_stem_logBench_BlockEntity::new, CRIMSON_STEM_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warped_stem_bench"), WARPED_STEM_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warped_stem_bench"), new BlockItem(WARPED_STEM_BENCH, new Item.Settings().group(seatsItemGroup)));
        WARPED_STEM_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:warped_stem_bench", FabricBlockEntityTypeBuilder.create(warped_stem_logBench_BlockEntity::new, WARPED_STEM_BENCH).build(null));

        /* LOG BENCHES 2 */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "oak_log_bench_2"), OAK_LOG_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "oak_log_bench_2"), new BlockItem(OAK_LOG_BENCH_2, new Item.Settings().group(seatsItemGroup)));
        OAK_LOG_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:oak_log_bench_2", FabricBlockEntityTypeBuilder.create(oak_logBench2_BlockEntity::new, OAK_LOG_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "birch_log_bench_2"), BIRCH_LOG_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "birch_log_bench_2"), new BlockItem(BIRCH_LOG_BENCH_2, new Item.Settings().group(seatsItemGroup)));
        BIRCH_LOG_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:birch_log_bench_2", FabricBlockEntityTypeBuilder.create(birch_logBench2_BlockEntity::new, BIRCH_LOG_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "spruce_log_bench_2"), SPRUCE_LOG_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "spruce_log_bench_2"), new BlockItem(SPRUCE_LOG_BENCH_2, new Item.Settings().group(seatsItemGroup)));
        SPRUCE_LOG_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:spruce_log_bench_2", FabricBlockEntityTypeBuilder.create(spruce_logBench2_BlockEntity::new, SPRUCE_LOG_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "dark_oak_log_bench_2"), DARK_OAK_LOG_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "dark_oak_log_bench_2"), new BlockItem(DARK_OAK_LOG_BENCH_2, new Item.Settings().group(seatsItemGroup)));
        DARK_OAK_LOG_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:dark_oak_log_bench_2", FabricBlockEntityTypeBuilder.create(dark_oak_logBench2_BlockEntity::new, DARK_OAK_LOG_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "acacia_log_bench_2"), ACACIA_LOG_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "acacia_log_bench_2"), new BlockItem(ACACIA_LOG_BENCH_2, new Item.Settings().group(seatsItemGroup)));
        ACACIA_LOG_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:acacia_log_bench_2", FabricBlockEntityTypeBuilder.create(acacia_logBench2_BlockEntity::new, ACACIA_LOG_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "jungle_log_bench_2"), JUNGLE_LOG_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "jungle_log_bench_2"), new BlockItem(JUNGLE_LOG_BENCH_2, new Item.Settings().group(seatsItemGroup)));
        JUNGLE_LOG_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:jungle_log_bench_2", FabricBlockEntityTypeBuilder.create(jungle_logBench2_BlockEntity::new, JUNGLE_LOG_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "crimson_stem_bench_2"), CRIMSON_STEM_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "crimson_stem_bench_2"), new BlockItem(CRIMSON_STEM_BENCH_2, new Item.Settings().group(seatsItemGroup)));
        CRIMSON_STEM_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:crimson_stem_bench_2", FabricBlockEntityTypeBuilder.create(crimson_stem_logBench2_BlockEntity::new, CRIMSON_STEM_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warped_stem_bench_2"), WARPED_STEM_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warped_stem_bench_2"), new BlockItem(WARPED_STEM_BENCH_2, new Item.Settings().group(seatsItemGroup)));
        WARPED_STEM_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:warped_stem_bench_2", FabricBlockEntityTypeBuilder.create(warped_stem_logBench2_BlockEntity::new, WARPED_STEM_BENCH_2).build(null));

        /* LOG BENCHES 3 */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "oak_log_bench_3"), OAK_LOG_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "oak_log_bench_3"), new BlockItem(OAK_LOG_BENCH_3, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "birch_log_bench_3"), BIRCH_LOG_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "birch_log_bench_3"), new BlockItem(BIRCH_LOG_BENCH_3, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "spruce_log_bench_3"), SPRUCE_LOG_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "spruce_log_bench_3"), new BlockItem(SPRUCE_LOG_BENCH_3, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "dark_oak_log_bench_3"), DARK_OAK_LOG_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "dark_oak_log_bench_3"), new BlockItem(DARK_OAK_LOG_BENCH_3, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "acacia_log_bench_3"), ACACIA_LOG_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "acacia_log_bench_3"), new BlockItem(ACACIA_LOG_BENCH_3, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "jungle_log_bench_3"), JUNGLE_LOG_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "jungle_log_bench_3"), new BlockItem(JUNGLE_LOG_BENCH_3, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "crimson_stem_bench_3"), CRIMSON_STEM_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "crimson_stem_bench_3"), new BlockItem(CRIMSON_STEM_BENCH_3, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warped_stem_bench_3"), WARPED_STEM_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warped_stem_bench_3"), new BlockItem(WARPED_STEM_BENCH_3, new Item.Settings().group(seatsItemGroup)));
    }

    public static void registerStools() {

        /* LOG SMALL STOOL */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "oak_log_small_stool"), OAK_LOG_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "oak_log_small_stool"), new BlockItem(OAK_LOG_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        OAK_LOG_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:oak_log_small_stool", FabricBlockEntityTypeBuilder.create(oak_logSmallStool_BlockEntity::new, OAK_LOG_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "birch_log_small_stool"), BIRCH_LOG_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "birch_log_small_stool"), new BlockItem(BIRCH_LOG_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        BIRCH_LOG_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:birch_log_small_stool", FabricBlockEntityTypeBuilder.create(birch_logSmallStool_BlockEntity::new, BIRCH_LOG_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "spruce_log_small_stool"), SPRUCE_LOG_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "spruce_log_small_stool"), new BlockItem(SPRUCE_LOG_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        SPRUCE_LOG_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:spruce_log_small_stool", FabricBlockEntityTypeBuilder.create(spruce_logSmallStool_BlockEntity::new, SPRUCE_LOG_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "dark_oak_log_small_stool"), DARK_OAK_LOG_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "dark_oak_log_small_stool"), new BlockItem(DARK_OAK_LOG_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        DARK_OAK_LOG_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:dark_oak_log_small_stool", FabricBlockEntityTypeBuilder.create(dark_oak_logSmallStool_BlockEntity::new, DARK_OAK_LOG_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "acacia_log_small_stool"), ACACIA_LOG_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "acacia_log_small_stool"), new BlockItem(ACACIA_LOG_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        ACACIA_LOG_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:acacia_log_small_stool", FabricBlockEntityTypeBuilder.create(acacia_logSmallStool_BlockEntity::new, ACACIA_LOG_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "jungle_log_small_stool"), JUNGLE_LOG_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "jungle_log_small_stool"), new BlockItem(JUNGLE_LOG_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        JUNGLE_LOG_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:jungle_log_small_stool", FabricBlockEntityTypeBuilder.create(jungle_logSmallStool_BlockEntity::new, JUNGLE_LOG_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "crimson_stem_small_stool"), CRIMSON_STEM_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "crimson_stem_small_stool"), new BlockItem(CRIMSON_STEM_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        CRIMSON_STEM_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:crimson_stem_small_stool", FabricBlockEntityTypeBuilder.create(crimson_stem_logSmallStool_BlockEntity::new, CRIMSON_STEM_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warped_stem_small_stool"), WARPED_STEM_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warped_stem_small_stool"), new BlockItem(WARPED_STEM_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        WARPED_STEM_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:warped_stem_small_stool", FabricBlockEntityTypeBuilder.create(warped_stem_logSmallStool_BlockEntity::new, WARPED_STEM_SMALL_STOOL).build(null));

        /* STRIPPED LOG SMALL STOOL */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_oak_log_small_stool"), STRIPPED_OAK_LOG_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_oak_log_small_stool"), new BlockItem(STRIPPED_OAK_LOG_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        STRIPPED_OAK_LOG_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:stripped_oak_log_small_stool", FabricBlockEntityTypeBuilder.create(stripped_oak_logSmallStool_BlockEntity::new, STRIPPED_OAK_LOG_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_birch_log_small_stool"), STRIPPED_BIRCH_LOG_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_birch_log_small_stool"), new BlockItem(STRIPPED_BIRCH_LOG_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        STRIPPED_BIRCH_LOG_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:stripped_birch_log_small_stool", FabricBlockEntityTypeBuilder.create(stripped_birch_logSmallStool_BlockEntity::new, STRIPPED_BIRCH_LOG_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_spruce_log_small_stool"), STRIPPED_SPRUCE_LOG_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_spruce_log_small_stool"), new BlockItem(STRIPPED_SPRUCE_LOG_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        STRIPPED_SPRUCE_LOG_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:stripped_spruce_log_small_stool", FabricBlockEntityTypeBuilder.create(stripped_spruce_logSmallStool_BlockEntity::new, STRIPPED_SPRUCE_LOG_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_dark_oak_log_small_stool"), STRIPPED_DARK_OAK_LOG_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_dark_oak_log_small_stool"), new BlockItem(STRIPPED_DARK_OAK_LOG_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        STRIPPED_DARK_OAK_LOG_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:stripped_dark_oak_log_small_stool", FabricBlockEntityTypeBuilder.create(stripped_dark_oak_logSmallStool_BlockEntity::new, STRIPPED_DARK_OAK_LOG_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_acacia_log_small_stool"), STRIPPED_ACACIA_LOG_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_acacia_log_small_stool"), new BlockItem(STRIPPED_ACACIA_LOG_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        STRIPPED_ACACIA_LOG_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:stripped_acacia_log_small_stool", FabricBlockEntityTypeBuilder.create(stripped_acacia_logSmallStool_BlockEntity::new, STRIPPED_ACACIA_LOG_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_jungle_log_small_stool"), STRIPPED_JUNGLE_LOG_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_jungle_log_small_stool"), new BlockItem(STRIPPED_JUNGLE_LOG_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        STRIPPED_JUNGLE_LOG_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:stripped_jungle_log_small_stool", FabricBlockEntityTypeBuilder.create(stripped_jungle_logSmallStool_BlockEntity::new, STRIPPED_JUNGLE_LOG_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_crimson_stem_small_stool"), STRIPPED_CRIMSON_STEM_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_crimson_stem_small_stool"), new BlockItem(STRIPPED_CRIMSON_STEM_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        STRIPPED_CRIMSON_STEM_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:stripped_crimson_stem_small_stool", FabricBlockEntityTypeBuilder.create(stripped_crimson_stem_logSmallStool_BlockEntity::new, STRIPPED_CRIMSON_STEM_SMALL_STOOL).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_warped_stem_small_stool"), STRIPPED_WARPED_STEM_SMALL_STOOL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_warped_stem_small_stool"), new BlockItem(STRIPPED_WARPED_STEM_SMALL_STOOL, new Item.Settings().group(seatsItemGroup)));
        STRIPPED_WARPED_STEM_SMALL_STOOL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:stripped_warped_stem_small_stool", FabricBlockEntityTypeBuilder.create(stripped_warped_stem_logSmallStool_BlockEntity::new, STRIPPED_WARPED_STEM_SMALL_STOOL).build(null));

    }

    public static void registerChairs() {

        /* SIMPLE LOG CHAIRS */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_oak_log_chair"), SIMPLE_OAK_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_oak_log_chair"), new BlockItem(SIMPLE_OAK_LOG_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_birch_log_chair"), SIMPLE_BIRCH_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_birch_log_chair"), new BlockItem(SIMPLE_BIRCH_LOG_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_spruce_log_chair"), SIMPLE_SPRUCE_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_spruce_log_chair"), new BlockItem(SIMPLE_SPRUCE_LOG_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_dark_oak_log_chair"), SIMPLE_DARK_OAK_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_dark_oak_log_chair"), new BlockItem(SIMPLE_DARK_OAK_LOG_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_acacia_log_chair"), SIMPLE_ACACIA_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_acacia_log_chair"), new BlockItem(SIMPLE_ACACIA_LOG_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_jungle_log_chair"), SIMPLE_JUNGLE_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_jungle_log_chair"), new BlockItem(SIMPLE_JUNGLE_LOG_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_warped_stem_chair"), SIMPLE_WARPED_STEM_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_warped_stem_chair"), new BlockItem(SIMPLE_WARPED_STEM_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_crimson_stem_chair"), SIMPLE_CRIMSON_STEM_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_crimson_stem_chair"), new BlockItem(SIMPLE_CRIMSON_STEM_CHAIR, new Item.Settings().group(seatsItemGroup)));

        /* SIMPLE LOG CHAIRS 2 */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_oak_log_chair_2"), SIMPLE_OAK_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_oak_log_chair_2"), new BlockItem(SIMPLE_OAK_LOG_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_birch_log_chair_2"), SIMPLE_BIRCH_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_birch_log_chair_2"), new BlockItem(SIMPLE_BIRCH_LOG_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_spruce_log_chair_2"), SIMPLE_SPRUCE_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_spruce_log_chair_2"), new BlockItem(SIMPLE_SPRUCE_LOG_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_dark_oak_log_chair_2"), SIMPLE_DARK_OAK_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_dark_oak_log_chair_2"), new BlockItem(SIMPLE_DARK_OAK_LOG_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_acacia_log_chair_2"), SIMPLE_ACACIA_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_acacia_log_chair_2"), new BlockItem(SIMPLE_ACACIA_LOG_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_jungle_log_chair_2"), SIMPLE_JUNGLE_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_jungle_log_chair_2"), new BlockItem(SIMPLE_JUNGLE_LOG_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_warped_stem_chair_2"), SIMPLE_WARPED_STEM_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_warped_stem_chair_2"), new BlockItem(SIMPLE_WARPED_STEM_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_crimson_stem_chair_2"), SIMPLE_CRIMSON_STEM_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_crimson_stem_chair_2"), new BlockItem(SIMPLE_CRIMSON_STEM_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        /* SIMPLE STRIPPED LOG CHAIRS */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_oak_log_chair"), SIMPLE_STRIPPED_OAK_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_oak_log_chair"), new BlockItem(SIMPLE_STRIPPED_OAK_LOG_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_birch_log_chair"), SIMPLE_STRIPPED_BIRCH_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_birch_log_chair"), new BlockItem(SIMPLE_STRIPPED_BIRCH_LOG_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_spruce_log_chair"), SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_spruce_log_chair"), new BlockItem(SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_dark_oak_log_chair"), SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_dark_oak_log_chair"), new BlockItem(SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_acacia_log_chair"), SIMPLE_STRIPPED_ACACIA_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_acacia_log_chair"), new BlockItem(SIMPLE_STRIPPED_ACACIA_LOG_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_jungle_log_chair"), SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_jungle_log_chair"), new BlockItem(SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_warped_stem_chair"), SIMPLE_STRIPPED_WARPED_STEM_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_warped_stem_chair"), new BlockItem(SIMPLE_STRIPPED_WARPED_STEM_CHAIR, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_crimson_stem_chair"), SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_crimson_stem_chair"), new BlockItem(SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR, new Item.Settings().group(seatsItemGroup)));

        /* SIMPLE STRIPPED LOG CHAIRS 2 */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_oak_log_chair_2"), SIMPLE_STRIPPED_OAK_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_oak_log_chair_2"), new BlockItem(SIMPLE_STRIPPED_OAK_LOG_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_birch_log_chair_2"), SIMPLE_STRIPPED_BIRCH_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_birch_log_chair_2"), new BlockItem(SIMPLE_STRIPPED_BIRCH_LOG_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_spruce_log_chair_2"), SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_spruce_log_chair_2"), new BlockItem(SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_dark_oak_log_chair_2"), SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_dark_oak_log_chair_2"), new BlockItem(SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_acacia_log_chair_2"), SIMPLE_STRIPPED_ACACIA_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_acacia_log_chair_2"), new BlockItem(SIMPLE_STRIPPED_ACACIA_LOG_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_jungle_log_chair_2"), SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_jungle_log_chair_2"), new BlockItem(SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_warped_stem_chair_2"), SIMPLE_STRIPPED_WARPED_STEM_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_warped_stem_chair_2"), new BlockItem(SIMPLE_STRIPPED_WARPED_STEM_CHAIR_2, new Item.Settings().group(seatsItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_crimson_stem_chair_2"), SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_crimson_stem_chair_2"), new BlockItem(SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR_2, new Item.Settings().group(seatsItemGroup)));
    }

    public static void registerTables() {
        /* LOG TABLES */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "oak_log_table"), OAK_LOG_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "oak_log_table"), new BlockItem(OAK_LOG_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "birch_log_table"), BIRCH_LOG_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "birch_log_table"), new BlockItem(BIRCH_LOG_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "spruce_log_table"), SPRUCE_LOG_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "spruce_log_table"), new BlockItem(SPRUCE_LOG_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "dark_oak_log_table"), DARK_OAK_LOG_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "dark_oak_log_table"), new BlockItem(DARK_OAK_LOG_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "acacia_log_table"), ACACIA_LOG_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "acacia_log_table"), new BlockItem(ACACIA_LOG_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "jungle_log_table"), JUNGLE_LOG_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "jungle_log_table"), new BlockItem(JUNGLE_LOG_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "crimson_stem_table"), CRIMSON_STEM_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "crimson_stem_table"), new BlockItem(CRIMSON_STEM_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warped_stem_table"), WARPED_STEM_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warped_stem_table"), new BlockItem(WARPED_STEM_TABLE, new Item.Settings().group(tablesItemGroup)));

        /* STRIPPED LOG TABLES */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_oak_log_table"), STRIPPED_OAK_LOG_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_oak_log_table"), new BlockItem(STRIPPED_OAK_LOG_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_birch_log_table"), STRIPPED_BIRCH_LOG_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_birch_log_table"), new BlockItem(STRIPPED_BIRCH_LOG_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_spruce_log_table"), STRIPPED_SPRUCE_LOG_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_spruce_log_table"), new BlockItem(STRIPPED_SPRUCE_LOG_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_dark_oak_log_table"), STRIPPED_DARK_OAK_LOG_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_dark_oak_log_table"), new BlockItem(STRIPPED_DARK_OAK_LOG_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_acacia_log_table"), STRIPPED_ACACIA_LOG_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_acacia_log_table"), new BlockItem(STRIPPED_ACACIA_LOG_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_jungle_log_table"), STRIPPED_JUNGLE_LOG_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_jungle_log_table"), new BlockItem(STRIPPED_JUNGLE_LOG_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_crimson_stem_table"), STRIPPED_CRIMSON_STEM_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_crimson_stem_table"), new BlockItem(STRIPPED_CRIMSON_STEM_TABLE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_warped_stem_table"), STRIPPED_WARPED_STEM_TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_warped_stem_table"), new BlockItem(STRIPPED_WARPED_STEM_TABLE, new Item.Settings().group(tablesItemGroup)));
    }

    /*public static void registerFences() {
        // LOG FENCES
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "oak_log_fence"), OAK_LOG_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "oak_log_fence"), new BlockItem(OAK_LOG_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "birch_log_fence"), BIRCH_LOG_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "birch_log_fence"), new BlockItem(BIRCH_LOG_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "spruce_log_fence"), SPRUCE_LOG_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "spruce_log_fence"), new BlockItem(SPRUCE_LOG_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "dark_oak_log_fence"), DARK_OAK_LOG_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "dark_oak_log_fence"), new BlockItem(DARK_OAK_LOG_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "acacia_log_fence"), ACACIA_LOG_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "acacia_log_fence"), new BlockItem(ACACIA_LOG_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "jungle_log_fence"), JUNGLE_LOG_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "jungle_log_fence"), new BlockItem(JUNGLE_LOG_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "crimson_stem_fence"), CRIMSON_STEM_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "crimson_stem_fence"), new BlockItem(CRIMSON_STEM_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warped_stem_fence"), WARPED_STEM_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warped_stem_fence"), new BlockItem(WARPED_STEM_FENCE, new Item.Settings().group(tablesItemGroup)));

        // STRIPPED LOG FENCES
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_oak_log_fence"), STRIPPED_OAK_LOG_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_oak_log_fence"), new BlockItem(STRIPPED_OAK_LOG_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_birch_log_fence"), STRIPPED_BIRCH_LOG_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_birch_log_fence"), new BlockItem(STRIPPED_BIRCH_LOG_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_spruce_log_fence"), STRIPPED_SPRUCE_LOG_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_spruce_log_fence"), new BlockItem(STRIPPED_SPRUCE_LOG_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_dark_oak_log_fence"), STRIPPED_DARK_OAK_LOG_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_dark_oak_log_fence"), new BlockItem(STRIPPED_DARK_OAK_LOG_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_acacia_log_fence"), STRIPPED_ACACIA_LOG_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_acacia_log_fence"), new BlockItem(STRIPPED_ACACIA_LOG_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_jungle_log_fence"), STRIPPED_JUNGLE_LOG_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_jungle_log_fence"), new BlockItem(STRIPPED_JUNGLE_LOG_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_crimson_stem_fence"), STRIPPED_CRIMSON_STEM_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_crimson_stem_fence"), new BlockItem(STRIPPED_CRIMSON_STEM_FENCE, new Item.Settings().group(tablesItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "stripped_warped_stem_fence"), STRIPPED_WARPED_STEM_FENCE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "stripped_warped_stem_fence"), new BlockItem(STRIPPED_WARPED_STEM_FENCE, new Item.Settings().group(tablesItemGroup)));

    } */
}

