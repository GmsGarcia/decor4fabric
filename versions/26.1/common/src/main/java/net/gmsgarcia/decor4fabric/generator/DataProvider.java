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
 * Writes the data-side files: loot tables, block tags and {@code en_us.json}.
 *
 * <p>No recipes are written. All 166 are deferred to Phase 4, which owns
 * {@code WorkBenchRecipe} and the {@code RecipeType} it registers under; the
 * crafting recipes are the one part of the tree that is a function of gameplay
 * rules rather than of a block's geometry, so generating them before the class
 * exists would mean inventing 166 files to delete.
 */
final class DataProvider {

    private DataProvider() {
    }

    private static final String DATA = "data/" + Decor4Fabric.MOD_ID;

    /**
     * A block's loot table: itself, or nothing.
     *
     * <p><b>1.18.2 gap closed here.</b> Only 88 of the 121 blocks had one. The
     * 16 fences, 16 fence gates and the workbench had none, so breaking a fence
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
     * fix. The workbench is excluded because it is metal despite its wood
     * sounds.
     */
    private static List<String> mineableWithAxe(List<BlockFacts> all) {
        List<String> out = new ArrayList<>();
        for (BlockFacts facts : all) {
            if (facts.family() == Family.FENCE || facts.family() == Family.FENCE_GATE) {
                continue;
            }
            if (facts.family() == Family.WORKBENCH) {
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
        if (facts.family() == Family.WORKBENCH) {
            return "Workbench";
        }
        WoodMeta wood = facts.wood();
        String name = wood.displayName();
        if (facts.stripped()) {
            name = "Stripped " + name;
        }
        return switch (facts.family()) {
            case WORKBENCH -> throw new IllegalStateException("handled above");
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
