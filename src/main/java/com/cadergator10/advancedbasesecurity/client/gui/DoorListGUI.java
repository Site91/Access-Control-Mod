package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonImg;
import com.cadergator10.advancedbasesecurity.client.gui.components.EditLinkBtn;
import com.cadergator10.advancedbasesecurity.client.gui.components.ITooltip;
import com.cadergator10.advancedbasesecurity.common.networking.DoorNamePacket;
import com.cadergator10.advancedbasesecurity.common.networking.DoorServerRequest;
import com.cadergator10.advancedbasesecurity.common.networking.ManagerNamePacket;
import com.cadergator10.advancedbasesecurity.util.ButtonTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public class DoorListGUI extends BaseGUI {
    UUID managerId;
    boolean isEdit = false;
    //data passed by packet
    List<DoorNamePacket.packetDoor> doors;
    HashMap<UUID, String> groupNames;
    //button data
    ButtonImg closeButton;
    ButtonImg passButton;
    ButtonImg userButton;
    ButtonImg sectorButton;
    EditLinkBtn modeButton;
//    ButtonImg upButton;
//    ButtonImg downButton;
    ButtonImg newButton;
//    GuiLabel noneLabel;
//    List<GuiButton> doorButtons;
//    List<Integer> buttonLevel;
    DoorLister doorLists;
    //other data
    int currPage = 1;
    int maxPageLength = 5;

    private static final ResourceLocation background = new ResourceLocation(AdvBaseSecurity.MODID, "textures/gui/basic.png");
    static final int WIDTH = 175;
    static final int HEIGHT = 195;

    public DoorListGUI(UUID managerId, List<DoorNamePacket.packetDoor> doors, HashMap<UUID, String> groupNames, boolean isEdit) {
        super(WIDTH, HEIGHT);
        this.managerId = managerId;
        this.doors = doors;
        this.groupNames = groupNames;
        this.isEdit = isEdit;
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
//        doorButtons = new LinkedList<>();
//        buttonLevel = new LinkedList<>();
        int botm = GUITop + HEIGHT - 19;
        this.buttonList.add(closeButton = new ButtonImg(id++, GUILeft + 3, botm, ButtonTooltip.Back));
        this.buttonList.add(newButton = new ButtonImg(id++, GUILeft + WIDTH - 19, botm, ButtonTooltip.AddDoor));
//        this.buttonList.add(upButton = new ButtonImg(id++, this.width - 20, this.height - 40, ButtonTooltip.UpButton));
//        this.buttonList.add(downButton = new ButtonImg(id++, this.width - 20, this.height - 20, ButtonTooltip.DownButton));
        this.buttonList.add(modeButton = new EditLinkBtn(id++, GUILeft + 3, GUITop + 133, isEdit));
        this.buttonList.add(userButton = new ButtonImg(id++, this.width / 2 - WIDTH / 6 - 4, botm, ButtonTooltip.EditUser));
        this.buttonList.add(passButton = new ButtonImg(id++, this.width / 2 + WIDTH / 6 - 4, botm, ButtonTooltip.EditPass));
        this.buttonList.add(sectorButton = new ButtonImg(id++, this.width / 2 + 8, botm, ButtonTooltip.SectorMenu));
        if(!isEdit){
            userButton.enabled = false;
            passButton.enabled = false;
            sectorButton.enabled = false;
            newButton.enabled = false;
        }
        //        this.labelList.add(noneLabel = new GuiLabel(fontRenderer, id++, this.width / 2 - 20, this.height / 2 + 40, 300, 20, 0xFFFFFF));
        //now for the doors
//        if(!doors.isEmpty()){
//            int pageCount = 1;
//            int thisCount = 0;
//            for(DoorNamePacket.packetDoor door : doors){
//                AdvBaseSecurity.instance.logger.info("Button for door " + door.name);
//                GuiButton temp = new GuiButton(id++, this.width / 2 - 100, (this.height / 8) + (thisCount * 30), 200, 16, door.name);
//                this.buttonList.add(temp);
//                doorButtons.add(temp);
//                buttonLevel.add(pageCount);
//                if(thisCount++ >= maxPageLength) {
//                    thisCount = 0;
//                    pageCount++;
//                }
//            }
//        }
        doorLists = new DoorLister(mc, WIDTH, 130, GUITop, GUILeft, width, height, doors);
        AdvBaseSecurity.instance.logger.info("Page size: " + ((doors.size() - 1) / maxPageLength));
    }

//    private void changeDoorBtn(){
//        drawString(Integer.toString(currPage), this.width - 20, 20, 0xFFFFFF);
//        //draw all door button stuff right
//        for(int i=0; i<doorButtons.size(); i++){
//            if((i / maxPageLength) + 1 == currPage){ //integer division always rounds down
//                doorButtons.get(i).visible = true;
//                doorButtons.get(i).enabled = true;
//            }
//            else{
//                doorButtons.get(i).visible = false;
//                doorButtons.get(i).enabled = false;
//            }
//        }
//    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(GUILeft, GUITop, 0, 0, WIDTH, HEIGHT);
        if(doors.isEmpty())
            drawCenteredString("No doors created yet", this.height / 2 + 40, 0xFFFFFF);
        else if(doorLists != null){
            doorLists.drawScreen(mouseX, mouseY, partialTicks);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button == closeButton){
            mc.player.closeScreen();
        }
//        else if(button == upButton && currPage < (doors.size() - 1) / maxPageLength){
//            currPage++;
//            //hide doors
//            changeDoorBtn();
//        }
//        else if(button == downButton && currPage > 1){
//            currPage--;
//            changeDoorBtn();
//        }
        else if(button == newButton){
            newButton.enabled = false; //make sure it can't be spammed
            DoorServerRequest packet = new DoorServerRequest(managerId, "newdoor", "");
            AdvBaseSecurity.instance.network.sendToServer(packet);
        }
        else if(button == modeButton){
            modeButton.onClick();
            isEdit = modeButton.isEdit();
            DoorServerRequest packet = new DoorServerRequest(managerId, "modeButtonHit", isEdit ? "true" : "false");
            AdvBaseSecurity.instance.network.sendToServer(packet);
            if(!isEdit){
                userButton.enabled = false;
                passButton.enabled = false;
                newButton.enabled = false;
                sectorButton.enabled = false;
            }
            else{
                userButton.enabled = true;
                passButton.enabled = true;
                newButton.enabled = true;
                sectorButton.enabled = true;
            }
        }
        else if(button == passButton){
            DoorServerRequest packet = new DoorServerRequest(managerId, "openpassmenu", "");
            AdvBaseSecurity.instance.network.sendToServer(packet);
        }
        else if(button == userButton){
            DoorServerRequest packet = new DoorServerRequest(managerId, "openusermenu", "");
            AdvBaseSecurity.instance.network.sendToServer(packet);
        }
        else if(button == sectorButton){
            DoorServerRequest packet = new DoorServerRequest(managerId, "opensectormenu", "");
            AdvBaseSecurity.instance.network.sendToServer(packet);
        }
        else{
//            for (int i = 0; i < doorButtons.size(); i++) {
//                GuiButton doorButton = doorButtons.get(i);
//                if (button == doorButton) {
//                    if(!modeButton.isEdit()) {
//                        DoorServerRequest packet = new DoorServerRequest(managerId, "managerdoorlink", doors.get(i).id.toString());
//                        AdvBaseSecurity.instance.network.sendToServer(packet);
//                        mc.player.closeScreen();
//                    }
//                    else{
//                        DoorServerRequest packet = new DoorServerRequest(managerId, "editdoor", doors.get(i).id.toString());
//                        AdvBaseSecurity.instance.network.sendToServer(packet);
//                    }
//                }
//            }
        }
        //TODO: Set this up to do stuff and modify a door table
    }
    class DoorLister extends GuiScrollingList {
        List<DoorNamePacket.packetDoor> pck;

        private int hoveredSlot = -1;
        private int i = 0;
        private boolean isHovering = false;

        public DoorLister(Minecraft client, int width, int height, int top, int left, int screenWidth, int screenHeight, List<DoorNamePacket.packetDoor> pck) {
            super(client, width, height, top, top + height, left, 12, screenWidth, screenHeight);
            this.pck = pck;
        }

        @Override
        protected int getSize() {
            return pck.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {
            if(!modeButton.isEdit()) {
                DoorServerRequest packet = new DoorServerRequest(managerId, "managerdoorlink", doors.get(index).id.toString());
                AdvBaseSecurity.instance.network.sendToServer(packet);
                mc.player.closeScreen();
            }
            else{
                DoorServerRequest packet = new DoorServerRequest(managerId, "editdoor", doors.get(index).id.toString());
                AdvBaseSecurity.instance.network.sendToServer(packet);
            }
        }

        @Override
        protected boolean isSelected(int index) {
            return false;
        }

        @Override
        protected void drawBackground() {

        }

        @Override
        protected void drawSlot(int slotIndex, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            //highlighted hovered slot
            if (mouseX >= left && mouseX <= entryRight && slotIndex >= 0 && slotIndex < getSize() && mouseY >= slotTop - 1 && mouseY <= slotTop + slotBuffer + 2) {

                if (pck.get(slotIndex) != null) {
                    int min = left;
                    int max = entryRight + 1;
                    BufferBuilder bufferBuilder = tess.getBuffer();

                    slotBuffer--;
                    this.hoveredSlot = slotIndex;
                    isHovering = true;
                    GlStateManager.disableTexture2D();
                    bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                    bufferBuilder.pos(min, slotTop + slotBuffer + 3, 0).tex(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    bufferBuilder.pos(max, slotTop + slotBuffer + 3, 0).tex(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    bufferBuilder.pos(max, slotTop - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    bufferBuilder.pos(min, slotTop - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    bufferBuilder.pos(min + 1, slotTop + slotBuffer + 2, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    bufferBuilder.pos(max - 1, slotTop + slotBuffer + 2, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    bufferBuilder.pos(max - 1, slotTop - 1, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    bufferBuilder.pos(min + 1, slotTop - 1, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    tess.draw();
                    GlStateManager.enableTexture2D();
                }
            }

            //trickery to correctly set the currently hovered slot back if no slot is being hovered
            if (i++ == getSize() - 1) {
                if (!isHovering) {
                    hoveredSlot = -1;
                    i = 0;
                }
                else {
                    isHovering = false;
                    i = 0;
                }
            }

            if (slotIndex >= 0 && slotIndex < pck.size() && pck.get(slotIndex) != null)
                fontRenderer.drawString(pck.get(slotIndex).name, width / 2 - fontRenderer.getStringWidth(pck.get(slotIndex).name) / 2, slotTop, 0xC6C6C6);
        }
    }
}
