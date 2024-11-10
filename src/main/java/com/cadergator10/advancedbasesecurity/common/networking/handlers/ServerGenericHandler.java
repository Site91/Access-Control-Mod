package com.cadergator10.advancedbasesecurity.common.networking.handlers;

import com.cadergator10.advancedbasesecurity.client.gui.DoorListGUI;
import com.cadergator10.advancedbasesecurity.common.networking.DoorNamePacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerGenericHandler implements IMessageHandler<DoorNamePacket, IMessage> {
    @Override
    public IMessage onMessage(DoorNamePacket messaged, MessageContext ctx) {
        return null;
    }
}
