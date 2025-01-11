package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.EditPassGUI;
import com.cadergator10.advancedbasesecurity.client.gui.EditSectorGUI;
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

public class SectorEditPacket implements IMessage {
    UUID editValidator; //Used to try and ensure that the recieved door is authorized.
    UUID managerId;
    HashMap<UUID, DoorHandler.Doors.Groups> sectors;
    public SectorEditPacket(){

    }
    public SectorEditPacket(UUID editValidator, UUID managerId, HashMap<UUID, DoorHandler.Doors.Groups> sectors){
        this.managerId = managerId;
        this.editValidator = editValidator;
        this.sectors = sectors;
    }

    public static void writeList(ByteBuf buf, HashMap<UUID, DoorHandler.Doors.Groups> sectors){
        buf.writeInt(sectors.size());
        BiConsumer<UUID, DoorHandler.Doors.Groups> biConsumer = (k,v) -> {
            ByteBufUtils.writeUTF8String(buf, v.id.toString());
            ByteBufUtils.writeUTF8String(buf, v.name.toString());
            buf.writeShort(v.status.getInt());
            boolean isGood = v.parentID != null;
            buf.writeBoolean(isGood);
            if(isGood){
                ByteBufUtils.writeUTF8String(buf, v.parentID.toString());
            }
        };
        sectors.forEach(biConsumer);
    }
    public static HashMap<UUID, DoorHandler.Doors.Groups> readList(ByteBuf buf){
        HashMap<UUID, DoorHandler.Doors.Groups> passeder = new HashMap<>();
        int num = buf.readInt();
        for(int i=0; i<num; i++){
            DoorHandler.Doors.Groups value = new DoorHandler.Doors.Groups();
            value.id = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            value.name = ByteBufUtils.readUTF8String(buf);
            value.status = DoorHandler.Doors.OneDoor.allDoorStatuses.fromInt(buf.readShort());
            boolean isGood = buf.readBoolean();
            if(isGood)
                value.parentID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            else
                value.parentID = null;
            passeder.put(value.id, value);
        }
        return passeder;
    }

    @Override
    public void toBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        ByteBufUtils.writeUTF8String(buf, gson.toJson(door));
        ByteBufUtils.writeUTF8String(buf, editValidator.toString());
        ByteBufUtils.writeUTF8String(buf, managerId.toString());
        writeList(buf, sectors);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        door = gson.fromJson(ByteBufUtils.readUTF8String(buf), DoorHandler.Doors.OneDoor.class);
        editValidator = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        managerId = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        sectors = readList(buf);
    }

    public static class Handler implements IMessageHandler<SectorEditPacket, IMessage> {

        @Override
        public IMessage onMessage(SectorEditPacket message, MessageContext ctx) {
            if(ctx.side.isClient()){
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Minecraft mc = Minecraft.getMinecraft();
                    if(mc.world.isRemote) {
                        //open up the GUI
                        Minecraft.getMinecraft().displayGuiScreen(new EditSectorGUI(message.editValidator, message.managerId, message.sectors));
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
    public static class HandlerS implements IMessageHandler<SectorEditPacket, IMessage> { //used for server

        @Override
        public IMessage onMessage(SectorEditPacket message, MessageContext ctx) {
            DoorHandler.Doors manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(message.managerId);
            if(manager != null && manager.validator.hasPermissions("sectors", message.editValidator)) {
                manager.validator.removePerm("sectors");
                //remove ones that no longer exist and update some too
                List<UUID> remove = new LinkedList<>();
                for(DoorHandler.Doors.Groups group : manager.groups.values()){
                    if(!message.sectors.containsKey(group.id.toString())){
                        remove.add(group.id);
                    }
                    else{
                        DoorHandler.Doors.Groups upd = message.sectors.get(group.id.toString());
                        if(upd.parentID != null)
                            group.parentID = upd.parentID;
                        group.name = upd.name;
                    }
                }
                for(UUID id : remove)
                    manager.groups.remove(id);
                //add new ones
                for(DoorHandler.Doors.Groups group : message.sectors.values()){
                    if(!manager.groups.containsKey(group.id)){ //add new one
                        group.status = DoorHandler.Doors.OneDoor.allDoorStatuses.ACCESS;
                        group.override = new LinkedList<>();
                        manager.groups.put(group.id, group);
                    }
                }
                manager.markDirty();
                DoorNamePacket packet = new DoorNamePacket(manager, true);
                AdvBaseSecurity.instance.network.sendTo(packet, ctx.getServerHandler().player);
            }
            return null;
        }
    }
}
