package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.EditDoorGUI;
import com.cadergator10.advancedbasesecurity.client.gui.ManagerListGUI;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.items.ItemDoorManager;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ManagerNamePacket implements IMessage {
    public List<packetDoor> doors;

    public static class packetDoor{
        public UUID id;
        public String name;
        public String creator;
        public packetDoor(){

        }
        public packetDoor(UUID id, String name, String creator) {
            this.id = id;
            this.name = name;
            this.creator = creator;
        }
    }

    public ManagerNamePacket(){

    }
    public ManagerNamePacket(List<DoorHandler.Doors> doorse){
        doors = new LinkedList<>();
        PlayerProfileCache cache = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache();
        for(DoorHandler.Doors doore : doorse){
            GameProfile profile = cache.getProfileByUUID(doore.creator);
            doors.add(new packetDoor(doore.id, doore.name, profile != null ? profile.getName() : "nobody"));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        ByteBufUtils.writeUTF8String(buf, gson.toJson(door));
        buf.writeInt(doors.size());
        for(packetDoor door : doors){
            ByteBufUtils.writeUTF8String(buf, door.id.toString());
            ByteBufUtils.writeUTF8String(buf, door.name);
            ByteBufUtils.writeUTF8String(buf, door.creator);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        door = gson.fromJson(ByteBufUtils.readUTF8String(buf), DoorHandler.Doors.OneDoor.class);
        int size = buf.readInt();
        doors = new LinkedList<>();
        for(int i=0; i<size; i++){
            doors.add(new packetDoor(UUID.fromString(ByteBufUtils.readUTF8String(buf)), ByteBufUtils.readUTF8String(buf), ByteBufUtils.readUTF8String(buf)));
        }
    }

    public static class Handler implements IMessageHandler<ManagerNamePacket, IMessage> {

        @Override
        public IMessage onMessage(ManagerNamePacket message, MessageContext ctx) {
            if(ctx.side.isClient()){
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Minecraft mc = Minecraft.getMinecraft();
                    if(mc.world.isRemote) {
                        //open up the GUI
                        Minecraft.getMinecraft().displayGuiScreen(new ManagerListGUI(message.doors));
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
    public static class HandlerS implements IMessageHandler<ManagerNamePacket, IMessage> { //used for server

        @Override
        public IMessage onMessage(ManagerNamePacket message, MessageContext ctx) {
            return null;
        }
    }
}
