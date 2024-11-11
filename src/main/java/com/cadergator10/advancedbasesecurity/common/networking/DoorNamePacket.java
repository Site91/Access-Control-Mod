package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.client.gui.DoorListGUI;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.util.control.Exception;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DoorNamePacket implements IMessage {
    public UUID editValidator;
    public List<packetDoor> doors;
    public HashMap<UUID, String> groupNames;
    public HashMap<UUID, DoorHandler.Doors.Groups> groups;

    public static class packetDoor{
        public UUID id;
        public String name;
        public int status;
        public int readerCount;
        public int doorCount;
        public UUID groupID;
        public packetDoor(){

        }
        public packetDoor(UUID id, String name, int status, int readerCount, int doorCount, UUID groupID) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.readerCount = readerCount;
            this.doorCount = doorCount;
            this.groupID = groupID;
        }
    }

    public DoorNamePacket(){

    }
    public DoorNamePacket(DoorHandler.Doors door, UUID editValidator){
        this.editValidator = editValidator;
        doors = new LinkedList<>();
        for(DoorHandler.Doors.OneDoor doore : door.doors){
            doors.add(new packetDoor(doore.doorId, doore.doorName, doore.doorStatus.getInt(), doore.Readers.size(), doore.Doors.size(), doore.groupID));
        }
        groups = door.groups;
    }

    @Override
    public void toBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        ByteBufUtils.writeUTF8String(buf, gson.toJson(door));
        ByteBufUtils.writeUTF8String(buf, editValidator.toString());
        buf.writeInt(doors.size());
        List<UUID> tempList = new LinkedList<>();
        for(packetDoor door : doors){
            ByteBufUtils.writeUTF8String(buf, door.id.toString());
            ByteBufUtils.writeUTF8String(buf, door.name);
            buf.writeInt(door.status);
            buf.writeInt(door.readerCount);
            buf.writeInt(door.doorCount);
            if(door.groupID != null && groups.containsKey(door.groupID) && !tempList.contains(door.groupID)){
                tempList.add(door.groupID);
            }
        }
        buf.writeInt(tempList.size());
        for(UUID id : tempList){
            ByteBufUtils.writeUTF8String(buf, groups.get(id).id.toString());
            ByteBufUtils.writeUTF8String(buf, groups.get(id).name);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        door = gson.fromJson(ByteBufUtils.readUTF8String(buf), DoorHandler.Doors.OneDoor.class);
        editValidator = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        int size = buf.readInt();
        doors = new LinkedList<>();
        for(int i=0; i<size; i++){
            doors.add(new packetDoor(UUID.fromString(ByteBufUtils.readUTF8String(buf)), ByteBufUtils.readUTF8String(buf), buf.readInt(), buf.readInt(), buf.readInt(), UUID.fromString(ByteBufUtils.readUTF8String(buf))));
        }
        size = buf.readInt();
        groupNames = new HashMap<>();
        for(int i=0; i<size; i++){
            groupNames.put(UUID.fromString(ByteBufUtils.readUTF8String(buf)), ByteBufUtils.readUTF8String(buf));
        }
    }
}
