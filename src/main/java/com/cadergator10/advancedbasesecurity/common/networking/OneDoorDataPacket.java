package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.EditDoorGUI;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class OneDoorDataPacket implements IMessage {
    UUID editValidator; //Used to try and ensure that the recieved door is authorized.
    UUID managerID;
    DoorHandler.Doors.OneDoor door;
    boolean checkGroup;
    List<ButtonEnum.groupIndex> groups;
    public OneDoorDataPacket(){

    }
    public OneDoorDataPacket(UUID editValidator, UUID managerID, DoorHandler.Doors.OneDoor door, boolean checkGroup){
        this.editValidator = editValidator;
        this.managerID = managerID;
        this.door = door;
        this.checkGroup = checkGroup;
    }
    private void formatGroups(){
        //use groups from doorhandler. DO NOT CHECK IF NOT SERVER
        groups = new LinkedList<>();
        BiConsumer<UUID, DoorHandler.Doors.Groups> grouper = (s,v) -> groups.add(new ButtonEnum.groupIndex(s.toString(), v.name));
        AdvBaseSecurity.instance.doorHandler.getDoorManager(managerID).groups.forEach(grouper);
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
    private void writeList(ByteBuf buf, List<UUID> passes){
        int count = passes.size();
        buf.writeInt(count);//size of the list
        for(int i=0; i<count; i++){
            ByteBufUtils.writeUTF8String(buf, passes.get(i).toString());
        }
    }
    private List<UUID> readList(ByteBuf buf){
        List<UUID> ids = new LinkedList<>();
        int count = buf.readInt();
        for(int i=0; i<count; i++){
            ids.add(UUID.fromString(ByteBufUtils.readUTF8String(buf)));
        }
        return ids;
    }

    @Override
    public void toBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        ByteBufUtils.writeUTF8String(buf, gson.toJson(door));
        ByteBufUtils.writeUTF8String(buf, editValidator.toString());
        ByteBufUtils.writeUTF8String(buf, managerID.toString());
        ByteBufUtils.writeUTF8String(buf, door.doorId.toString());
        ByteBufUtils.writeUTF8String(buf, door.doorName);
        buf.writeByte(door.doorStatus.getInt());
        writePass(buf, door.passes);
        writePass(buf, door.override);
        ByteBufUtils.writeUTF8String(buf, door.groupID != null ? door.groupID.toString() : "NUL");
        buf.writeInt(door.isDoorOpen);
        buf.writeInt(door.currTick);
        buf.writeBoolean(door.defaultToggle);
        buf.writeInt(door.defaultTick);
        writeList(buf, door.Doors);
        writeList(buf, door.Readers);
        ByteBufUtils.writeUTF8String(buf, door.readerLabel);
        buf.writeByte(door.readerLabelColor);
        buf.writeInt(door.readerLights);
        //do groups
//        if(group != null){
//            buf.writeBoolean(true);
//            ByteBufUtils.writeUTF8String(buf, group);
//        }
//        else
//            buf.writeBoolean(false);
        buf.writeBoolean(checkGroup);
        if (checkGroup) {
            formatGroups();
            int aCount = groups.size();
            buf.writeInt(aCount);
            for (int i = 0; i < aCount; i++) {
                ByteBufUtils.writeUTF8String(buf, groups.get(i).id.toString());
                ByteBufUtils.writeUTF8String(buf, groups.get(i).name);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        door = gson.fromJson(ByteBufUtils.readUTF8String(buf), DoorHandler.Doors.OneDoor.class);
        editValidator = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        managerID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        door = new DoorHandler.Doors.OneDoor();
        door.doorId = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        door.doorName = ByteBufUtils.readUTF8String(buf);
        door.doorStatus = DoorHandler.Doors.OneDoor.allDoorStatuses.fromInt(buf.readByte());
        door.passes = readPass(buf);
        door.override = readPass(buf);
        String tempGroup = ByteBufUtils.readUTF8String(buf);
        if(!tempGroup.equals("NUL"))
            door.groupID = UUID.fromString(tempGroup);
        door.isDoorOpen = buf.readInt();
        door.currTick = buf.readInt();
        door.defaultToggle = buf.readBoolean();
        door.defaultTick = buf.readInt();
        door.Doors = readList(buf);
        door.Readers = readList(buf);
        door.readerLabel = ByteBufUtils.readUTF8String(buf);
        door.readerLabelColor = buf.readByte();
        door.readerLights = buf.readInt();
//        boolean hasGroup = buf.readBoolean();
//        if(hasGroup)
//            group = ByteBufUtils.readUTF8String(buf);
        checkGroup = buf.readBoolean();
        if(checkGroup) {
            groups = new LinkedList<>();
            int aCount = buf.readInt();
            for (int i = 0; i < aCount; i++) {
                groups.add(new ButtonEnum.groupIndex(ByteBufUtils.readUTF8String(buf), ByteBufUtils.readUTF8String(buf)));
            }
        }
    }

    public static class Handler implements IMessageHandler<OneDoorDataPacket, IMessage> {

        @Override
        public IMessage onMessage(OneDoorDataPacket message, MessageContext ctx) {
            if(ctx.side.isClient()){
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Minecraft mc = Minecraft.getMinecraft();
                    if(mc.world.isRemote) {
                        //open up the GUI
                        Minecraft.getMinecraft().displayGuiScreen(new EditDoorGUI(message.editValidator, message.managerID, message.door, message.groups));
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
    public static class HandlerS implements IMessageHandler<OneDoorDataPacket, IMessage> { //used for server

        @Override
        public IMessage onMessage(OneDoorDataPacket message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                DoorHandler.Doors manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(message.managerID);
                manager.recievedUpdate(message.editValidator, message.door);
                DoorNamePacket packet = new DoorNamePacket(manager, true);
                AdvBaseSecurity.instance.network.sendTo(packet, ctx.getServerHandler().player);
            });
            return null;
        }
    }
}
