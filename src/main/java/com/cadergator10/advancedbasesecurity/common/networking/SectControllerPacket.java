package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.SectorControllerGUI;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntitySectorController;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class SectControllerPacket implements IMessage {
    public DoorHandler.DoorIdentifier ids;
    public List<DoorHandler.Doors.OneDoor.OnePass> overrides;
    HashMap<UUID, DoorHandler.Doors.Groups> sectors;
    public boolean pushToChildren;
    public boolean toggle;
    public DoorHandler.Doors.OneDoor.allDoorStatuses thisStatus;
    BlockPos pos;
    public SectControllerPacket(){

    }
    public SectControllerPacket(TileEntitySectorController te){
        ids = te.getIds();
        overrides = te.getOverrides();
        this.sectors = AdvBaseSecurity.instance.doorHandler.getDoorManager(ids.ManagerID).groups;
        pushToChildren = te.pushToChildren;
        toggle = te.toggle;
        this.thisStatus = te.thisStatus;
        this.pos = te.getPos();
    }
    public SectControllerPacket(DoorHandler.DoorIdentifier ids, BlockPos pos, List<DoorHandler.Doors.OneDoor.OnePass> overrides, boolean pushChild, boolean toggle, DoorHandler.Doors.OneDoor.allDoorStatuses thisStatus){
        this.ids = ids;
        this.overrides = overrides;
        this.pushToChildren = pushChild;
        this.toggle = toggle;
        this.sectors = null;
        this.thisStatus = thisStatus;
        this.pos = pos;
    }
    private void writeOverrides(ByteBuf buf, List<DoorHandler.Doors.OneDoor.OnePass> rides){
        buf.writeInt(rides != null ? rides.size() : 0);
        for(DoorHandler.Doors.OneDoor.OnePass pass : rides){
            ByteBufUtils.writeUTF8String(buf, pass.id.toString());
            ByteBufUtils.writeUTF8String(buf, pass.passID);
            buf.writeShort(pass.priority);
            buf.writeInt(pass.addPasses != null ? pass.addPasses.size() : 0);
            for(UUID id : pass.addPasses)
                ByteBufUtils.writeUTF8String(buf, id.toString());
            buf.writeShort(pass.passType.getInt());
            int num = pass.passValueI != -1 ? 1 : pass.passValueS != null ? 2 : 0;
            buf.writeShort(num);
            if(num == 1)
                buf.writeInt(pass.passValueI);
            else if(num == 2)
                ByteBufUtils.writeUTF8String(buf, pass.passValueS);
        }
    }
    private List<DoorHandler.Doors.OneDoor.OnePass> readOverrides(ByteBuf buf){
        List<DoorHandler.Doors.OneDoor.OnePass> passeder = new LinkedList<>();
        int size = buf.readInt();
        for(int i=0; i<size; i++){
            DoorHandler.Doors.OneDoor.OnePass pass = new DoorHandler.Doors.OneDoor.OnePass();
            pass.id = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            pass.passID = ByteBufUtils.readUTF8String(buf);
            pass.priority = buf.readShort();
            int adds = buf.readInt();
            pass.addPasses = new LinkedList<>();
            for(int j=0; j<adds; j++){
                pass.addPasses.add(UUID.fromString(ByteBufUtils.readUTF8String(buf)));
            }
            pass.passType = DoorHandler.Doors.OneDoor.OnePass.type.fromInt(buf.readShort());
            int num = buf.readShort();
            if(num == 1)
                pass.passValueI = buf.readInt();
            else if(num == 2) {
                pass.passValueS = ByteBufUtils.readUTF8String(buf);
                pass.passValueI = -1;
            }
            else
                pass.passValueI = -1;
            passeder.add(pass);
        }
        return passeder;
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
        ByteBufUtils.writeUTF8String(buf, ids.ManagerID.toString());
        boolean good = ids.DoorID != null;
        buf.writeBoolean(good);
        if(good)
            ByteBufUtils.writeUTF8String(buf, ids.DoorID.toString());
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeShort(thisStatus.getInt());
        writeOverrides(buf, overrides);
        buf.writeBoolean(pushToChildren);
        buf.writeBoolean(toggle);
        writeList(buf, sectors);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ids = new DoorHandler.DoorIdentifier();
        ids.ManagerID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        boolean good = buf.readBoolean();
        if(good)
            ids.DoorID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        else
            ids.DoorID = null;
        pos = new BlockPos(buf.readInt(),buf.readInt(),buf.readInt());
        thisStatus = DoorHandler.Doors.OneDoor.allDoorStatuses.fromInt(buf.readShort());
        overrides = readOverrides(buf);
        pushToChildren = buf.readBoolean();
        toggle = buf.readBoolean();
        sectors = readList(buf);
    }

    public static class Handler implements IMessageHandler<SectControllerPacket, IMessage> {

        @Override
        public IMessage onMessage(SectControllerPacket message, MessageContext ctx) {
            if(ctx.side.isClient()){
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Minecraft mc = Minecraft.getMinecraft();
                    if(mc.world.isRemote) {
                        //open up the GUI
                        Minecraft.getMinecraft().displayGuiScreen(new SectorControllerGUI(message, message.pos, message.sectors));
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
    public static class HandlerS implements IMessageHandler<SectControllerPacket, IMessage> { //used for server

        @Override
        public IMessage onMessage(SectControllerPacket message, MessageContext ctx) {
            DoorHandler.Doors manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(message.ids);
            if(manager != null && manager.hasPerms(ctx.getServerHandler().player)) {
                ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                    TileEntity tem = ctx.getServerHandler().player.getServerWorld().getTileEntity(message.pos);
                    if (tem instanceof TileEntitySectorController) {
                        TileEntitySectorController te = (TileEntitySectorController) tem;
                        te.newUpdate(message);
                    }
                });
            }
            return null;
        }
    }
}
