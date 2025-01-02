package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonImg;
import com.cadergator10.advancedbasesecurity.client.gui.components.EditLinkBtn;
import com.cadergator10.advancedbasesecurity.client.gui.components.ITooltip;
import com.cadergator10.advancedbasesecurity.common.networking.DoorNamePacket;
import com.cadergator10.advancedbasesecurity.common.networking.DoorServerRequest;
import com.cadergator10.advancedbasesecurity.util.ButtonTooltip;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public class DoorListGUI extends BaseGUI {
    UUID managerId;
    //data passed by packet
    List<DoorNamePacket.packetDoor> doors;
    HashMap<UUID, String> groupNames;
    //button data
    ButtonImg closeButton;
    ButtonImg passButton;
    ButtonImg userButton;
    EditLinkBtn modeButton;
    ButtonImg upButton;
    ButtonImg downButton;
    ButtonImg newButton;
//    GuiLabel noneLabel;
    List<GuiButton> doorButtons;
    List<Integer> buttonLevel;
    //other data
    int currPage = 1;
    int maxPageLength = 5;
    public DoorListGUI(UUID managerId, List<DoorNamePacket.packetDoor> doors, HashMap<UUID, String> groupNames) {
        super();
        this.managerId = managerId;
        this.doors = doors;
        this.groupNames = groupNames;
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
        this.buttonList.add(closeButton = new ButtonImg(id++, this.width / 2 - 150, this.height - (this.height / 4) + 10, ButtonTooltip.Back));
        this.buttonList.add(newButton = new ButtonImg(id++, this.width / 2 + 50, this.height - (this.height / 4) + 10, ButtonTooltip.AddDoor));
        this.buttonList.add(upButton = new ButtonImg(id++, this.width - 20, this.height - 40, ButtonTooltip.UpButton));
        this.buttonList.add(downButton = new ButtonImg(id++, this.width - 20, this.height - 20, ButtonTooltip.DownButton));
        this.buttonList.add(modeButton = new EditLinkBtn(id++, this.width / 2 - 200, 20));
        this.buttonList.add(userButton = new ButtonImg(id++, this.width / 2 - 150, this.height - (this.height / 4) + 30, ButtonTooltip.EditUser));
        this.buttonList.add(passButton = new ButtonImg(id++, this.width / 2 + 50, this.height - (this.height / 4) + 30, ButtonTooltip.EditPass));
        //        this.labelList.add(noneLabel = new GuiLabel(fontRenderer, id++, this.width / 2 - 20, this.height / 2 + 40, 300, 20, 0xFFFFFF));
        //now for the doors
        if(!doors.isEmpty()){
            int pageCount = 1;
            int thisCount = 0;
            for(DoorNamePacket.packetDoor door : doors){
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
            DoorServerRequest packet = new DoorServerRequest(managerId, "newdoor", "");
            AdvBaseSecurity.instance.network.sendToServer(packet);
        }
        else if(button == modeButton){
            modeButton.onClick();
        }
        else if(button == passButton){
            DoorServerRequest packet = new DoorServerRequest(managerId, "openpassmenu", "");
            AdvBaseSecurity.instance.network.sendToServer(packet);
        }
        else if(button == userButton){
            DoorServerRequest packet = new DoorServerRequest(managerId, "openusermenu", "");
            AdvBaseSecurity.instance.network.sendToServer(packet);
        }
        else{
            for (int i = 0; i < doorButtons.size(); i++) {
                GuiButton doorButton = doorButtons.get(i);
                if (button == doorButton) {
                    if(!modeButton.isEdit()) {
                        DoorServerRequest packet = new DoorServerRequest(managerId, "managerdoorlink", doors.get(i).id.toString());
                        AdvBaseSecurity.instance.network.sendToServer(packet);
                        mc.player.closeScreen();
                    }
                    else{
                        DoorServerRequest packet = new DoorServerRequest(managerId, "editdoor", doors.get(i).id.toString());
                        AdvBaseSecurity.instance.network.sendToServer(packet);
                    }
                }
            }
        }
        //TODO: Set this up to do stuff and modify a door table
    }
}
