package net.gmsgarcia.decor4fabric.generator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.gmsgarcia.decor4fabric.generator.Catalogue.BlockFacts;
import net.minecraft.world.item.DyeColor;

/**
 * Writes the block models, the shared axe and carpet models, and the item
 * definitions.
 *
 * <p><b>What is generated and what is not.</b> The nineteen files under
 * {@code models/block/models/} and {@code workbench.json} are hand-authored
 * geometry -- cuboids, rotations and display transforms -- and they are copied
 * into {@code src/main/resources} unchanged rather than generated. Everything
 * written here is a thin {@code parent} + {@code textures} wrapper, which is a
 * pure function of the family, the wood and the stripped flag. That split is
 * what makes the output reviewable: a diff in {@code src/main/generated} is
 * always a texture or a path change, never a moved vertex.
 *
 * <p>Item definitions are emitted in both modern forms because 26.x still has
 * both: {@code assets/<ns>/items/<id>.json} points at
 * {@code minecraft:item/<id>}, which in turn is the model wrapper under
 * {@code models/item/}. Vanilla keeps the legacy directory during the
 * transition, so a resource pack that only ships one of the two still works.
 */
final class ModelProvider {

    private ModelProvider() {
    }

    /**
     * The relative output paths, with their documents, for one block.
     *
     * <p>Returning every file at once is what lets {@link ResourceGenerator}
     * assert the registry against the tree: the assertion needs the complete set
     * of files a block owns, including the three a table needs and the four a
     * gate needs, and a per-kind writer would make that impossible to state.
     */
    static Map<String, Object> filesFor(BlockFacts facts) {
        Map<String, Object> out = new LinkedHashMap<>();
        switch (facts.family()) {
            case WORKBENCH -> {
                // workbench.json is hand-authored geometry, so the only
                // generated file is the item definition pointing at it.
                out.put("items/" + facts.path() + ".json", itemDefinition(facts));
                out.put("models/item/" + facts.path() + ".json",
                        Json.obj("parent", "decor4fabric:block/" + facts.path()));
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

    /**
     * The parent model for one of the three bench heights.
     *
     * <p>1.18.2 named these {@code log_bench_model}, {@code log_bench_model_2}
     * and {@code log_bench_model_3}, and the number is the height, not an index
     * into this list.
     */
    private static String benchParent(int index) {
        return "models/" + (index == 0 ? "log_bench_model" : "log_bench_model_" + index);
    }

    /** A family that needs one block model plus the two item files. */
    private static void single(BlockFacts facts, Map<String, Object> out,
                              String parent, Map<String, Object> textures) {
        out.put("models/block/" + facts.path() + ".json",
                Json.obj("parent", "decor4fabric:block/" + parent, "textures", textures));
        out.put("models/item/" + facts.path() + ".json",
                Json.obj("parent", "decor4fabric:block/" + facts.path()));
        out.put("items/" + facts.path() + ".json", itemDefinition(facts));
    }

    /**
     * A table: top, leg and an inventory model for the item.
     *
     * <p><b>1.18.2 bug fixed here.</b> Both table parents
     * ({@code log_table_leg_model} and {@code log_table_inventory_model}) draw
     * the leg's four side faces from a texture slot named {@code stripped} and
     * its top and bottom from {@code top}. The name is historical: the slot
     * simply means "the leg's side texture". 1.18.2 had the two files swapped
     * -- {@code oak_log_table_leg} pointed {@code stripped} at
     * {@code stripped_oak_log} and {@code stripped_oak_log_table_leg} pointed it
     * at {@code oak_log} -- so every table in the mod had legs cut from the
     * other variant of its own wood. A leg is part of its table, so the slot
     * gets the block's own log texture and the two files are no longer
     * different from each other.
     *
     * <p>The {@code top} slot is stripped-aware for the same reason: 1.18.2
     * pointed both files at {@code oak_log_top}, so a stripped table's leg
     * showed bark rings on its cut end.
     */
    private static void table(BlockFacts facts, Map<String, Object> out) {
        String path = facts.path();
        out.put("models/block/" + path + "_top.json", Json.obj(
                "parent", "decor4fabric:block/models/log_table_top_model",
                "textures", Json.obj(
                        "log", facts.ownTexture(),
                        "particle", facts.ownTexture())));
        out.put("models/block/" + path + "_leg.json", Json.obj(
                "parent", "decor4fabric:block/models/log_table_leg_model",
                "textures", Json.obj(
                        "stripped", facts.ownTexture(),
                        "top", facts.ownTopTexture(),
                        "particle", facts.ownTexture())));
        out.put("models/block/" + path + "_inventory.json", Json.obj(
                "parent", "decor4fabric:block/models/log_table_inventory_model",
                "textures", Json.obj(
                        "stripped", facts.ownTexture(),
                        "top", facts.ownTopTexture(),
                        "log", facts.ownTexture(),
                        "particle", facts.ownTexture())));
        out.put("models/item/" + path + ".json",
                Json.obj("parent", "decor4fabric:block/" + path + "_inventory"));
        out.put("items/" + path + ".json", itemDefinition(facts));
    }

    /** A fence: post, side and an inventory model for the item. */
    private static void fence(BlockFacts facts, Map<String, Object> out) {
        String path = facts.path();
        String dir = facts.modelDir() + "/";
        Map<String, Object> textures = Json.obj(
                "top", facts.ownTopTexture(),
                "sides", facts.ownTexture());
        out.put("models/block/" + dir + path + "_post.json",
                Json.obj("parent", "decor4fabric:block/models/fence_post", "textures", textures));
        out.put("models/block/" + dir + path + "_side.json",
                Json.obj("parent", "decor4fabric:block/models/fence_side", "textures", textures));
        out.put("models/block/" + dir + path + "_inventory.json",
                Json.obj("parent", "decor4fabric:block/models/fence_inventory",
                        "textures", textures));
        out.put("models/item/" + path + ".json",
                Json.obj("parent", "decor4fabric:block/" + dir + path + "_inventory"));
        out.put("items/" + path + ".json", itemDefinition(facts));
    }

    /** A fence gate: four models against vanilla's own templates. */
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
                Json.obj("parent", "decor4fabric:block/" + dir + path));
        out.put("items/" + path + ".json", itemDefinition(facts));
    }

    /**
     * The modern {@code items/<id>.json} form.
     *
     * <p>26.x replaced the bare {@code {"parent": ...}} item model with a typed
     * model reference, and the legacy file under {@code models/item/} still has
     * to exist as the thing that reference points at.
     */
    private static Map<String, Object> itemDefinition(BlockFacts facts) {
        return Json.obj("model", Json.obj(
                "type", "minecraft:model",
                "model", "minecraft:item/" + facts.path()));
    }

    /**
     * The wood-independent models, written once.
     *
     * <p>These twelve axe models and sixteen carpet models are keyed only by
     * material or dye, never by wood, so they are emitted from the first bench
     * and first stool of the catalogue rather than per block. Generating them
     * per block would produce 132 identical duplicates under twelve names.
     */
    static Map<String, Object> sharedModels() {
        Map<String, Object> out = new LinkedHashMap<>();

        List<String> axes = List.of("wooden", "stone", "iron", "golden", "diamond", "netherite");
        for (int i = 0; i < 2; i++) {
            String dir = i == 0 ? "log_bench_axe" : "log_bench_2_axe";
            String parent = i == 0 ? "models/log_bench_model_axe_model"
                    : "models/log_bench_model_2_axe_model";
            String prefix = i == 0 ? "log_bench_" : "log_bench_2_";
            for (String axe : axes) {
                out.put("models/block/repetitive_models/" + dir + "/" + prefix + axe + "_axe.json",
                        Json.obj(
                                "parent", "decor4fabric:block/" + parent,
                                "textures", Json.obj(
                                        "axe_rot", "decor4fabric:item/" + axe + "_axe_rot",
                                        "axe_rot_mir", "decor4fabric:item/" + axe + "_axe_rot_mir")));
            }
        }

        for (DyeColor dye : DyeColor.values()) {
            String wool = "minecraft:block/" + dye.getSerializedName() + "_wool";
            out.put("models/block/repetitive_models/small_stool_carpet/"
                            + dye.getSerializedName() + ".json",
                    Json.obj(
                            "parent", "decor4fabric:block/models/small_stool_carpet_model",
                            "textures", Json.obj("wool", wool, "particle", wool)));
        }
        return out;
    }
}
