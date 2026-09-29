package net.gmsgarcia.decor4fabric.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A minimal, order-preserving JSON writer for the generated resource tree.
 *
 * <p>Minecraft ships Gson, so a generator could use that. It is not used here for
 * two reasons. First, the output is committed to git and diffed by hand, and
 * Gson's {@code JsonObject} is a {@code LinkedTreeMap} whose iteration order is
 * stable but whose key ordering has to be managed by the caller anyway -- so the
 * library buys nothing. Second, this generator has to compile in the same
 * {@code common} source set as the mod and run under three different Minecraft
 * versions, and depending on a class whose package moved between them is a
 * portability problem this file exists to avoid.
 *
 * <p>Values are plain Java: {@link Map} for objects (insertion ordered, so the
 * caller's literal order is the file's order), {@link List} for arrays, and
 * {@link String}, {@link Integer} or {@link Boolean} for scalars. Output is
 * two-space indented with a trailing newline, which is what
 * {@code JsonWriter} in the vanilla generators produces, so a generated file and
 * a datagen file diff cleanly against each other.
 */
final class Json {

    private Json() {
    }

    /**
     * Builds an object from alternating key/value arguments.
     *
     * <p>An odd argument count is a programming error rather than a malformed
     * file, so it throws here instead of silently dropping the last key.
     */
    static Map<String, Object> obj(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "obj() needs alternating key/value arguments, got " + keyValuePairs.length);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            out.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return out;
    }

    /** Builds an array. */
    static List<Object> arr(Object... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    /** Builds an array of strings, the shape {@code tag.values} needs. */
    static List<Object> strings(Iterable<String> values) {
        List<Object> out = new ArrayList<>();
        values.forEach(out::add);
        return out;
    }

    /**
     * Renders {@code value} as pretty-printed JSON with a trailing newline.
     *
     * <p>Empty objects and arrays are written as {@code {}} and {@code []}. The
     * vanilla writers emit {@code {}} too, and matching that keeps the output
     * diffable against anything else in the tree.
     */
    static String write(Object value) {
        StringBuilder out = new StringBuilder();
        append(out, value, 0);
        out.append('\n');
        return out.toString();
    }

    private static void append(StringBuilder out, Object value, int depth) {
        if (value instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                out.append("{}");
                return;
            }
            out.append("{\n");
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                indent(out, depth + 1);
                writeString(out, String.valueOf(entry.getKey()));
                out.append(": ");
                append(out, entry.getValue(), depth + 1);
                if (++i < map.size()) {
                    out.append(',');
                }
                out.append('\n');
            }
            indent(out, depth);
            out.append('}');
        } else if (value instanceof List<?> list) {
            if (list.isEmpty()) {
                out.append("[]");
                return;
            }
            out.append("[\n");
            for (int i = 0; i < list.size(); i++) {
                indent(out, depth + 1);
                append(out, list.get(i), depth + 1);
                if (i < list.size() - 1) {
                    out.append(',');
                }
                out.append('\n');
            }
            indent(out, depth);
            out.append(']');
        } else if (value instanceof String text) {
            writeString(out, text);
        } else if (value instanceof Boolean || value instanceof Integer) {
            out.append(value);
        } else if (value == null) {
            out.append("null");
        } else {
            throw new IllegalArgumentException("unsupported JSON value: " + value.getClass());
        }
    }

    private static void indent(StringBuilder out, int depth) {
        out.append("  ".repeat(depth));
    }

    private static void writeString(StringBuilder out, String text) {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }
}
