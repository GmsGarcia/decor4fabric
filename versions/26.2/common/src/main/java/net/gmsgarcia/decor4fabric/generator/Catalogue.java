package net.gmsgarcia.decor4fabric.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.content.DecorBlocks;
import net.gmsgarcia.decor4fabric.generator.Woods.WoodMeta;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Resolves each catalogue entry to the family it belongs to and to the state
 * properties that family actually declares.
 *
 * <p>This is the piece that keeps the generator honest. 1.18.2 hand-wrote 121
 * blockstate files against a state definition that no longer exists: since then
 * every seating family gained {@code waterlogged}, benches and stools gained
 * {@code occupied}, and gates carry {@code powered}. A generator that re-listed
 * those properties by hand would drift the first time a family changed, so this
 * class derives two sets from each block instance and {@link #assertProperties}
 * proves they agree.
 *
 * <p>The distinction that matters is <b>declared</b> versus <b>model-affecting</b>.
 * Vanilla does not enumerate every state property in a blockstate key:
 * {@code assets/minecraft/blockstates/oak_stairs.json} omits {@code waterlogged}
 * entirely, and {@code chiseled_bookshelf.json} omits it while enumerating six
 * {@code occupied} properties, because variant keys are <em>partial</em> matches
 * and an absent property means "any value". A block that has a property the
 * model ignores therefore needs no extra variants, which is precisely why adding
 * {@code occupied} in Phase 2 did not multiply the bench files, and why Phase 3
 * can add it to the remaining seatable families without touching this generator.
 */
final class Catalogue {

    private Catalogue() {
    }

    /**
     * The ten block families, with the id suffix that identifies each.
     *
     * <p>{@code suffix} is matched longest-first, which is what stops
     * {@code _fence} from swallowing {@code _fence_gate} and {@code _chair} from
     * swallowing {@code _chair_2}. Those two collisions are silent: the wrong
     * family produces a well-formed blockstate that points at the wrong model.
     */
    enum Family {
        WORKBENCH("workbench"),
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

        /**
         * The properties that select a model for this family, by name.
         *
         * <p>Everything not listed here is either unmodelled (see
         * {@link #IGNORABLE}) or a bug, and {@link #assertProperties} treats a
         * second unlisted property as the latter.
         */
        Set<String> modelProperties() {
            return switch (this) {
                case WORKBENCH, HIGH_BENCH, CHAIR, ARMCHAIR -> Set.of("facing");
                // Benches used to model "facing" and "axe_type" here. The axe is
                // drawn from the block entity now, so a bench's only modelled
                // property is its facing -- the same as a workbench's.
                case BENCH, BENCH_2 -> Set.of("facing");
                case SMALL_STOOL -> Set.of("facing", "wool_color");
                case TABLE, FENCE -> Set.of("north", "east", "south", "west");
                case FENCE_GATE -> Set.of("facing", "in_wall", "open");
            };
        }
    }

    /**
     * Properties a block may declare without the model caring.
     *
     * <p>Each is unmodelled on purpose. {@code waterlogged} and
     * {@code occupied} change collision and Phase 3's seat state, never
     * geometry; {@code powered} is redstone state, and vanilla's own
     * {@code oak_fence_gate.json} omits it for the same reason.
     */
    static final Set<String> IGNORABLE = Set.of("waterlogged", "occupied", "powered");

    /**
     * One catalogue entry with everything the providers need to emit files.
     *
     * @param entry   the catalogue entry, in registration order
     * @param family  the family its id identifies
     * @param wood    the wood, or null for the workbench
     * @param stripped whether the id carries the {@code stripped_} prefix
     * @param block   a real instance, used to read the live {@code StateDefinition}
     */
    record BlockFacts(DecorBlocks.Entry entry, Family family, WoodMeta wood,
                      boolean stripped, Block block) {

        String path() {
            return entry.path();
        }

        /**
         * The log texture this block is made of: the stripped one for a
         * {@code stripped_} id, the plain one otherwise.
         */
        String ownTexture() {
            return stripped ? wood.strippedLogTexture() : wood.logTexture();
        }

        /** The matching top texture, stripped-aware. */
        String ownTopTexture() {
            return stripped ? wood.strippedLogTexture() + "_top" : wood.logTopTexture();
        }

        /**
         * The <em>other</em> log: plain for a stripped id, stripped for a plain
         * one.
         *
         * <p>1.18.2's models lean on this deliberately rather than by accident.
         * A stripped chair still shows bare wood on the frame and the unstripped
         * block on the seat, and an unstripped chair does the reverse, so the
         * chair, stool, table-leg and bench_2 models all reference both textures
         * and swap them per variant.
         */
        String otherTexture() {
            return stripped ? wood.logTexture() : wood.strippedLogTexture();
        }

        /**
         * The other log's <em>top</em> texture, stripped-aware.
         *
         * <p>Only the small stool uses this. 1.18.2's stool model points
         * {@code top} at the opposite variant's log top -- an unstripped stool
         * has a stripped top and vice versa -- which looks deliberate next to
         * the bench models pointing {@code top} at their own.
         */
        String otherTopTexture() {
            return stripped ? wood.logTopTexture() : wood.strippedLogTexture() + "_top";
        }

        /** {@code minecraft:block/<id>_planks}, the small stool's particle. */
        String planksTexture() {
            return wood.planksTexture();
        }

        /** {@code block.decor4fabric.<path>}, the key {@code useBlockDescriptionPrefix} wants. */
        String translationKey() {
            return "block.decor4fabric." + path();
        }

        /**
         * The model subdirectory this block's own models live in, or {@code ""}
         * for families that keep their model beside the blockstate.
         *
         * <p>Only fences and gates are filed under a subdirectory, and 1.18.2
         * files their stripped variants separately: a stripped gate's models are
         * in {@code stripped_fence_gates/}, not in {@code fence_gates/}. That
         * fact has to be true in two places at once -- where
         * {@link ModelProvider} writes the model and where
         * {@link BlockStateProvider} points the blockstate at it -- and when the
         * two providers each worked it out for themselves they disagreed, which
         * left every stripped fence and gate referencing models that were never
         * written: a missing-model error on a client, and nothing at all on a
         * server. Deriving it once, here, is what makes that class of bug
         * impossible.
         */
        String modelDir() {
            return switch (family) {
                case FENCE -> stripped ? "stripped_fences" : "fences";
                case FENCE_GATE -> stripped ? "stripped_fence_gates" : "fence_gates";
                default -> "";
            };
        }

        /**
         * The model path a blockstate should reference, relative to the assets
         * root and without a namespace ({@code block/fences/oak_log_fence_post}).
         *
         * <p>{@link ModelRef#apply()} prepends {@code decor4fabric:}, so the
         * namespace is not repeated here. The model file name always includes the
         * {@code stripped_} prefix; the prefix in the directory and the prefix
         * in the file name are two separate decisions, which is exactly why both
         * this and {@link #modelDir()} exist rather than one string built at
         * each call site.
         */
        String ownModelPath(String suffix) {
            return "block/" + modelDir() + "/" + path() + suffix;
        }
    }

    /**
     * Every entry in registration order, each resolved to a family and a live
     * block instance.
     *
     * <p>Registration order is preserved because it is the numeric registry id a
     * world stores; the generator never re-sorts the catalogue, it only sorts
     * the <em>contents</em> of tag and language files.
     */
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

    /**
     * The family an id belongs to.
     *
     * <p>Longest suffix wins, so {@code oak_log_fence_gate} is a
     * {@link Family#FENCE_GATE} and not a {@link Family#FENCE}, and
     * {@code oak_log_chair_2} is an {@link Family#ARMCHAIR}.
     */
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

    /** The properties a block instance declares, in declaration order. */
    static List<Property<?>> declaredProperties(Block block) {
        List<Property<?>> out = new ArrayList<>();
        block.defaultBlockState().getProperties().forEach(out::add);
        return out;
    }

    /**
     * Fails unless a family's model-affecting set and the block's real
     * declaration agree.
     *
     * <p>Two failure modes, both silent without this check. A model-affecting
     * property the block does not declare produces a blockstate whose keys
     * never match, so every state renders as the missing-model cube. A declared
     * property that is neither model-affecting nor {@link #IGNORABLE} produces a
     * blockstate that silently ignores a visual difference -- the class of bug
     * that is invisible until a client shows a wrong texture.
     */
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

    /**
     * The axis-aligned properties, for providers that need to walk the four
     * horizontal directions in a fixed order.
     *
     * <p>North, east, south, west is the order Minecraft uses for
     * {@code HorizontalDirectionalBlock.FACING} and for the {@code y} rotation
     * of a model authored facing north, so it is used for both rather than
     * inventing a second ordering.
     */
    static final List<String> DIRECTIONS = List.of("north", "east", "south", "west");

    /**
     * The {@code y} rotation a model authored facing north needs for each
     * direction.
     *
     * <p>{@code y} is a clockwise quarter turn about the vertical axis, so north
     * is 0, east 90, south 180 and west 270. Two families are authored facing a
     * different way and are corrected by a constant: 1.18.2's
     * {@code _log_bench_3} used 90/180/270/0, and its {@code _log_bench_2} used
     * 90/180/270/0 for the axe models.
     */
    static int rotationFor(String facing) {
        return switch (facing) {
            case "north" -> 0;
            case "east" -> 90;
            case "south" -> 180;
            case "west" -> 270;
            default -> throw new IllegalArgumentException(facing);
        };
    }

    /** A rotated, {@code uvlock}ed variant of a model reference. */
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

        /** The {@code apply} object a multipart entry needs. */
        Map<String, Object> apply() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("model", Decor4Fabric.MOD_ID + ":" + model);
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
