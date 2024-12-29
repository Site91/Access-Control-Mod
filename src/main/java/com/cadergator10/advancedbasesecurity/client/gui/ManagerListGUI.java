package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.networking.DoorServerRequest;
import com.cadergator10.advancedbasesecurity.common.networking.ManagerNamePacket;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ManagerListGUI extends GuiScreen {

    //data passed by packet
    List<ManagerNamePacket.packetDoor> doors;
    //button data
    GuiButton closeButton;
    GuiButton upButton;
    GuiButton downButton;
    GuiButton newButton;
//    GuiLabel noneLabel;
    List<GuiButton> doorButtons;
    List<Integer> buttonLevel;

    private static final ResourceLocation background = new ResourceLocation(AdvBaseSecurity.MODID, "textures/gui/basic.png");

    //other data
    int currPage = 1;
    int maxPageLength = 5;
    public ManagerListGUI(List<ManagerNamePacket.packetDoor> doors) {
        super();
        this.doors = doors;
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
        doorButtons = new LinkedList<>();
        buttonLevel = new LinkedList<>();
        this.buttonList.add(closeButton = new GuiButton(id++, this.width / 2 - 150, this.height - (this.height / 4) + 10, "Close"));
        this.buttonList.add(newButton = new GuiButton(id++, this.width / 2 + 50, this.height - (this.height / 4) + 10, "New Manager"));
        this.buttonList.add(upButton = new GuiButton(id++, this.width - 20, this.height - 40, 16, 16, "/\\"));
        this.buttonList.add(downButton = new GuiButton(id++, this.width - 20, this.height - 20, 16, 16, "\\/"));
//        this.labelList.add(noneLabel = new GuiLabel(fontRenderer, id++, this.width / 2 - 20, this.height / 2 + 40, 300, 20, 0xFFFFFF));
        //now for the doors
        if(!doors.isEmpty()){
            int pageCount = 1;
            int thisCount = 0;
            for(ManagerNamePacket.packetDoor door : doors){
                AdvBaseSecurity.instance.logger.info("Button for door " + door.name);
                GuiButton temp = new GuiButton(id++, this.width / 2 - 100, (this.height / 8) + (thisCount * 30), 200, 16, door.name);
                this.buttonList.add(temp);
                doorButtons.add(temp);
                buttonLevel.add(pageCount);
                if(thisCount++ >= maxPageLength) {
                    thisCount = 0;
                    pageCount++;
                }
            }
        }
        AdvBaseSecurity.instance.logger.info("Page size: " + ((doors.size() - 1) / maxPageLength));
    }

    private void changeDoorBtn(){
        drawString(Integer.toString(currPage), this.width - 20, 20, 0xFFFFFF);
        //draw all door button stuff right
        for(int i=0; i<doorButtons.size(); i++){
            if((i / maxPageLength) + 1 == currPage){ //integer division always rounds down
                doorButtons.get(i).visible = true;
                doorButtons.get(i).enabled = true;
            }
            else{
                doorButtons.get(i).visible = false;
                doorButtons.get(i).enabled = false;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(width, height, 0, 0, 176, 166);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if(doors.isEmpty())
            drawCenteredString("No doors created yet", this.height / 2 + 40, 0xFFFFFF);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button == closeButton){
            mc.player.closeScreen();
        }
        else if(button == upButton && currPage < (doors.size() - 1) / maxPageLength){
            currPage++;
            //hide doors
            changeDoorBtn();
        }
        else if(button == downButton && currPage > 1){
            currPage--;
            changeDoorBtn();
        }
        else if(button == newButton){
            newButton.enabled = false; //make sure it can't be spammed
            DoorServerRequest packet = new DoorServerRequest("newmanager", "");
            AdvBaseSecurity.instance.network.sendToServer(packet);
        }
        else{
            for (int i = 0; i < doorButtons.size(); i++) {
                GuiButton doorButton = doorButtons.get(i);
                if (button == doorButton) {
                    DoorServerRequest packet = new DoorServerRequest("linkdoormanager", doors.get(i).id.toString());
                    AdvBaseSecurity.instance.network.sendToServer(packet);
                }
            }
        }
        //TODO: Set this up to do stuff and modify a door table
    }
}
