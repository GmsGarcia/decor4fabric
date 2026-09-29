package net.gmsgarcia.decor4fabric.content;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.blockentity.LogBenchBlockEntity;
import net.gmsgarcia.decor4fabric.blockentity.SmallStoolBlockEntity;
import net.gmsgarcia.decor4fabric.registry.DecorTags;
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

/**
 * The block catalogue: what gets registered, under which id, in which order.
 *
 * <p><b>Ids are frozen.</b> Every id below is copied from 1.18.2's
 * {@code blockRegistry}, and the order is the order of its {@code register*}
 * methods as called from {@code mainDecor}. Both are load-bearing: the id appears
 * in existing worlds, datapacks and item-frame NBT, and a registry position
 * becomes a numeric id in save data on older versions.
 *
 * <p>Three facts about the original are easy to get wrong and are therefore
 * spelled out here rather than left to the loops below:
 *
 * <ul>
 *   <li><b>Order is family-major, not wood-major.</b> {@code registerBenches}
 *       emitted all woods' {@code _log_bench}, then all {@code _log_bench_2},
 *       then all {@code _log_bench_3}. Writing "for each wood, emit all nine
 *       variants" produces a completely different registry and is the obvious
 *       thing to type by mistake.
 *   <li><b>The wood order is not the vanilla order.</b> It is oak, birch,
 *       spruce, dark oak, acacia, jungle, crimson, warped -- birch before
 *       spruce, dark oak before acacia.
 *   <li><b>Only some families have stripped variants.</b> Benches have none.
 *       Stools, chairs, tables, fences and gates each have a {@code stripped_}
 *       twin, so a wood is 15 blocks, not 9: 3 benches, 2 stools, 4 chairs,
 *       2 tables, 2 fences, 2 gates.
 * </ul>
 *
 * <p>Block item ids are the block ids verbatim. 1.18.2 registered
 * {@code Registry.ITEM} under the same string it used for {@code Registry.BLOCK},
 * with no {@code _block} suffix, so 121 block ids and 121 item ids share 121
 * names.
 *
 * <p>The workbench is {@code workbench}, not {@code work_bench}.
 */
public final class DecorBlocks {

    private DecorBlocks() {
    }

    /**
     * The woods, in 1.18.2's registration order, followed by the Tier 2 additions.
     *
     * <p>{@code segment} is the middle of an id, and it is not derivable from
     * {@code id}: crimson and warped use {@code crimson_stem} and
     * {@code warped_stem} where every other wood uses {@code <wood>_log}.
     */
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

        /** The leading part of an id, e.g. {@code oak} or {@code dark_oak}. */
        public String id() {
            return this.id;
        }

        /** The middle of an id, e.g. {@code oak_log} or {@code crimson_stem}. */
        public String segment() {
            return this.segment;
        }

        /** 26.1's {@code WoodType}, needed by {@code FenceGateBlock}'s constructor. */
        public WoodType woodType() {
            return this.woodType;
        }
    }

    /** The three custom tabs, in 1.18.2's {@code blockRegistry} declaration order. */
    public static final ResourceKey<CreativeModeTab> SEATS_TAB = tabKey("seats");
    public static final ResourceKey<CreativeModeTab> TABLES_TAB = tabKey("tables");
    public static final ResourceKey<CreativeModeTab> FENCES_TAB = tabKey("fences");

    /**
     * Where the workbench item goes.
     *
     * <p>1.18.2 put it in {@code ItemGroup.DECORATIONS}, and that tab is gone.
     * Dumping every string constant out of 26.1's {@code CreativeModeTabs} with
     * {@code javap -c} gives the complete surviving set:
     *
     * <pre>
     * building_blocks   colored_blocks   combat          food_and_drinks
     * functional_blocks   hotbar        ingredients      inventory
     * natural_blocks    op_blocks       redstone_blocks   search
     * spawn_eggs        tools_and_utilities
     * </pre>
     *
     * <p>No {@code decorations} among them: upstream folded that tab's contents
     * into {@code functional_blocks}, which is therefore the target here. This is
     * forced by the vanilla tab set, not a preference.
     *
     * <p>The key is rebuilt from the identifier rather than referenced as
     * {@code CreativeModeTabs.FUNCTIONAL_BLOCKS}, because in 26.1 that constant is
     * {@code private static final} -- as is every other tab constant in the class,
     * which is why the lookup has to go through
     * {@code BuiltInRegistries.CREATIVE_MODE_TAB.getValue(...)}. Rebuilding the key
     * from {@code minecraft:functional_blocks} yields the same key by value:
     * {@link ResourceKey} compares by registry plus identifier, not by identity.
     */
    public static final ResourceKey<CreativeModeTab> WORKBENCH_TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                    Identifier.fromNamespaceAndPath("minecraft", "functional_blocks"));

    /**
     * One block to register.
     *
     * @param path the registry path, which is also the block item's id
     * @param spec the properties and class
     * @param tab  the custom tab it appears in, or {@code null} when it is not in
     *             one of the three and a loader has to inject it elsewhere
     * @param tags the {@code decor4fabric} block tags it belongs to, possibly
     *             empty. A block can be in more than one.
     */
    public record Entry(String path, ResourceKey<Block> key, BlockSpec spec,
                        ResourceKey<CreativeModeTab> tab, List<TagKey<Block>> tags) {

        /** The block item's id: identical to the block's, as in 1.18.2. */
        public ResourceKey<Item> itemKey() {
            return ResourceKey.create(Registries.ITEM, id(path));
        }
    }

    /**
     * The workbench, named.
     *
     * <p>It leads {@link #ALL} because 1.18.2's {@code registerWorkBench()} ran
     * first, and it is the only entry with no {@link #WORKBENCH_TAB} of its own
     * -- its item has to be injected into a <em>vanilla</em> tab, which is a
     * loader-specific job and therefore cannot be expressed by the {@code tab}
     * field like the other three tabs are.
     *
     * <p>Declared before {@link #ALL} because {@link #buildAll()} adds this very
     * field, and Java runs static initialisers in textual order: declaring it
     * below {@code ALL} would hand {@code buildAll} a null.
     */
    public static final Entry WORKBENCH_ENTRY = new Entry("workbench", BlockSpec.key("workbench"),
            BlockFamilies.WORKBENCH, null, List.of());

    /**
     * The eight woods 1.18.2 shipped, in the order its registry used.
     *
     * <p>Spelled out as a list rather than taken from {@link Wood#values()} so
     * that adding a wood to the enum is a deliberate act. {@code values()} order
     * happens to be correct today, which is exactly what makes it dangerous:
     * it encodes a load-bearing invariant in nothing but declaration order, and
     * nothing in the compiler checks it.
     *
     * <p>Declared above {@link #ALL}, not below it, and that is load-bearing.
     * {@link #buildAll()} reads both wood lists, and Java runs static
     * initialisers in textual order, so a list declared after {@code ALL} is
     * still null when {@code buildAll} runs. That failure is invisible to the
     * compiler and to every build: it throws
     * {@code NullPointerException: Cannot invoke "java.util.List.iterator()"}
     * from {@link #addFamilies} the first time the class loads in a real game,
     * with a stack trace pointing at a foreach loop rather than at the field
     * order that actually caused it. {@link #WORKBENCH_ENTRY} above documents
     * the same trap for the same reason.
     */
    private static final List<Wood> LEGACY_WOODS = List.of(
            Wood.OAK, Wood.BIRCH, Wood.SPRUCE, Wood.DARK_OAK,
            Wood.ACACIA, Wood.JUNGLE, Wood.CRIMSON, Wood.WARPED);

    /** Cherry, mangrove and pale oak: 3 woods x 15 blocks = 45 new ids. */
    private static final List<Wood> TIER_2_WOODS =
            List.of(Wood.CHERRY, Wood.MANGROVE, Wood.PALE_OAK);

    /**
     * The 121 legacy blocks followed by the 45 Tier 2 blocks, in registration
     * order.
     *
     * <p>Held as one unmodifiable list so the bootstrap, the tab generators and
     * any test all walk the same sequence rather than three parallel
     * reconstructions of it.
     */
    public static final List<Entry> ALL = buildAll();

    /**
     * 1 + 8 woods x 15 = 121, plus 3 Tier 2 woods x 15 = 45.
     *
     * <p>Checked in a static initialiser rather than a test because the failure
     * this guards against is not a logic bug that a test would catch: it is a
     * well-meaning edit to a loop bound, a wood added to the enum, or a
     * {@code stripped_} prefix dropped from one family, each of which compiles
     * cleanly and shifts every id after it. With the count asserted, that fails
     * at class load with a specific message instead of producing a jar whose
     * block ids no longer match the worlds people already have.
     */
    static {
        int expected = 121 + 45;
        if (ALL.size() != expected) {
            throw new IllegalStateException("block catalogue has " + ALL.size()
                    + " entries, expected " + expected);
        }
    }

    /**
     * The registration order, which is the save format.
     *
     * <p>Two passes over the family list, not one pass over the woods: the whole
     * 1.18.2 catalogue is emitted first, then the Tier 2 additions. Within a
     * pass the family order is height-major and family-major exactly as legacy
     * did it, i.e. all 8 legacy woods of {@code _bench}, then all 8 of
     * {@code _bench_2}, and so on.
     *
     * <p>Why two passes rather than just letting {@link Wood#values()} run 11
     * deep: the position of an entry in this list is its numeric registry id,
     * and those numbers are what a 1.18.2 world stores. Iterating all 11 woods
     * inside every family looks equivalent and is not -- it puts
     * {@code cherry_bench}, {@code mangrove_bench} and {@code pale_oak_bench} at
     * positions 10-12, so {@code oak_bench_2} lands on id 13 instead of 10 and
     * every id from there on shifts. That compiles, builds and loads; the damage
     * only surfaces as a world whose furniture has turned into other furniture.
     * The count assertion cannot catch it either, because the total is 166
     * either way.
     */
    private static List<Entry> buildAll() {
        List<Entry> out = new ArrayList<>(166);

        // registerWorkBench() -- the one id that leads both passes.
        out.add(WORKBENCH_ENTRY);

        addFamilies(out, LEGACY_WOODS);
        addFamilies(out, TIER_2_WOODS);

        return List.copyOf(out);
    }

    /** Appends all 15 families for one wood set, in legacy order. */
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

    /**
     * One block entity type. 1.18.2 validated each type against the blocks allowed
     * to hold it, and the valid-block sets are not derivable from the tag data --
     * they are the two families that implemented {@code BlockEntityProvider}.
     *
     * @param accepts selects the catalogue entries this type is valid for
     */
    public record BlockEntityEntry(String path, ResourceKey<BlockEntityType<?>> key,
                                   BlockEntityType.BlockEntitySupplier<?> factory,
                                   Predicate<Entry> accepts) {
    }

    /**
     * {@code log_bench} covers both low benches -- every wood at both heights --
     * and <b>not</b> {@code _log_bench_3}, which never had a block entity in
     * 1.18.2. 16 valid blocks once Tier 2 is counted.
     */
    public static final BlockEntityEntry LOG_BENCH = new BlockEntityEntry(
            "log_bench",
            ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id("log_bench")),
            LogBenchBlockEntity::new,
            entry -> entry.path().endsWith("_bench") || entry.path().endsWith("_bench_2"));

    /** {@code log_small_stool} covers plain and stripped stools alike. */
    public static final BlockEntityEntry LOG_SMALL_STOOL = new BlockEntityEntry(
            "log_small_stool",
            ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id("log_small_stool")),
            SmallStoolBlockEntity::new,
            entry -> entry.path().endsWith("_small_stool"));

    public static final List<BlockEntityEntry> BLOCK_ENTITIES = List.of(LOG_BENCH, LOG_SMALL_STOOL);

    // ------------------------------------------------------------------
    // Creative tabs
    // ------------------------------------------------------------------

    /**
     * The three custom tabs, in 1.18.2's {@code blockRegistry} declaration order.
     *
     * @param iconPath the path of the block whose item is the tab icon, from
     *                 1.18.2's {@code FabricItemGroupBuilder.build} calls
     */
    public record TabEntry(ResourceKey<CreativeModeTab> key, String path, String iconPath) {
    }

    public static final List<TabEntry> TABS = List.of(
            new TabEntry(SEATS_TAB, "seats", "stripped_oak_log_chair"),
            new TabEntry(TABLES_TAB, "tables", "oak_log_table"),
            new TabEntry(FENCES_TAB, "fences", "oak_log_fence"));

    /**
     * Builds a tab.
     *
     * <p>Both the icon and the contents resolve through {@code lookup} lazily,
     * because a tab is constructed during registration while the items it lists
     * are queued in the same pass. 26.1 splits this into
     * {@code icon(Supplier<ItemStack>)} and
     * {@code displayItems(DisplayItemsGenerator)}, so both can defer; 1.18.2's
     * {@code FabricItemGroupBuilder} took plain values and worked only because
     * registration order happened to line up.
     *
     * <p>26.1 also replaced the no-argument {@code CreativeModeTab.builder()} with
     * {@code builder(Row, int column)}. A mod tab takes a top-row slot after the
     * vanilla ones.
     *
     * <p><b>The {@code builder(Row, 0)} call below is deprecated on NeoForge and
     * that warning must not be "fixed".</b> NeoForge patches in a no-argument
     * {@code builder()} and marks the vanilla {@code builder(Row, int)} deprecated
     * — but the vanilla {@code builder(Row, int)} is the <em>only</em> one that
     * exists on Fabric, and the two are identical on NeoForge:
     *
     * <pre>
     *   // net.neoforged patched CreativeModeTab, 26.1.2.112
     *   public static CreativeModeTab.Builder builder() {
     *       return new CreativeModeTab.Builder(Row.TOP, 0);
     *   }
     *   // and builder(Row, int) deprecated, pointing at builder()
     * </pre>
     *
     * <p>So switching to the no-arg form to silence it compiles on NeoForge and
     * fails on Fabric with "cannot find symbol: method builder()". The warning is
     * the correct outcome for common code that has to serve both, and it is the
     * expected cost of keeping tab construction out of the loader seam.
     */
    public static CreativeModeTab buildTab(TabEntry tab, Function<String, Block> lookup) {
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.decor4fabric." + tab.path()))
                .icon(() -> new ItemStack(lookup.apply(tab.iconPath())))
                .displayItems((parameters, output) -> {
                    for (Entry entry : ALL) {
                        if (tab.key().equals(entry.tab())) {
                            output.accept(lookup.apply(entry.path()));
                        }
                    }
                })
                .build();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Decor4Fabric.MOD_ID, path);
    }

    private static ResourceKey<CreativeModeTab> tabKey(String path) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, id(path));
    }
}