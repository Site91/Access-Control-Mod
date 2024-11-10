package com.cadergator10.advancedbasesecurity.common.commands;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.util.ResponseHolder;
import com.cadergator10.advancedbasesecurity.util.WebsocketConnection;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.json.JSONObject;

import java.util.UUID;
import java.util.function.Consumer;

public class TimeCommand extends CommandBase {
	String uuid = "";
	Consumer<WebsocketConnection.ConsumerDouble> resp = a -> PrintResult(a.obj, this.uuid);

	@Override
	public String getName() {
		return "getrealtime";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "command.advancedbasesecurity.timecommand.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		uuid = sender.getCommandSenderEntity() == null || !sender.getCommandSenderEntity().getClass().equals(EntityPlayerMP.class) ? "non-player" : ((EntityPlayerMP) sender.getCommandSenderEntity()).getUniqueID().toString();
		JSONObject obj = new JSONObject();
		obj.put("reqtype", "getrealtime");
		obj.put("uuid",uuid);
		WebsocketConnection ws = new WebsocketConnection(resp, obj);
		ws.run();
	}
	public void PrintResult(JSONObject response, String uuid) {
		if (response.has("closed") && response.getBoolean("closed")){
			if (!uuid.equals("non-player")) {
				FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(uuid)).sendMessage(new TextComponentString("An error has occurred and the websocket requests were cleared for the time command"));
			} else
				AdvBaseSecurity.instance.logger.info("An error has occurred and the websocket requests were cleared for the time command");
		}
		else {
			if (!uuid.equals("non-player")) {
				FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(uuid)).sendMessage(new TextComponentString(response.getString("time")));
			} else
				AdvBaseSecurity.instance.logger.info(response.getString("time"));
		}
	}

}
