package com.cadergator10.advancedbasesecurity.common.commands;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.networking.DoorNamePacket;
import com.cadergator10.advancedbasesecurity.util.WebsocketHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.awt.*;

public class BaseSecurityCommand extends CommandBase {

	@Override
	public String getName() {
		return "basesecurity";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "command.advancedbasesecurity.basesecuritycommand.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!server.getEntityWorld().isRemote) {
			if (args.length > 0) {
				switch (args[0]) {
					case "doors":
						if (args.length > 1) {
							//send packet to user
							switch (args[1]) {
								case "edit":
									DoorNamePacket packet = new DoorNamePacket(AdvBaseSecurity.instance.doorHandler.DoorGroups);
									ICommandSender sendered = sender.getCommandSenderEntity();
									if(sendered == null || !sender.getCommandSenderEntity().getClass().equals(EntityPlayerMP.class))
									{
										sender.sendMessage(new TextComponentString(Color.RED + "Cannot run command in console. Must be a player"));
										break;
									}
									AdvBaseSecurity.instance.network.sendTo(packet, ((EntityPlayerMP) sendered));
								default:
									sender.sendMessage(new TextComponentString(Color.RED + "Invalid Arguments"));
									break;
							}
						} else {
							sender.sendMessage(new TextComponentString(Color.RED + "Invalid Arguments"));
						}
						break;
					default:
						sender.sendMessage(new TextComponentString(Color.RED + "Invalid Arguments"));
						break;
				}
			}
			else {
				sender.sendMessage(new TextComponentString(Color.RED + "Invalid Arguments"));
			}
		}
	}
}
