package com.cadergator10.advancedbasesecurity.common.commands;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.items.IDCard;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import com.cadergator10.advancedbasesecurity.common.networking.DoorNamePacket;
import com.cadergator10.advancedbasesecurity.common.networking.OneDoorDataPacket;
import com.cadergator10.advancedbasesecurity.common.networking.PassEditPacket;
import com.cadergator10.advancedbasesecurity.common.networking.UserEditPacket;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

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
								case "create":
									OneDoorDataPacket packet2 = new OneDoorDataPacket(AdvBaseSecurity.instance.doorHandler.getEditValidator(), AdvBaseSecurity.instance.doorHandler.addNewDoor(), true);
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
											sender.sendMessage(new TextComponentString(TextFormatting.RED + "Door with the name " + args[2] + " does not exist"));
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
								case "open":
									if(args.length > 3){
										DoorHandler.Doors.OneDoor door1 = AdvBaseSecurity.instance.doorHandler.getDoorFromName(args[2]);
										if(door1 == null){
											sender.sendMessage(new TextComponentString(TextFormatting.RED + "Door with the name " + args[2] + " does not exist"));
										}
										else{
											try{
												int num = Math.min(60,Math.max(1,Integer.parseInt(args[3])));
												AdvBaseSecurity.instance.doorHandler.changeDoorState(door1.doorId, true, num * 20);
											}
											catch (Exception e){
												sender.sendMessage(new TextComponentString(TextFormatting.RED + args[3] + " is not a number"));
											}
										}
									}
									else{
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /basesecurity doors open <doorName> <time>"));
									}
									break;
								case "toggle":
									if(args.length > 2){
										DoorHandler.Doors.OneDoor door1 = AdvBaseSecurity.instance.doorHandler.getDoorFromName(args[2]);
										if(door1 == null){
											sender.sendMessage(new TextComponentString(TextFormatting.RED + "Door with the name " + args[2] + " does not exist"));
										}
										else{
											AdvBaseSecurity.instance.doorHandler.changeDoorState(door1.doorId, door1.isDoorOpen == 0, 0);
										}
									}
									else{
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /basesecurity doors open <doorName> <time>"));
									}
									break;
								default:
									sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /basesecurity doors <edit/create/link/open/toggle>"));
									break;
							}
						} else {
							sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /basesecurity doors <edit/create/link/open/toggle>"));
						}
						break;
					case "groups":
						if (args.length > 1) {
							switch(args[1]){
								case "create":
									if (args.length <= 2){
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Missing group name"));
										break;
									}
									String parentName = null;
									if(args.length > 3){
										parentName = args[3];
									}
									//initialize group values
									DoorHandler.Doors.Groups group = new DoorHandler.Doors.Groups();
									group.id = UUID.randomUUID();
									group.status = DoorHandler.Doors.OneDoor.allDoorStatuses.ACCESS;
									group.name = args[2];
									group.override = null;
									group.parentID = null;
									if(parentName != null){
										//get group
										UUID parent = AdvBaseSecurity.instance.doorHandler.getDoorGroupID(parentName);
										if(parent != null)
											group.parentID = parent;
									}
									//add to list
									AdvBaseSecurity.instance.doorHandler.DoorGroups.groups.put(group.id, group);
									AdvBaseSecurity.instance.doorHandler.DoorGroups.markDirty();
									break;
								case "count":
									if (args.length <= 2){
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Missing group name"));
										break;
									}
									//count the number of doors with this group
									try {
										UUID groupID = UUID.fromString(args[2]);
										if(AdvBaseSecurity.instance.doorHandler.getDoorGroup(groupID) == null){
											sender.sendMessage(new TextComponentString(TextFormatting.RED + "Group name doesn't exist"));
											break;
										}
										List<UUID> biggroup = AdvBaseSecurity.instance.doorHandler.getDoorGroupChildren(groupID, true);
										int smallCount = 0;
										int bigCount = 0;
										for(DoorHandler.Doors.OneDoor door : AdvBaseSecurity.instance.doorHandler.DoorGroups.doors){
											if(door.groupID.equals(groupID))
												smallCount++;
											if(biggroup.contains(door.groupID))
												bigCount++;
										}
										sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "Doors in this group: " + smallCount + "\nDoors in child groups: " + bigCount + "\nDoors together: " + (bigCount + smallCount)));
										break;
									}
									catch (Exception e){
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid Arguments: " + e));
										break;
									}
								case "setstatus":
									if (args.length <= 3){
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Missing group name or status"));
										break;
									}
									//break checks
									int doorStatus;
									try{
										doorStatus = Integer.parseInt(args[3]);
										if(doorStatus != 0 && Math.abs(doorStatus) != 2){ //override ones don't work
											sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid status. Must be 0 (reg. access) 2 (all access) or -2 (no access). Overrides (1 and -1) are not supported through the command line"));
											break;
										}
									}
									catch(Exception e){
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid status int"));
										break;
									}
									//find group
									UUID groupID = AdvBaseSecurity.instance.doorHandler.getDoorGroupID(args[2]);
									if(groupID == null){
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid Group Name: does not exist"));
										break;
									}
									boolean letCascade = true;
									if(args.length > 4)
										try {
											letCascade = Boolean.parseBoolean(args[4]);
										} catch(Exception e){
											AdvBaseSecurity.instance.logger.info("Arg " + args[4] + " is not a boolean in the command BaseSecurity");
										}
									DoorHandler.Doors.Groups group2 = AdvBaseSecurity.instance.doorHandler.getDoorGroup(groupID);
									group2.status = DoorHandler.Doors.OneDoor.allDoorStatuses.fromInt(doorStatus);
									AdvBaseSecurity.instance.doorHandler.updateGroups(group2, letCascade); //auto pushes updates to doors as well, no worries! :D
									sender.sendMessage(new TextComponentString(TextFormatting.DARK_GREEN + "Successfully updated group(s) to the status: " + doorStatus));
//									//all groups //commented out since a function already exists for it
//									int letCascade = 2;
//									if(args.length > 4)
//										try {
//											letCascade = Integer.parseInt(args[4]);
//										} catch(Exception e){
//											AdvBaseSecurity.instance.logger.info("Arg " + args[4] + " is not an integer in the command BaseSecurity");
//										}
//									List<UUID> groups = new LinkedList<>();
//									groups.add(groupID);
//									if(letCascade != 0) { //0 = only selected group; 1 = direct children; 2 = all children
//										groups.addAll(AdvBaseSecurity.instance.doorHandler.getDoorGroupChildren(groupID, letCascade != 1)); //if cascade is false, it only gets direct children. if true, all children
//									}
//									//update statuses
//									for(UUID group : groups){
//										AdvBaseSecurity.instance.doorHandler.getDoorGroup(group).status = DoorHandler.Doors.OneDoor.allDoorStatuses.fromInt(doorStatus);
//									}
									break;
								default:
									sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /basesecurity doors <edit/create/link>"));
									break;
							}
						}
						else{
							sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /basesecurity groups <create/count/setstatus>"));
						}
						break;
					case "users":
						if (args.length > 1) {
							ICommandSender sendered = sender.getCommandSenderEntity();
							switch(args[1]){
								case "edit":
									UserEditPacket packet = new UserEditPacket(AdvBaseSecurity.instance.doorHandler.getEditValidator(), AdvBaseSecurity.instance.doorHandler.DoorGroups.users, true);
									if(sendered == null || !sender.getCommandSenderEntity().getClass().equals(EntityPlayerMP.class))
									{
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot run command in console. Must be a player"));
										break;
									}
									AdvBaseSecurity.instance.network.sendTo(packet, ((EntityPlayerMP) sendered));
									break;
								case "link":
									if(sendered == null || !sender.getCommandSenderEntity().getClass().equals(EntityPlayerMP.class))
									{
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot run command in console. Must be a player"));
										break;
									}
									if(args.length <= 2){
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Please provide the user's name"));
										break;
									}
									//check for item
									ItemStack heldItem = ((EntityPlayerMP)sendered).getHeldItemMainhand();
									if(heldItem.getItem() instanceof IDCard){
										//args[2] == door name
										DoorHandler.Doors.Users user = AdvBaseSecurity.instance.doorHandler.getUserByName(args[2]);
										if(user == null){
											sender.sendMessage(new TextComponentString(TextFormatting.RED + "User with the name " + args[2] + " does not exist"));
											break;
										}
										//set card ID
										IDCard.CardTag tag = new IDCard.CardTag(heldItem);
										tag.cardId = user.id;
										tag.playerId = ((EntityPlayerMP) sendered).getUniqueID();
										tag.color = 0xFFFFFF;
										heldItem.setTagCompound(tag.writeToNBT(new NBTTagCompound()));
										heldItem.setStackDisplayName(user.name);
										sender.sendMessage(new TextComponentString(TextFormatting.DARK_GREEN + "Linked user of name " + user.name + " and uuid of " + user.id));
										break;
									}
									else{
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Hold ID card in your main hand to set ID"));
										break;
									}
								default:
									sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /basesecurity users <edit/link>"));
									break;
							}
						}
						else{
							sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /basesecurity users <edit/link>"));
						}
						break;
					case "passes":
						if (args.length > 1) {
							ICommandSender sendered = sender.getCommandSenderEntity();
							switch(args[1]){
								case "edit":
									PassEditPacket packet = new PassEditPacket(AdvBaseSecurity.instance.doorHandler.getEditValidator(), AdvBaseSecurity.instance.doorHandler.DoorGroups.passes);
									if(sendered == null || !sender.getCommandSenderEntity().getClass().equals(EntityPlayerMP.class))
									{
										sender.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot run command in console. Must be a player"));
										break;
									}
									AdvBaseSecurity.instance.network.sendTo(packet, ((EntityPlayerMP) sendered));
									break;
								case "list":
									StringBuilder comb;
									if(AdvBaseSecurity.instance.doorHandler.DoorGroups.passes.isEmpty())
										comb = new StringBuilder("no passes");
									else
									{
										comb = new StringBuilder();
										BiConsumer<String, DoorHandler.Doors.PassValue> pars = (s,v) -> comb.append(String.format("(%s | %s)", v.passName, v.passId));
										AdvBaseSecurity.instance.doorHandler.DoorGroups.passes.forEach(pars);
									}
									sender.sendMessage(new TextComponentString("Passes: " + comb));
								default:
									sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /basesecurity passes <edit/list>"));
									break;
							}
						}
						else{
							sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /basesecurity passes <edit/list>"));
						}
						break;
					case "info":
						sender.sendMessage(new TextComponentString("Doors: " + AdvBaseSecurity.instance.doorHandler.DoorGroups.doors.size() + "\nPasses: " + AdvBaseSecurity.instance.doorHandler.DoorGroups.passes.size() + "\nGroups: " + AdvBaseSecurity.instance.doorHandler.DoorGroups.groups.size() + "\nUsers: " + AdvBaseSecurity.instance.doorHandler.DoorGroups.users.size() + "\nreaderList: " + AdvBaseSecurity.instance.doorHandler.allReaders.size() + "\ndoorcontrollerList: " + AdvBaseSecurity.instance.doorHandler.allDoorControllers.size() + "\ndoorsList: " + AdvBaseSecurity.instance.doorHandler.allDoors.size() + "\nDoors in World: " + AdvBaseSecurity.instance.doorHandler.IndDoors.doors.size()));
						break;
					default:
						sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /basesecurity <info/doors/groups/users/passes> ..."));
						break;
				}
			}
			else {
				sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid Arguments"));
			}
		}
	}
}
