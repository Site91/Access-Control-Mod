package com.cadergator10.advancedbasesecurity.common.commands;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.util.WebsocketHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.awt.*;

public class WebsocketCommand extends CommandBase {

	@Override
	public String getName() {
		return "bswebsocket";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "command.advancedbasesecurity.websocketcommand.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length > 0 && AdvBaseSecurity.instance.ws != null){
			switch(args[0]){
				case "status":
					sender.sendMessage(new TextComponentString("The current websocket status is: " + WebsocketHandler.socketStatus));
					break;
				case "stop":
					if(WebsocketHandler.socketStatus == WebsocketHandler.socketStatusEnum.STOPPED || WebsocketHandler.socketStatus == WebsocketHandler.socketStatusEnum.DISABLED){
						sender.sendMessage(new TextComponentString("The websocket is already stopped or disabled"));
					}
					else{
						AdvBaseSecurity.instance.ws.niceClose();
						sender.sendMessage(new TextComponentString("Stopping the Websocket!"));
					}
					break;
				case "start":
					if(WebsocketHandler.socketStatus == WebsocketHandler.socketStatusEnum.DISABLED)
						sender.sendMessage(new TextComponentString(Color.RED + "Websocket is disabled. Please enable the websocket in the config."));
					else if(WebsocketHandler.socketStatus == WebsocketHandler.socketStatusEnum.STOPPED || WebsocketHandler.socketStatus == WebsocketHandler.socketStatusEnum.RETRYING){
						AdvBaseSecurity.instance.ws.commandConnect();
						sender.sendMessage(new TextComponentString("Attempting to reconnect to the websocket..."));
					}
					else{
						sender.sendMessage(new TextComponentString("Already in the status of: " + WebsocketHandler.socketStatus));
					}
					break;
				case "lastclose":
					sender.sendMessage(new TextComponentString("Last error/close message: " + AdvBaseSecurity.instance.ws.getLastError()));
					break;
//				case "reconnect":
//					if(WebsocketHandler.socketStatus == WebsocketHandler.socketStatusEnum.DISABLED)
//						sender.sendMessage(new TextComponentString(Color.RED + "Websocket is disabled. Please enable the websocket in the config."));
//					else if(WebsocketHandler.socketStatus == WebsocketHandler.socketStatusEnum.STOPPED || WebsocketHandler.socketStatus == WebsocketHandler.socketStatusEnum.RETRYING){
//						AdvBaseSecurity.instance.ws.commandConnect();
//						sender.sendMessage(new TextComponentString("Attempting to reconnect to the websocket..."));
//					}
//					else{
//						AdvBaseSecurity.instance.ws.reconnect();
//						sender.sendMessage(new TextComponentString("Reconnecting to the server..."));
//					}
//					break;
				default:
					sender.sendMessage(new TextComponentString(Color.RED + "Invalid Arguments"));
			}
		}
	}
}
