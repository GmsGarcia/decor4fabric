package net.gmsgarcia.decor4fabric;

import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.gmsgarcia.decor4fabric.sitOnStuff.Sit;
import net.gmsgarcia.decor4fabric.sitOnStuff.SitEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class clientDecor implements ClientModInitializer {

    @Override
    public void onInitializeClient()
    {
        ScreenRegistry.register(net.gmsgarcia.decor4fabric.registry.screenRegistry.COMPRESSOR_SCREEN_HANDLER, net.gmsgarcia.decor4fabric.screens.CompressorScreen::new);
        EntityRendererRegistry.INSTANCE.register(Sit.SIT_ENTITY_TYPE, EmptyRenderer::new);
        ClientLoginNetworking.registerGlobalReceiver(Sit.VERSION_CHECK, this::onServerRequest);
    }

    private CompletableFuture<PacketByteBuf> onServerRequest(MinecraftClient minecraft, ClientLoginNetworkHandler listener, PacketByteBuf inBuf, Consumer<GenericFutureListener<? extends Future<? super Void>>> consumer)
    {
        //VERSION_CHECK request received from server, send back own version
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeInt(Sit.PROTOCOL_VERSION);
        return CompletableFuture.completedFuture(buf);
    }

    private static class EmptyRenderer extends EntityRenderer<SitEntity>
    {
        protected EmptyRenderer(EntityRendererFactory.Context ctx)
        {
            super(ctx);
        }

        @Override
        public boolean shouldRender(SitEntity entity, Frustum frustum, double d, double e, double f)
        {
            return false;
        }

        @Override
        public Identifier getTexture(SitEntity entity)
        {
            return null;
        }
    }
}