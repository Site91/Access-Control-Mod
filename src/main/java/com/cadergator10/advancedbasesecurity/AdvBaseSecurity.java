package com.cadergator10.advancedbasesecurity;

import com.cadergator10.advancedbasesecurity.common.CommonProxy;
import com.cadergator10.advancedbasesecurity.common.SoundHandler;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
    public static final String VERSION = "0.1.1";
    @Mod.Instance(value = MODID)
    public static AdvBaseSecurity instance;
    @SidedProxy(clientSide = "com.cadergator10.advancedbasesecurity.client.ClientProxy", serverSide = "com.cadergator10.advancedbasesecurity.common.CommonProxy")
    public static CommonProxy proxy; //simply sides.
    public final Logger logger = LogManager.getFormatterLogger(MODID);
    public DoorHandler doorHandler; //The main global handler
    public SimpleNetworkWrapper network;
    public static boolean isSCPInstalled = false; //If the SCP mod is installed. If so, some networking stuff or extra features enabled

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preinit(event);
        SoundHandler.registerSounds();
        network = NetworkRegistry.INSTANCE.newSimpleChannel("AdvBaseSecurity"); //setup network
        int packetID = 0;
        if(event.getSide() == Side.CLIENT) { //registering all packets. If packets handle GUIS, register separately with different handlers as GUI cannot be on server side
            network.registerMessage(DoorNamePacket.Handler.class, DoorNamePacket.class, packetID++, Side.CLIENT);
            network.registerMessage(RequestPassesPacket.Handler.class, RequestPassesPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(RequestPassesPacket.HandlerS.class, RequestPassesPacket.class, packetID++, Side.SERVER);
            network.registerMessage(UserEditPacket.Handler.class, UserEditPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(UserEditPacket.HandlerS.class, UserEditPacket.class, packetID++, Side.SERVER);
            network.registerMessage(PassEditPacket.Handler.class, PassEditPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(PassEditPacket.HandlerS.class, PassEditPacket.class, packetID++, Side.SERVER);
            network.registerMessage(SectControllerPacket.Handler.class, SectControllerPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(SectControllerPacket.HandlerS.class, SectControllerPacket.class, packetID++, Side.SERVER);
            network.registerMessage(SectorEditPacket.Handler.class, SectorEditPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(SectorEditPacket.HandlerS.class, SectorEditPacket.class, packetID++, Side.SERVER);
            network.registerMessage(ManagerNamePacket.Handler.class, ManagerNamePacket.class, packetID++, Side.CLIENT);
            network.registerMessage(OneDoorDataPacket.Handler.class, OneDoorDataPacket.class, packetID++, Side.CLIENT);
        }
        else{
            network.registerMessage(DoorNamePacket.HandlerS.class, DoorNamePacket.class, packetID++, Side.CLIENT);
            network.registerMessage(RequestPassesPacket.HandlerS.class, RequestPassesPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(RequestPassesPacket.HandlerS.class, RequestPassesPacket.class, packetID++, Side.SERVER);
            network.registerMessage(UserEditPacket.HandlerS.class, UserEditPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(UserEditPacket.HandlerS.class, UserEditPacket.class, packetID++, Side.SERVER);
            network.registerMessage(PassEditPacket.HandlerS.class, PassEditPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(PassEditPacket.HandlerS.class, PassEditPacket.class, packetID++, Side.SERVER);
            network.registerMessage(SectControllerPacket.HandlerS.class, SectControllerPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(SectControllerPacket.HandlerS.class, SectControllerPacket.class, packetID++, Side.SERVER);
            network.registerMessage(SectorEditPacket.HandlerS.class, SectorEditPacket.class, packetID++, Side.CLIENT);
            network.registerMessage(SectorEditPacket.HandlerS.class, SectorEditPacket.class, packetID++, Side.SERVER);
            network.registerMessage(ManagerNamePacket.HandlerS.class, ManagerNamePacket.class, packetID++, Side.CLIENT);
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
        MinecraftForge.EVENT_BUS.register(doorHandler);  //register all event handlers in the DoorHandler instance
    }
    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinWorldEvent event){
        if(!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerMP){
            ((EntityPlayerMP) event.getEntity()).sendMessage(new TextComponentString("Access Control mod created by Cadergator10\nPlease note this is VERY EARLY ACCESS so there will be many issues!\n1. There are no recipes yet\n2. Block breakability has not been worked on (anyone could break the blocks easily)\n3. Other issues may arise too\nCurrently this is working in a state good for Adventure mode (maps) and just for fun/amusement.\nEnjoy! :)"));
        }
    }
    @SubscribeEvent
    public static void onRegisterModels(ModelRegistryEvent event) {
        instance.proxy.registerModels(); //register models on proxy. Pretty much only the client proxy
    }

    @EventHandler
    public void ServerStart(FMLServerStartingEvent event){ //was used for other reasons, but no longer needed.
        //event.registerServerCommand(new BaseSecurityCommand()); //command removed since phasing out of it
        AdvBaseSecurity.instance.logger.info("In ServerStart");
        //prep door
        //doorHandler.onWorldLoad(event);
    }

    @EventHandler
    public void ServerStarted(FMLServerStartedEvent event){

    }

    @EventHandler
    public void ServerClosed(FMLServerStoppedEvent event){ //Ensure stuff is cleared out in case of singleplayer.
        doorHandler.onWorldUnload(event);
    }

    public static void LogDebug(String msg){
        AdvBaseSecurity.instance.logger.debug(msg);
    }

    public static void Log(String msg){
        AdvBaseSecurity.instance.logger.info(msg);
    }

    public static void Log(Logger lvl, String msg){
        AdvBaseSecurity.instance.logger.log(lvl.getLevel(), msg);
    }
}
