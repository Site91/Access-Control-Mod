package com.cadergator10.advancedbasesecurity.common;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public World getWorld(int dimId) {
        //overridden separately for client and server.
        return null;
    }

    public void registerSounds() {

    }

    public void preinit(FMLPreInitializationEvent event){
//        MinecraftForge.EVENT_BUS.register(new TickHandler());
    }
    public void init(FMLInitializationEvent event){
        System.out.println("Hey yall! Scott here! And today, we're going to be sending REQUESTS!!!");
        event.getSide();
//        String test = null;
//        HTTPBinRequest request = new HTTPBinRequest();
//        HttpResponse resp = request.executeGet("http://httpbin.org/get");//request.executePostJson("http://httpbin.org/anything","{\"ping\":\"pong\"}");
//        test = resp.toString();
//        System.out.println("And the thing is that this thing says ");
//        System.out.println(test);
    }
    protected void registerRenderers() {


    }
    public void registerModels() {
    }

}
