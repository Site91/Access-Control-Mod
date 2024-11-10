package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.DoorNamePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class DoorListGUI extends GuiScreen {
    //data passed by packet
    List<DoorNamePacket.packetDoor> doors;
    HashMap<UUID, String> groupNames;
    //button data
    GuiButton closeButton;
    GuiButton upButton;
    GuiButton downButton;
    GuiLabel noneLabel;
    List<GuiButton> doorButtons;
    List<Integer> buttonLevel;
    //other data
    int currPage = 1;
    int maxPageLength = 5;
    public DoorListGUI(List<DoorNamePacket.packetDoor> doors, HashMap<UUID, String> groupNames) {
        super();
        this.doors = doors;
        this.groupNames = groupNames;
    }

    @Override
    public void initGui() {
        super.initGui();
        int id=-1;
        this.buttonList.add(closeButton = new GuiButton(id++, this.width / 2 - 100, this.height - (this.height / 4) + 10, "Close"));
        this.buttonList.add(upButton = new GuiButton(id++, this.width - 20, this.height - 20, "/\\"));
        this.buttonList.add(downButton = new GuiButton(id++, this.width - 20, this.height - 40, "\\/"));
        this.labelList.add(noneLabel = new GuiLabel(fontRenderer, id++, this.width / 2 - 20, this.height / 2 + 40, 300, 20, 0xFFFFFF));
        //now for the doors
        if(!doors.isEmpty()){
            int pageCount = 1;
            int thisCount = 1;
            for(DoorNamePacket.packetDoor door : doors){
                GuiButton temp = new GuiButton(id++, this.width / 2 - 100, this.height - (this.height / 4) + 50, door.name);
                this.buttonList.add(temp);
                doorButtons.add(temp);
                buttonLevel.add(pageCount);
                if(thisCount++ > maxPageLength) {
                    thisCount = 1;
                    pageCount++;
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button == closeButton){
            mc.player.closeScreen();
        }
        else if(button == upButton && currPage < (doorButtons.size() - 1) / maxPageLength){
            currPage++;
            //hide doors
        }
        else if(button == downButton && currPage > 1){
            currPage--;
        }
        //TODO: Set this up to do stuff and modify a door table
    }
}
