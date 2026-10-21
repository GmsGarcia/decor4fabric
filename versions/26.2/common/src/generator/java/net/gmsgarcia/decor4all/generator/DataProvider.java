package net.gmsgarcia.decor4all.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.content.DecorBlocks;
import net.gmsgarcia.decor4all.generator.Catalogue.BlockFacts;
import net.gmsgarcia.decor4all.generator.Catalogue.Family;
import net.gmsgarcia.decor4all.generator.Woods.WoodMeta;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

// Writes the data-side files: loot tables, block tags, recipes and en_us.json.
final class DataProvider {

    private DataProvider() {
    }

    private static final String DATA = "data/" + Decor4All.MOD_ID;

    // A block's loot table: itself, or nothing.
    static Map<String, Object> lootTable(BlockFacts facts) {
        String id = Decor4All.MOD_ID + ":" + facts.path();
        return Json.obj(
                "type", "minecraft:block",
                "random_sequence", Decor4All.MOD_ID + ":blocks/" + facts.path(),
                "pools", Json.arr(Json.obj(
                        "rolls", 1,
                        "entries", Json.arr(Json.obj(
                                "type", "minecraft:item",
                                "name", id)),
                        "conditions", Json.arr(Json.obj(
                                "condition", "minecraft:survives_explosion")))));
    }

    // Every block tag, keyed by its path under data/.
    static Map<String, Object> tags(List<BlockFacts> all) {
        Map<String, Map<String, Boolean>> byTag = new TreeMap<>();

        for (BlockFacts facts : all) {
            for (TagKey<Block> tag : facts.entry().tags()) {
                String path = pathOf(tag);
                byTag.computeIfAbsent(path, p -> new LinkedHashMap<>())
                        .put(Decor4All.MOD_ID + ":" + facts.path(), true);
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

    // The blocks that are mineable with an axe.
    private static List<String> mineableWithAxe(List<BlockFacts> all) {
        List<String> out = new ArrayList<>();
        for (BlockFacts facts : all) {
            if (facts.family() == Family.FENCE || facts.family() == Family.FENCE_GATE) {
                continue;
            }
            if (facts.family() == Family.CARPENTER_TABLE) {
                continue;
            }
            out.add(Decor4All.MOD_ID + ":" + facts.path());
        }
        return out;
    }

    private static List<String> idsOf(List<BlockFacts> all, Family family) {
        List<String> out = new ArrayList<>();
        for (BlockFacts facts : all) {
            if (facts.family() == family) {
                out.add(Decor4All.MOD_ID + ":" + facts.path());
            }
        }
        return out;
    }

    private static Map<String, Boolean> sorted(List<String> ids) {
        Map<String, Boolean> out = new TreeMap<>();
        ids.forEach(id -> out.put(id, true));
        return out;
    }

    // The data/-relative path of a block tag, e.g. data/decor4all/tags/block/benches.json.
    private static String pathOf(TagKey<Block> tag) {
        return DATA + "/tags/block/" + tag.location().getPath() + ".json";
    }

    // The carpentry table recipe for one block, keyed by its data/ path.
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
            if (facts.family() == Family.CARPENTER_TABLE) {
                out.put(DATA + "/recipe/" + facts.path() + ".json",
                        carpenterTableCraftingRecipe(true));
            } else {
                out.put(DATA + "/recipe/" + facts.path() + ".json",
                        carpenterTableRecipe(facts));
            }
        }
        return out;
    }

    // Recipe-unlock advancements. Currently grants the carpentry table crafting recipes when the
    // player first picks up any planks.
    static Map<String, Map<String, Object>> advancements() {
        Map<String, Map<String, Object>> out = new LinkedHashMap<>();
        out.put(DATA + "/advancement/recipes/misc/carpenter_table_unlock_on_planks.json",
                Json.obj(
                        "parent", "minecraft:recipes/root",
                        "criteria", Json.obj(
                                "has_planks", Json.obj(
                                        "trigger", "minecraft:inventory_changed",
                                        "conditions", Json.obj(
                                                "items", Json.arr(
                                                        Json.obj("items", "#minecraft:planks")))),
                                "has_the_recipe", Json.obj(
                                        "trigger", "minecraft:recipe_unlocked",
                                        "conditions", Json.obj(
                                                "recipe", Decor4All.MOD_ID + ":carpenter_table"))),
                        "requirements", Json.arr(
                                Json.arr("has_the_recipe", "has_planks")),
                        "rewards", Json.obj(
                                "recipes", Json.arr(
                                        Decor4All.MOD_ID + ":carpenter_table",
                                        Decor4All.MOD_ID + ":carpenter_table_mirror"))));
        return out;
    }

    // One carpentry table recipe: the wood's own log in, #yieldOf of the block out.
    private static Map<String, Object> carpenterTableRecipe(BlockFacts facts) {
        return Json.obj(
                "type", Decor4All.MOD_ID + ":carpenter_table",
                "ingredient", Json.arr(ingredientOf(facts)),
                "result", Json.obj(
                        "id", Decor4All.MOD_ID + ":" + facts.path(),
                        "count", yieldOf(facts)));
    }

    // One of the two vanilla crafting recipes that make the carpentry table: four planks plus one
    // paper and one iron ingot, laid out as a 3x2 grid with the paper and iron in the top row.
    private static Map<String, Object> carpenterTableCraftingRecipe(boolean paperOnLeft) {
        return Json.obj(
                "type", "minecraft:crafting_shaped",
                "group", Decor4All.MOD_ID + ":carpenter_table",
                "pattern", paperOnLeft
                        ? Json.arr("PI", "##", "##")
                        : Json.arr("IP", "##", "##"),
                "key", Json.obj(
                        "#", "#minecraft:planks",
                        "P", "minecraft:paper",
                        "I", "minecraft:iron_ingot"),
                "result", Json.obj("id", Decor4All.MOD_ID + ":carpenter_table", "count", 1));
    }

    // The vanilla log item a block is cut from.
    private static String ingredientOf(BlockFacts facts) {
        String segment = facts.wood().segment();
        return "minecraft:" + (facts.stripped() ? "stripped_" + segment : segment);
    }

    // How many of a block one log yields. Tabulated; see #recipes.
    private static int yieldOf(BlockFacts facts) {
        return switch (facts.family()) {
            case CARPENTER_TABLE -> throw new IllegalStateException("handled above");
            case BENCH, FENCE -> 3;
            case BENCH_2, HIGH_BENCH, SMALL_STOOL, TABLE, FENCE_GATE -> 2;
            case CHAIR, ARMCHAIR -> 1;
        };
    }

    // assets/decor4all/lang/en_us.json.
    static Map<String, Object> lang(List<BlockFacts> all) {
        Map<String, Object> out = new TreeMap<>();
        out.put("container.decor4all.carpenter_table", "Carpentry Table");
        out.put("itemGroup.decor4all.seats", "Seats");
        out.put("itemGroup.decor4all.tables", "Tables");
        out.put("itemGroup.decor4all.fences", "Fences");

        for (BlockFacts facts : all) {
            out.put(facts.translationKey(), displayName(facts));
        }
        return out;
    }

    // A block's English display name, e.g. Small Stripped Oak Log Stool.
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
