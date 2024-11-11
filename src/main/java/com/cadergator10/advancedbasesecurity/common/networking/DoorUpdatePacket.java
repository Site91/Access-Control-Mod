package com.cadergator10.advancedbasesecurity.common.networking;

import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class DoorUpdatePacket implements IMessage { //if new door is made or update to current one.
    UUID editValidator;
    DoorHandler.Doors.OneDoor door;

    public DoorUpdatePacket(){

    }
    public DoorUpdatePacket(UUID editValidator, DoorHandler.Doors.OneDoor door){
        this.editValidator = editValidator;
        this.door = door;
    }
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class Handler implements IMessageHandler<DoorUpdatePacket, IMessage> {

        @Override
        public IMessage onMessage(DoorUpdatePacket message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft mc = Minecraft.getMinecraft();
                if(mc.world.isRemote) {
                    //open up the GUI
                    //Minecraft.getMinecraft().displayGuiScreen(new DoorListGUI(message.door));
                }
                else{
                    //AdvBaseSecurity.instance.doorHandler.recievedUpdate();
                }
            });
            return null;
        }
    }
}
