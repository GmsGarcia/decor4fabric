package net.gmsgarcia.decor4fabric.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.gmsgarcia.decor4fabric.screens.screenhandlers.CompressorScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class screenRegistry {

    public static final ScreenHandlerType<CompressorScreenHandler> COMPRESSOR_SCREEN_HANDLER;

    static {
        //We use registerSimple here because our Entity is not an ExtendedScreenHandlerFactory
        //but a NamedScreenHandlerFactory.
        //In a later Tutorial you will see what ExtendedScreenHandlerFactory can do!
        COMPRESSOR_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier("decor4fabric", "compressor"), CompressorScreenHandler::new);
    }
}
