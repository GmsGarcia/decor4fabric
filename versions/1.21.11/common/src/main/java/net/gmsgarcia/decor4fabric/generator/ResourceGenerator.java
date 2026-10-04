package net.gmsgarcia.decor4fabric.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.generator.Catalogue.BlockFacts;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

/**
 * Regenerates {@code src/main/generated/resources} for one Minecraft version.
 *
 * <p>Run it with {@code ./gradlew :<version>:common:generateResources}, or
 * {@code ./gradlew generateAllResources} for all three ported versions in
 * sequence.
 *
 * <p><b>Why a generator rather than committed 1.18.2 files.</b> The original
 * tree held 121 hand-maintained blockstates, 121 item models and 88 loot tables
 * whose correctness depended on nothing but the author's memory of the state
 * definitions -- and it had drifted. Two of the three bench families rotated
 * the no-axe model differently from the axe models sitting on top of it, so an
 * empty bench faced a different way from a loaded one. 33 blocks had no loot
 * table at all, so breaking a fence dropped nothing. Two fence translation
 * keys said "Crimson Steam". Regenerating from live block instances makes those
 * classes of bug unrepresentable, because the state properties come from the
 * same {@code Block} objects the game registers.
 *
 * <p><b>Determinism.</b> The output directory is deleted and rewritten on every
 * run, tag and language values are sorted, and no iteration touches a
 * {@code HashMap}, so two runs on the same code produce byte-identical trees and
 * a diff always means a real change.
 *
 * <p><b>What is not here.</b> Crafting recipes. They belong to Phase 4, which
 * owns {@code WorkBenchRecipe} and the {@code RecipeType} it registers under;
 * see {@link DataProvider}.
 */
public final class ResourceGenerator {

    private ResourceGenerator() {
    }

    /** The output root, relative to the working directory the Gradle task sets. */
    private static final Path OUTPUT = Path.of("src", "main", "generated", "resources");

    private static final String ASSET = "assets/" + Decor4Fabric.MOD_ID;
    private static final String DATA = "data/" + Decor4Fabric.MOD_ID;

    public static void main(String[] args) throws IOException {
        // Both calls are load-bearing and the order is not interchangeable.
        // tryDetectVersion sets the version string SharedConstants' data fixers
        // read on first touch, and Bootstrap.bootStrap freezes the vanilla
        // registries. Touching any block class before the second call fails with
        // "Not bootstrapped (called from registry minecraft:...)" from inside
        // BuiltInRegistries' static initialiser; skipping the first fails with
        // "Game version not set" from DataFixers.
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        // bootStrap() freezes the built-in registries, which closes the window
        // that Block's constructor needs to create its intrusive holder. Reopen
        // it before constructing anything; see IntrusiveHolderAccess for why
        // this cannot be done the way the game does it.
        IntrusiveHolderAccess.reopen();

        List<BlockFacts> all = Catalogue.all();
        System.out.println("catalogue: " + all.size() + " blocks");

        Map<String, Object> files = build(all);
        assertCoversRegistry(all, files);
        assertReferencesResolve(files);

        Path root = OUTPUT;
        deleteRecursively(root);
        write(root, files);
        System.out.println("wrote " + files.size() + " files to " + root.toAbsolutePath());
    }

    /**
     * Runs every provider and returns the whole tree keyed by resource path.
     *
     * <p>Providers hand back paths relative to their own root -- {@code
     * models/...} and {@code blockstates/...} for the asset side, complete
     * {@code data/...} paths for the data side -- and the join happens here, in
     * one place. That is deliberate: the asset directory layout is the thing
     * that moved between 1.18.2 and 26.x, and scattering the prefix across
     * every provider would mean five files to change the next time it moves.
     */
    private static Map<String, Object> build(List<BlockFacts> all) {
        Map<String, Object> files = new LinkedHashMap<>();

        for (BlockFacts facts : all) {
            Catalogue.assertProperties(facts);

            Map<String, Object> blockstate = BlockStateProvider.blockstate(facts);
            // Re-derive the rotation and leg rules from the finished document.
            // Every bug this fixes produced a file that loaded without error, so
            // the only place it can be caught is here.
            BlockStateProvider.assertInvariants(facts, blockstate);

            files.put(ASSET + "/blockstates/" + facts.path() + ".json", blockstate);
            files.put(DATA + "/loot_table/" + facts.path() + ".json",
                    DataProvider.lootTable(facts));
            for (Map.Entry<String, Object> entry : ModelProvider.filesFor(facts).entrySet()) {
                files.put(ASSET + "/" + entry.getKey(), entry.getValue());
            }
        }

        // Wood-independent, so emitted once rather than per block: sixteen carpet
        // models.
        ModelProvider.sharedModels()
                .forEach((path, document) -> files.put(ASSET + "/" + path, document));

        files.putAll(DataProvider.tags(all));
        files.put(ASSET + "/lang/en_us.json", DataProvider.lang(all));
        return files;
    }

    /**
     * Fails unless every registered block produced the files it must have.
     *
     * <p>The generated models are per-family -- a table needs three, a gate four
     * -- and that shape is the providers' own contract, so what is asserted here
     * is presence: a blockstate, an item definition, the legacy item model and a
     * loot table. A block that loses one of these is a missing model on a
     * client, a raw translation key in a tooltip, or a block that drops nothing,
     * and none of those fail a server smoke test.
     *
     * <p>Stale files in the other direction -- an artefact from a renamed id --
     * are handled by deleting the output root before writing, so they cannot
     * survive a run.
     */
    private static void assertCoversRegistry(List<BlockFacts> all, Map<String, Object> files) {
        List<String> missing = new ArrayList<>();
        for (BlockFacts facts : all) {
            String id = facts.path();
            for (String path : List.of(
                    ASSET + "/blockstates/" + id + ".json",
                    ASSET + "/items/" + id + ".json",
                    ASSET + "/models/item/" + id + ".json",
                    DATA + "/loot_table/" + id + ".json")) {
                if (!files.containsKey(path)) {
                    missing.add(id + " -> " + path);
                }
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("catalogue blocks missing generated files: " + missing);
        }
    }

    /**
     * The hand-authored source root, relative to the same working directory.
     *
     * <p>Needed because the tree is deliberately split: models and data are
     * derived and live under {@code generated/}, but the 19 abstract geometry
     * parents and every PNG are source, not output. See {@code §8.3}.
     */
    private static final Path HAND = Path.of("src", "main", "resources");

    /**
     * Fails unless every non-vanilla {@code model} and {@code texture}
     * reference in the generated tree points at something that exists.
     *
     * <p>This is the check that catches missing source art. The nineteen abstract
     * geometry parents and every PNG are hand-authored, not derived, so a
     * generator that only ever validates its own output cannot see them go
     * missing -- it was quite happy emitting correct JSON that pointed at files
     * nobody had copied. The symptom is a "Missing textures in model" warning on
     * every client boot, which no server-side smoke test sees.
     *
     * <p>The twelve {@code <axe>_rot} / {@code <axe>_rot_mir} PNGs that used to
     * be this check's motivating example are gone along with the axe models that
     * referenced them; the check itself is unchanged and still guards the
     * carpet models and the geometry parents.
     *
     * <p>So the check resolves against <em>both</em> trees: a model reference
     * may land in the tree this run just built or in {@code src/main/resources},
     * and a texture must exist under the hand-authored {@code textures/}
     * directory, because the generator emits no images at all.
     *
     * <p>Vanilla references are skipped, and so are unprefixed ones: an
     * unprefixed resource location is {@code minecraft:} by definition, which is
     * what {@code "parent": "block/block"} in {@code fence_inventory.json} means
     * and must keep meaning.
     */
    private static void assertReferencesResolve(Map<String, Object> files) {
        // Derived model paths come from the tree being generated, not from
        // whatever the previous run left on disk. This check runs before OUTPUT
        // is deleted and rewritten, so reading the directory would validate the
        // previous run's tree and report a half-written output -- or an output
        // deleted just now -- as thousands of missing files.
        String modelsPrefix = ASSET + "/models/";
        Set<String> generatedModels = new TreeSet<>();
        for (String path : files.keySet()) {
            if (path.startsWith(modelsPrefix) && path.endsWith(".json")) {
                generatedModels.add(path.substring(modelsPrefix.length()));
            }
        }

        List<String> missing = new ArrayList<>();

        for (Map.Entry<String, Object> entry : files.entrySet()) {
            String from = entry.getKey();
            if (!from.startsWith(ASSET + "/")) {
                continue;
            }
            collectReferences(entry.getValue(), from, missing, generatedModels);
        }

        if (!missing.isEmpty()) {
            throw new IllegalStateException("generated resources reference "
                    + missing.size() + " file(s) that do not exist. Derived files are in "
                    + OUTPUT + ", hand-authored ones in " + HAND + " -- copy the source art "
                    + "across and re-run. Missing: " + missing);
        }
    }

    /** Walks one document, recording every unresolvable reference. */
    private static void collectReferences(Object node, String from, List<String> missing,
            Set<String> generatedModels) {
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                String key = String.valueOf(e.getKey());
                Object value = e.getValue();
                switch (key) {
                    // A model reference: "parent" in a model, and the nested
                    // {"model": {"type": ..., "model": ...}} of an item definition.
                    case "parent" -> checkModelRef(value, from, missing, generatedModels);
                    case "model" -> {
                        if (value instanceof String s) {
                            checkModelRef(s, from, missing, generatedModels);
                        } else if (value instanceof Map<?, ?> inner) {
                            collectReferences(inner, from, missing, generatedModels);
                        }
                    }
                    // A texture reference, unless it is a "#slot" indirection.
                    case "textures" -> {
                        if (value instanceof Map<?, ?> textures) {
                            for (Object t : textures.values()) {
                                if (t instanceof String s && !s.startsWith("#")) {
                                    checkTextureRef(s, from, missing);
                                }
                            }
                        }
                    }
                    default -> collectReferences(value, from, missing, generatedModels);
                }
            }
        } else if (node instanceof List<?> list) {
            for (Object element : list) {
                collectReferences(element, from, missing, generatedModels);
            }
        }
    }

    /** A model must exist, generated or hand-authored. */
    private static void checkModelRef(Object value, String from, List<String> missing,
            Set<String> generatedModels) {
        if (!(value instanceof String ref) || ref.startsWith("#") || isVanilla(ref)) {
            return;
        }
        String rel = stripNamespace(ref) + ".json";
        if (generatedModels.contains(rel)) {
            return;
        }
        if (!Files.isRegularFile(HAND.resolve(ASSET + "/models").resolve(rel))) {
            missing.add(from + " -> model " + ref);
        }
    }

    /** A texture must exist as a PNG under the hand-authored textures/ tree. */
    private static void checkTextureRef(String ref, String from, List<String> missing) {
        if (isVanilla(ref)) {
            return;
        }
        if (!Files.isRegularFile(HAND.resolve(ASSET + "/textures").resolve(stripNamespace(ref) + ".png"))) {
            missing.add(from + " -> texture " + ref);
        }
    }

    /**
     * Whether a reference points into {@code minecraft:}.
     *
     * <p>An unprefixed reference is {@code minecraft:} -- that is what
     * {@code ResourceLocation} does with a missing namespace, and
     * {@code "parent": "block/block"} in {@code fence_inventory.json} relies on
     * it to reach vanilla's block base model.
     */
    private static boolean isVanilla(String ref) {
        return ref.startsWith("minecraft:") || ref.indexOf(':') < 0;
    }

    private static String stripNamespace(String ref) {
        int colon = ref.indexOf(':');
        return colon < 0 ? ref : ref.substring(colon + 1);
    }

    /** Writes every document, creating parent directories. */
    private static void write(Path root, Map<String, Object> files) throws IOException {
        Set<String> sorted = new TreeSet<>(files.keySet());
        for (String path : sorted) {
            Path target = root.resolve(path);
            Files.createDirectories(target.getParent());
            Files.writeString(target, Json.write(files.get(path)), StandardCharsets.UTF_8);
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        List<Path> paths = new ArrayList<>();
        try (var walk = Files.walk(root)) {
            walk.forEach(paths::add);
        }
        for (int i = paths.size() - 1; i >= 0; i--) {
            Files.deleteIfExists(paths.get(i));
        }
    }
}
