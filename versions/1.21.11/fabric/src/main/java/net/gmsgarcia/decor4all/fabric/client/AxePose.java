package net.gmsgarcia.decor4all.fabric.client;

import java.util.Locale;

// The axe pose as live values, so it can be tuned in game instead of by rebuilding.
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

    // The field names this class accepts, for a command's suggestions and for its error message.
    public static String[] fields() {
        return new String[] {"spin", "lean", "offsetX", "offsetY", "offsetZ", "scale"};
    }

    // Assigns one field.
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

    // Restores the compiled-in pose.
    public static void reset() {
        spinDegrees = LogBenchRenderer.AXE_SPIN_DEGREES;
        leanDegrees = LogBenchRenderer.AXE_LEAN_DEGREES;
        offsetX = LogBenchRenderer.AXE_OFFSET_X;
        offsetY = LogBenchRenderer.AXE_OFFSET_Y;
        offsetZ = LogBenchRenderer.AXE_OFFSET_Z;
        scale = LogBenchRenderer.AXE_SCALE;
    }

    // The current pose, for printing after a change.
    public static String describe() {
        return String.format(
                Locale.ROOT,
                "spin %s, lean %s, offsetX %s, offsetY %s, offsetZ %s, scale %s",
                fmt(spinDegrees), fmt(leanDegrees), fmt(offsetX), fmt(offsetY), fmt(offsetZ), fmt(scale));
    }

    // Trims the trailing zeros Float#toString leaves on every value, so a printed pose reads 90
    // rather than 90.0 while still keeping fractional values such as -0.15 intact.
    private static String fmt(float value) {
        String text = Float.toString(value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }
}