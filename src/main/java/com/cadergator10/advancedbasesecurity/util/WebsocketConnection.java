package com.cadergator10.advancedbasesecurity.util;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

import java.util.Random;
import java.util.function.Consumer;

public class WebsocketConnection{
	public JSONObject sender;
	public JSONObject receiver;
	public Consumer<ConsumerDouble> obj;
	public int msgId;

	static int idCount = 0;
	static int MAX_INT = Integer.MAX_VALUE;

	public WebsocketConnection(Consumer<ConsumerDouble> obj, JSONObject sender){
		this.sender = sender;
		this.obj = obj;
		msgId = idCount; //ID to send to the server setup. ensures that responses to the server make it to the right place
		if(idCount >= MAX_INT)
			idCount = -1;
		idCount++;
	}
	public void run(){
		WebsocketHandler.performRequest(sender ,this);
	}

	public static class ConsumerDouble{
		public JSONObject obj;
		public TickEvent event;
		public ConsumerDouble(JSONObject obj, TickEvent.ServerTickEvent event){
			this.obj = obj;
			this.event = event;
		}
	}
}
