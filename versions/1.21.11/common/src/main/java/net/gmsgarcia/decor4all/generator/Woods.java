package net.gmsgarcia.decor4all.generator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.gmsgarcia.decor4all.content.DecorBlocks.Wood;

// The one place a wood's id, textures and display name are spelled out.
final class Woods {

    private Woods() {
    }

    // The id prefix that marks a block as its stripped variant.
    private static final String STRIPPED = "stripped_";

    // One wood's generated-texture bindings.
    record WoodMeta(String id, String segment) {

        // minecraft:block/, e.g. dark_oak_log.
        String logTexture() {
            return "minecraft:block/" + segment;
        }

        // minecraft:block/stripped_.
        String strippedLogTexture() {
            return "minecraft:block/stripped_" + segment;
        }

        // minecraft:block/_top, e.g. dark_oak_log_top.
        String logTopTexture() {
            return "minecraft:block/" + segment + "_top";
        }

        // {@code minecraft:block/_planks>, used for the stool's particle.
        String planksTexture() {
            return "minecraft:block/" + id + "_planks";
        }

        // The segment with underscores turned into spaces and every word capitalised, e.g.
        // dark_oak_log to Dark Oak Log.
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

    // All eleven woods, in catalogue order, keyed by their enum constant.
    private static final Map<Wood, WoodMeta> BY_WOOD = new LinkedHashMap<>();

    static {
        for (Wood wood : Wood.values()) {
            BY_WOOD.put(wood, new WoodMeta(wood.id(), wood.segment()));
        }
    }

    static WoodMeta of(Wood wood) {
        return BY_WOOD.get(wood);
    }

    // Every wood's metadata, in enum order.
    static List<WoodMeta> all() {
        return List.copyOf(BY_WOOD.values());
    }

    // Recovers the wood and stripped-ness of a catalogue entry from its id.
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

    // True when the id carries the stripped_ prefix.
    static boolean isStripped(String path) {
        return path.startsWith(STRIPPED);
    }
}
