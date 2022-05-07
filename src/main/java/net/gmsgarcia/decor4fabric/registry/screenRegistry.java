package net.gmsgarcia.decor4fabric.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.gmsgarcia.decor4fabric.screen.workBenchScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class screenRegistry {

    public static ScreenHandlerType<workBenchScreenHandler> WORKBENCH_SCREENHANDLER ;

    public static void registerScreens() {
        WORKBENCH_SCREENHANDLER = ScreenHandlerRegistry.registerSimple(new Identifier("decor4fabric", "workbench"), workBenchScreenHandler::new);
    }
}
