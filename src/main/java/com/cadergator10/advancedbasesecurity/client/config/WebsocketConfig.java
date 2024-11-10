package com.cadergator10.advancedbasesecurity.client.config;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = AdvBaseSecurity.MODID, category = "websocket")
@EventBusSubscriber(modid = AdvBaseSecurity.MODID)
public class WebsocketConfig {
	//@formatter:off
	@Name("enableWebsocket")
	@LangKey("advancedbasesecurity.gui.config.websocket.enablewebsocket")
	@Comment("Use online services to manage your server through the website (must have authorized code to use)")
	@RequiresMcRestart
	public static boolean enableWebsocket = false;

	@Name("websocketUrl")
	@LangKey("advancedbasesecurity.gui.config.websocket.url")
	@Comment("Address to try and connect to.")
	public static String websocketUrl = "N/A";

	@Name("websocketCode")
	@LangKey("advancedbasesecurity.gui.config.websocket.code")
	@Comment("Access code to link websocket.")
	public static String websocketCode = "N/A";

	@Name("websocketVerification")
	@Comment("Enable/Disable verification of the websocket on connection. Disable if getting the \"certificate does not match expected hostname\" error")
	@RequiresMcRestart
	public static boolean websocketVerification = true;

	@Name("websocketRetries")
	@Comment("IN MINUTES! Whenever a disconnect event occurs, how long after each failed retry should it wait? If auto-retries exceeds the length of list, it will either default to 1 hour or whatever is at the end of the list if that delay is longer. Websocket always retries immediately on first failure to connect.")
	public static int[] websocketRetryDelays = {1,1,5,10,30};

	@Name("Retry Websocket Connection")
	@Comment("Once the retry amount exceeds the Websocket Retries list, should the websocket keep trying automatically? If false, the websocket will go into a stopped state until \"/websocket connection start\" is run.")
	public static boolean retryWebsocket = false;

	private WebsocketConfig() {}

	@SubscribeEvent
	public static void onConfigChanged(OnConfigChangedEvent event) {
//		if (AdvBaseSecurity.MODID.equals(event.getModID()))
//			loadEffects();
	}
}