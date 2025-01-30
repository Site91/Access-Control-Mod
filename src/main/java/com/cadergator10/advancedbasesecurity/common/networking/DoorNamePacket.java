package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.client.gui.DoorListGUI;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DoorNamePacket implements IMessage {
    public UUID managerId;
    public List<packetDoor> doors;
    public List<DoorListGUI.nameHeld> users;
    public HashMap<UUID, String> groupNames;
    public HashMap<UUID, DoorHandler.Doors.Groups> groups;
    public boolean isEdit;

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
    public DoorNamePacket(DoorHandler.Doors door, boolean isEdit){
        this.managerId = door.id;
        this.isEdit = isEdit;
        doors = new LinkedList<>();
        for(DoorHandler.Doors.OneDoor doore : door.doors){
            doors.add(new packetDoor(doore.doorId, doore.doorName, doore.doorStatus.getInt(), doore.Readers.size(), doore.Doors.size(), doore.groupID));
        }
        groups = door.groups;
        users = new LinkedList<>();
    }
    public DoorNamePacket(DoorHandler.Doors door, boolean isEdit, List<UUID> users){
        this(door, isEdit);
        for(UUID id : users){
            this.users.add(new DoorListGUI.nameHeld(id, FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(id).getName()));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        ByteBufUtils.writeUTF8String(buf, gson.toJson(door));
        ByteBufUtils.writeUTF8String(buf, managerId.toString());
        buf.writeBoolean(isEdit);
        buf.writeInt(doors.size());
        List<UUID> tempList = new LinkedList<>();
        for(packetDoor door : doors){
            ByteBufUtils.writeUTF8String(buf, door.id.toString());
            ByteBufUtils.writeUTF8String(buf, door.name);
            buf.writeInt(door.status);
            buf.writeInt(door.readerCount);
            buf.writeInt(door.doorCount);
            boolean hasGroup = door.groupID != null && groups.containsKey(door.groupID) && !tempList.contains(door.groupID);
            buf.writeBoolean(hasGroup);
            if(hasGroup){
                tempList.add(door.groupID);
                ByteBufUtils.writeUTF8String(buf, door.groupID.toString());
            }
        }
        buf.writeInt(tempList.size());
        for(UUID id : tempList){
            ByteBufUtils.writeUTF8String(buf, groups.get(id).id.toString());
            ByteBufUtils.writeUTF8String(buf, groups.get(id).name);
        }
        buf.writeInt(users.size());
        for(DoorListGUI.nameHeld nme : users){
            ByteBufUtils.writeUTF8String(buf, nme.id.toString());
            ByteBufUtils.writeUTF8String(buf, nme.name);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        door = gson.fromJson(ByteBufUtils.readUTF8String(buf), DoorHandler.Doors.OneDoor.class);
        managerId = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        isEdit = buf.readBoolean();
        int size = buf.readInt();
        doors = new LinkedList<>();
        for(int i=0; i<size; i++){
            doors.add(new packetDoor(UUID.fromString(ByteBufUtils.readUTF8String(buf)), ByteBufUtils.readUTF8String(buf), buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean() ? UUID.fromString(ByteBufUtils.readUTF8String(buf)) : null));
        }
        size = buf.readInt();
        groupNames = new HashMap<>();
        for(int i=0; i<size; i++){
            groupNames.put(UUID.fromString(ByteBufUtils.readUTF8String(buf)), ByteBufUtils.readUTF8String(buf));
        }
        size = buf.readInt();
        users = new LinkedList<>();
        for(int i=0; i<size; i++){
            users.add(new DoorListGUI.nameHeld(UUID.fromString(ByteBufUtils.readUTF8String(buf)), ByteBufUtils.readUTF8String(buf)));
        }
    }
}
