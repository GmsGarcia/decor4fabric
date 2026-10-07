package net.gmsgarcia.decor4fabric.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.gmsgarcia.decor4fabric.VanillaRegistrar;
import net.gmsgarcia.decor4fabric.content.DecorBlocks;

/**
 * Fabric entrypoint: the only loader-specific part of the mod.
 *
 * <p>{@code ModInitializer} runs before the registry freeze, so
 * {@link VanillaRegistrar} can build each object inline and
 * {@link Decor4Fabric#init} logs real counts when it returns. NeoForge is handed
 * the identical set of factories but has to defer them, so it logs later.
 */
public class FabricDecor4Fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Decor4Fabric.init(new VanillaRegistrar());
        addCarpenterTableToVanillaTab();
    }

    /**
     * Puts the carpentry table item into {@code minecraft:functional_blocks}.
     *
     * <p>1.18.2 put it in {@code ItemGroup.DECORATIONS}, which no longer exists;
     * {@link DecorBlocks#CARPENTER_TABLE_TAB} records which surviving tab replaced it.
     *
     * <p>This is the one place the mod has to reach outside its own tabs. The
     * other 165 items are listed by {@link DecorBlocks#buildTab} in common code,
     * because a tab this mod owns can declare its own contents. A <em>vanilla</em>
     * tab already has its contents generator, so the only way in is to modify the
     * output after vanilla has built it -- which is an event, and the event is a
     * loader API. NeoForge does the same thing with
     * {@code BuildCreativeModeTabContentsEvent}; see {@code NeoForgeDecor4Fabric}.
     *
     * <p>Two API notes for anyone revisiting this:
     *
     * <ul>
     *   <li>Fabric API's creative-tab module is {@code fabric-creative-tab-api-v1}
     *       and the entry point is {@code CreativeModeTabEvents.modifyOutputEvent}.
     *       It is <b>not</b> the older {@code fabric-item-group-api-v1} /
     *       {@code ItemGroupEvents} pair, which no longer ships in Fabric API for
     *       26.x -- {@code ItemGroupEvents} is simply not in the 26.1 bundle.
     *   <li>The callback runs every time a tab's contents are built, which is more
     *       than once (on operator-status and feature-flag changes), so it must not
     *       have side effects beyond accepting the stack. Accepting the same stack
     *       twice is harmless here because each run gets a fresh output set.
     * </ul>
     */
    private static void addCarpenterTableToVanillaTab() {
        CreativeModeTabEvents.modifyOutputEvent(DecorBlocks.CARPENTER_TABLE_TAB)
                .register(output -> output.accept(Decor4Fabric.carpenterTableItem()));
    }
}
