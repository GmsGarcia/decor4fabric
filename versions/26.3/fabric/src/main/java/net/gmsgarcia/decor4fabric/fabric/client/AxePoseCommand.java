package net.gmsgarcia.decor4fabric.fabric.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/**
 * {@code /decor4fabric axe ...}, a development command for tuning the stored
 * axe's pose without a rebuild.
 *
 * <p>Java cannot hot-swap a {@code static final} constant, so tuning the pose
 * the honest way means editing, recompiling and relaunching for every value
 * tried. That is tolerable once and miserable in a loop, and the numbers being
 * tuned are pure visual judgement with no right answer derivable from the API.
 * This command is the loop: type a value, look at the bench, type the next one.
 *
 * <pre>
 *   /decor4fabric axe show
 *   /decor4fabric axe set &lt;field&gt; &lt;value&gt;
 *   /decor4fabric axe reset
 * </pre>
 *
 * <p>Registered on the <em>client</em> dispatcher, so it is never sent to the
 * server, never shows up in another player's command tree, and needs no
 * permission -- which is what a visual debug aid wants. It changes only this
 * client's rendering, so nothing about the world can be desynchronised by it.
 *
 * <p>What it cannot show is the pose as compiled, which is the point of
 * {@code reset}: the six constants in {@link LogBenchRenderer} are what a fresh
 * launch uses.
 *
 * <p>Once the pose is settled this class and {@link AxePose} are both dead
 * weight, and the numbers they settled on belong back in the renderer.
 */
public final class AxePoseCommand {

    private AxePoseCommand() {
    }

    /** The command tree, for the client-side registration callback. */
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("decor4fabric")
                .then(literal("axe")
                        .executes(AxePoseCommand::show)
                        .then(literal("show")
                                .executes(AxePoseCommand::show))
                        .then(literal("reset")
                                .executes(AxePoseCommand::reset))
                        .then(literal("set")
                                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument(
                                                "field", StringArgumentType.word())
                                        .suggests(FIELD_SUGGESTIONS)
                                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, Float>argument(
                                                        "value", FloatArgumentType.floatArg())
                                                .executes(AxePoseCommand::setField))))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    private static int show(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("Current pose: " + AxePose.describe()));
        return 1;
    }

    private static int reset(CommandContext<FabricClientCommandSource> context) {
        AxePose.reset();
        context.getSource().sendFeedback(Component.literal("Pose reset to " + AxePose.describe()));
        return 1;
    }

    private static int setField(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        String field = StringArgumentType.getString(context, "field");
        float value = FloatArgumentType.getFloat(context, "value");
        if (!AxePose.set(field, value)) {
            // The two refusals need different words: an unknown name is a typo,
            // and a rejected scale is a value the renderer cannot survive.
            source.sendFeedback(Component.literal(
                    field.equalsIgnoreCase("scale")
                            ? "scale must be greater than 0"
                            : "unknown field, expected one of " + String.join(", ", AxePose.fields())));
            return 0;
        }
        source.sendFeedback(Component.literal("Pose now: " + AxePose.describe()));
        return 1;
    }

    /** Completes the field names, so they need not be remembered. */
    private static final SuggestionProvider<FabricClientCommandSource> FIELD_SUGGESTIONS =
            (context, builder) -> {
                for (String field : AxePose.fields()) {
                    builder.suggest(field);
                }
                return builder.buildFuture();
            };
}