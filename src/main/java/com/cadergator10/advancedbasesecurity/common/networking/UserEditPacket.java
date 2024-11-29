package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.EditDoorGUI;
import com.cadergator10.advancedbasesecurity.client.gui.EditUserGUI;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.util.control.Exception;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class UserEditPacket implements IMessage {
    UUID editValidator; //Used to try and ensure that the recieved door is authorized.
    List<DoorHandler.Doors.Users> users;
    List<DoorHandler.Doors.PassValue> passes;
    boolean isServer;
    public UserEditPacket(){

    }
    public UserEditPacket(UUID editValidator, List<DoorHandler.Doors.Users> users, boolean isServer){
        this.editValidator = editValidator;
        this.users = users;
        this.isServer = isServer;
    }

    private void writeList(ByteBuf buf, HashMap<String, DoorHandler.Doors.Users.UserPass> passe){
        BiConsumer<String, DoorHandler.Doors.Users.UserPass> bic = (s,u) -> {
            buf.writeBoolean(true);
            ByteBufUtils.writeUTF8String(buf, s);
            buf.writeInt(u.type);
            int size = u.passValue.size();
            buf.writeInt(size);
            for(int i=0; i<size; i++){
                ByteBufUtils.writeUTF8String(buf, u.passValue.get(i));
            }
        };
        passe.forEach(bic);
        buf.writeBoolean(false);
    }
    private HashMap<String, DoorHandler.Doors.Users.UserPass> readList(ByteBuf buf){
        HashMap<String, DoorHandler.Doors.Users.UserPass> passe = new HashMap<>();
        boolean keepGoing = buf.readBoolean();
        while(keepGoing){
            DoorHandler.Doors.Users.UserPass pass = new DoorHandler.Doors.Users.UserPass(
                    ByteBufUtils.readUTF8String(buf), buf.readInt()
            );
            pass.passValue = new LinkedList<>();
            int size = buf.readInt();
            for(int i=0; i<size; i++){
                pass.passValue.add(ByteBufUtils.readUTF8String(buf));
            }
            passe.put(pass.passId, pass);
            keepGoing = buf.readBoolean();
        }
        return passe;
    }

    @Override
    public void toBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        ByteBufUtils.writeUTF8String(buf, gson.toJson(door));
        ByteBufUtils.writeUTF8String(buf, editValidator.toString());
        buf.writeInt(users.size());
        for(int i=0; i<users.size(); i++){
            ByteBufUtils.writeUTF8String(buf, users.get(i).id.toString());
            ByteBufUtils.writeUTF8String(buf, users.get(i).name);
            buf.writeBoolean(users.get(i).owner != null);
            if(users.get(i).owner != null)
                ByteBufUtils.writeUTF8String(buf, users.get(i).owner.toString());
            buf.writeBoolean(users.get(i).staff);
            buf.writeBoolean(users.get(i).blocked);
            writeList(buf, users.get(i).passes);
        }
        buf.writeBoolean(isServer);
        if(isServer){
            RequestPassesPacket.writeList(buf);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
//        Gson gson = new GsonBuilder().create();
//        door = gson.fromJson(ByteBufUtils.readUTF8String(buf), DoorHandler.Doors.OneDoor.class);
        editValidator = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        users = new LinkedList<>();
        int count = buf.readInt();
        for(int i=0; i<count; i++){
            DoorHandler.Doors.Users user = new DoorHandler.Doors.Users();
            user.id = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            user.name = ByteBufUtils.readUTF8String(buf);
            boolean owner = buf.readBoolean();
            if(owner)
                user.owner = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            else
                user.owner = null;
            user.staff = buf.readBoolean();
            user.blocked = buf.readBoolean();
            user.passes = readList(buf);
            users.add(user);
        }
        isServer = buf.readBoolean();
        if(isServer){
            passes = RequestPassesPacket.readList(buf);
        }
    }

    public static class Handler implements IMessageHandler<UserEditPacket, IMessage> {

        @Override
        public IMessage onMessage(UserEditPacket message, MessageContext ctx) {
            if(ctx.side.isClient()){
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Minecraft mc = Minecraft.getMinecraft();
                    if(mc.world.isRemote) {
                        //open up the GUI
                        Minecraft.getMinecraft().displayGuiScreen(new EditUserGUI(message.editValidator, message.users, message.passes));
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
    public static class HandlerS implements IMessageHandler<UserEditPacket, IMessage> { //used for server

        @Override
        public IMessage onMessage(UserEditPacket message, MessageContext ctx) {
            if(AdvBaseSecurity.instance.doorHandler.checkValidator(message.editValidator)) {
                AdvBaseSecurity.instance.doorHandler.DoorGroups.users = message.users;
                AdvBaseSecurity.instance.doorHandler.verifyUserPasses();
            }
            return null;
        }
    }
}
