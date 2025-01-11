package com.cadergator10.advancedbasesecurity.common.networking.handlers;

import com.cadergator10.advancedbasesecurity.client.gui.DoorListGUI;
import com.cadergator10.advancedbasesecurity.common.networking.DoorNamePacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DoorNamedHandler implements IMessageHandler<DoorNamePacket, IMessage> {
    @Override
    public IMessage onMessage(DoorNamePacket messaged, MessageContext ctx) {
        if(messaged instanceof DoorNamePacket) {
            DoorNamePacket message = (DoorNamePacket) messaged;
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
                if (mc.world.isRemote)
                    //open up the GUI
                    mc.displayGuiScreen(new DoorListGUI(message.managerId, message.doors, message.groupNames, message.isEdit));
            });
        }
        return null;
    }
}
