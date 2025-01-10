package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.EditDoorPassGUI;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class RequestPassesPacket implements IMessage {
    UUID editValidator; //Used to try and ensure that the recieved door is authorized.
    UUID managerId;
    boolean isServer;
    List<DoorHandler.Doors.PassValue> passes;
    public RequestPassesPacket(){

    }
    public RequestPassesPacket(UUID editValidator, UUID managerId, boolean isServer){
        this.editValidator = editValidator;
        this.managerId = managerId;
        this.isServer = isServer;
    }
    private void formatGroups(){
        //use groups from doorhandler. DO NOT CHECK IF NOT SERVER
        
    }
    private void writePass(ByteBuf buf, List<DoorHandler.Doors.OneDoor.OnePass> passes){
        int count = passes.size();
        buf.writeInt(count);//size of the list
        for(int i=0; i<count; i++){
            DoorHandler.Doors.OneDoor.OnePass pass = passes.get(i);
            ByteBufUtils.writeUTF8String(buf, pass.id.toString());
            ByteBufUtils.writeUTF8String(buf, pass.passID);
            buf.writeByte(pass.passType.getInt());
            buf.writeShort(pass.priority);
            int aCount = pass.addPasses != null ? pass.addPasses.size() : 0;
            buf.writeInt(aCount);
            for(int j=0; j<aCount; j++){
                ByteBufUtils.writeUTF8String(buf, pass.addPasses.get(j).toString());
            }
            if(pass.passValueS != null) {
                buf.writeBoolean(true);
                ByteBufUtils.writeUTF8String(buf, pass.passValueS);
            }
            else {
                buf.writeBoolean(false);
                buf.writeInt(pass.passValueI);
            }
        }
    }
    private List<DoorHandler.Doors.OneDoor.OnePass> readPass(ByteBuf buf){
        List<DoorHandler.Doors.OneDoor.OnePass> passes = new LinkedList<>();
        int count = buf.readInt();
        for(int i=0; i<count; i++){
            DoorHandler.Doors.OneDoor.OnePass pass = new DoorHandler.Doors.OneDoor.OnePass();
            pass.id = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            pass.passID = ByteBufUtils.readUTF8String(buf);
            pass.passType = DoorHandler.Doors.OneDoor.OnePass.type.fromInt(buf.readByte());
            pass.priority = buf.readShort();
            int aCount = buf.readInt();
            pass.addPasses = new LinkedList<>();
            for(int j=0; j<aCount; j++){
                pass.addPasses.add(UUID.fromString(ByteBufUtils.readUTF8String(buf)));
            }
            boolean isS = buf.readBoolean();
            if(isS){
                pass.passValueS = ByteBufUtils.readUTF8String(buf);
            }
            else{
                pass.passValueI = buf.readInt();
            }
            passes.add(pass);
        }
        return passes;
    }
    public static void writeList(ByteBuf buf, UUID managerId){
        DoorHandler.Doors manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(managerId);
        if(manager != null) {
            buf.writeInt(manager.passes.size());
            BiConsumer<String, DoorHandler.Doors.PassValue> biConsumer = (k, v) -> {
                AdvBaseSecurity.instance.logger.info(v.passId + " : " + v.passName);
                ByteBufUtils.writeUTF8String(buf, v.passId);
                ByteBufUtils.writeUTF8String(buf, v.passName);
                buf.writeShort(v.passType.getInt());
                boolean isGood = v.groupNames != null && !v.groupNames.isEmpty();
                buf.writeBoolean(isGood);
                if (isGood) {
                    buf.writeInt(v.groupNames.size());
                    for (String str : v.groupNames) {
                        ByteBufUtils.writeUTF8String(buf, str);
                    }
                }
            };
            manager.passes.forEach(biConsumer);
        }
        else{
            buf.writeInt(0);
        }
    }
    public static List<DoorHandler.Doors.PassValue> readList(ByteBuf buf){
        List<DoorHandler.Doors.PassValue> passeder = new LinkedList<>();
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
            passeder.add(value);
        }
        return passeder;
    }

    @Override
    public void toBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        ByteBufUtils.writeUTF8String(buf, gson.toJson(door));
        buf.writeBoolean(editValidator != null);
        if(editValidator != null) {
            ByteBufUtils.writeUTF8String(buf, editValidator.toString());
            ByteBufUtils.writeUTF8String(buf, managerId.toString());
        }
        buf.writeBoolean(isServer);
        if(isServer){
            //add in passes
//            passes = new LinkedList<>();
//            BiConsumer<String, DoorHandler.Doors.PassValue> biConsumer = (k,v) -> {
//                passes.add(v);
//            };
//            buf.writeInt(passes.size());
//            AdvBaseSecurity.instance.doorHandler.DoorGroups.passes.forEach(biConsumer);
//            for(DoorHandler.Doors.PassValue pass : passes){
//
//            }
            writeList(buf, managerId);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        door = gson.fromJson(ByteBufUtils.readUTF8String(buf), DoorHandler.Doors.OneDoor.class);
        boolean check = buf.readBoolean();
        if(check) {
            editValidator = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            managerId = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        }
        isServer = buf.readBoolean();
        if(isServer){
            passes = readList(buf);
        }
    }

    public static class Handler implements IMessageHandler<RequestPassesPacket, IMessage> {

        @Override
        public IMessage onMessage(RequestPassesPacket message, MessageContext ctx) {
            if(ctx.side.isClient()){
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Minecraft mc = Minecraft.getMinecraft();
                    if(mc.world.isRemote) {
                        //open up the GUI
                        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                        if(screen instanceof EditDoorPassGUI){
                            ((EditDoorPassGUI) screen).finishInit(message.isServer, message.editValidator, message.passes);
                        }
                        //Minecraft.getMinecraft().displayGuiScreen(new EditDoorGUI(message.editValidator, message.door, message.groups));
                    }
                    else{
                        //AdvBaseSecurity.instance.doorHandler.recievedUpdate(message.editValidator, message.door);
                    }
                });
            }
            else{ //commented out since this should never run
            }
            return null;
        }
    }
    public static class HandlerS implements IMessageHandler<RequestPassesPacket, IMessage> { //used for server

        @Override
        public IMessage onMessage(RequestPassesPacket message, MessageContext ctx) {
            DoorHandler.Doors manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(message.managerId);
            if(manager != null/* && !manager.validator.hasPermissions("passes", message.editValidator)*/) //removed portion because there is no validator needed for it
                return new RequestPassesPacket(null, manager.id, true);
            else
                return new RequestPassesPacket(null, null, false); //invalid edit validator.
        }
    }
}
