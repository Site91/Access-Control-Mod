package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.EditPassGUI;
import com.cadergator10.advancedbasesecurity.client.gui.EditUserGUI;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class PassEditPacket implements IMessage {
    UUID editValidator; //Used to try and ensure that the recieved door is authorized.
    HashMap<String, DoorHandler.Doors.PassValue> passes;
    public PassEditPacket(){

    }
    public PassEditPacket(UUID editValidator, HashMap<String, DoorHandler.Doors.PassValue> passes){
        this.editValidator = editValidator;
        this.passes = passes;
    }

    public static void writeList(ByteBuf buf, HashMap<String, DoorHandler.Doors.PassValue> passes){
        buf.writeInt(passes.size());
        BiConsumer<String, DoorHandler.Doors.PassValue> biConsumer = (k,v) -> {
            ByteBufUtils.writeUTF8String(buf, v.passId);
            ByteBufUtils.writeUTF8String(buf, v.passName);
            buf.writeShort(v.passType.getInt());
            boolean isGood = v.groupNames != null && !v.groupNames.isEmpty();
            buf.writeBoolean(isGood);
            if(isGood){
                buf.writeInt(v.groupNames.size());
                for(String str : v.groupNames){
                    ByteBufUtils.writeUTF8String(buf, str);
                }
            }
        };
        passes.forEach(biConsumer);
    }
    public static HashMap<String, DoorHandler.Doors.PassValue> readList(ByteBuf buf){
        HashMap<String, DoorHandler.Doors.PassValue> passeder = new HashMap<>();
        int num = buf.readInt();
        for(int i=0; i<num; i++){
            DoorHandler.Doors.PassValue value = new DoorHandler.Doors.PassValue();
            value.passId = ByteBufUtils.readUTF8String(buf);
            value.passName = ByteBufUtils.readUTF8String(buf);
            value.passType = DoorHandler.Doors.PassValue.type.fromInt(buf.readShort());
            boolean isGood = buf.readBoolean();
            if(isGood){
                value.groupNames = new LinkedList<>();
                int count = buf.readInt();
                for(int j=0; j<count; j++){
                    value.groupNames.add(ByteBufUtils.readUTF8String(buf));
                }
            }
            passeder.put(value.passId, value);
        }
        return passeder;
    }

    @Override
    public void toBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        ByteBufUtils.writeUTF8String(buf, gson.toJson(door));
        ByteBufUtils.writeUTF8String(buf, editValidator.toString());
        writeList(buf, passes);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        door = gson.fromJson(ByteBufUtils.readUTF8String(buf), DoorHandler.Doors.OneDoor.class);
        editValidator = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        passes = readList(buf);
    }

    public static class Handler implements IMessageHandler<PassEditPacket, IMessage> {

        @Override
        public IMessage onMessage(PassEditPacket message, MessageContext ctx) {
            if(ctx.side.isClient()){
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Minecraft mc = Minecraft.getMinecraft();
                    if(mc.world.isRemote) {
                        //open up the GUI
                        Minecraft.getMinecraft().displayGuiScreen(new EditPassGUI(message.editValidator, message.passes));
                    }
                    else{
                        //AdvBaseSecurity.instance.doorHandler.recievedUpdate(message.editValidator, message.door);
                    }
                });
            }
            else{ //commented out since this should never run
//                ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
//                    AdvBaseSecurity.instance.doorHandler.recievedUpdate(message.editValidator, message.door);
//                    DoorNamePacket packet = new DoorNamePacket(AdvBaseSecurity.instance.doorHandler.DoorGroups, AdvBaseSecurity.instance.doorHandler.getEditValidator());
//                    AdvBaseSecurity.instance.network.sendTo(packet, ctx.getServerHandler().player);
//                });
            }
            return null;
        }
    }
    public static class HandlerS implements IMessageHandler<PassEditPacket, IMessage> { //used for server

        @Override
        public IMessage onMessage(PassEditPacket message, MessageContext ctx) {
            if(AdvBaseSecurity.instance.doorHandler.checkValidator(message.editValidator)) {
                AdvBaseSecurity.instance.doorHandler.DoorGroups.passes = message.passes;
                AdvBaseSecurity.instance.doorHandler.verifyUserPasses();
            }
            return null;
        }
    }
}
