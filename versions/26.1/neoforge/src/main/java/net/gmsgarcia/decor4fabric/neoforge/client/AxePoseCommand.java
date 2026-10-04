package net.gmsgarcia.decor4fabric.neoforge.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
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
 * <p>This is NeoForge's copy, and differs from Fabric's in three places: the
 * source type is {@link CommandSourceStack} rather than Fabric's wrapper,
 * feedback is {@code sendSuccess} rather than {@code sendFeedback}, and the tree
 * arrives via {@code RegisterClientCommandsEvent} rather than a Fabric event.
 * The rest is the same command, and the numbers themselves live in
 * {@link AxePose}, which is a second copy for the same reason the renderer is.
 *
 * <p>Once the pose is settled this class and {@link AxePose} are both dead
 * weight, and the numbers they settled on belong back in the renderer.
 */
public final class AxePoseCommand {

    private AxePoseCommand() {
    }

    /** The command tree, for the client-side registration event. */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("decor4fabric")
                .then(literal("axe")
                        .executes(AxePoseCommand::show)
                        .then(literal("show")
                                .executes(AxePoseCommand::show))
                        .then(literal("reset")
                                .executes(AxePoseCommand::reset))
                        .then(literal("set")
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument(
                                                "field", StringArgumentType.word())
                                        .suggests(FIELD_SUGGESTIONS)
                                        .then(RequiredArgumentBuilder.<CommandSourceStack, Float>argument(
                                                        "value", FloatArgumentType.floatArg())
                                                .executes(AxePoseCommand::setField))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    private static int show(CommandContext<CommandSourceStack> context) {
        feedback(context.getSource(), () -> "Current pose: " + AxePose.describe());
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        AxePose.reset();
        feedback(context.getSource(), () -> "Pose reset to " + AxePose.describe());
        return 1;
    }

    private static int setField(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String field = StringArgumentType.getString(context, "field");
        float value = FloatArgumentType.getFloat(context, "value");
        if (!AxePose.set(field, value)) {
            // The two refusals need different words: an unknown name is a typo,
            // and a rejected scale is a value the renderer cannot survive.
            feedback(source, () -> field.equalsIgnoreCase("scale")
                    ? "scale must be greater than 0"
                    : "unknown field, expected one of " + String.join(", ", AxePose.fields()));
            return 0;
        }
        feedback(source, () -> "Pose now: " + AxePose.describe());
        return 1;
    }

    /** Completes the field names, so they need not be remembered. */
    private static final SuggestionProvider<CommandSourceStack> FIELD_SUGGESTIONS =
            (context, builder) -> {
                for (String field : AxePose.fields()) {
                    builder.suggest(field);
                }
                return builder.buildFuture();
            };

    private static void feedback(CommandSourceStack source, Supplier<String> message) {
        source.sendSuccess(() -> Component.literal(message.get()), false);
    }
}