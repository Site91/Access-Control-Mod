package com.cadergator10.advancedbasesecurity.common.commands;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.util.HTTPBinRequest;
import com.cadergator10.advancedbasesecurity.util.ResponseHolder;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.UUID;
import java.util.function.Consumer;

public class RequestCommand extends CommandBase {

	Consumer<ResponseHolder> resp = a -> PrintResult(a);

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		//AdvBaseSecurity.instance.logger.info("execute called");

		if (!server.getEntityWorld().isRemote && params != null && params.length == 1) {
			AdvBaseSecurity.instance.logger.info("Class of sender: " + sender.getCommandSenderEntity().getClass().toString());
			AdvBaseSecurity.instance.logger.info("Class needed: " + EntityPlayer.class.toString());
			AdvBaseSecurity.instance.logger.info("Sender Entity: " + sender.getCommandSenderEntity().toString());
			HTTPBinRequest bin = new HTTPBinRequest(resp, HTTPBinRequest.ReqType.GET, params[0], new String[] {sender.getCommandSenderEntity() == null || !sender.getCommandSenderEntity().getClass().equals(EntityPlayerMP.class) ? "non-player" : ((EntityPlayerMP) sender.getCommandSenderEntity()).getUniqueID().toString()});
			bin.start();

//			TextComponentString text = new TextComponentString(httpResponse.toString());
//			text.getStyle().setColor(TextFormatting.RED);
//			sender.sendMessage(text);
		}
	}

	public void PrintResult(ResponseHolder response){
		AdvBaseSecurity.instance.logger.info(response.response.toString());
		AdvBaseSecurity.instance.logger.info(response.args[0]);
		if(!response.args[0].equals("non-player")){
			TextComponentString text = new TextComponentString(response.response.toString());
			text.getStyle().setColor(TextFormatting.GREEN);
			EntityPlayerMP pl = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(response.args[0]));
			pl.sendMessage(text);
		}
	}

	@Override
	public String getName() {
		return "requestcommand";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "command.advancedbasesecurity.requestcommand.usage";
	}
}
