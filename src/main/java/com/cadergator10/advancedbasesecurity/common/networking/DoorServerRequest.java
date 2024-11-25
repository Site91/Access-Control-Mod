package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.util.control.Exception;

import java.util.UUID;

public class DoorServerRequest implements IMessage { //Request a GUI from the server
    UUID editValidator;
    String request;
    String requestData;

    public DoorServerRequest(){}
    public DoorServerRequest(UUID editValidator, String request, String requestData){
        this.editValidator = editValidator;
        this.request = request;
        this.requestData = requestData;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        editValidator = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        request = ByteBufUtils.readUTF8String(buf);
        requestData = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, editValidator.toString());
        ByteBufUtils.writeUTF8String(buf, request);
        ByteBufUtils.writeUTF8String(buf, requestData);
    }

    public static class Handler implements IMessageHandler<DoorServerRequest, IMessage> {

        @Override
        public IMessage onMessage(DoorServerRequest message, MessageContext ctx) {
            //server side only so all good
            boolean canUse = AdvBaseSecurity.instance.doorHandler.checkValidator(message.editValidator); //if true, they can edit stuff
            EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
            if(message.request.equals("newdoor")){ //make new door
                if(canUse){
                    DoorHandler.Doors.OneDoor door = AdvBaseSecurity.instance.doorHandler.addNewDoor();
                    OneDoorDataPacket doorPacket = new OneDoorDataPacket(AdvBaseSecurity.instance.doorHandler.getEditValidator(), door, true);
                    AdvBaseSecurity.instance.network.sendTo(doorPacket, serverPlayer);
                }
                else
                    serverPlayer.sendMessage(new TextComponentString("You were not authorized to perform this command (invalid session id)"));
            }
            else if(message.request.equals("editdoor")){//edit door with ID.
                if(canUse) {
                    DoorHandler.Doors.OneDoor door = AdvBaseSecurity.instance.doorHandler.getDoorFromID(UUID.fromString(message.requestData));
                    if (door != null) {
                        OneDoorDataPacket packet = new OneDoorDataPacket(AdvBaseSecurity.instance.doorHandler.getEditValidator(), door, true);
                        AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
                    }
                }
                else{
                    serverPlayer.sendMessage(new TextComponentString("You were not authorized to perform this command (invalid session id)"));
                }
            }
            else if(message.request.equals("doorlist")){ //send them the door list (same as command)
                DoorNamePacket packet = new DoorNamePacket(AdvBaseSecurity.instance.doorHandler.DoorGroups, AdvBaseSecurity.instance.doorHandler.getEditValidator());
                AdvBaseSecurity.instance.network.sendTo(packet, serverPlayer);
            }
            return null;
        }
    }
}
