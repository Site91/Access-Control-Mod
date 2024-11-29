package com.cadergator10.advancedbasesecurity.util;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class TickHandler {
	public static Queue<EventHolder> threads = new LinkedList<>();
	@SubscribeEvent
	public void Tick(TickEvent.ServerTickEvent event){
		if(event.phase == TickEvent.Phase.START) {
	//		if(!threads.isEmpty()){
	//			for(EventHolder t : threads){
	//				if(t.processId.equals("requestcommand")){
	//					EntityPlayer pl = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(t.data2));
	//					pl.sendMessage(new TextComponentString(t.data));
	//				}
	//			}
	//		}
			if (AdvBaseSecurity.instance.ws != null)
				AdvBaseSecurity.instance.ws.Tick(event);
		}
	}
}

