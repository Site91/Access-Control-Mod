package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonImg;
import com.cadergator10.advancedbasesecurity.client.gui.components.GUITextFieldTooltip;
import com.cadergator10.advancedbasesecurity.common.networking.DoorServerRequest;
import com.cadergator10.advancedbasesecurity.common.networking.ManagerNamePacket;
import com.cadergator10.advancedbasesecurity.util.ButtonTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ManagerListGUI extends BaseGUI implements GuiPageButtonList.GuiResponder {

    //data passed by packet
    List<ManagerNamePacket.packetDoor> doors;
    GUITextFieldTooltip doorName;
    //button data
    ButtonImg closeButton;
//    ButtonImg upButton;
//    ButtonImg downButton;
    ButtonImg newButton;
//    GuiLabel noneLabel;
//    List<GuiButton> doorButtons;
//    List<Integer> buttonLevel;
    ManagerList managerList;

    static final int WIDTH = 175;
    static final int HEIGHT = 195;

    private static final ResourceLocation background = new ResourceLocation(AdvBaseSecurity.MODID, "textures/gui/basic.png");

    //other data
    int currPage = 1;
    int maxPageLength = 5;
    public ManagerListGUI(List<ManagerNamePacket.packetDoor> doors) {
        super(WIDTH, HEIGHT);
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
//        doorButtons = new LinkedList<>();
//        buttonLevel = new LinkedList<>();
        managerList = new ManagerList(mc, WIDTH, 100, GUITop, GUILeft, width, height, doors);
        this.buttonList.add(closeButton = new ButtonImg(id++, GUILeft + 3, GUITop + HEIGHT - 19, ButtonTooltip.Back));
        this.buttonList.add(newButton = new ButtonImg(id++, GUILeft + WIDTH - 19, GUITop + HEIGHT - 19, ButtonTooltip.AddManager));
        doorName = new GUITextFieldTooltip(id++, fontRenderer, GUILeft + WIDTH - 123, GUITop + HEIGHT - 19, 100, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.managername"));
        doorName.setGuiResponder(this);
        doorName.setText("new");
//        this.buttonList.add(upButton = new ButtonImg(id++, this.width - 20, this.height - 40, ButtonTooltip.UpButton));
//        this.buttonList.add(downButton = new ButtonImg(id++, this.width - 20, this.height - 20, ButtonTooltip.DownButton));
//        this.labelList.add(noneLabel = new GuiLabel(fontRenderer, id++, this.width / 2 - 20, this.height / 2 + 40, 300, 20, 0xFFFFFF));
        //now for the doors
//        if(!doors.isEmpty()){
//            int pageCount = 1;
//            int thisCount = 0;
//            for(ManagerNamePacket.packetDoor door : doors){
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
        else if(managerList != null){
            managerList.drawScreen(mouseX, mouseY, partialTicks);
        }
        doorName.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
        processField(doorName, mouseX, mouseY);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode != Keyboard.KEY_ESCAPE) {
            if(doorName.isFocused()) {
                doorName.textboxKeyTyped(typedChar, keyCode);
            }
        }
        else
            super.keyTyped(typedChar, keyCode);
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        doorName.mouseClicked(mouseX, mouseY, mouseButton);
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
            if(!doorName.getText().equals("")) {
                newButton.enabled = false; //make sure it can't be spammed
                DoorServerRequest packet = new DoorServerRequest("newmanager", doorName.getText());
                AdvBaseSecurity.instance.network.sendToServer(packet);
            }
        }
        else{
//            for (int i = 0; i < doorButtons.size(); i++) {
//                GuiButton doorButton = doorButtons.get(i);
//                if (button == doorButton) {
//                    DoorServerRequest packet = new DoorServerRequest("linkdoormanager", doors.get(i).id.toString());
//                    AdvBaseSecurity.instance.network.sendToServer(packet);
//                }
//            }
        }
        //TODO: Set this up to do stuff and modify a door table
    }

    @Override
    public void setEntryValue(int id, boolean value) {

    }

    @Override
    public void setEntryValue(int id, float value) {

    }

    @Override
    public void setEntryValue(int id, String value) {

    }

    class ManagerList extends GuiScrollingList {
        List<ManagerNamePacket.packetDoor> pck;

        private int hoveredSlot = -1;
        private int i = 0;
        private boolean isHovering = false;

        public ManagerList(Minecraft client, int width, int height, int top, int left, int screenWidth, int screenHeight,List<ManagerNamePacket.packetDoor> pck) {
            super(client, width, height, top, top + height, left, 12, screenWidth, screenHeight);
            this.pck = pck;
        }

        @Override
        protected int getSize() {
            return pck.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {
            DoorServerRequest packet = new DoorServerRequest("linkdoormanager", pck.get(index).id.toString());
            AdvBaseSecurity.instance.network.sendToServer(packet);
            mc.player.closeScreen();
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
