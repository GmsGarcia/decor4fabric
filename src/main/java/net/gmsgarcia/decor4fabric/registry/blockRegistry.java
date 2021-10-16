package net.gmsgarcia.decor4fabric.registry;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.gmsgarcia.decor4fabric.blocks.*;
import net.gmsgarcia.decor4fabric.blocks.block_entities.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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
    public static BlockEntityType<warped_stem_logBench_BlockEntity> WARPED_STEM_BENCH_BLOCK_ENTITY;
    public static BlockEntityType<crimson_stem_logBench_BlockEntity> CRIMSON_STEM_BENCH_BLOCK_ENTITY;

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
    public static BlockEntityType<warped_stem_logBench2_BlockEntity> WARPED_STEM_BENCH_2_BLOCK_ENTITY;
    public static BlockEntityType<crimson_stem_logBench2_BlockEntity> CRIMSON_STEM_BENCH_2_BLOCK_ENTITY;

    public static final Block OAK_LOG_BENCH_3 = new logBench3();
    public static final Block BIRCH_LOG_BENCH_3 = new logBench3();
    public static final Block SPRUCE_LOG_BENCH_3 = new logBench3();
    public static final Block DARK_OAK_LOG_BENCH_3 = new logBench3();
    public static final Block ACACIA_LOG_BENCH_3 = new logBench3();
    public static final Block JUNGLE_LOG_BENCH_3 = new logBench3();
    public static final Block CRIMSON_STEM_BENCH_3 = new logBench3();
    public static final Block WARPED_STEM_BENCH_3 = new logBench3();

    ////////////
    // CHAIRS //
    ////////////
    public static final Block SIMPLE_OAK_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_BIRCH_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_SPRUCE_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_DARK_OAK_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_ACACIA_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_JUNGLE_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_WARPED_STEM_CHAIR = new simple_logChair();
    public static final Block SIMPLE_CRIMSON_STEM_CHAIR = new simple_logChair();

    public static final Block SIMPLE_OAK_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_BIRCH_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_SPRUCE_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_DARK_OAK_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_ACACIA_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_JUNGLE_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_WARPED_STEM_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_CRIMSON_STEM_CHAIR_2 = new simple_logChair2();

    public static final Block SIMPLE_STRIPPED_OAK_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_BIRCH_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_ACACIA_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_WARPED_STEM_CHAIR = new simple_logChair();
    public static final Block SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR = new simple_logChair();

    public static final Block SIMPLE_STRIPPED_OAK_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_BIRCH_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_ACACIA_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_WARPED_STEM_CHAIR_2 = new simple_logChair2();
    public static final Block SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR_2 = new simple_logChair2();

    public static final Block TABLE = new table();

    public static final ItemGroup decorItemGroup = FabricItemGroupBuilder.build(new Identifier("decor4fabric", "decor_itemgroup"), () -> new ItemStack(SIMPLE_STRIPPED_OAK_LOG_CHAIR));


    public static void registerBenches() {

        /* LOG BENCHES 1 */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "oak_log_bench"), OAK_LOG_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "oak_log_bench"), new BlockItem(OAK_LOG_BENCH, new Item.Settings().group(decorItemGroup)));
        OAK_LOG_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:oak_log_bench", FabricBlockEntityTypeBuilder.create(oak_logBench_BlockEntity::new, OAK_LOG_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "birch_log_bench"), BIRCH_LOG_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "birch_log_bench"), new BlockItem(BIRCH_LOG_BENCH, new Item.Settings().group(decorItemGroup)));
        BIRCH_LOG_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:birch_log_bench", FabricBlockEntityTypeBuilder.create(birch_logBench_BlockEntity::new, BIRCH_LOG_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "spruce_log_bench"), SPRUCE_LOG_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "spruce_log_bench"), new BlockItem(SPRUCE_LOG_BENCH, new Item.Settings().group(decorItemGroup)));
        SPRUCE_LOG_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:spruce_log_bench", FabricBlockEntityTypeBuilder.create(spruce_logBench_BlockEntity::new, SPRUCE_LOG_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "dark_oak_log_bench"), DARK_OAK_LOG_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "dark_oak_log_bench"), new BlockItem(DARK_OAK_LOG_BENCH, new Item.Settings().group(decorItemGroup)));
        DARK_OAK_LOG_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:dark_oak_log_bench", FabricBlockEntityTypeBuilder.create(dark_oak_logBench_BlockEntity::new, DARK_OAK_LOG_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "acacia_log_bench"), ACACIA_LOG_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "acacia_log_bench"), new BlockItem(ACACIA_LOG_BENCH, new Item.Settings().group(decorItemGroup)));
        ACACIA_LOG_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:acacia_log_bench", FabricBlockEntityTypeBuilder.create(acacia_logBench_BlockEntity::new, ACACIA_LOG_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "jungle_log_bench"), JUNGLE_LOG_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "jungle_log_bench"), new BlockItem(JUNGLE_LOG_BENCH, new Item.Settings().group(decorItemGroup)));
        JUNGLE_LOG_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:jungle_log_bench", FabricBlockEntityTypeBuilder.create(jungle_logBench_BlockEntity::new, JUNGLE_LOG_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "crimson_stem_bench"), CRIMSON_STEM_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "crimson_stem_bench"), new BlockItem(CRIMSON_STEM_BENCH, new Item.Settings().group(decorItemGroup)));
        CRIMSON_STEM_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:crimson_stem_bench", FabricBlockEntityTypeBuilder.create(crimson_stem_logBench_BlockEntity::new, CRIMSON_STEM_BENCH).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warped_stem_bench"), WARPED_STEM_BENCH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warped_stem_bench"), new BlockItem(WARPED_STEM_BENCH, new Item.Settings().group(decorItemGroup)));
        WARPED_STEM_BENCH_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:warped_stem_bench", FabricBlockEntityTypeBuilder.create(warped_stem_logBench_BlockEntity::new, WARPED_STEM_BENCH).build(null));

        /* LOG BENCHES 2 */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "oak_log_bench_2"), OAK_LOG_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "oak_log_bench_2"), new BlockItem(OAK_LOG_BENCH_2, new Item.Settings().group(decorItemGroup)));
        OAK_LOG_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:oak_log_bench_2", FabricBlockEntityTypeBuilder.create(oak_logBench2_BlockEntity::new, OAK_LOG_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "birch_log_bench_2"), BIRCH_LOG_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "birch_log_bench_2"), new BlockItem(BIRCH_LOG_BENCH_2, new Item.Settings().group(decorItemGroup)));
        BIRCH_LOG_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:birch_log_bench_2", FabricBlockEntityTypeBuilder.create(birch_logBench2_BlockEntity::new, BIRCH_LOG_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "spruce_log_bench_2"), SPRUCE_LOG_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "spruce_log_bench_2"), new BlockItem(SPRUCE_LOG_BENCH_2, new Item.Settings().group(decorItemGroup)));
        SPRUCE_LOG_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:spruce_log_bench_2", FabricBlockEntityTypeBuilder.create(spruce_logBench2_BlockEntity::new, SPRUCE_LOG_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "dark_oak_log_bench_2"), DARK_OAK_LOG_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "dark_oak_log_bench_2"), new BlockItem(DARK_OAK_LOG_BENCH_2, new Item.Settings().group(decorItemGroup)));
        DARK_OAK_LOG_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:dark_oak_log_bench_2", FabricBlockEntityTypeBuilder.create(dark_oak_logBench2_BlockEntity::new, DARK_OAK_LOG_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "acacia_log_bench_2"), ACACIA_LOG_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "acacia_log_bench_2"), new BlockItem(ACACIA_LOG_BENCH_2, new Item.Settings().group(decorItemGroup)));
        ACACIA_LOG_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:acacia_log_bench_2", FabricBlockEntityTypeBuilder.create(acacia_logBench2_BlockEntity::new, ACACIA_LOG_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "jungle_log_bench_2"), JUNGLE_LOG_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "jungle_log_bench_2"), new BlockItem(JUNGLE_LOG_BENCH_2, new Item.Settings().group(decorItemGroup)));
        JUNGLE_LOG_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:jungle_log_bench_2", FabricBlockEntityTypeBuilder.create(jungle_logBench2_BlockEntity::new, JUNGLE_LOG_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "crimson_stem_bench_2"), CRIMSON_STEM_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "crimson_stem_bench_2"), new BlockItem(CRIMSON_STEM_BENCH_2, new Item.Settings().group(decorItemGroup)));
        CRIMSON_STEM_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:crimson_stem_bench_2", FabricBlockEntityTypeBuilder.create(crimson_stem_logBench2_BlockEntity::new, CRIMSON_STEM_BENCH_2).build(null));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warped_stem_bench_2"), WARPED_STEM_BENCH_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warped_stem_bench_2"), new BlockItem(WARPED_STEM_BENCH_2, new Item.Settings().group(decorItemGroup)));
        WARPED_STEM_BENCH_2_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "decor4fabric:warped_stem_bench_2", FabricBlockEntityTypeBuilder.create(warped_stem_logBench2_BlockEntity::new, WARPED_STEM_BENCH_2).build(null));

        /* LOG BENCHES 3 */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "oak_log_bench_3"), OAK_LOG_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "oak_log_bench_3"), new BlockItem(OAK_LOG_BENCH_3, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "birch_log_bench_3"), BIRCH_LOG_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "birch_log_bench_3"), new BlockItem(BIRCH_LOG_BENCH_3, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "spruce_log_bench_3"), SPRUCE_LOG_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "spruce_log_bench_3"), new BlockItem(SPRUCE_LOG_BENCH_3, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "dark_oak_log_bench_3"), DARK_OAK_LOG_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "dark_oak_log_bench_3"), new BlockItem(DARK_OAK_LOG_BENCH_3, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "acacia_log_bench_3"), ACACIA_LOG_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "acacia_log_bench_3"), new BlockItem(ACACIA_LOG_BENCH_3, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "jungle_log_bench_3"), JUNGLE_LOG_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "jungle_log_bench_3"), new BlockItem(JUNGLE_LOG_BENCH_3, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "crimson_stem_bench_3"), CRIMSON_STEM_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "crimson_stem_bench_3"), new BlockItem(CRIMSON_STEM_BENCH_3, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "warped_stem_bench_3"), WARPED_STEM_BENCH_3);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warped_stem_bench_3"), new BlockItem(WARPED_STEM_BENCH_3, new Item.Settings().group(decorItemGroup)));
    }

    public static void registerChairs() {

        /* SIMPLE LOG CHAIRS */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_oak_log_chair"), SIMPLE_OAK_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_oak_log_chair"), new BlockItem(SIMPLE_OAK_LOG_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_birch_log_chair"), SIMPLE_BIRCH_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_birch_log_chair"), new BlockItem(SIMPLE_BIRCH_LOG_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_spruce_log_chair"), SIMPLE_SPRUCE_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_spruce_log_chair"), new BlockItem(SIMPLE_SPRUCE_LOG_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_dark_oak_log_chair"), SIMPLE_DARK_OAK_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_dark_oak_log_chair"), new BlockItem(SIMPLE_DARK_OAK_LOG_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_acacia_log_chair"), SIMPLE_ACACIA_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_acacia_log_chair"), new BlockItem(SIMPLE_ACACIA_LOG_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_jungle_log_chair"), SIMPLE_JUNGLE_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_jungle_log_chair"), new BlockItem(SIMPLE_JUNGLE_LOG_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_warped_stem_chair"), SIMPLE_WARPED_STEM_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_warped_stem_chair"), new BlockItem(SIMPLE_WARPED_STEM_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_crimson_stem_chair"), SIMPLE_CRIMSON_STEM_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_crimson_stem_chair"), new BlockItem(SIMPLE_CRIMSON_STEM_CHAIR, new Item.Settings().group(decorItemGroup)));

        /* SIMPLE LOG CHAIRS 2 */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_oak_log_chair_2"), SIMPLE_OAK_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_oak_log_chair_2"), new BlockItem(SIMPLE_OAK_LOG_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_birch_log_chair_2"), SIMPLE_BIRCH_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_birch_log_chair_2"), new BlockItem(SIMPLE_BIRCH_LOG_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_spruce_log_chair_2"), SIMPLE_SPRUCE_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_spruce_log_chair_2"), new BlockItem(SIMPLE_SPRUCE_LOG_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_dark_oak_log_chair_2"), SIMPLE_DARK_OAK_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_dark_oak_log_chair_2"), new BlockItem(SIMPLE_DARK_OAK_LOG_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_acacia_log_chair_2"), SIMPLE_ACACIA_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_acacia_log_chair_2"), new BlockItem(SIMPLE_ACACIA_LOG_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_jungle_log_chair_2"), SIMPLE_JUNGLE_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_jungle_log_chair_2"), new BlockItem(SIMPLE_JUNGLE_LOG_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_warped_stem_chair_2"), SIMPLE_WARPED_STEM_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_warped_stem_chair_2"), new BlockItem(SIMPLE_WARPED_STEM_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_crimson_stem_chair_2"), SIMPLE_CRIMSON_STEM_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_crimson_stem_chair_2"), new BlockItem(SIMPLE_CRIMSON_STEM_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        /* SIMPLE STRIPPED LOG CHAIRS */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_oak_log_chair"), SIMPLE_STRIPPED_OAK_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_oak_log_chair"), new BlockItem(SIMPLE_STRIPPED_OAK_LOG_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_birch_log_chair"), SIMPLE_STRIPPED_BIRCH_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_birch_log_chair"), new BlockItem(SIMPLE_STRIPPED_BIRCH_LOG_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_spruce_log_chair"), SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_spruce_log_chair"), new BlockItem(SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_dark_oak_log_chair"), SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_dark_oak_log_chair"), new BlockItem(SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_acacia_log_chair"), SIMPLE_STRIPPED_ACACIA_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_acacia_log_chair"), new BlockItem(SIMPLE_STRIPPED_ACACIA_LOG_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_jungle_log_chair"), SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_jungle_log_chair"), new BlockItem(SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_warped_stem_chair"), SIMPLE_STRIPPED_WARPED_STEM_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_warped_stem_chair"), new BlockItem(SIMPLE_STRIPPED_WARPED_STEM_CHAIR, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_crimson_stem_chair"), SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_crimson_stem_chair"), new BlockItem(SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR, new Item.Settings().group(decorItemGroup)));

        /* SIMPLE STRIPPED LOG CHAIRS 2 */
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_oak_log_chair_2"), SIMPLE_STRIPPED_OAK_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_oak_log_chair_2"), new BlockItem(SIMPLE_STRIPPED_OAK_LOG_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_birch_log_chair_2"), SIMPLE_STRIPPED_BIRCH_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_birch_log_chair_2"), new BlockItem(SIMPLE_STRIPPED_BIRCH_LOG_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_spruce_log_chair_2"), SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_spruce_log_chair_2"), new BlockItem(SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_dark_oak_log_chair_2"), SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_dark_oak_log_chair_2"), new BlockItem(SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_acacia_log_chair_2"), SIMPLE_STRIPPED_ACACIA_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_acacia_log_chair_2"), new BlockItem(SIMPLE_STRIPPED_ACACIA_LOG_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_jungle_log_chair_2"), SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_jungle_log_chair_2"), new BlockItem(SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_warped_stem_chair_2"), SIMPLE_STRIPPED_WARPED_STEM_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_warped_stem_chair_2"), new BlockItem(SIMPLE_STRIPPED_WARPED_STEM_CHAIR_2, new Item.Settings().group(decorItemGroup)));

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_stripped_crimson_stem_chair_2"), SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR_2);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "simple_stripped_crimson_stem_chair_2"), new BlockItem(SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR_2, new Item.Settings().group(decorItemGroup)));
    }

/*    public static void registerTables() {
        *//* TABLE *//*
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "table"), TABLE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "table"), new BlockItem(TABLE, new Item.Settings().group(decorItemGroup)));

    }*/
}

