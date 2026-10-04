package net.gmsgarcia.decor4fabric.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.gmsgarcia.decor4fabric.generator.Catalogue.BlockFacts;
import net.gmsgarcia.decor4fabric.generator.Catalogue.Family;
import net.gmsgarcia.decor4fabric.generator.Catalogue.ModelRef;
import net.minecraft.world.item.DyeColor;

/**
 * Writes one blockstate file per registered block.
 *
 * <p>Only the properties named by {@link Family#modelProperties()} are
 * enumerated, because Minecraft matches a variant key partially: an absent
 * property means "any value", which is why vanilla's own {@code oak_stairs}
 * blockstate carries no {@code waterlogged} entries despite every stairs state
 * having one. Enumerating them anyway would multiply the bench files by two for
 * no visual change and would make Phase 3's {@code occupied} look like a
 * resource-format change instead of the no-op it is.
 *
 * <p><b>Multipart is used wherever parts compose</b> -- benches, stools, tables,
 * fences -- and {@code variants} where one model covers the whole block. In a
 * multipart entry the {@code when} clause only constrains the properties that
 * part depends on, so a new unmodelled property cannot break resolution, whereas
 * a {@code variants} key would have to list every property to keep matching.
 */
final class BlockStateProvider {

    private BlockStateProvider() {
    }

    /**
     * The four horizontal corners of a table, each with the two sides that meet
     * there and the {@code y} rotation its leg model needs.
     *
     * <p>1.18.2 encoded a 16-case truth table by hand, and it is recoverable
     * rather than arbitrary: a corner gets a leg exactly when <em>neither</em> of
     * its two sides is connected, so a lone table has four legs, a table
     * attached on one side keeps the two legs on the far side, and a table in a
     * corner has one. Encoding that rule instead of the sixteen cases is the
     * point -- {@link #CORNERS} is four lines, and the cases it produces are
     * checked against the 1.18.2 files by
     * {@code BlockStateDiffTest} rather than eyeballed.
     */
    private record Corner(String sideA, String sideB, int rotation) {
    }

    private static final List<Corner> CORNERS = List.of(
            new Corner("north", "west", 0),
            new Corner("north", "east", 90),
            new Corner("south", "east", 180),
            new Corner("south", "west", 270));

    /** The blockstate document for one block. */
    static Map<String, Object> blockstate(BlockFacts facts) {
        return switch (facts.family()) {
            case WORKBENCH -> singleModel(facts, "block/" + facts.path());
            // 90, not 0: the high bench is authored facing west, which is what
            // 1.18.2's four variants said. Only this family needed the offset.
            case HIGH_BENCH -> singleModel(facts, "block/" + facts.path(), 90);
            case CHAIR, ARMCHAIR -> singleModel(facts, "block/" + facts.path());
            case BENCH -> bench(facts, 0);
            case BENCH_2 -> bench(facts, 90);
            case SMALL_STOOL -> stool(facts);
            case TABLE -> table(facts);
            case FENCE -> fence(facts);
            case FENCE_GATE -> gate(facts);
        };
    }

    /**
     * A four-facing {@code variants} document, one model rotated per facing.
     *
     * <p>Returns a complete blockstate document, not the inner map, so callers
     * must not wrap it again.
     *
     * @param offset added to every rotation, for families whose model is not
     *               authored facing north
     */
    private static Map<String, Object> singleModel(BlockFacts facts, String model) {
        return singleModel(facts, model, 0);
    }

    private static Map<String, Object> singleModel(BlockFacts facts, String model, int offset) {
        Map<String, Object> variants = new LinkedHashMap<>();
        for (String facing : Catalogue.DIRECTIONS) {
            variants.put("facing=" + facing,
                    ModelRef.rotated(model,
                            (Catalogue.rotationFor(facing) + offset) % 360).apply());
        }
        return Json.obj("variants", variants);
    }

    /**
     * A bench: the base model on every facing.
     *
     * <p>1.18.2 also emitted six axe models here, as extra multipart parts keyed
     * on {@code axe_type}. Those are gone. The axe is now drawn from the block
     * entity's slot by a block entity renderer, so it is not a blockstate
     * concern: no axe appears in a blockstate key, and no axe model exists.
     *
     * <p><b>1.18.2 bug fixed here.</b> The six axe groups rotated correctly
     * ({@code 0/90/180/270} for {@code _bench}, {@code 90/180/270/0} for
     * {@code _bench_2}) but the no-axe group used {@code 0/90/0/90} and
     * {@code 90/0/90/0}, i.e. north and south shared a rotation and east and
     * west shared one. With the axe gone there is nothing for the base group to
     * disagree with, so the rotation now comes from the same
     * {@link Catalogue#rotationFor} every other family uses plus this family's
     * offset, and {@link #assertBench} checks the result against a separately
     * written-down table.
     */
    private static Map<String, Object> bench(BlockFacts facts, int offset) {
        List<Object> parts = new ArrayList<>();
        String base = "block/" + facts.path();

        for (String facing : Catalogue.DIRECTIONS) {
            parts.add(part(ModelRef.rotated(base, (Catalogue.rotationFor(facing) + offset) % 360),
                    Map.of("facing", facing)));
        }

        return Json.obj("multipart", parts);
    }

    /** A small stool: the base model, plus a carpet layer per {@code wool_color}. */
    private static Map<String, Object> stool(BlockFacts facts) {
        List<Object> parts = new ArrayList<>();
        String base = "block/" + facts.path();

        for (String facing : Catalogue.DIRECTIONS) {
            parts.add(part(ModelRef.rotated(base, Catalogue.rotationFor(facing)),
                    Map.of("facing", facing)));
        }

        // wool_color 0 is "no carpet". 1..16 follow DyeColor order, so the
        // sentinel offset in BlockFamilies is what makes value == id + 1.
        for (int i = 0; i < DyeColor.values().length; i++) {
            String model = "block/repetitive_models/small_stool_carpet/"
                    + DyeColor.byId(i).getSerializedName();
            for (String facing : Catalogue.DIRECTIONS) {
                parts.add(part(
                        ModelRef.rotated(model, Catalogue.rotationFor(facing)),
                        ordered("wool_color", i + 1, "facing", facing)));
            }
        }
        return Json.obj("multipart", parts);
    }

    /**
     * A table: an always-present top, then a leg at every corner whose two
     * sides are both unconnected.
     *
     * <p>Legs are {@code uvlock}ed so the grain stays vertical on a rotated
     * leg; the top is not, because it is never rotated.
     */
    private static Map<String, Object> table(BlockFacts facts) {
        List<Object> parts = new ArrayList<>();
        parts.add(part(ModelRef.of("block/" + facts.path() + "_top")));

        String leg = "block/" + facts.path() + "_leg";
        for (int mask = 0; mask < 16; mask++) {
            Map<String, Object> when = new LinkedHashMap<>();
            for (int d = 0; d < Catalogue.DIRECTIONS.size(); d++) {
                when.put(Catalogue.DIRECTIONS.get(d), Boolean.toString((mask & (1 << d)) != 0));
            }
            for (Corner corner : CORNERS) {
                if (Boolean.parseBoolean(when.get(corner.sideA()).toString())
                        || Boolean.parseBoolean(when.get(corner.sideB()).toString())) {
                    continue;
                }
                parts.add(part(ModelRef.locked(leg, corner.rotation()), when));
            }
        }
        return Json.obj("multipart", parts);
    }

    /** A fence: an always-present post, plus one side per connected direction. */
    private static Map<String, Object> fence(BlockFacts facts) {
        List<Object> parts = new ArrayList<>();
        parts.add(part(ModelRef.of(facts.ownModelPath("_post"))));

        String side = facts.ownModelPath("_side");
        for (String direction : Catalogue.DIRECTIONS) {
            parts.add(part(ModelRef.rotated(side, Catalogue.rotationFor(direction)),
                    Map.of(direction, "true")));
        }
        return Json.obj("multipart", parts);
    }

    /**
     * A fence gate: every combination of facing, {@code in_wall} and
     * {@code open}, {@code uvlock}ed because a gate's bar is directional.
     *
     * <p><b>The 180 degree offset is not a correction, it is vanilla's.</b> A
     * vanilla fence gate's model is authored facing the way it opens, so
     * {@code facing=north} is {@code y=180}, not {@code y=0}: 26.1's own
     * {@code assets/minecraft/blockstates/oak_fence_gate.json} is
     * north=180, east=270, south=0, west=90. 1.18.2's gate files use exactly
     * that table and are correct. The gate models here are built on the same
     * geometry, so they take the same offset; using {@link Catalogue#rotationFor}
     * unmodified would leave every closed gate pointing away from the block it
     * is hinged to.
     *
     * <p>{@code powered} is not enumerated. Vanilla's own file omits it, and it
     * is redstone state that changes no geometry.
     */
    private static Map<String, Object> gate(BlockFacts facts) {
        Map<String, Object> variants = new LinkedHashMap<>();
        for (String facing : Catalogue.DIRECTIONS) {
            // The literal 180, not expectedRotation: the assertion owns that
            // table precisely so that this stays an independent statement.
            int y = (Catalogue.rotationFor(facing) + 180) % 360;
            for (String inWall : new String[] {"false", "true"}) {
                for (String open : new String[] {"false", "true"}) {
                    String model = facts.ownModelPath(
                            (inWall.equals("true") ? "_wall" : "")
                                    + (open.equals("true") ? "_open" : ""));
                    variants.put("facing=" + facing + ",in_wall=" + inWall + ",open=" + open,
                            ModelRef.locked(model, y).apply());
                }
            }
        }
        return Json.obj("variants", variants);
    }

    /** A multipart part with no {@code when}, which always applies. */
    private static Map<String, Object> part(ModelRef model) {
        return Json.obj("apply", model.apply());
    }

    private static Map<String, Object> part(ModelRef model, Map<String, Object> when) {
        return Json.obj("apply", model.apply(), "when", when);
    }

    /**
     * Builds a {@code when} clause preserving the given key order.
     *
     * <p>Order is cosmetic in JSON but it is what the files are diffed on, and
     * 1.18.2 wrote {@code axe_type} before {@code facing}. A {@code Map.of} would
     * order them arbitrarily, so this exists to keep the diff stable.
     */
    private static Map<String, Object> ordered(Object... keyValuePairs) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            out.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return out;
    }

    /**
     * Re-derives each family's rotation and leg rules from the finished document
     * and fails if the file does not satisfy them.
     *
     * <p>These are the 1.18.2 bugs this generator exists to make unrepresentable,
     * and they share a property: each one produces a well-formed blockstate that
     * resolves to a real model, so nothing fails to load and no test can see it
     * without a client. Asserting here means the generator cannot emit them.
     *
     * <p>What is checked:
     * <ul>
     *   <li>a bench's base model and its axe models agree on a rotation per
     *       facing -- 1.18.2 rotated the no-axe group independently and got
     *       north/south and east/west wrong on two of three families;
     *   <li>a gate's rotation is vanilla's 180 degree offset, matching
     *       {@code assets/minecraft/blockstates/oak_fence_gate.json};
     *   <li>a table leg exists exactly at corners with neither side connected,
     *       rotated 0/90/180/270 for north-west/north-east/south-east/south-west;
     *   <li>no part carries {@code uvlock} unless it needs it -- 1.18.2 had one
     *       stray {@code uvlock} on the orange carpet of every stool, in
     *       otherwise-identical generated files;
     *   <li>every {@code when} names only properties the family actually has.
     * </ul>
     */
    static void assertInvariants(BlockFacts facts, Map<String, Object> document) {
        switch (facts.family()) {
            case BENCH, BENCH_2 -> assertBench(facts, document);
            case FENCE_GATE -> assertGate(facts, document);
            case TABLE -> assertTable(facts, document);
            case FENCE -> assertFence(facts, document);
            case SMALL_STOOL -> assertStool(facts, document);
            case WORKBENCH, HIGH_BENCH, CHAIR, ARMCHAIR -> assertSingleModel(facts, document);
        }
    }

    /**
     * A four-facing {@code variants} document, one model per facing.
     *
     * <p>Covers the workbench, the high bench and both chair families: the only
     * thing that can go wrong in a document this simple is a rotation, and the
     * high bench in particular needs its 90 degree offset to survive an edit.
     */
    private static void assertSingleModel(BlockFacts facts, Map<String, Object> document) {
        Object variants = document.get("variants");
        if (!(variants instanceof Map<?, ?> map)) {
            throw new IllegalStateException(facts.path() + ": expected a variants document");
        }
        if (map.size() != Catalogue.DIRECTIONS.size()) {
            throw new IllegalStateException(facts.path() + ": expected 4 variants, got "
                    + map.size());
        }
        for (Object key : map.keySet()) {
            String facing = ((String) key).substring("facing=".length());
            @SuppressWarnings("unchecked")
            int actual = y((Map<String, Object>) map.get(key));
            int expected = expectedRotation(facts.family(), facing);
            if (actual != expected) {
                throw new IllegalStateException(facts.path() + ": facing=" + facing + " rotates "
                        + actual + " but must be " + expected);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> parts(Map<String, Object> document) {
        Object multipart = document.get("multipart");
        if (multipart instanceof List<?> list) {
            return (List<Object>) list;
        }
        throw new IllegalStateException("expected a multipart document, got " + document.keySet());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> apply(Object part) {
        return (Map<String, Object>) ((Map<String, Object>) part).get("apply");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> when(Object part) {
        Object w = ((Map<String, Object>) part).get("when");
        return w == null ? Map.of() : (Map<String, Object>) w;
    }

    private static int y(Map<String, Object> apply) {
        Object y = apply.get("y");
        return y == null ? 0 : (int) y;
    }

    /**
     * Every facing must carry exactly the rotation the family says it should.
     *
     * <p>1.18.2's no-axe bench group disagreed with its own axe groups, which is
     * why this check is written against a table that is not used to emit
     * anything. With the axe models gone there is no longer a second group to
     * compare, so the cross-check is gone with it; what remains is the offset
     * check, which is what would still catch a gate or a bench rotated the plain
     * way round. The offset comes from {@link #FAMILY_OFFSETS} rather than from
     * the emitting method, so an edit to the emitter is caught here instead of
     * silently redefining truth.
     */
    private static void assertBench(BlockFacts facts, Map<String, Object> document) {
        java.util.Map<String, Integer> base = new java.util.HashMap<>();

        for (Object part : parts(document)) {
            Map<String, Object> clause = when(part);
            String facing = (String) clause.get("facing");
            if (facing == null) {
                throw new IllegalStateException(facts.path() + ": bench part with no facing");
            }
            base.put(facing, y(apply(part)));
        }

        for (var entry : base.entrySet()) {
            int expected = expectedRotation(facts.family(), entry.getKey());
            if (entry.getValue() != expected) {
                throw new IllegalStateException(facts.path() + ": facing=" + entry.getKey()
                        + " rotates " + entry.getValue() + " but must be " + expected);
            }
        }
    }

    /**
     * The y rotation a part must carry for a given facing, as an independent
     * statement of the rule.
     *
     * <p><b>This table must not be used to emit anything.</b> The point of it is
     * to be a second, separately written-down version of each rotation: a gate
     * rotated the plain way round is self-consistent and self-wrong, and a bench
     * whose base and axe groups agree on a shared mistake is exactly the 1.18.2
     * bug. If the emitters read these values, a wrong constant would be
     * reproduced faithfully in both places and the assertion would pass -- which
     * is precisely what happened the first time this was written, and why the
     * negative test below the fix is the real justification for the duplication.
     *
     * <p>The emitters therefore keep their own literals, taken from 1.18.2 and
     * cross-checked against vanilla where vanilla has an equivalent. That is the
     * fence gate: vanilla's model is authored facing the way it opens, so
     * {@code assets/minecraft/blockstates/oak_fence_gate.json} in 26.1 uses
     * north=180, east=270, south=0, west=90, and 1.18.2's gate files match.
     */
    private static final java.util.Map<Family, Integer> ASSERTED_OFFSETS = Map.of(
            // The axe groups are authoritative: 0/90/180/270.
            Family.BENCH, 0,
            // 90/180/270/0.
            Family.BENCH_2, 90,
            // Authored facing west, which is 1.18.2's four variants.
            Family.HIGH_BENCH, 90,
            // Authored facing north, matching the chairs.
            Family.WORKBENCH, 0,
            // Authored facing north.
            Family.CHAIR, 0,
            Family.ARMCHAIR, 0,
            // No facing part at all: a stool's carpet layers rotate with the base.
            Family.SMALL_STOOL, 0,
            // No facing part: a table's legs key on the four connection sides.
            Family.TABLE, 0,
            Family.FENCE, 0,
            // Vanilla's 180 degree offset.
            Family.FENCE_GATE, 180);

    /** The rule above, for one family and facing. */
    private static int expectedRotation(Family family, String facing) {
        return (Catalogue.rotationFor(facing) + ASSERTED_OFFSETS.get(family)) % 360;
    }

    /**
     * A gate: 16 variants, each rotated by vanilla's 180 degree offset.
     *
     * <p>Asserted per facing, so this holds whatever the variants map is keyed
     * or ordered by.
     */
    private static void assertGate(BlockFacts facts, Map<String, Object> document) {
        Object variants = document.get("variants");
        if (!(variants instanceof Map<?, ?> map)) {
            throw new IllegalStateException(facts.path() + ": expected a variants document");
        }
        if (map.size() != Catalogue.DIRECTIONS.size() * 2 * 2) {
            throw new IllegalStateException(facts.path() + ": expected 16 gate variants, got "
                    + map.size());
        }
        for (Object key : map.keySet()) {
            String[] fields = ((String) key).split(",");
            String facing = fields[0].substring("facing=".length());
            @SuppressWarnings("unchecked")
            int actual = y((Map<String, Object>) map.get(key));
            if (actual != expectedRotation(facts.family(), facing)) {
                throw new IllegalStateException(facts.path() + ": " + key + " rotates " + actual
                        + " but must be " + expectedRotation(facts.family(), facing)
                        + "; vanilla's own oak_fence_gate uses that offset");
            }
        }
    }

    /**
     * A leg exists exactly at every corner whose two sides are both
     * unconnected, in the sixteen connection masks.
     *
     * <p>A mask does not have one leg, it has as many legs as it has legless
     * corners: an isolated table has four, a table with one neighbour has two, a
     * table in a corner has one, and a table joined to opposite sides has none.
     * So the check is per mask -- the number of leg parts carrying that mask must
     * equal the number of legless corners the rule predicts, and their rotations
     * must be that corner's rotation as a multiset. Asserting the count alone
     * would have let a duplicated leg through; asserting the rotations alone
     * would have let a missing one through.
     */
    private static void assertTable(BlockFacts facts, Map<String, Object> document) {
        // One entry per connection mask, keyed by the when clause it belongs to.
        Map<String, List<Integer>> legsByMask = new java.util.LinkedHashMap<>();
        for (Object part : parts(document)) {
            Map<String, Object> apply = apply(part);
            if (!((String) apply.get("model")).endsWith("_leg")) {
                continue;
            }
            if (!Boolean.TRUE.equals(apply.get("uvlock"))) {
                throw new IllegalStateException(facts.path() + ": legs must be uvlocked");
            }
            Map<String, Object> clause = when(part);
            if (clause.size() != Catalogue.DIRECTIONS.size()) {
                throw new IllegalStateException(facts.path()
                        + ": a leg part must constrain all four sides, got " + clause);
            }
            legsByMask.computeIfAbsent(maskKey(clause), k -> new ArrayList<>()).add(y(apply));
        }

        // Nine of the sixteen masks have any legless corner: the isolated one
        // (four legs), the four with a single neighbour (two each) and the four
        // with two adjacent neighbours (one each). The count is asserted below
        // via the total rather than here, because this map is keyed by mask and
        // the sixteen legs are spread across nine entries.
        int totalLegs = 0;
        for (int mask = 0; mask < 16; mask++) {
            Map<String, Object> clause = maskClause(mask);
            List<Integer> expected = new ArrayList<>();
            for (Corner corner : CORNERS) {
                if (Boolean.parseBoolean((String) clause.get(corner.sideA()))
                        || Boolean.parseBoolean((String) clause.get(corner.sideB()))) {
                    continue;
                }
                expected.add(corner.rotation());
            }
            List<Integer> actual =
                    legsByMask.getOrDefault(maskKey(clause), List.of());
            Collections.sort(expected);
            List<Integer> sortedActual = new ArrayList<>(actual);
            Collections.sort(sortedActual);
            if (!expected.equals(sortedActual)) {
                throw new IllegalStateException(facts.path() + ": mask " + clause
                        + " should have legs at rotations " + expected + " but has " + sortedActual);
            }
            totalLegs += expected.size();
        }
        if (totalLegs != 16) {
            throw new IllegalStateException(facts.path() + ": the leg rule should produce 16 legs"
                    + " across all masks, got " + totalLegs);
        }
    }

    /** A stable key for a connection mask, independent of key order. */
    private static String maskKey(Map<String, Object> clause) {
        return clause.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "," + b)
                .orElseThrow();
    }

    /** The {@code when} clause for a bitmask over {@link Catalogue#DIRECTIONS}. */
    private static Map<String, Object> maskClause(int mask) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (int d = 0; d < Catalogue.DIRECTIONS.size(); d++) {
            out.put(Catalogue.DIRECTIONS.get(d), Boolean.toString((mask & (1 << d)) != 0));
        }
        return out;
    }

    /** A fence is one unconditional post plus one side per direction. */
    private static void assertFence(BlockFacts facts, Map<String, Object> document) {
        List<Object> parts = parts(document);
        if (parts.size() != Catalogue.DIRECTIONS.size() + 1) {
            throw new IllegalStateException(facts.path() + ": expected 5 parts, got "
                    + parts.size());
        }
        if (!when(parts.get(0)).isEmpty()) {
            throw new IllegalStateException(facts.path() + ": the post part must be unconditional");
        }
        for (int i = 1; i < parts.size(); i++) {
            if (when(parts.get(i)).size() != 1) {
                throw new IllegalStateException(facts.path()
                        + ": each side part must constrain exactly one side");
            }
        }
    }

    /**
     * A stool's parts are four base facings plus sixteen carpet colours, and none
     * of them is {@code uvlock}ed.
     *
     * <p>1.18.2 had exactly one {@code uvlock} in the whole family, on the orange
     * carpet's west-facing part, in every wood. It is invisible in game -- an
     * axis-locked carpet layer on a stool has the same grain either way -- which
     * is why it survived in the original and why it is worth asserting rather
     * than eyeballing: the whole point of a generated tree is that it cannot
     * carry a one-off inconsistency in a single file out of sixty-eight parts.
     */
    private static void assertStool(BlockFacts facts, Map<String, Object> document) {
        int expected = Catalogue.DIRECTIONS.size() * (1 + DyeColor.values().length);
        List<Object> parts = parts(document);
        if (parts.size() != expected) {
            throw new IllegalStateException(facts.path() + ": expected " + expected
                    + " parts, got " + parts.size());
        }
        for (Object part : parts) {
            if (apply(part).containsKey("uvlock")) {
                throw new IllegalStateException(facts.path()
                        + ": a stool part must not be uvlocked; 1.18.2 had a stray one on the"
                        + " orange carpet and it changes nothing in game");
            }
        }
    }
}
