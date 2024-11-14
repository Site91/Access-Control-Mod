package com.cadergator10.advancedbasesecurity.common.commands;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import com.cadergator10.advancedbasesecurity.common.networking.DoorNamePacket;
import com.cadergator10.advancedbasesecurity.common.networking.OneDoorDataPacket;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

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
							ICommandSender sendered = sender.getCommandSenderEntity();
							switch (args[1]) {
								case "edit":
									DoorNamePacket packet = new DoorNamePacket(AdvBaseSecurity.instance.doorHandler.DoorGroups, AdvBaseSecurity.instance.doorHandler.getEditValidator());
									if(sendered == null || !sender.getCommandSenderEntity().getClass().equals(EntityPlayerMP.class))
									{
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot run command in console. Must be a player"));
										break;
									}
									AdvBaseSecurity.instance.network.sendTo(packet, ((EntityPlayerMP) sendered));
									break;
								case "add":
									OneDoorDataPacket packet2 = new OneDoorDataPacket(AdvBaseSecurity.instance.doorHandler.getEditValidator(), AdvBaseSecurity.instance.doorHandler.addNewDoor(), false);
									if(sendered == null || !sender.getCommandSenderEntity().getClass().equals(EntityPlayerMP.class))
									{
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot run command in console. Must be a player"));
										break;
									}
									AdvBaseSecurity.instance.network.sendTo(packet2, ((EntityPlayerMP) sendered));
									break;
								case "link":
									if(sendered == null || !sender.getCommandSenderEntity().getClass().equals(EntityPlayerMP.class))
									{
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot run command in console. Must be a player"));
										break;
									}
									if(args.length <= 2){
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Please provide the door name"));
										break;
									}
									//check for item
									ItemStack heldItem = ((EntityPlayerMP)sendered).getHeldItemMainhand();
									if(heldItem.getItem() instanceof ItemLinkingCard){
										//args[2] == door name
										DoorHandler.Doors.OneDoor door1 = AdvBaseSecurity.instance.doorHandler.getDoorFromName(args[2]);
										if(door1 == null){
											sender.sendMessage(new TextComponentString(TextFormatting.RED + "Door with the name" + args[2] + "does not exist"));
											break;
										}
										//set card ID
										ItemLinkingCard.CardTag tag = new ItemLinkingCard.CardTag(heldItem);
										tag.doorId = door1.doorId;
										heldItem.setTagCompound(tag.writeToNBT(new NBTTagCompound()));
										heldItem.setStackDisplayName("Door: " + door1.doorName);
										sender.sendMessage(new TextComponentString(TextFormatting.DARK_GREEN + "Linked door of name " + door1.doorName + " and uuid of " + door1.doorId));
										break;
									}
									else{
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Hold linking card in your main hand to set ID"));
										break;
									}
								default:
									sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid Arguments"));
									break;
							}
						} else {
							sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid Arguments"));
						}
						break;
					case "groups":
						if (args.length > 1) {
							switch(args[1]){
								case "setstatus":
									if (args.length <= 3){
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Missing group name or status"));
										break;
									}
									//find group
									DoorHandler.Doors.Groups group = AdvBaseSecurity.instance.doorHandler.getDoorGroupByName(args[2]);
									if(group == null){
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid Group Name: does not exist"));
										break;
									}
									//
									break;
								default:
									sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid Arguments"));
									break;
							}
						}
					default:
						sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid Arguments"));
						break;
				}
			}
			else {
				sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid Arguments"));
			}
		}
	}
}
