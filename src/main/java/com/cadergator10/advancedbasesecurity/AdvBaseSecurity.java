package com.cadergator10.advancedbasesecurity;

import com.cadergator10.advancedbasesecurity.common.CommonProxy;
import com.cadergator10.advancedbasesecurity.common.commands.BaseSecurityCommand;
import com.cadergator10.advancedbasesecurity.common.commands.RequestCommand;
import com.cadergator10.advancedbasesecurity.common.commands.TimeCommand;
import com.cadergator10.advancedbasesecurity.common.commands.WebsocketCommand;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.*;
import com.cadergator10.advancedbasesecurity.common.networking.handlers.DoorNamedHandler;
import com.cadergator10.advancedbasesecurity.common.networking.handlers.ServerGenericHandler;
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
    public DoorHandler doorHandler;
    public SimpleNetworkWrapper network;
    public static boolean isSCPInstalled = false;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preinit(event);
        network = NetworkRegistry.INSTANCE.newSimpleChannel("AdvBaseSecurity");
        int packetID = 0;
        if(event.getSide() == Side.CLIENT) {
            network.registerMessage(DoorNamedHandler.class, DoorNamePacket.class, packetID++, Side.CLIENT);
            network.registerMessage(RequestPassesPacket.Handler.class, RequestPassesPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(RequestPassesPacket.HandlerS.class, RequestPassesPacket.class, packetID++, Side.SERVER);
            network.registerMessage(UserEditPacket.Handler.class, UserEditPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(UserEditPacket.HandlerS.class, UserEditPacket.class, packetID++, Side.SERVER);
            network.registerMessage(PassEditPacket.Handler.class, PassEditPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(PassEditPacket.HandlerS.class, PassEditPacket.class, packetID++, Side.SERVER);
            network.registerMessage(OneDoorDataPacket.Handler.class, OneDoorDataPacket.class, packetID++, Side.CLIENT);
        }
        else{
            network.registerMessage(ServerGenericHandler.class, DoorNamePacket.class, packetID++, Side.CLIENT);
            network.registerMessage(RequestPassesPacket.HandlerS.class, RequestPassesPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(RequestPassesPacket.HandlerS.class, RequestPassesPacket.class, packetID++, Side.SERVER);
            network.registerMessage(UserEditPacket.HandlerS.class, UserEditPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(UserEditPacket.HandlerS.class, UserEditPacket.class, packetID++, Side.SERVER);
            network.registerMessage(PassEditPacket.HandlerS.class, PassEditPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(PassEditPacket.HandlerS.class, PassEditPacket.class, packetID++, Side.SERVER);
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
        isSCPInstalled = Loader.isModLoaded("scproleplay");
        System.out.println("SCP Roleplay Mod is installed: " + isSCPInstalled);
        proxy.init(event);
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
    public void ServerClosed(FMLServerStoppedEvent event){
        doorHandler.onWorldUnload(event);
    }
}
