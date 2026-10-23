package net.gmsgarcia.decor4all.generator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.gmsgarcia.decor4all.generator.Catalogue.BlockFacts;
import net.minecraft.world.item.DyeColor;

// Writes the block models, the shared axe and carpet models, and the item definitions.
final class ModelProvider {

    private ModelProvider() {
    }

    // The relative output paths, with their documents, for one block.
    static Map<String, Object> filesFor(BlockFacts facts) {
        Map<String, Object> out = new LinkedHashMap<>();
        switch (facts.family()) {
            case CARPENTER_TABLE -> {
                // carpenter_table.json is hand-authored geometry, so the only
                // generated file is the item definition pointing at it.
                out.put("items/" + facts.path() + ".json", itemDefinition(facts));
                out.put("models/item/" + facts.path() + ".json",
                        Json.obj("parent", "decor4all:block/" + facts.path()));
            }
            case BENCH -> single(facts, out, benchParent(0), Json.obj(
                    "sides", facts.ownTexture(),
                    "top", facts.ownTopTexture(),
                    "particle", facts.ownTexture()));
            case BENCH_2 -> single(facts, out, benchParent(1), Json.obj(
                    "top", facts.ownTopTexture(),
                    "sides", facts.ownTexture(),
                    "stripped_sides", facts.otherTexture(),
                    "particle", facts.ownTexture()));
            case HIGH_BENCH -> single(facts, out, benchParent(2), Json.obj(
                    "top", facts.ownTopTexture(),
                    "sides", facts.ownTexture(),
                    "stripped_sides", facts.otherTexture(),
                    "particle", facts.ownTexture()));
            case SMALL_STOOL -> single(facts, out, "models/small_stool_model", Json.obj(
                    "log", facts.ownTexture(),
                    "stripped_log", facts.otherTexture(),
                    "top", facts.otherTopTexture(),
                    "particle", facts.planksTexture()));
            case CHAIR -> single(facts, out, "models/log_chair_model", Json.obj(
                    "log", facts.otherTexture(),
                    "stripped_log", facts.ownTexture(),
                    "particle", facts.otherTexture()));
            case ARMCHAIR -> single(facts, out, "models/log_chair_2_model", Json.obj(
                    "log", facts.otherTexture(),
                    "stripped_log", facts.ownTexture(),
                    "particle", facts.otherTexture()));
            case TABLE -> table(facts, out);
            case FENCE -> fence(facts, out);
            case FENCE_GATE -> gate(facts, out);
        }
        return out;
    }

    // The parent model for one of the three bench heights.
    private static String benchParent(int index) {
        return "models/" + (index == 0 ? "log_bench_model" : "log_bench_model_" + (index + 1));
    }

    // A family that needs one block model plus the two item files.
    private static void single(BlockFacts facts, Map<String, Object> out,
                              String parent, Map<String, Object> textures) {
        out.put("models/block/" + facts.path() + ".json",
                Json.obj("parent", "decor4all:block/" + parent, "textures", textures));
        out.put("models/item/" + facts.path() + ".json",
                Json.obj("parent", "decor4all:block/" + facts.path()));
        out.put("items/" + facts.path() + ".json", itemDefinition(facts));
    }

    // A table: top, leg and an inventory model for the item.
    private static void table(BlockFacts facts, Map<String, Object> out) {
        String path = facts.path();
        out.put("models/block/" + path + "_top.json", Json.obj(
                "parent", "decor4all:block/models/log_table_top_model",
                "textures", Json.obj(
                        "log", facts.ownTexture(),
                        "particle", facts.ownTexture())));
        out.put("models/block/" + path + "_leg.json", Json.obj(
                "parent", "decor4all:block/models/log_table_leg_model",
                "textures", Json.obj(
                        "stripped", facts.otherTexture(),
                        "top", facts.wood().logTopTexture(),
                        "particle", facts.ownTexture())));
        out.put("models/block/" + path + "_inventory.json", Json.obj(
                "parent", "decor4all:block/models/log_table_inventory_model",
                "textures", Json.obj(
                        "stripped", facts.otherTexture(),
                        "top", facts.wood().logTopTexture(),
                        "log", facts.ownTexture(),
                        "particle", facts.ownTexture())));
        out.put("models/item/" + path + ".json",
                Json.obj("parent", "decor4all:block/" + path + "_inventory"));
        out.put("items/" + path + ".json", itemDefinition(facts));
    }

    // A fence: post, side and an inventory model for the item.
    private static void fence(BlockFacts facts, Map<String, Object> out) {
        String path = facts.path();
        String dir = facts.modelDir() + "/";
        Map<String, Object> textures = Json.obj(
                "top", facts.ownTopTexture(),
                "sides", facts.ownTexture());
        out.put("models/block/" + dir + path + "_post.json",
                Json.obj("parent", "decor4all:block/models/fence_post", "textures", textures));
        out.put("models/block/" + dir + path + "_side.json",
                Json.obj("parent", "decor4all:block/models/fence_side", "textures", textures));
        out.put("models/block/" + dir + path + "_inventory.json",
                Json.obj("parent", "decor4all:block/models/fence_inventory",
                        "textures", textures));
        out.put("models/item/" + path + ".json",
                Json.obj("parent", "decor4all:block/" + dir + path + "_inventory"));
        out.put("items/" + path + ".json", itemDefinition(facts));
    }

    // A fence gate: four models against vanilla's own templates.
    private static void gate(BlockFacts facts, Map<String, Object> out) {
        String path = facts.path();
        String dir = facts.modelDir() + "/";
        Map<String, Object> textures = Json.obj("texture", facts.ownTexture());
        for (String suffix : new String[] {"", "_open", "_wall", "_wall_open"}) {
            String template = suffix.isEmpty() ? "template_fence_gate"
                    : "template_fence_gate" + suffix;
            out.put("models/block/" + dir + path + suffix + ".json",
                    Json.obj("parent", "minecraft:block/" + template, "textures", textures));
        }
        out.put("models/item/" + path + ".json",
                Json.obj("parent", "decor4all:block/" + dir + path));
        out.put("items/" + path + ".json", itemDefinition(facts));
    }

    // The modern items/.json form.
    private static Map<String, Object> itemDefinition(BlockFacts facts) {
        return Json.obj("model", Json.obj(
                "type", "minecraft:model",
                "model", "decor4all:item/" + facts.path()));
    }

    // The wood-independent models, written once.
    static Map<String, Object> sharedModels() {
        Map<String, Object> out = new LinkedHashMap<>();

        for (DyeColor dye : DyeColor.values()) {
            String wool = "minecraft:block/" + dye.getSerializedName() + "_wool";
            out.put("models/block/repetitive_models/small_stool_carpet/"
                            + dye.getSerializedName() + ".json",
                    Json.obj(
                            "parent", "decor4all:block/models/small_stool_carpet_model",
                            "textures", Json.obj("wool", wool, "particle", wool)));
        }
        return out;
    }
}
