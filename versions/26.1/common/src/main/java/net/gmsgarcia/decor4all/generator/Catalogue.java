package net.gmsgarcia.decor4all.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.content.DecorBlocks;
import net.gmsgarcia.decor4all.generator.Woods.WoodMeta;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

// Resolves each catalogue entry to the family it belongs to and to the state properties that
// family actually declares.
final class Catalogue {

    private Catalogue() {
    }

    // The ten block families, with the id suffix that identifies each.
    enum Family {
        CARPENTER_TABLE("carpenter_table"),
        BENCH("_bench"),
        BENCH_2("_bench_2"),
        HIGH_BENCH("_bench_3"),
        SMALL_STOOL("_small_stool"),
        CHAIR("_chair"),
        ARMCHAIR("_chair_2"),
        TABLE("_table"),
        FENCE("_fence"),
        FENCE_GATE("_fence_gate");

        private final String suffix;

        Family(String suffix) {
            this.suffix = suffix;
        }

        String suffix() {
            return suffix;
        }

        // The properties that select a model for this family, by name.
        Set<String> modelProperties() {
            return switch (this) {
                case CARPENTER_TABLE, HIGH_BENCH, CHAIR, ARMCHAIR -> Set.of("facing");
                // Benches used to model "facing" and "axe_type" here. The axe is
                // drawn from the block entity now, so a bench's only modelled
                // property is its facing -- the same as a carpentry table's.
                case BENCH, BENCH_2 -> Set.of("facing");
                case SMALL_STOOL -> Set.of("facing", "wool_color");
                case TABLE, FENCE -> Set.of("north", "east", "south", "west");
                case FENCE_GATE -> Set.of("facing", "in_wall", "open");
            };
        }
    }

    // Properties a block may declare without the model caring.
    static final Set<String> IGNORABLE = Set.of("waterlogged", "occupied", "powered");

    // One catalogue entry with everything the providers need to emit files.
    record BlockFacts(DecorBlocks.Entry entry, Family family, WoodMeta wood,
                      boolean stripped, Block block) {

        String path() {
            return entry.path();
        }

        // The log texture this block is made of: the stripped one for a stripped_ id, the plain
        // one otherwise.
        String ownTexture() {
            return stripped ? wood.strippedLogTexture() : wood.logTexture();
        }

        // The matching top texture, stripped-aware.
        String ownTopTexture() {
            return stripped ? wood.strippedLogTexture() + "_top" : wood.logTopTexture();
        }

        // The other log: plain for a stripped id, stripped for a plain one.
        String otherTexture() {
            return stripped ? wood.logTexture() : wood.strippedLogTexture();
        }

        // The other log's top texture, stripped-aware.
        String otherTopTexture() {
            return stripped ? wood.logTopTexture() : wood.strippedLogTexture() + "_top";
        }

        // minecraft:block/_planks, the small stool's particle.
        String planksTexture() {
            return wood.planksTexture();
        }

        // block.decor4all., the key useBlockDescriptionPrefix wants.
        String translationKey() {
            return "block.decor4all." + path();
        }

        // The model subdirectory this block's own models live in, or "" for families that keep
        // their model beside the blockstate.
        String modelDir() {
            return switch (family) {
                case FENCE -> stripped ? "stripped_fences" : "fences";
                case FENCE_GATE -> stripped ? "stripped_fence_gates" : "fence_gates";
                default -> "";
            };
        }

        // The model path a blockstate should reference, relative to the assets root and without a
        // namespace (block/fences/oak_log_fence_post).
        String ownModelPath(String suffix) {
            return "block/" + modelDir() + "/" + path() + suffix;
        }
    }

    // Every entry in registration order, each resolved to a family and a live block instance.
    static List<BlockFacts> all() {
        List<BlockFacts> out = new ArrayList<>(DecorBlocks.ALL.size());
        for (DecorBlocks.Entry entry : DecorBlocks.ALL) {
            out.add(new BlockFacts(
                    entry,
                    familyOf(entry.path()),
                    Woods.match(entry.path()),
                    Woods.isStripped(entry.path()),
                    entry.spec().create(entry.key())));
        }
        return out;
    }

    // The family an id belongs to.
    private static Family familyOf(String path) {
        String tail = path.startsWith("stripped_") ? path.substring("stripped_".length()) : path;
        Family best = null;
        for (Family family : Family.values()) {
            if (tail.endsWith(family.suffix())
                    && (best == null || family.suffix().length() > best.suffix().length())) {
                best = family;
            }
        }
        if (best == null) {
            throw new IllegalStateException("no family matches catalogue id " + path);
        }
        return best;
    }

    // The properties a block instance declares, in declaration order.
    static List<Property<?>> declaredProperties(Block block) {
        List<Property<?>> out = new ArrayList<>();
        block.defaultBlockState().getProperties().forEach(out::add);
        return out;
    }

    // Fails unless a family's model-affecting set and the block's real declaration agree.
    static void assertProperties(BlockFacts facts) {
        Set<String> declared = new LinkedHashSet<>();
        for (Property<?> property : declaredProperties(facts.block())) {
            declared.add(property.getName());
        }

        Set<String> expected = facts.family().modelProperties();
        Set<String> missing = new LinkedHashSet<>(expected);
        missing.removeAll(declared);
        if (!missing.isEmpty()) {
            throw new IllegalStateException(facts.path() + " (" + facts.family()
                    + ") blockstate needs properties the block does not declare: " + missing);
        }

        Set<String> unmodelled = new LinkedHashSet<>(declared);
        unmodelled.removeAll(expected);
        unmodelled.removeAll(IGNORABLE);
        if (!unmodelled.isEmpty()) {
            throw new IllegalStateException(facts.path() + " (" + facts.family()
                    + ") declares properties no model uses and that are not ignorable: " + unmodelled
                    + ". Add them to " + Family.class.getSimpleName() + ".modelProperties()"
                    + " or to IGNORABLE, do not let the blockstate ignore them.");
        }
    }

    // The axis-aligned properties, for providers that need to walk the four horizontal directions
    // in a fixed order.
    static final List<String> DIRECTIONS = List.of("north", "east", "south", "west");

    // The y rotation a model authored facing north needs for each direction.
    static int rotationFor(String facing) {
        return switch (facing) {
            case "north" -> 0;
            case "east" -> 90;
            case "south" -> 180;
            case "west" -> 270;
            default -> throw new IllegalArgumentException(facing);
        };
    }

    // A rotated, uvlocked variant of a model reference.
    record ModelRef(String model, int y, boolean uvlock) {

        static ModelRef of(String model) {
            return new ModelRef(model, 0, false);
        }

        static ModelRef rotated(String model, int y) {
            return new ModelRef(model, y, false);
        }

        static ModelRef locked(String model, int y) {
            return new ModelRef(model, y, true);
        }

        // The apply object a multipart entry needs.
        Map<String, Object> apply() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("model", Decor4All.MOD_ID + ":" + model);
            if (y != 0) {
                out.put("y", y);
            }
            if (uvlock) {
                out.put("uvlock", true);
            }
            return out;
        }
    }
}
