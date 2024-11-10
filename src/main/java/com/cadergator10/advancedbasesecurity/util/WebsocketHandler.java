package com.cadergator10.advancedbasesecurity.util;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.config.WebsocketConfig;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.framing.PongFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.*;

public class WebsocketHandler extends WebSocketClient {

	public enum socketStatusEnum {DISABLED, STOPPED, CONNECTING, ACTIVE, RETRYING}
	//Disabled = not enabled in config, so no attempt was even made. Requires a restart to attempt a websocket connection.
	//Stopped = websocket not active. There is no attempt to start a connection. Either the websocket failed to connect the first time, or manually stopped via command
	//Connecting = currently waiting for the response from server whether the connection is open or closed.
	//Active = Active websocket connection and working.
	//Retrying = disconnect event and on a timer before attempting to reconnect.

	public static socketStatusEnum socketStatus = socketStatusEnum.DISABLED;
	private static int timer = 0;
	public static int retries = 0;
	public static boolean firstTime = true;
	public static boolean ourStop = false;
	private String lastError = "no errors logged";


	static Queue<WebsocketConnection> connections = new LinkedList<>();
	static Queue<WebsocketConnection> pushUpdate = new LinkedList<>();

	public WebsocketHandler(Map<String, String> httpHeaders){
		super(URI.create(WebsocketConfig.websocketUrl), httpHeaders);
		socketStatus = socketStatusEnum.STOPPED;
	}
	public WebsocketHandler(){
		super(URI.create(WebsocketConfig.websocketUrl));
		socketStatus = socketStatusEnum.STOPPED;
	}

	@Override
	public void connect() {
		super.connect();
		ourStop = false;
		if(socketStatus == socketStatusEnum.STOPPED && firstTime) //just in case for some reason this is run after successfully connected.
		{
			retries = 0;
			socketStatus = socketStatusEnum.CONNECTING;
		}
	}
	public void commandConnect(){ //if run through command, then reset retries
		retries = 0;
		reconnect();
	}

	@Override
	public void reconnect() {
		super.reconnect();
		ourStop = false;
		if(socketStatus == socketStatusEnum.RETRYING){
			socketStatus = socketStatusEnum.CONNECTING;
			timer = 0;
			retries++;
		}
		else if(socketStatus == socketStatusEnum.STOPPED && firstTime) //just in case for some reason this is run after successfully connected.
		{
			retries = 0;
			socketStatus = socketStatusEnum.CONNECTING;
		}
	}

	public void niceClose(){
		if(socketStatus == socketStatusEnum.ACTIVE || socketStatus == socketStatusEnum.CONNECTING) {
			ourStop = true;
			close();
			socketStatus = socketStatusEnum.STOPPED;
		}
		else if(socketStatus == socketStatusEnum.RETRYING){ //Just switch to stopped state
			socketStatus = socketStatusEnum.STOPPED;
			timer = 0;
			retries = 0;
		}
	}

	//Called form TickHandler
	public void Tick(TickEvent.ServerTickEvent event){ //If in retry waiting period decrease timer.
		if(socketStatus != socketStatusEnum.DISABLED){
			if(!pushUpdate.isEmpty()){
				while(!pushUpdate.isEmpty()){
					WebsocketConnection con = pushUpdate.poll();
					con.obj.accept(new WebsocketConnection.ConsumerDouble(con.receiver, event));
				}
			}
			if(socketStatus == socketStatusEnum.RETRYING){
				timer--;
				if(timer <= 0){
					reconnect();
				}
			}
			}
	}

	//If error occurs, clear all waiting requests
	private void clearResponses(){
		while(!connections.isEmpty()){
			JSONObject obj = new JSONObject();
			obj.append("closed", true);
			WebsocketConnection con = connections.poll();
			con.receiver = obj;
			pushUpdate.add(con);
		}
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		socketStatus = socketStatusEnum.ACTIVE;
		retries = 0;
		AdvBaseSecurity.instance.logger.info("Successfully connected to the websocket!");
	}

	@Override
	public void onMessage(String message) {
		try {
			AdvBaseSecurity.instance.logger.info(message);
			JSONObject jsonObj = new JSONObject(message);
			if (!jsonObj.isEmpty() && jsonObj.has("reqtype")) { //This should be a valid message
				String requestType = jsonObj.getString("reqtype");
				JSONObject returnObj = new JSONObject();
				boolean sendBack = false;
				if(jsonObj.has("clientid")){
					int msgId = jsonObj.getInt("clientid");
					WebsocketConnection con = null;
					for(WebsocketConnection c : connections){
						if(c.msgId == msgId){
							con = c;
							break;
						}
					}
					if(con != null){
						con.receiver = jsonObj;
						pushUpdate.add(con);
						connections.remove(con);
					}
					else{
						AdvBaseSecurity.instance.logger.info("No message with ID " + msgId + "\nRequest type: " + requestType);
					}
				}
				else {
					if(jsonObj.has("serverid")){
						returnObj.put("serverid",jsonObj.getInt("serverid")); //pass serverID back so it knows what request it is.
					}
					switch (requestType) {
						case "playerlist":
							List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
							AdvBaseSecurity.instance.logger.info("Players: " + players.size());
							if(!players.isEmpty())
								AdvBaseSecurity.instance.logger.info("First Player: " + players.get(0).getDisplayName().getUnformattedText());
							List<String> returnPlayers = new LinkedList<>();
							List<String> returnPlayersU = new LinkedList<>();
							for (EntityPlayerMP p : players) {
								returnPlayers.add(p.getDisplayName().getUnformattedText());
								returnPlayersU.add(p.getUniqueID().toString());
							}
							returnObj.put("playerNames", returnPlayers);
							returnObj.put("playerUUIDs", returnPlayersU);
							sendBack = true;
							break;
						case "playermsg":
							EntityPlayerMP player = jsonObj.has("uuid") ? FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(jsonObj.getString("uuid"))) : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(jsonObj.getString("username"));
							player.sendMessage(new TextComponentString(jsonObj.getString("message")));
							break;
						case "playerkill":
							EntityPlayerMP player2 = jsonObj.has("uuid") ? FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(jsonObj.getString("uuid"))) : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(jsonObj.getString("username"));
							player2.setHealth(0);
							break;
						case "print":
							AdvBaseSecurity.instance.logger.info(jsonObj.getString("message"));
							break;
						default:
							AdvBaseSecurity.instance.logger.info("Websocket: Invalid request type: " + requestType);
							break;
					}
					if (sendBack) {
						AdvBaseSecurity.instance.ws.send(returnObj.toString());
					}
				}
			}
		} catch (JSONException e) {
			AdvBaseSecurity.instance.logger.info("Invalid websocket message: " + message + "\n" + e);
		}
	}

	@Override
	public void onClose(int i, String s, boolean b) {
		AdvBaseSecurity.instance.logger.info("Websocket closed/ code: " + i);
		lastError = "Close code: " + i + "/Message: " + s;
		clearResponses();
		if(i == 1006 && !ourStop){ //error 1006 means websocket error. our stop false means not our own stop. retry
			if(retries == 0){ //first error. Try again IMMEDIATELY!!
				socketStatus = socketStatusEnum.RETRYING;
				timer = 5;
				return;
			}
			else if(retries > WebsocketConfig.websocketRetryDelays.length){ //beyond retry timer length.
				if(WebsocketConfig.retryWebsocket)
				{
					socketStatus = socketStatusEnum.RETRYING;
					timer = (Math.max(WebsocketConfig.websocketRetryDelays[WebsocketConfig.websocketRetryDelays.length - 1],60)) * 60 * 20; //1 hour (60 min) to seconds (60) to ticks (20). If last value in list is bigger then use that instead
					retries++;
					return;
				}
				//beyond limit, so stop
				socketStatus = socketStatusEnum.STOPPED;
			}
			else{
				socketStatus = socketStatusEnum.RETRYING;
				timer = WebsocketConfig.websocketRetryDelays[retries - 1];
				retries++;
			}
		}
	}

	@Override
	public void onError(Exception e) {
		AdvBaseSecurity.instance.logger.info("WEBSOCKET ERROR: " + e);
		lastError = e.toString();
		socketStatus = socketStatusEnum.STOPPED;
		clearResponses();
	}

	@Override
	public void onWebsocketPing(WebSocket conn, Framedata f) {
		conn.sendFrame(new PongFrame((PingFrame)f));
	}

	//	WebSocketAdapter properMessages = new WebSocketAdapter() {
//		@Override
//		public void onTextMessage(WebSocket websocket, String message) throws Exception {
//			// Received a text message
//			try {
//				JSONObject jsonObj = new JSONObject(message);
//				if (!jsonObj.isEmpty() && jsonObj.has("reqtype")) { //This should be a valid message
//					String requestType = jsonObj.getString("reqtype");
//					JSONObject returnObj = new JSONObject();
//					boolean sendBack = false;
//					if(jsonObj.has("clientid")){
//						int msgId = jsonObj.getInt("clientid");
//						WebsocketConnection con = null;
//						for(WebsocketConnection c : connections){
//							if(c.msgId == msgId){
//								con = c;
//								break;
//							}
//						}
//						if(con != null){
//							con.obj.accept(jsonObj);
//							connections.remove(con);
//						}
//						else{
//							AdvBaseSecurity.instance.logger.info("No message with ID " + msgId + "\nRequest type: " + requestType);
//						}
//					}
//					else {
//						if(jsonObj.has("serverid")){
//							returnObj.put("serverid",jsonObj.getInt("serverid")); //pass serverID back so it knows what request it is.
//						}
//						switch (requestType) {
//							case "playerlist":
//								List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
//								List<player> returnPlayers = new LinkedList<>();
//								for (EntityPlayerMP p : players) {
//									returnPlayers.add(new player(p.getDisplayName().toString(), p.getUniqueID().toString()));
//								}
//								returnObj.put("players", returnPlayers);
//								sendBack = true;
//								break;
//							case "playermsg":
//								EntityPlayerMP player = jsonObj.has("uuid") ? FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(jsonObj.getString("uuid"))) : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(jsonObj.getString("username"));
//								player.sendMessage(new TextComponentString(jsonObj.getString("message")));
//								break;
//							case "playerkill":
//								EntityPlayerMP player2 = jsonObj.has("uuid") ? FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(jsonObj.getString("uuid"))) : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(jsonObj.getString("username"));
//								player2.setHealth(0);
//								break;
//							case "print":
//								AdvBaseSecurity.instance.logger.info(jsonObj.getString("message"));
//								break;
//							default:
//								AdvBaseSecurity.instance.logger.info("Websocket: Invalid request type: " + requestType);
//								break;
//						}
//						if (sendBack) {
//							ws.sendText(returnObj.toString());
//						}
//					}
//				}
//			} catch (JSONException e) {
//				AdvBaseSecurity.instance.logger.info("Invalid websocket message: " + message);
//			}
//		}
//
//		@Override
//		public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
//			AdvBaseSecurity.instance.logger.info("WEBSOCKET ERROR: " + exception);
//		}
//
//		@Override
//		public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
//			//Perform ping test
//			AdvBaseSecurity.instance.logger.info("Successfully connected to the websocket! Expecting authorization message...");
//			//ws.sendText("Hey yall scott here!");
//
//			//
//		}
//	};
	public String getLastError(){
		return lastError;
	}

	public static void performRequest(JSONObject obj, WebsocketConnection con){
		connections.add(con);
		obj.put("clientid",con.msgId);
		AdvBaseSecurity.instance.ws.send(obj.toString());
	}
}

class player{
	player(String name, String uuid){
		this.uuid = uuid;
		this.name = name;
	}
	public String uuid;
	public String name;
}
