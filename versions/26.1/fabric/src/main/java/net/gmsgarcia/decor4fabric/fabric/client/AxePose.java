package net.gmsgarcia.decor4fabric.fabric.client;

import java.util.Locale;

/**
 * The axe pose as live values, so it can be tuned in game instead of by
 * rebuilding.
 *
 * <p>{@link LogBenchRenderer}'s constants are the <em>compiled-in</em> pose and
 * are still the single source of truth for it: this class starts from them, and
 * {@link #reset()} copies them back. Nothing here is persisted, and nothing
 * here is sent to the server -- the whole class is a per-session override that
 * dies with the game.
 *
 * <p>Six mirrored copies of this class exist, one per loader, for the same
 * reason the renderer itself is six: a client-only class cannot live in
 * {@code common}, which is compiled against a dedicated server too. The values
 * are therefore set per loader and do not travel between them.
 *
 * <p>This is a development aid. Once the pose is settled, the numbers below
 * belong back in the renderer as constants and this class goes away.
 */
public final class AxePose {

    private static float spinDegrees = LogBenchRenderer.AXE_SPIN_DEGREES;
    private static float leanDegrees = LogBenchRenderer.AXE_LEAN_DEGREES;
    private static float offsetX = LogBenchRenderer.AXE_OFFSET_X;
    private static float offsetY = LogBenchRenderer.AXE_OFFSET_Y;
    private static float offsetZ = LogBenchRenderer.AXE_OFFSET_Z;
    private static float scale = LogBenchRenderer.AXE_SCALE;

    private AxePose() {
    }

    public static float spinDegrees() {
        return spinDegrees;
    }

    public static float leanDegrees() {
        return leanDegrees;
    }

    public static float offsetX() {
        return offsetX;
    }

    public static float offsetY() {
        return offsetY;
    }

    public static float offsetZ() {
        return offsetZ;
    }

    public static float scale() {
        return scale;
    }

    /**
     * The field names this class accepts, for a command's suggestions and for
     * its error message.
     */
    public static String[] fields() {
        return new String[] {"spin", "lean", "offsetX", "offsetY", "offsetZ", "scale"};
    }

    /**
     * Assigns one field.
     *
     * <p>Returns {@code false} for an unrecognised name so the caller can
     * report it, rather than throwing: this is reached from typed chat input.
     *
     * <p>Scale is refused at zero and below rather than accepted. A zero or
     * negative scale makes the pose matrix singular, and a singular matrix sent
     * to the renderer produces NaN vertices instead of a merely invisible axe,
     * which is a much worse failure to diagnose from a screenshot.
     */
    public static boolean set(String field, float value) {
        String name = field.toLowerCase(Locale.ROOT);
        if (name.equals("scale") && value <= 0.0F) {
            return false;
        }
        switch (name) {
            case "spin" -> spinDegrees = value;
            case "lean" -> leanDegrees = value;
            case "offsetx" -> offsetX = value;
            case "offsety" -> offsetY = value;
            case "offsetz" -> offsetZ = value;
            case "scale" -> scale = value;
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Restores the compiled-in pose.
     */
    public static void reset() {
        spinDegrees = LogBenchRenderer.AXE_SPIN_DEGREES;
        leanDegrees = LogBenchRenderer.AXE_LEAN_DEGREES;
        offsetX = LogBenchRenderer.AXE_OFFSET_X;
        offsetY = LogBenchRenderer.AXE_OFFSET_Y;
        offsetZ = LogBenchRenderer.AXE_OFFSET_Z;
        scale = LogBenchRenderer.AXE_SCALE;
    }

    /**
     * The current pose, for printing after a change.
     */
    public static String describe() {
        return String.format(
                Locale.ROOT,
                "spin %s, lean %s, offsetX %s, offsetY %s, offsetZ %s, scale %s",
                fmt(spinDegrees), fmt(leanDegrees), fmt(offsetX), fmt(offsetY), fmt(offsetZ), fmt(scale));
    }

    /**
     * Trims the trailing zeros {@link Float#toString} leaves on every value, so
     * a printed pose reads {@code 90} rather than {@code 90.0} while still
     * keeping fractional values such as {@code -0.15} intact.
     */
    private static String fmt(float value) {
        String text = Float.toString(value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }
}