package net.gmsgarcia.decor4fabric.generator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.gmsgarcia.decor4fabric.content.DecorBlocks.Wood;

/**
 * The one place a wood's id, textures and display name are spelled out.
 *
 * <p>Every generated file that mentions wood needs the same three things: the
 * vanilla log texture, the vanilla stripped log texture, and a title-cased
 * segment for {@code en_us.json}. Deriving all three from {@link Wood} alone is
 * not possible -- {@code crimson_stem} has no {@code crimson_log}, and the
 * stripped texture is {@code stripped_<segment>}, not {@code <segment>_stripped}
 * -- so the mapping is data rather than string surgery.
 *
 * <p>Texture names follow vanilla's own convention exactly, so no texture file
 * has to be shipped: {@code <segment>}, {@code stripped_<segment>},
 * {@code <segment>_top} and {@code <id>_planks} are all real vanilla assets for
 * all eleven woods.
 */
final class Woods {

    private Woods() {
    }

    /** The id prefix that marks a block as its stripped variant. */
    private static final String STRIPPED = "stripped_";

    /**
     * One wood's generated-texture bindings.
     *
     * @param id     the id prefix, e.g. {@code dark_oak}
     * @param segment the middle of a block id, e.g. {@code dark_oak_log}
     */
    record WoodMeta(String id, String segment) {

        /** {@code minecraft:block/<segment>}, e.g. {@code dark_oak_log}. */
        String logTexture() {
            return "minecraft:block/" + segment;
        }

        /** {@code minecraft:block/stripped_<segment>}. */
        String strippedLogTexture() {
            return "minecraft:block/stripped_" + segment;
        }

        /** {@code minecraft:block/<segment>_top}, e.g. {@code dark_oak_log_top}. */
        String logTopTexture() {
            return "minecraft:block/" + segment + "_top";
        }

        /** {@code minecraft:block/<id>_planks>, used for the stool's particle. */
        String planksTexture() {
            return "minecraft:block/" + id + "_planks";
        }

        /**
         * The segment with underscores turned into spaces and every word
         * capitalised, e.g. {@code dark_oak_log} to {@code Dark Oak Log}.
         *
         * <p>1.18.2's names are not consistent about this: {@code oak_log_bench}
         * is "Small Oak Log" while {@code crimson_stem_bench} is "Small Crimson
         * Stem", which is exactly what title-casing the segment produces for
         * both. Deriving the display name from the segment rather than from the
         * wood id is what makes those two agree.
         */
        String displayName() {
            StringBuilder out = new StringBuilder(segment.length());
            boolean capitalise = true;
            for (int i = 0; i < segment.length(); i++) {
                char c = segment.charAt(i);
                if (c == '_') {
                    out.append(' ');
                    capitalise = true;
                } else if (capitalise) {
                    out.append(Character.toUpperCase(c));
                    capitalise = false;
                } else {
                    out.append(c);
                }
            }
            return out.toString();
        }
    }

    /** All eleven woods, in catalogue order, keyed by their enum constant. */
    private static final Map<Wood, WoodMeta> BY_WOOD = new LinkedHashMap<>();

    static {
        for (Wood wood : Wood.values()) {
            BY_WOOD.put(wood, new WoodMeta(wood.id(), wood.segment()));
        }
    }

    static WoodMeta of(Wood wood) {
        return BY_WOOD.get(wood);
    }

    /** Every wood's metadata, in enum order. */
    static List<WoodMeta> all() {
        return List.copyOf(BY_WOOD.values());
    }

    /**
     * Recovers the wood and stripped-ness of a catalogue entry from its id.
     *
     * <p>The catalogue exposes {@link Wood} to the code that builds it but not to
     * the generator, which walks {@code DecorBlocks.ALL} in registration order
     * because that order is the save format. Matching the id back is therefore
     * the only way to keep the two in step, and it is done by longest-segment
     * first so {@code dark_oak} cannot shadow {@code oak} on
     * {@code dark_oak_log_bench}.
     *
     * <p>The {@code stripped_} prefix has to come off before matching. It
     * describes the block's appearance, not its wood, and leaving it in place
     * means {@code stripped_oak_log_bench} starts with {@code stripped_} and
     * matches no segment at all -- which silently yields a null wood and then a
     * null wood on every stripped block's textures.
     *
     * @return the wood, or null when the id is not wood-derived (the carpentry table)
     */
    static WoodMeta match(String path) {
        String candidate = isStripped(path) ? path.substring(STRIPPED.length()) : path;
        WoodMeta best = null;
        for (WoodMeta meta : BY_WOOD.values()) {
            if (candidate.equals(meta.segment())
                    || candidate.startsWith(meta.segment() + "_")) {
                if (best == null || meta.segment().length() > best.segment().length()) {
                    best = meta;
                }
            }
        }
        return best;
    }

    /** True when the id carries the {@code stripped_} prefix. */
    static boolean isStripped(String path) {
        return path.startsWith(STRIPPED);
    }
}
