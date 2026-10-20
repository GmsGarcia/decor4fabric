package net.gmsgarcia.decor4all.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.blockentity.LogBenchBlockEntity;
import net.gmsgarcia.decor4all.blockentity.SmallStoolBlockEntity;
import net.gmsgarcia.decor4all.registry.DecorTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.WoodType;

// The block catalogue: what gets registered, under which id, in which order.
public final class DecorBlocks {

    private DecorBlocks() {
    }

    // The woods, in 1.18.2's registration order, followed by the Tier 2 additions.
    public enum Wood {
        OAK("oak", "oak_log", WoodType.OAK),
        BIRCH("birch", "birch_log", WoodType.BIRCH),
        SPRUCE("spruce", "spruce_log", WoodType.SPRUCE),
        DARK_OAK("dark_oak", "dark_oak_log", WoodType.DARK_OAK),
        ACACIA("acacia", "acacia_log", WoodType.ACACIA),
        JUNGLE("jungle", "jungle_log", WoodType.JUNGLE),
        CRIMSON("crimson", "crimson_stem", WoodType.CRIMSON),
        WARPED("warped", "warped_stem", WoodType.WARPED),
        // Tier 2, appended after all 121 legacy ids and never interleaved.
        CHERRY("cherry", "cherry_log", WoodType.CHERRY),
        MANGROVE("mangrove", "mangrove_log", WoodType.MANGROVE),
        PALE_OAK("pale_oak", "pale_oak_log", WoodType.PALE_OAK);

        private final String id;
        private final String segment;
        private final WoodType woodType;

        Wood(String id, String segment, WoodType woodType) {
            this.id = id;
            this.segment = segment;
            this.woodType = woodType;
        }

        // The leading part of an id, e.g. oak or dark_oak.
        public String id() {
            return this.id;
        }

        // The middle of an id, e.g. oak_log or crimson_stem.
        public String segment() {
            return this.segment;
        }

        // 26.1's WoodType, needed by FenceGateBlock's constructor.
        public WoodType woodType() {
            return this.woodType;
        }
    }

    // The three custom tabs, in 1.18.2's blockRegistry declaration order.
    public static final ResourceKey<CreativeModeTab> SEATS_TAB = tabKey("seats");
    public static final ResourceKey<CreativeModeTab> TABLES_TAB = tabKey("tables");
    public static final ResourceKey<CreativeModeTab> FENCES_TAB = tabKey("fences");

    // Where the carpentry table item goes.
    public static final ResourceKey<CreativeModeTab> CARPENTER_TABLE_TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                    Identifier.fromNamespaceAndPath("minecraft", "functional_blocks"));

    // One block to register.
    public record Entry(String path, ResourceKey<Block> key, BlockSpec spec,
                        ResourceKey<CreativeModeTab> tab, List<TagKey<Block>> tags) {

        // The block item's id: identical to the block's, as in 1.18.2.
        public ResourceKey<Item> itemKey() {
            return ResourceKey.create(Registries.ITEM, id(path));
        }
    }

    // The carpentry table, named.
    public static final Entry CARPENTER_TABLE_ENTRY = new Entry("carpenter_table", BlockSpec.key("carpenter_table"),
            BlockFamilies.CARPENTER_TABLE, null, List.of());

    // The eight woods 1.18.2 shipped, in the order its registry used.
    private static final List<Wood> LEGACY_WOODS = List.of(
            Wood.OAK, Wood.BIRCH, Wood.SPRUCE, Wood.DARK_OAK,
            Wood.ACACIA, Wood.JUNGLE, Wood.CRIMSON, Wood.WARPED);

    // Cherry, mangrove and pale oak: 3 woods x 15 blocks = 45 new ids.
    private static final List<Wood> TIER_2_WOODS =
            List.of(Wood.CHERRY, Wood.MANGROVE, Wood.PALE_OAK);

    // All 11 woods, legacy first and Tier 2 after, which is both the order
    // #LEGACY_WOODS/#TIER_2_WOODS already imply and the order a reader expects inside a single
    // family.
    private static final List<Wood> ALL_WOODS = buildAllWoods();

    // The 121 legacy blocks followed by the 45 Tier 2 blocks, in registration order.
    public static final List<Entry> ALL = buildAll();

    // 1 + 8 woods x 15 = 121, plus 3 Tier 2 woods x 15 = 45.
    static {
        int expected = 121 + 45;
        if (ALL.size() != expected) {
            throw new IllegalStateException("block catalogue has " + ALL.size()
                    + " entries, expected " + expected);
        }
    }

    // The same 166 entries as #ALL in the order a creative tab should show them: family by family,
    // with all 11 woods together inside each family.
    public static final List<Entry> DISPLAY = buildDisplay();

    static {
        // Same reasoning as the count assertion above, and the same reason it
        // compares paths rather than entries: an Entry carries a BlockSpec,
        // whose factory is a Function, and a Function is only ever equal to
        // itself, so two independently built lists hold entries that look alike
        // and are not equals. The path is the block id, and the id is the thing
        // that actually has to line up. A TreeSet rather than a List so that a
        // duplicated path and a missing one cannot cancel out and still compare
        // equal.
        if (DISPLAY.size() != ALL.size()
                || !pathsOf(ALL).equals(pathsOf(DISPLAY))) {
            throw new IllegalStateException("display order has " + DISPLAY.size()
                    + " entries and is not a permutation of the "
                    + ALL.size() + " registered blocks");
        }
    }

    // The block ids of entries, deduplicated; see the assertion above.
    private static Set<String> pathsOf(List<Entry> entries) {
        Set<String> paths = new TreeSet<>();
        for (Entry entry : entries) {
            paths.add(entry.path());
        }
        return paths;
    }

    // Concatenates the two wood sets; see #ALL_WOODS.
    private static List<Wood> buildAllWoods() {
        List<Wood> woods = new ArrayList<>(LEGACY_WOODS);
        woods.addAll(TIER_2_WOODS);
        return List.copyOf(woods);
    }

    // The registration order, which is the save format.
    private static List<Entry> buildAll() {
        List<Entry> out = new ArrayList<>(166);

        // registerCarpenterTable() -- the one id that leads both passes.
        out.add(CARPENTER_TABLE_ENTRY);

        addFamilies(out, LEGACY_WOODS);
        addFamilies(out, TIER_2_WOODS);

        return List.copyOf(out);
    }

    // The display order described on #DISPLAY.
    private static List<Entry> buildDisplay() {
        List<Entry> out = new ArrayList<>(166);

        out.add(CARPENTER_TABLE_ENTRY);

        addFamilies(out, ALL_WOODS);

        return List.copyOf(out);
    }

    // Appends all 15 families for one wood set, in legacy order.
    private static void addFamilies(List<Entry> out, List<Wood> woods) {
        // registerBenches(): all woods per height, height-major.
        for (Wood wood : woods) {
            out.add(bench(wood, ""));
        }
        for (Wood wood : woods) {
            out.add(bench(wood, "_2"));
        }
        for (Wood wood : woods) {
            out.add(bench(wood, "_3"));
        }

        // registerStools(): plain run, then the stripped run.
        for (Wood wood : woods) {
            out.add(stool(wood, false));
        }
        for (Wood wood : woods) {
            out.add(stool(wood, true));
        }

        // registerChairs(): plain, plain_2, stripped, stripped_2.
        for (Wood wood : woods) {
            out.add(chair(wood, false, false));
        }
        for (Wood wood : woods) {
            out.add(chair(wood, false, true));
        }
        for (Wood wood : woods) {
            out.add(chair(wood, true, false));
        }
        for (Wood wood : woods) {
            out.add(chair(wood, true, true));
        }

        // registerTables()
        for (Wood wood : woods) {
            out.add(table(wood, false));
        }
        for (Wood wood : woods) {
            out.add(table(wood, true));
        }

        // registerFences()
        for (Wood wood : woods) {
            out.add(fence(wood, false));
        }
        for (Wood wood : woods) {
            out.add(fence(wood, true));
        }

        // registerFencesGates()
        for (Wood wood : woods) {
            out.add(gate(wood, false));
        }
        for (Wood wood : woods) {
            out.add(gate(wood, true));
        }
    }

    private static Entry bench(Wood wood, String suffix) {
        String path = wood.segment() + "_bench" + suffix;
        BlockSpec spec = switch (suffix) {
            case "" -> BlockFamilies.LOG_BENCH;
            case "_2" -> BlockFamilies.LOG_BENCH_2;
            case "_3" -> BlockFamilies.HIGH_BENCH;
            default -> throw new IllegalArgumentException(suffix);
        };
        TagKey<Block> tag = suffix.equals("_3")
                ? DecorTags.Blocks.HIGH_BENCHES
                : DecorTags.Blocks.BENCHES;
        return new Entry(path, BlockSpec.key(path), spec, SEATS_TAB, List.of(tag));
    }

    private static Entry stool(Wood wood, boolean stripped) {
        String path = prefix(wood, stripped) + "_small_stool";
        return new Entry(path, BlockSpec.key(path), BlockFamilies.SMALL_STOOL, SEATS_TAB,
                List.of(DecorTags.Blocks.SMALL_STOOLS));
    }

    private static Entry chair(Wood wood, boolean stripped, boolean arms) {
        String path = prefix(wood, stripped) + "_chair" + (arms ? "_2" : "");
        BlockSpec spec = arms ? BlockFamilies.ARMCHAIR : BlockFamilies.CHAIR;
        return new Entry(path, BlockSpec.key(path), spec, SEATS_TAB,
                List.of(DecorTags.Blocks.CHAIRS));
    }

    private static Entry table(Wood wood, boolean stripped) {
        String path = prefix(wood, stripped) + "_table";
        return new Entry(path, BlockSpec.key(path), BlockFamilies.LOG_TABLE, TABLES_TAB,
                List.of(DecorTags.Blocks.TABLES));
    }

    private static Entry fence(Wood wood, boolean stripped) {
        String path = prefix(wood, stripped) + "_fence";
        return new Entry(path, BlockSpec.key(path), BlockFamilies.LOG_FENCE, FENCES_TAB, List.of());
    }

    private static Entry gate(Wood wood, boolean stripped) {
        String path = prefix(wood, stripped) + "_fence_gate";
        return new Entry(path, BlockSpec.key(path), BlockFamilies.logFenceGate(wood.woodType()),
                FENCES_TAB, List.of());
    }

    private static String prefix(Wood wood, boolean stripped) {
        return stripped ? "stripped_" + wood.segment() : wood.segment();
    }

    // ------------------------------------------------------------------
    // Block entity types
    // ------------------------------------------------------------------

    // One block entity type. 1.18.2 validated each type against the blocks allowed to hold it, and
    // the valid-block sets are not derivable from the tag data -- they are the two families that
    // implemented BlockEntityProvider.
    public record BlockEntityEntry(String path, ResourceKey<BlockEntityType<?>> key,
                                   BlockEntityType.BlockEntitySupplier<?> factory,
                                   Predicate<Entry> accepts) {
    }

    // log_bench covers the one axe-storing bench -- *_bench -- and not *_bench_2 (a pure seat
    // since Phase 4, no block entity) nor *_bench_3, which never had one in 1.18.2. 11 valid
    // blocks once all woods are counted.
    public static final BlockEntityEntry LOG_BENCH = new BlockEntityEntry(
            "log_bench",
            ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id("log_bench")),
            LogBenchBlockEntity::new,
            entry -> entry.path().endsWith("_bench"));

    // log_small_stool covers plain and stripped stools alike.
    public static final BlockEntityEntry LOG_SMALL_STOOL = new BlockEntityEntry(
            "log_small_stool",
            ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id("log_small_stool")),
            SmallStoolBlockEntity::new,
            entry -> entry.path().endsWith("_small_stool"));

    public static final List<BlockEntityEntry> BLOCK_ENTITIES = List.of(LOG_BENCH, LOG_SMALL_STOOL);

    // ------------------------------------------------------------------
    // Creative tabs
    // ------------------------------------------------------------------

    // The three custom tabs, in 1.18.2's blockRegistry declaration order.
    public record TabEntry(ResourceKey<CreativeModeTab> key, String path, String iconPath) {
    }

    public static final List<TabEntry> TABS = List.of(
            new TabEntry(SEATS_TAB, "seats", "stripped_oak_log_chair"),
            new TabEntry(TABLES_TAB, "tables", "oak_log_table"),
            new TabEntry(FENCES_TAB, "fences", "oak_log_fence"));

    // Builds a tab.
    public static CreativeModeTab buildTab(TabEntry tab, Function<String, Block> lookup) {
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.decor4all." + tab.path()))
                .icon(() -> new ItemStack(lookup.apply(tab.iconPath())))
                .displayItems((parameters, output) -> {
                    for (Entry entry : DISPLAY) {
                        if (tab.key().equals(entry.tab())) {
                            output.accept(lookup.apply(entry.path()));
                        }
                    }
                })
                .build();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Decor4All.MOD_ID, path);
    }

    private static ResourceKey<CreativeModeTab> tabKey(String path) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, id(path));
    }
}