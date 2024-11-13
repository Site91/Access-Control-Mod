package com.cadergator10.advancedbasesecurity.common;

import com.cadergator10.advancedbasesecurity.client.renderer.RenderCardReader;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityCardReader;
import com.cadergator10.advancedbasesecurity.util.TickHandler;
import com.cadergator10.advancedbasesecurity.util.WebsocketHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.HashMap;
import java.util.HashSet;

public class CommonProxy {

    public World getWorld(int dimId) {
        //overridden separately for client and server.
        return null;
    }

    public void registerSounds() {

    }

    public void preinit(FMLPreInitializationEvent event){
        MinecraftForge.EVENT_BUS.register(new TickHandler());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCardReader.class, new RenderCardReader());
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
