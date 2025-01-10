package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonImg;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonToggle;
import com.cadergator10.advancedbasesecurity.client.gui.components.GUITextFieldTooltip;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.DoorServerRequest;
import com.cadergator10.advancedbasesecurity.common.networking.OneDoorDataPacket;
import com.cadergator10.advancedbasesecurity.util.ButtonTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class EditDoorGUI extends BaseGUI implements GuiPageButtonList.GuiResponder {
    //data passed by packet
    DoorHandler.Doors.OneDoor door;
    List<ButtonEnum.groupIndex> groups;
    int groupIndex = 0;

    UUID editValidator;
    UUID managerId;

    boolean clean = false;

    //button data
    ButtonImg backButton;
    ButtonImg saveButton;
    //GUI buttons
    ButtonImg editPasses;
    //door values
    GUITextFieldTooltip nameField;
    ButtonToggle toggleDoor;
    ButtonImg doorDelayUp;
    ButtonImg doorDelayDown;
    GUITextFieldTooltip doorDelayInput;
    ButtonEnum groupSelect;
    ButtonImg clearDevices;

    private static final ResourceLocation background = new ResourceLocation(AdvBaseSecurity.MODID, "textures/gui/basic.png");
    static final int WIDTH = 175;
    static final int HEIGHT = 195;

    //other data
    boolean letPress;
    public EditDoorGUI(UUID editValidator, UUID managerId, DoorHandler.Doors.OneDoor door, List<ButtonEnum.groupIndex> groups) {
        super(WIDTH, HEIGHT);
        this.editValidator = editValidator;
        this.managerId = managerId;
        this.door = door;
        this.groups = groups;
        if(door.groupID != null){
            for(int i=0; i<groups.size(); i++)
                if(door.groupID.equals(groups.get(i).id)){
                    groupIndex = i + 1;
                    break;
                }
        }
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
        letPress = true;
        int botm = GUITop + HEIGHT - 19;
        this.buttonList.add(backButton = new ButtonImg(id++, GUILeft + 3, botm, ButtonTooltip.Back));
        this.buttonList.add(saveButton = new ButtonImg(id++, GUILeft + WIDTH - 19, botm, ButtonTooltip.SaveDoor));

        nameField = new GUITextFieldTooltip(id++, fontRenderer, this.width / 2 - ((WIDTH - 40) / 2), GUITop + 3, WIDTH - 40, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.doorname"));
        nameField.setGuiResponder(this);
        nameField.setText(door.doorName);
        this.buttonList.add(groupSelect = new ButtonEnum(id++, GUILeft + 3, GUITop + 23, 80, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.doorgroup"), true, groups, groupIndex));
        this.buttonList.add(editPasses = new ButtonImg(id++, GUILeft + WIDTH - 43, GUITop + 43, ButtonTooltip.EditPass));
        this.buttonList.add(clearDevices = new ButtonImg(id++, GUILeft + WIDTH - 19, GUITop + 43, ButtonTooltip.ClearDevices, new String[] {(Integer.toString(door.Readers.size() + door.Doors.size()))}));
        this.buttonList.add(toggleDoor = new ButtonToggle(id++, GUILeft + WIDTH - 83, GUITop + 23, 80, 16, "Stay Open", I18n.translateToLocal("gui.tooltips.advancedbasesecurity.togglebutton"), door.defaultToggle));
        this.buttonList.add(doorDelayDown = new ButtonImg(id++, GUILeft + 57, GUITop + 43, ButtonTooltip.DelayDown));
        this.buttonList.add(doorDelayUp = new ButtonImg(id++, GUILeft + 37, GUITop + 43, ButtonTooltip.DelayUp));
        doorDelayInput = new GUITextFieldTooltip(id++, fontRenderer, GUILeft + 3, GUITop + 43, 30, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.delayinput"));
        doorDelayInput.setText(Integer.toString(door.defaultTick / 20));
        doorDelayInput.setGuiResponder(this);
        doorDelayInput.setValidator((r) -> {
            try{
                if(!r.isEmpty()) {
                    int num = Integer.parseInt(r);
                    return num >= 0 && num <= 60;
                }
                else{
                    return true;
                }
            }
            catch(Exception e){
                return false;
            }
        });

//        this.labelList.add(noneLabel = new GuiLabel(fontRenderer, id++, this.width / 2 - 20, this.height / 2 + 40, 300, 20, 0xFFFFFF));
        //now for the doors
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(GUILeft, GUITop, 0, 0, WIDTH, HEIGHT);
        nameField.drawTextBox();
        doorDelayInput.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
        processField(nameField, mouseX, mouseY);
        processField(doorDelayInput, mouseX, mouseY);
    }

    private char[] allowedChars = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '\u0008', '\u001B'
    }; //0-9, backspace and escape

    private boolean isValidChar(char c) {
        for (int i = 0; i < allowedChars.length; i++) {
            if (c == allowedChars[i])
                return true;
        }

        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode != Keyboard.KEY_ESCAPE) {
            if(nameField.isFocused()) {
                nameField.textboxKeyTyped(typedChar, keyCode);
            }
            else if(doorDelayInput.isFocused() && isValidChar(typedChar)) {
                doorDelayInput.textboxKeyTyped(typedChar, keyCode);
            }
        }
        else
            super.keyTyped(typedChar, keyCode);
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        doorDelayInput.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(letPress) {
            if(button == backButton){
                letPress = false;
                clean = true;
                DoorServerRequest packet = new DoorServerRequest(editValidator, "door:" + door.doorId, managerId, "removeperm", "");
                AdvBaseSecurity.instance.network.sendToServer(packet);
                packet = new DoorServerRequest(managerId, "doorlist", ""); //get the door list again
                AdvBaseSecurity.instance.network.sendToServer(packet);
            }
            else if(button == saveButton){
                letPress = false;
                clean = true;
                door.defaultTick = !doorDelayInput.getText().equals("") ? Math.max(1, Integer.parseInt(doorDelayInput.getText())) * 20 : 100;
                door.doorName = !nameField.getText().isEmpty() ? nameField.getText() : "new door";
                OneDoorDataPacket packet = new OneDoorDataPacket(editValidator, managerId, door, false);
                AdvBaseSecurity.instance.network.sendToServer(packet);
            }
            else if(button == groupSelect){
                groupSelect.onClick();
                String id = groupSelect.getUUID();
                door.groupID = !id.equals("none") ? UUID.fromString(id) : null;
            }
            else if(button == clearDevices){
                door.Readers = new LinkedList<>();
                door.Doors = new LinkedList<>();
                clearDevices.displayString = "Clear 0 Devices";
            }
            else if(button == doorDelayDown){
                doorDelayInput.setText(!doorDelayInput.getText().isEmpty() ? Integer.toString(Math.max(0, Integer.parseInt(doorDelayInput.getText()) - 1)) : "0");
            }
            else if(button == doorDelayUp){
                doorDelayInput.setText(!doorDelayInput.getText().isEmpty() ? Integer.toString(Math.min(60, Integer.parseInt(doorDelayInput.getText()) + 1)) : "1");
            }
            else if(button == toggleDoor){
                door.defaultToggle = toggleDoor.onClick();
            }
            else if(button == editPasses){
                clean = true;
                letPress = false;
                door.doorName = !nameField.getText().isEmpty() ? nameField.getText() : "new door";
                Minecraft.getMinecraft().displayGuiScreen(new EditDoorPassGUI(editValidator, managerId, door, groups));
            }
        }
    }
    @Override
    public void onGuiClosed() { //done to ensure the perm is removed even if esc pressed
        super.onGuiClosed();
        if(clean)
            return;
        DoorServerRequest packet = new DoorServerRequest(editValidator, "door:" + door.doorId.toString(), managerId,"removeperm", "");
        AdvBaseSecurity.instance.network.sendToServer(packet);
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
}
