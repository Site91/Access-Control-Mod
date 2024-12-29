package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.items.ItemDoorManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class DoorServerRequest implements IMessage { //Request a GUI from the server
    UUID editValidator;
    UUID managerId;
    String validatorID;
    String request;
    String requestData;

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
            boolean canUse = false; //if true, they can edit stuff
            DoorHandler.Doors manager = null;
            if(message.managerId != null){
                manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(message.managerId);
                if(manager != null) {
                    if(message.editValidator != null && message.validatorID != null)
                        canUse = manager.validator.hasPermissions(message.validatorID, message.editValidator);
                }
            }
            EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
            if(message.request.equals("newdoor")){ //make new door
                if(canUse){
                    DoorHandler.Doors.OneDoor door = manager.addNewDoor();
                    UUID newid = UUID.randomUUID();
                    boolean added = manager.validator.addPermissions("door:" + door.doorId.toString(), newid, true);
                    if(added) {
                        OneDoorDataPacket doorPacket = new OneDoorDataPacket(newid, manager.id, door, true);
                        AdvBaseSecurity.instance.network.sendTo(doorPacket, serverPlayer);
                    }
                }
                else
                    serverPlayer.sendMessage(new TextComponentString("You were not authorized to perform this command (invalid session id)"));
            }
            else if(message.request.equals("editdoor")){//edit door with ID.
                if(canUse) {
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
                if(manager != null) {
                    DoorNamePacket packet = new DoorNamePacket(manager);
                    AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                }
            }
            else if(message.request.equals("linkdoormanager")){ //change manager ID of doormanager and open new gui
                ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                    ItemStack item = ctx.getServerHandler().player.getHeldItemMainhand();
                    if(item.getItem() instanceof ItemDoorManager){
                        DoorHandler.Doors door = AdvBaseSecurity.instance.doorHandler.getDoorManager(UUID.fromString(message.requestData));
                        if(door.hasPerms(ctx.getServerHandler().player)) {
                            ItemDoorManager.ManagerTag tag = new ItemDoorManager.ManagerTag(item);
                            tag.managerID = door.id;
                            item.setTagCompound(tag.writeToNBT(new NBTTagCompound()));
                            item.setStackDisplayName(ItemDoorManager.getItemName() + " (" + door.name + ")");
                            DoorNamePacket packet = new DoorNamePacket(door);
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
                        if(AdvBaseSecurity.instance.doorHandler.getManagerCount(serverPlayer) >= 1){
                            serverPlayer.sendMessage(new TextComponentString("Too many managers under name! Max of 1"));
                            return;
                        }
                        UUID newid = UUID.randomUUID();
                        DoorHandler.Doors door = AdvBaseSecurity.instance.doorHandler.addDoorManager(ctx.getServerHandler().player, message.requestData);
                        //their door, so they already got perms
                        ItemDoorManager.ManagerTag tag = new ItemDoorManager.ManagerTag(item);
                        tag.managerID = door.id;
                        item.setTagCompound(tag.writeToNBT(new NBTTagCompound()));
                        item.setStackDisplayName(ItemDoorManager.getItemName() + " (" + door.name + ")");
                        DoorNamePacket packet = new DoorNamePacket(door);
                        AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                    }
                });
                return null;
            }
            else if(message.request.equals("removeperm")){
                if(manager != null && canUse){
                    manager.validator.removePerm(message.validatorID);
                }
            }
            else if(message.request.equals("openusermenu")){
                ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                    ctx.getServerHandler().player.openGui(AdvBaseSecurity.instance, 1, ctx.getServerHandler().player.world, 0, 0, 0);
                });
            }
            else if(message.request.equals("getuserdata")){
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
                if(manager != null && manager.hasPerms(serverPlayer) && message.requestData != null && manager.getDoorFromID(UUID.fromString(message.requestData)) != null){
                    ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                        ItemStack item = ctx.getServerHandler().player.getHeldItemMainhand();
                        if(item.getItem() instanceof ItemDoorManager){
                            ItemDoorManager.ManagerTag tag = new ItemDoorManager.ManagerTag(item);
                            tag.doorIDScan = UUID.fromString(message.requestData);
                            tag.currentScanMode = 1;
                            item.setTagCompound(tag.writeToNBT(new NBTTagCompound()));
                            serverPlayer.sendMessage(new TextComponentString("Successfully set door manager linking to door with ID of " + message.requestData));
                        }
                    });
                }
            }
            else if(message.request.equals("openpassmenu")){
                UUID id = UUID.randomUUID();
                boolean worked = manager.validator.addPermissions("passes", id, false);
                if(worked) {
                    PassEditPacket packet = new PassEditPacket(id, message.managerId, manager.passes);
                    AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                }
                else
                    serverPlayer.sendMessage(new TextComponentString("You were not authorized to perform this command (session in progress)"));
            }
            return null;
        }
    }
}
