package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.DoorNamePacket;
import com.cadergator10.advancedbasesecurity.common.networking.DoorServerRequest;
import com.cadergator10.advancedbasesecurity.common.networking.DoorUpdatePacket;
import com.cadergator10.advancedbasesecurity.common.networking.OneDoorDataPacket;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class EditDoorGUI extends GuiScreen {
    //data passed by packet
    DoorHandler.Doors.OneDoor door;
    String group;
    UUID editValidator;
    //button data
    GuiButton backButton;
    GuiButton saveButton;
    GuiTextField nameField;
    //other data
    boolean letPress;
    public EditDoorGUI(UUID editValidator, DoorHandler.Doors.OneDoor door, String group) {
        super();
        this.door = door;
        this.group = group;
    }

    void drawString(String string, int x, int y, int color){
        FontRenderer fr = mc.fontRenderer;
        fr.drawString(string, x, y, color);
    }


    void drawCenteredString(String string, int y, int color){
        drawString(string, this.width/2 - mc.fontRenderer.getStringWidth(string)/2, y, color);
    }

    @Override
    public void initGui() {
        super.initGui();
        int id=-1;
        this.buttonList.add(backButton = new GuiButton(id++, this.width / 2 - 100, this.height - (this.height / 4) + 10, "Back"));
        this.buttonList.add(saveButton = new GuiButton(id++, this.width / 2 + 100, this.height - (this.height / 4) + 10, "Save"));
        nameField = new GuiTextField(id++, fontRenderer, 20, 20, 30, 16);
//        this.labelList.add(noneLabel = new GuiLabel(fontRenderer, id++, this.width / 2 - 20, this.height / 2 + 40, 300, 20, 0xFFFFFF));
        //now for the doors
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(letPress) {
            if(button == backButton){
                letPress = false;
                DoorServerRequest packet = new DoorServerRequest(editValidator, "doorlist", ""); //get the door list again
                AdvBaseSecurity.instance.network.sendToServer(packet);
            }
            else if(button == saveButton){
                letPress = false;
                OneDoorDataPacket packet = new OneDoorDataPacket(editValidator, door, false);
                AdvBaseSecurity.instance.network.sendToServer(packet);
            }
        }
    }
}
