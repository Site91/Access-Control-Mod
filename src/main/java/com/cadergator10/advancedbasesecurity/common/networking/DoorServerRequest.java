package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.items.ItemDoorManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
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
            managerId = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        }
        request = ByteBufUtils.readUTF8String(buf);
        requestData = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(editValidator == null);
        if(editValidator != null){
            ByteBufUtils.writeUTF8String(buf, editValidator.toString());
            ByteBufUtils.writeUTF8String(buf, validatorID);
            ByteBufUtils.writeUTF8String(buf, managerId.toString());
        }
        ByteBufUtils.writeUTF8String(buf, request);
        ByteBufUtils.writeUTF8String(buf, requestData);
    }

    public static class Handler implements IMessageHandler<DoorServerRequest, IMessage> {

        @Override
        public IMessage onMessage(DoorServerRequest message, MessageContext ctx) {
            //server side only so all good
            boolean canUse = false; //if true, they can edit stuff
            DoorHandler.Doors manager = null;
            if(message.editValidator != null){
                manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(message.managerId);
                if(manager != null)
                    canUse = manager.validator.hasPermissions(message.validatorID, message.editValidator);
            }
            EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
            if(message.request.equals("newdoor")){ //make new door
                if(canUse){
                    DoorHandler.Doors.OneDoor door = manager.addNewDoor();
                    UUID newid = UUID.randomUUID();
                    boolean added = manager.validator.addPermissions("door:" + door.doorId.toString(), newid, true);
                    if(added) {
                        OneDoorDataPacket doorPacket = new OneDoorDataPacket(newid, door, true);
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
                            OneDoorDataPacket packet = new OneDoorDataPacket(newid, door, true);
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
                            item.setTagCompound(tag.writeToNBT(item.getTagCompound()));
                            item.setStackDisplayName(ItemDoorManager.getItemName() + " (" + door.name + ")");
                            DoorNamePacket packet = new DoorNamePacket(door);
                            AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                        }
                    }
                });
                return null;
            }
            return null;
        }
    }
}
