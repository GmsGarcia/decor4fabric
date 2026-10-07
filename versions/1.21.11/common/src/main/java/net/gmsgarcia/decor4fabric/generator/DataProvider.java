package net.gmsgarcia.decor4fabric.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.content.DecorBlocks;
import net.gmsgarcia.decor4fabric.generator.Catalogue.BlockFacts;
import net.gmsgarcia.decor4fabric.generator.Catalogue.Family;
import net.gmsgarcia.decor4fabric.generator.Woods.WoodMeta;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Writes the data-side files: loot tables, block tags, recipes and
 * {@code en_us.json}.
 *
 * <p>Recipes are here as of Phase 4, which is when {@code CarpenterTableRecipe} and
 * the {@code RecipeType} it registers under came into existence. They are
 * generated rather than hand-written for the same reason the loot tables are:
 * the recipe for a block is a function of its family and its wood, both of which
 * the catalogue already knows, and 1.18.2's 120 files were a transcription of
 * exactly that function.
 */
final class DataProvider {

    private DataProvider() {
    }

    private static final String DATA = "data/" + Decor4Fabric.MOD_ID;

    /**
     * A block's loot table: itself, or nothing.
     *
     * <p><b>1.18.2 gap closed here.</b> Only 88 of the 121 blocks had one. The
     * 16 fences, 16 fence gates and the carpentry table had none, so breaking a fence
     * in 1.18.2 dropped nothing at all. Since a loot table is required for a
     * block to drop <em>anything</em> in 1.21.2+, carrying that over would have
     * made the port strictly worse than what it replaced, so all 166 get one.
     * Each table carries a {@code random_sequence}, which 1.18.2 did not have
     * and which stops the drop-rolling RNG from being reseeded per block.
     */
    static Map<String, Object> lootTable(BlockFacts facts) {
        String id = Decor4Fabric.MOD_ID + ":" + facts.path();
        return Json.obj(
                "type", "minecraft:block",
                "random_sequence", Decor4Fabric.MOD_ID + ":blocks/" + facts.path(),
                "pools", Json.arr(Json.obj(
                        "rolls", 1,
                        "entries", Json.arr(Json.obj(
                                "type", "minecraft:item",
                                "name", id)),
                        "conditions", Json.arr(Json.obj(
                                "condition", "minecraft:survives_explosion")))));
    }

    /**
     * Every block tag, keyed by its path under {@code data/}.
     *
     * <p>The five mod tags are derived from {@link DecorBlocks.Entry#tags()}
     * rather than from a list here, so adding a block to a family automatically
     * puts it in the right tag. The three vanilla tags are hand-listed because
     * vanilla owns their contents and the mod only adds to them.
     *
     * <p>Values are sorted by id. The tree is committed and diffed by hand, and
     * 1.18.2's tags were alphabetical per wood, so sorting keeps a new wood's
     * entries in one predictable place instead of at the end.
     */
    static Map<String, Object> tags(List<BlockFacts> all) {
        Map<String, Map<String, Boolean>> byTag = new TreeMap<>();

        for (BlockFacts facts : all) {
            for (TagKey<Block> tag : facts.entry().tags()) {
                String path = pathOf(tag);
                byTag.computeIfAbsent(path, p -> new LinkedHashMap<>())
                        .put(Decor4Fabric.MOD_ID + ":" + facts.path(), true);
            }
        }

        // Vanilla-owned tags the mod contributes to. wooden_fences is what makes
        // fences burnable as a group; mineable/axe is what makes an axe the
        // right tool.
        //
        // These are keyed as "data/<ns>/tags/..." paths like every other entry
        // in this map, not as "minecraft:tags/..." resource locations. A colon
        // is not a legal path character on Windows, so a location-shaped key
        // would fail at write time rather than at review time.
        List<String> fences = idsOf(all, Family.FENCE);
        byTag.put("data/minecraft/tags/block/wooden_fences.json", sorted(fences));
        byTag.put("data/minecraft/tags/block/fences.json", sorted(fences));
        byTag.put("data/minecraft/tags/block/fence_gates.json",
                sorted(idsOf(all, Family.FENCE_GATE)));
        byTag.put("data/minecraft/tags/block/mineable/axe.json", sorted(mineableWithAxe(all)));

        Map<String, Object> out = new LinkedHashMap<>();
        byTag.forEach((path, ids) -> out.put(path, Json.obj(
                "replace", false,
                "values", Json.strings(ids.keySet()))));
        return out;
    }

    /**
     * The blocks that are mineable with an axe.
     *
     * <p>Everything wooden except fences and fence gates, matching 1.18.2's 88
     * entries. Fences and gates are excluded deliberately: vanilla puts wooden
     * fences in {@code mineable/axe} itself, and 1.18.2 did not add its own to
     * that tag, so a port that did would be a behaviour change rather than a
     * fix. The carpentry table is excluded because it is metal despite its wood
     * sounds.
     */
    private static List<String> mineableWithAxe(List<BlockFacts> all) {
        List<String> out = new ArrayList<>();
        for (BlockFacts facts : all) {
            if (facts.family() == Family.FENCE || facts.family() == Family.FENCE_GATE) {
                continue;
            }
            if (facts.family() == Family.CARPENTER_TABLE) {
                continue;
            }
            out.add(Decor4Fabric.MOD_ID + ":" + facts.path());
        }
        return out;
    }

    private static List<String> idsOf(List<BlockFacts> all, Family family) {
        List<String> out = new ArrayList<>();
        for (BlockFacts facts : all) {
            if (facts.family() == family) {
                out.add(Decor4Fabric.MOD_ID + ":" + facts.path());
            }
        }
        return out;
    }

    private static Map<String, Boolean> sorted(List<String> ids) {
        Map<String, Boolean> out = new TreeMap<>();
        ids.forEach(id -> out.put(id, true));
        return out;
    }

    /**
     * The {@code data/}-relative path of a block tag, e.g.
     * {@code data/decor4fabric/tags/block/benches.json}.
     *
     * <p>{@code block} is written literally rather than read off
     * {@code Registries.BLOCK}. It is the vanilla tag directory name and not
     * something the registry key is obliged to keep matching, and the tag file
     * has to be where the game looks for it.
     */
    private static String pathOf(TagKey<Block> tag) {
        return DATA + "/tags/block/" + tag.location().getPath() + ".json";
    }

    /**
     * The carpentry table recipe for one block, keyed by its {@code data/} path.
     *
     * <p>1.18.2 wrote 120 of these by hand and they fall into nine families with
     * a fixed yield each. The yields are not derivable from the block's
     * geometry -- a chair yields 1 and a bench yields 3 -- so they are tabulated
     * here from the recovered files rather than computed:
     *
     * <pre>
     * BENCH       3    BENCH_2      2    HIGH_BENCH   2
     * CHAIR       1    ARMCHAIR     1
     * TABLE       2    SMALL_STOOL  2
     * FENCE       3    FENCE_GATE   2
     * </pre>
     *
     * <p>The ingredient is the wood's own log, and the
     * <em>stripped</em> log for a {@code stripped_} block, which is what 1.18.2
     * did: 48 of its 120 recipes were stripped-to-stripped, so a stripped chair
     * never appears in the recipe list when a plain log is in the slot.
     *
     * <h2>The JSON changed shape, not meaning</h2>
     *
     * <p>1.18.2 emitted {@code "ingredient": {"item": ...}}, {@code "result":
     * "ns:id"} and a sibling {@code "count"}. Modern {@link
     * net.minecraft.world.item.crafting.Ingredient#CODEC} takes a list, and a
     * modern {@link net.minecraft.world.item.ItemStack} codec takes an object
     * carrying the count, so the count moves inside the result and the
     * ingredient gains a bracket:
     *
     * <pre>
     * 1.18.2  "ingredient": {"item": "minecraft:oak_log"},
     *         "result": "decor4fabric:oak_log_bench", "count": 3
     * 26.x    "ingredient": [{"item": "minecraft:oak_log"}],
     *         "result": {"id": "decor4fabric:oak_log_bench", "count": 3}
     * </pre>
     *
     * <p>The emitted shape is identical on 1.21.11, which parses it with its own
     * hand-written copy of the same codec -- see {@code CarpenterTableRecipe} -- so
     * one recipe file serves every target.
     */
    static Map<String, Map<String, Object>> recipes(List<BlockFacts> all) {
        Map<String, Map<String, Object>> out = new LinkedHashMap<>();
        for (BlockFacts facts : all) {
            // Every block's recipe is keyed by its own path, including the
            // carpentry table's -- the carpentry table is the one block not made at a
            // carpentry table, so it gets a vanilla crafting recipe instead, but it
            // still lands at the same predictable path.
            //
            // The directory is "recipe", singular. 1.18.2 used "recipes", and
            // that plural is the historical spelling here because it was copied
            // out of the old tree -- but it was renamed before 1.21 and every
            // supported target reads the singular form only. Verified against
            // the vanilla jars: 26.1 and 1.21.11 each ship ~1500 entries under
            // data/minecraft/recipe/ and contain no data/minecraft/recipes/ at
            // all. Emitting the plural here would not fail the build, the jars
            // would still contain all 166 files, and the carpentry table would open to
            // an empty grid because nothing ever scans the directory.
            out.put(DATA + "/recipe/" + facts.path() + ".json",
                    facts.family() == Family.CARPENTER_TABLE
                            ? carpenterTableCraftingRecipe()
                            : carpenterTableRecipe(facts));
        }
        return out;
    }

    /**
     * One carpentry table recipe: the wood's own log in, {@link #yieldOf} of the block out.
     *
     * <p>The ingredient is a one-element array of a bare item id, not the
     * {@code {"item": ...}} object 1.18.2 wrote. {@code Ingredient.CODEC} is a
     * {@code HolderSetCodec} and the object form was dropped from it; the only shapes
     * it still takes are a bare id string, a bare tag string, or an array of those.
     * An array of an object satisfies neither branch, and the datapack load reports
     *
     * <pre>
     * Failed to parse either. First: Not a JSON object: [{"item":"minecraft:oak_log"}];
     * Second: Not a string: {"item":"minecraft:oak_log"}; List must have contents
     * </pre>
     *
     * for all 165 recipes. Caught by running the client, not by the generator: it
     * emitted what it was told, and the build was green.
     *
     * <p>The array form is used rather than a bare string because a single-element
     * array is what makes adding a second accepted input a one-token change, and both
     * parse identically.
     */
    private static Map<String, Object> carpenterTableRecipe(BlockFacts facts) {
        return Json.obj(
                "type", Decor4Fabric.MOD_ID + ":carpenter_table",
                "ingredient", Json.arr(ingredientOf(facts)),
                "result", Json.obj(
                        "id", Decor4Fabric.MOD_ID + ":" + facts.path(),
                        "count", yieldOf(facts)));
    }

    /**
     * The vanilla crafting recipe that makes the carpentry table itself.
     *
     * <p>Reproduced from 1.18.2, including the deliberate oddity that the pattern
     * uses blue dye as the key symbol: a shaped recipe's key is arbitrary, and
     * 1.18.2's was {@code #} mapped to blue dye.
     *
     * <p>The four key entries are bare id strings for the same reason
     * {@link #carpenterTableRecipe}'s ingredient is: a shaped recipe's key values go
     * through the same {@code HolderSetCodec}, so 1.18.2's {@code {"item": ...}} fails
     * with "No key fabric:type in MapLike[...]" and then "Not a string".
     */
    private static Map<String, Object> carpenterTableCraftingRecipe() {
        return Json.obj(
                "type", "minecraft:crafting_shaped",
                "pattern", Json.arr("#P", "SS", "OO"),
                "key", Json.obj(
                        "#", "minecraft:blue_dye",
                        "P", "minecraft:paper",
                        "S", "minecraft:stripped_oak_log",
                        "O", "minecraft:oak_log"),
                "result", Json.obj("id", Decor4Fabric.MOD_ID + ":carpenter_table", "count", 1));
    }

    /**
     * The vanilla log item a block is cut from.
     *
     * <p>{@link WoodMeta#segment()} is the middle of the block id and is already
     * the vanilla log's own id -- {@code dark_oak_log}, {@code crimson_stem} --
     * which is why this needs no per-wood table of item ids.
     */
    private static String ingredientOf(BlockFacts facts) {
        String segment = facts.wood().segment();
        return "minecraft:" + (facts.stripped() ? "stripped_" + segment : segment);
    }

    /** How many of a block one log yields. Tabulated; see {@link #recipes}. */
    private static int yieldOf(BlockFacts facts) {
        return switch (facts.family()) {
            case CARPENTER_TABLE -> throw new IllegalStateException("handled above");
            case BENCH, FENCE -> 3;
            case BENCH_2, HIGH_BENCH, SMALL_STOOL, TABLE, FENCE_GATE -> 2;
            case CHAIR, ARMCHAIR -> 1;
        };
    }

    /**
     * {@code assets/decor4fabric/lang/en_us.json}.
     *
     * <p>Sorted by key so the file reads alphabetically, which is how 1.18.2
     * grouped it (by family, then by wood) and how every other lang file in
     * Minecraft is written.
     *
     * <p><b>1.18.2 typo fixed here:</b> {@code crimson_stem_fence} and
     * {@code crimson_stem_fence_gate} were "Crimson <b>Steam</b> Fence" and
     * "Crimson Steam Fence Gate". Generating the names from the segment makes
     * them "Crimson Stem", which is what the block is.
     *
     * <p>The family suffixes are the part that is genuinely per-family rather
     * than derivable: {@code _bench} is "Small Oak Log" while {@code _bench_2}
     * is "Oak Log Outdoor Bench", so the table below is the authority on how
     * 1.18.2 named each shape.
     */
    static Map<String, Object> lang(List<BlockFacts> all) {
        Map<String, Object> out = new TreeMap<>();
        out.put("container.decor4fabric.carpenter_table", "Carpentry Table");
        out.put("itemGroup.decor4fabric.seats", "Seats");
        out.put("itemGroup.decor4fabric.tables", "Tables");
        out.put("itemGroup.decor4fabric.fences", "Fences");

        for (BlockFacts facts : all) {
            out.put(facts.translationKey(), displayName(facts));
        }
        return out;
    }

    /** A block's English display name, e.g. {@code Small Stripped Oak Log Stool}. */
    private static String displayName(BlockFacts facts) {
        if (facts.family() == Family.CARPENTER_TABLE) {
            return "Carpentry Table";
        }
        WoodMeta wood = facts.wood();
        String name = wood.displayName();
        if (facts.stripped()) {
            name = "Stripped " + name;
        }
        return switch (facts.family()) {
            case CARPENTER_TABLE -> throw new IllegalStateException("handled above");
            // Benches have no stripped variant, so `name` is never prefixed here.
            // The "Small" prefix is the odd one out in 1.18.2's naming and is
            // reproduced exactly.
            case BENCH -> "Small " + name;
            case BENCH_2 -> name + " Outdoor Bench";
            case HIGH_BENCH -> name + " Bench";
            case SMALL_STOOL -> "Small " + name + " Stool";
            case CHAIR -> name + " Chair";
            case ARMCHAIR -> name + " Chair With Armrests";
            case TABLE -> name + " Table";
            case FENCE -> name + " Fence";
            case FENCE_GATE -> name + " Fence Gate";
        };
    }
}
