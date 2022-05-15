package net.gmsgarcia.decor4fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.gmsgarcia.decor4fabric.sitOnStuff.Sit;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.LiteralText;

import static net.gmsgarcia.decor4fabric.mainDecor.getModVersion;

@Environment(EnvType.SERVER)
public class serverDecor implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer()
    {
        //version check
        ServerLoginNetworking.registerGlobalReceiver(Sit.VERSION_CHECK, this::onClientResponse);
        ServerLoginConnectionEvents.QUERY_START.register(this::onLoginStart);
    }

    /**
     * On login start, send VERSION_CHECK request
     */
    private void onLoginStart(ServerLoginNetworkHandler serverLoginPacketListener, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer sync)
    {
        //request the client to send its sit version number
        sender.sendPacket(Sit.VERSION_CHECK, PacketByteBufs.empty());
    }

    /**
     * Handle the VERSION_CHECK response
     */
    private void onClientResponse(MinecraftServer server, ServerLoginNetworkHandler listener, boolean understood, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer loginSynchronizer, PacketSender packetSender)
    {
        float bufVersion = buf.readFloat();

        if(!understood || bufVersion != getModVersion())
        {
            listener.disconnect(Sit.INCORRECT_VERSION);
        }

    }
}