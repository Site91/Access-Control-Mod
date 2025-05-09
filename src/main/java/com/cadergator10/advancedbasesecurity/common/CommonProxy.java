package com.cadergator10.advancedbasesecurity.common;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * Proxy for both Server and Client (eg: shared, non-visual stuff: basic core)
 */
public class CommonProxy {

    public World getWorld(int dimId) {
        //overridden separately for client and server.
        return null;
    }

    public void registerSounds() {
        //AUTOGENERATED STUB (client side only)
    }

    public void preinit(FMLPreInitializationEvent event){ //no longer used after TickHandler moved to SCP mod
//        MinecraftForge.EVENT_BUS.register(new TickHandler());
    }
    public void init(FMLInitializationEvent event){
        NetworkRegistry.INSTANCE.registerGuiHandler(AdvBaseSecurity.instance, new GuiHandler());
//        String test = null;
//        HTTPBinRequest request = new HTTPBinRequest();
//        HttpResponse resp = request.executeGet("http://httpbin.org/get");//request.executePostJson("http://httpbin.org/anything","{\"ping\":\"pong\"}");
//        test = resp.toString();
//        System.out.println("And the thing is that this thing says ");
//        System.out.println(test);
    }
    protected void registerRenderers() {
        //AUTOGENERATED STUB (client side only)
    }
    public void registerModels() {
        //AUTOGENERATED STUB (client side only)
    }

}
