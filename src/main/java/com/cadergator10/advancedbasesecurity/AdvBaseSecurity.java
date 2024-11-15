package com.cadergator10.advancedbasesecurity;

import com.cadergator10.advancedbasesecurity.common.commands.BaseSecurityCommand;
import com.cadergator10.advancedbasesecurity.common.commands.RequestCommand;
import com.cadergator10.advancedbasesecurity.common.commands.TimeCommand;
import com.cadergator10.advancedbasesecurity.common.commands.WebsocketCommand;
import com.cadergator10.advancedbasesecurity.common.CommonProxy;
import com.cadergator10.advancedbasesecurity.client.config.WebsocketConfig;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.DoorNamePacket;
import com.cadergator10.advancedbasesecurity.common.networking.DoorServerRequest;
import com.cadergator10.advancedbasesecurity.common.networking.DoorUpdatePacket;
import com.cadergator10.advancedbasesecurity.common.networking.OneDoorDataPacket;
import com.cadergator10.advancedbasesecurity.common.networking.handlers.DoorNamedHandler;
import com.cadergator10.advancedbasesecurity.common.networking.handlers.ServerGenericHandler;
import com.cadergator10.advancedbasesecurity.util.WebsocketHandler;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.IdentityHashMap;
import java.util.Map;

@Mod.EventBusSubscriber
@Mod(modid = AdvBaseSecurity.MODID, name = AdvBaseSecurity.NAME, version = AdvBaseSecurity.VERSION, dependencies = "required-after: baubles")
public class AdvBaseSecurity
{
    public static final String MODID = "advancedbasesecurity";
    public static final String NAME = "Advanced Base Security";
    public static final String VERSION = "1.0.0";
    @Mod.Instance(value = MODID)
    public static AdvBaseSecurity instance;
    @SidedProxy(clientSide = "com.cadergator10.advancedbasesecurity.client.ClientProxy", serverSide = "com.cadergator10.advancedbasesecurity.common.CommonProxy")
    public static CommonProxy proxy;
    public final Logger logger = LogManager.getFormatterLogger(MODID);
    public WebsocketHandler ws;
    public DoorHandler doorHandler;
    public SimpleNetworkWrapper network;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preinit(event);
        network = NetworkRegistry.INSTANCE.newSimpleChannel("AdvBaseSecurity");
        int packetID = 0;
        if(event.getSide() == Side.CLIENT) {
            network.registerMessage(DoorNamedHandler.class, DoorNamePacket.class, packetID++, Side.CLIENT);
            network.registerMessage(OneDoorDataPacket.Handler.class, OneDoorDataPacket.class, packetID++, Side.CLIENT);
        }
        else{
            network.registerMessage(ServerGenericHandler.class, DoorNamePacket.class, packetID++, Side.CLIENT);
            network.registerMessage(OneDoorDataPacket.HandlerS.class, OneDoorDataPacket.class, packetID++, Side.CLIENT);
        }
        network.registerMessage(OneDoorDataPacket.HandlerS.class, OneDoorDataPacket.class, packetID++, Side.SERVER);
        network.registerMessage(DoorServerRequest.Handler.class, DoorServerRequest.class, packetID++, Side.SERVER);
        network.registerMessage(DoorUpdatePacket.Handler.class, DoorUpdatePacket.class, packetID++, Side.SERVER);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        System.out.println("Baubles is installed: " + Loader.isModLoaded("baubles"));
        proxy.init(event);
        if(event.getSide() != Side.CLIENT) {
            if (WebsocketConfig.enableWebsocket) {
                Map<String, String> httpHeaders = new IdentityHashMap<>();
                httpHeaders.put("Authorization", WebsocketConfig.websocketCode);
                ws = new WebsocketHandler(httpHeaders);
                ws.connect();
                //using some host verification stuff
//            try{
//                ws.connectBlocking();
//                //verify
//                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
//                SSLSocket socket = (SSLSocket) ws.getSocket();
//                SSLSession s = socket.getSession();
//                logger.debug("verifying");
//                if (!hv.verify("echo.websocket.org", s)) {
//                    logger.debug("wrong url");
//                    logger.error("Client", "Expected echo.websocket.org, found " + s.getPeerPrincipal());
//                    throw new SSLHandshakeException("Expected websocket.org, found " + s.getPeerPrincipal());
//                } else {
//                    logger.info("Client", "Success");
//                }
//            }
//            catch (Exception e){
//                logger.error("WEBSOCKET ERROR: " + e);
//            }
            }
        }
        doorHandler = new DoorHandler();
        MinecraftForge.EVENT_BUS.register(doorHandler);
    }
    @SubscribeEvent
    public static void onRegisterModels(ModelRegistryEvent event) {
        instance.proxy.registerModels();
    }

    @EventHandler
    public void ServerStart(FMLServerStartingEvent event){
        event.registerServerCommand(new RequestCommand());
        event.registerServerCommand(new TimeCommand());
        event.registerServerCommand(new WebsocketCommand());
        event.registerServerCommand(new BaseSecurityCommand());
        AdvBaseSecurity.instance.logger.info("In ServerStart");
        //prep door
        //doorHandler.onWorldLoad(event);
    }

    @EventHandler
    public void ServerStarted(FMLServerStartedEvent event){

    }


    @EventHandler
    public void ServerClose(FMLServerStoppingEvent event) {
        if (WebsocketConfig.enableWebsocket && event.getSide() != Side.CLIENT) {
            ws.niceClose();
        }
    }

    @EventHandler
    public void ServerClosed(FMLServerStoppedEvent event){
        doorHandler.onWorldUnload(event);
    }
}
