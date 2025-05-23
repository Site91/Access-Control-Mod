package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.config.DoorConfig;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.inventory.doorManagerContainer;
import com.cadergator10.advancedbasesecurity.common.items.ItemDoorManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

/**
 * Most requests done from the client to the server. Does various things and often requires validators, managerId, etc.
 */
public class DoorServerRequest implements IMessage { //Request a GUI from the server
    UUID editValidator; //A check to ensure the user actually can change things.
    UUID managerId; //The ID of the doormanager itself.
    String validatorID; //The code the editValidator is linked to. Not usually used most of the time.
    String request; //The actual request itself.
    String requestData; //Any data passed along with the request.

    public DoorServerRequest(){}
    public DoorServerRequest(UUID editValidator, String validatorID, UUID managerId, String request, String requestData){
        this.editValidator = editValidator;
        this.validatorID = validatorID;
        this.managerId = managerId;
        this.request = request;
        this.requestData = requestData;
    }
    public DoorServerRequest(UUID managerId, String request, String requestData){
        this.editValidator = null;
        this.validatorID = null;
        this.managerId = managerId;
        this.request = request;
        this.requestData = requestData;
    }
    public DoorServerRequest(String request, String requestData){
        this.editValidator = null;
        this.validatorID = null;
        this.managerId = null;
        this.request = request;
        this.requestData = requestData;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        boolean canMore = buf.readBoolean();
        if(canMore){
            editValidator = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            validatorID = ByteBufUtils.readUTF8String(buf);
        }
        canMore = buf.readBoolean();
        if(canMore)
            managerId = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        request = ByteBufUtils.readUTF8String(buf);
        requestData = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(editValidator != null);
        if(editValidator != null){
            ByteBufUtils.writeUTF8String(buf, editValidator.toString());
            ByteBufUtils.writeUTF8String(buf, validatorID);
        }
        buf.writeBoolean(managerId != null);
        if(managerId != null)
            ByteBufUtils.writeUTF8String(buf, managerId.toString());
        ByteBufUtils.writeUTF8String(buf, request);
        ByteBufUtils.writeUTF8String(buf, requestData);
    }

    public static class Handler implements IMessageHandler<DoorServerRequest, IMessage> {

        @Override
        public IMessage onMessage(DoorServerRequest message, MessageContext ctx) {
            //server side only so all good
            boolean canUse = false; //if true, they can edit stuff. Only useful if ALL the vars are filled, which is barely any (most do their own check)
            DoorHandler.Doors manager = null;
            if(message.managerId != null){
                manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(message.managerId); //auto grabs the manager for use.
                if(manager != null) {
                    if(message.editValidator != null && message.validatorID != null) //phase out. validatorID can be abused by a rogue client
                        canUse = manager.validator.hasPermissions(message.validatorID, message.editValidator);
                }
            }
            EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
            AdvBaseSecurity.LogDebug("User " + serverPlayer.getName() + " and ID " + serverPlayer.getUniqueID() + " sent ServerRequest: request:" + message.request + " | requestData: " + message.requestData);
            if(message.request.equals("newdoor")){ //Make a new door
                if(manager != null && manager.hasPerms(serverPlayer)){ //Check if user has perms to do it. Either creator or given permission by creator. Not duplicating comment further down.
                    DoorHandler.Doors.OneDoor door = manager.addNewDoor();
                    UUID newid = UUID.randomUUID();
                    boolean added = manager.validator.addPermissions("door:" + door.doorId.toString(), newid, true); //Get editValidator to send to client. Not duplicating comment further down.
                    if(added) {
                        OneDoorDataPacket doorPacket = new OneDoorDataPacket(newid, manager.id, door, true);
                        AdvBaseSecurity.instance.network.sendTo(doorPacket, serverPlayer);
                    }
                }
                else
                    serverPlayer.sendMessage(new TextComponentString("You were not authorized to perform this command (invalid session id)"));
            }
            else if(message.request.equals("editdoor")){//edit door with ID.
                if(manager != null && manager.hasPerms(serverPlayer)) {
                    DoorHandler.Doors.OneDoor door = manager.getDoorFromID(UUID.fromString(message.requestData));
                    if (door != null) {
                        UUID newid = UUID.randomUUID();
                        boolean added = manager.validator.addPermissions("door:" + door.doorId.toString(), newid, false);
                        if(added) {
                            OneDoorDataPacket packet = new OneDoorDataPacket(newid, manager.id, door, true);
                            AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                        }
                        else
                            serverPlayer.sendMessage(new TextComponentString("You were not authorized to perform this command (session in progress)"));
                    }
                }
                else{
                    serverPlayer.sendMessage(new TextComponentString("You were not authorized to perform this command (invalid session id)"));
                }
            }
            else if(message.request.equals("doorlist")){ //send them the door list (same as command)
                if(manager != null && manager.hasPerms(serverPlayer)) {
                    DoorNamePacket packet = new DoorNamePacket(manager, true, manager.allowedPlayers);
                    AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                }
            }
            else if(message.request.equals("linkdoormanager")){ //change manager ID of doormanager and open new gui
                ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> { //makes sure this runs on the tick. Not duplicating comment further down
                    ItemStack item = ctx.getServerHandler().player.getHeldItemMainhand();
                    if(item.getItem() instanceof ItemDoorManager){
                        DoorHandler.Doors door = AdvBaseSecurity.instance.doorHandler.getDoorManager(UUID.fromString(message.requestData));
                        if(door.hasPerms(ctx.getServerHandler().player)) {
                            ItemDoorManager.ManagerTag tag = new ItemDoorManager.ManagerTag(item);
                            tag.managerID = door.id;
                            item.setTagCompound(tag.writeToNBT(new NBTTagCompound()));
                            item.setStackDisplayName(new TextComponentTranslation(ItemDoorManager.getItemName() + ".name").getUnformattedText() + " (" + door.name + ")");
                            DoorNamePacket packet = new DoorNamePacket(door, true);
                            AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                        }
                    }
                });
                return null;
            }
            else if(message.request.equals("newmanager")){ //create another manager and link it
                ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                    ItemStack item = ctx.getServerHandler().player.getHeldItemMainhand();
                    if(item.getItem() instanceof ItemDoorManager){
                        //check max door managers
                        if(AdvBaseSecurity.instance.doorHandler.getManagerCount(serverPlayer) >= DoorConfig.managerLimit){
                            serverPlayer.sendMessage(new TextComponentString("Too many managers under name! Max of " + DoorConfig.managerLimit));
                            return;
                        }
                        UUID newid = UUID.randomUUID();
                        DoorHandler.Doors door = AdvBaseSecurity.instance.doorHandler.addDoorManager(ctx.getServerHandler().player, message.requestData);
                        //their door, so they already got perms
                        ItemDoorManager.ManagerTag tag = new ItemDoorManager.ManagerTag(item);
                        tag.managerID = door.id;
                        item.setTagCompound(tag.writeToNBT(new NBTTagCompound()));
                        item.setStackDisplayName(new TextComponentTranslation(ItemDoorManager.getItemName() + ".name").getUnformattedText() + " (" + door.name + ")");
                        DoorNamePacket packet = new DoorNamePacket(door, true);
                        AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                    }
                });
                return null;
            }
            else if(message.request.equals("removeperm")){ //Remove the permission of the manager if they have access.
                if(manager != null && canUse){
                    manager.validator.removePerm(message.validatorID);
                }
            }
            else if(message.request.equals("openusermenu")){ //Opens the GUI on the clint for editing users.
                ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                    ctx.getServerHandler().player.openGui(AdvBaseSecurity.instance, 1, ctx.getServerHandler().player.world, 0, 0, 0);
                });
            }
            else if(message.request.equals("getuserdata")){ //Get all the users in a doorManager.
                if(manager != null){
                    UUID eV = UUID.randomUUID(); //edit validator ID
                    boolean worked = manager.validator.addPermissions("useredit", eV, false);
                    if(worked) {
                        UserEditPacket packet = new UserEditPacket(true, eV, manager.users, true, manager.id);
                        AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                    }
                    else{
                        UserEditPacket packet = new UserEditPacket(false, null, null, true, manager.id);
                        AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                    }
                }
            }
            else if(message.request.equals("managerdoorlink")){ //set tag of doormanager so that doors scanned with it will update their doors.
                if(manager != null && manager.hasPerms(serverPlayer) && message.requestData != null){
                    DoorHandler.Doors.OneDoor door = manager.getDoorFromID(UUID.fromString(message.requestData));
                    if(door == null)
                        return null;
                    String name = manager.name;
                    ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                        ItemStack item = ctx.getServerHandler().player.getHeldItemMainhand();
                        if(item.getItem() instanceof ItemDoorManager){
                            ItemDoorManager.ManagerTag tag = new ItemDoorManager.ManagerTag(item);
                            tag.doorIDScan = UUID.fromString(message.requestData);
                            tag.currentScanMode = 1;
                            item.setTagCompound(tag.writeToNBT(new NBTTagCompound()));
                            item.setStackDisplayName(new TextComponentTranslation(ItemDoorManager.getItemName() + ".name").getFormattedText() + " (" + name + ") §9LINKING DOOR " + door.doorName);
                            serverPlayer.sendMessage(new TextComponentString("Successfully set door manager linking to door with ID of " + message.requestData + " and name " + door.doorName));
                        }
                    });
                }
            }
            else if(message.request.equals("openpassmenu")){ //Open the Pass Editing GUI.
                UUID id = UUID.randomUUID();
                boolean worked = manager.validator.addPermissions("passes", id, false);
                if(worked) {
                    PassEditPacket packet = new PassEditPacket(id, message.managerId, manager.passes);
                    AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                }
                else
                    serverPlayer.sendMessage(new TextComponentString("You were not authorized to perform this command (session in progress)"));
            }
            else if(message.request.equals("opensectormenu")){ //Open the Sector Editing GUI.
                UUID id = UUID.randomUUID();
                boolean worked = manager.validator.addPermissions("sectors", id, false);
                if(worked) {
                    SectorEditPacket packet = new SectorEditPacket(id, message.managerId, manager.groups);
                    AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                }
                else
                    serverPlayer.sendMessage(new TextComponentString("You were not authorized to perform this command (session in progress)"));
            }
            else if(message.request.equals("producecard")) { //when the create card button on doormanager is pressed.
                if(canUse && manager.hasPerms(serverPlayer) && message.requestData != null){ //manager always not null if canUse
                    try {
                        DoorHandler.Doors.Users user = manager.getUser(UUID.fromString(message.requestData));
                        if (user != null) {
                            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                                ItemStack item = ctx.getServerHandler().player.getHeldItemMainhand();
                                if (item.getItem() instanceof ItemDoorManager) {
                                    Container cont = serverPlayer.openContainer;
                                    if(cont instanceof doorManagerContainer) {
                                        boolean work = ((doorManagerContainer)cont).writeCard(serverPlayer, new DoorHandler.DoorIdentifier(message.managerId, user.id), user.name);
                                        AdvBaseSecurity.instance.logger.info("User " + serverPlayer.getName() + " wrote a card with ID " + user.id + " and name " + user.name + ". Success: " + work);
                                    }
                                }
                            });
                        }
                    }
                    catch(Exception ignored){

                    }
                }
            }
            else if(message.request.equals("modeButtonHit")){ //If the mode button was clicked on the DoorManager, this will reset linking if it was currently doing so.
                if(manager != null && manager.hasPerms(serverPlayer) && message.requestData != null){ //manager always not null if canUse
                    String name = manager.name;
                    ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                        ItemStack item = ctx.getServerHandler().player.getHeldItemMainhand();
                        if (item.getItem() instanceof ItemDoorManager) {
                            ItemDoorManager.ManagerTag tag = new ItemDoorManager.ManagerTag(item);
                            if(message.requestData.equals("true") && tag.currentScanMode == 1) { //reset linking
                                tag.currentScanMode = 0;
                                tag.doorIDScan = null;
                                item.setTagCompound(tag.writeToNBT(new NBTTagCompound()));
                                item.setStackDisplayName(new TextComponentTranslation(ItemDoorManager.getItemName() + ".name").getUnformattedText() + " (" + name + ")");
                            }
                        }
                    });
                }
            }
            else if(message.request.equals("removemanagerplayer") && message.requestData != null){ //Remove an authorized user from a doorManager
                if(manager != null && manager.creator.equals(serverPlayer.getUniqueID())){
                    UUID id = UUID.fromString(message.requestData);
                    for(int i=0; i<manager.allowedPlayers.size(); i++){
                        if(manager.allowedPlayers.get(i).equals(id)){
                            manager.allowedPlayers.remove(i);
                            manager.markDirty();
                            return null;
                        }
                    }
                }
            }
            else if(message.request.equals("addmanagerplayer") && message.requestData != null){ //Add an authorized user from a doorManager
                if(manager != null && manager.creator.equals(serverPlayer.getUniqueID())){
                    EntityPlayerMP play = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(message.requestData);
                    if(play == null)
                        return null;
                    UUID id = play.getUniqueID();
                    for(int i=0; i<manager.allowedPlayers.size(); i++){
                        if(manager.allowedPlayers.get(i).equals(id)){
                            return null;
                        }
                    }
                    manager.allowedPlayers.add(id);
                    manager.markDirty();
                }
            }
            return null;
        }
    }
}
