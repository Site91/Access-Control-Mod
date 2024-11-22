package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonToggle;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.DoorServerRequest;
import com.cadergator10.advancedbasesecurity.common.networking.OneDoorDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.Int;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class EditDoorGUI extends GuiScreen {
    //data passed by packet
    DoorHandler.Doors.OneDoor door;
    List<ButtonEnum.groupIndex> groups;
    int groupIndex = 0;

    UUID editValidator;
    //button data
    GuiButton backButton;
    GuiButton saveButton;
    //GUI buttons
    GuiButton editPasses;
    //door values
    GuiTextField nameField;
    ButtonToggle toggleDoor;
    GuiButton doorDelayUp;
    GuiButton doorDelayDown;
    GuiTextField doorDelayInput;
    ButtonEnum groupSelect;
    GuiButton clearDevices;

    //other data
    boolean letPress;
    public EditDoorGUI(UUID editValidator, DoorHandler.Doors.OneDoor door, List<ButtonEnum.groupIndex> groups) {
        super();
        this.editValidator = editValidator;
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
        this.buttonList.add(backButton = new GuiButton(id++, this.width / 2 - 100, this.height - (this.height / 4) + 10, 90, 16, "Back"));
        this.buttonList.add(saveButton = new GuiButton(id++, this.width / 2 + 100, this.height - (this.height / 4) + 10, 90, 16, "Save"));
        
        nameField = new GuiTextField(id++, fontRenderer, this.width / 2 - 100, 20, 60, 16);
        this.buttonList.add(groupSelect = new ButtonEnum(id++, this.width / 2 + 100, 20, 80, 16, true, groups, groupIndex));
        this.buttonList.add(editPasses = new GuiButton(id++, this.width / 2 - 100, 40, 60, 16, "Edit passes"));
        this.buttonList.add(clearDevices = new GuiButton(id++, this.width / 2 + 100, 40, 60, 16, "Clear " + (door.Readers.size() + door.Doors.size()) + " Devices"));
        this.buttonList.add(toggleDoor = new ButtonToggle(id++, this.width / 2 - 100, 60, 80, 16, "Stay Open", door.defaultToggle));
        this.buttonList.add(doorDelayDown = new GuiButton(id++, this.width / 2 + 20, 60, 16, 16, "<"));
        this.buttonList.add(doorDelayUp = new GuiButton(id++, this.width / 2 + 80, 60, 16, 16, ">"));
        doorDelayInput = new GuiTextField(id++, fontRenderer, this.width / 2 + 50, 60, 40, 16);
        doorDelayInput.setText(Integer.toString(door.defaultTick));
        doorDelayInput.setValidator((r) -> {
            try{
                int num = Integer.parseInt(r);
                return num >= 0 && num <= 60;
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
                door.defaultTick = Integer.parseInt(doorDelayInput.getText());
                door.doorName = !nameField.getText().isEmpty() ? nameField.getText() : "new door";
                OneDoorDataPacket packet = new OneDoorDataPacket(editValidator, door, false);
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
                doorDelayInput.setText(Integer.toString(Math.max(0, Integer.parseInt(doorDelayInput.getText()) - 1)));
            }
            else if(button == doorDelayUp){
                doorDelayInput.setText(Integer.toString(Math.min(60, Integer.parseInt(doorDelayInput.getText()) + 1)));
            }
            else if(button == toggleDoor){
                door.defaultToggle = toggleDoor.isStateTriggered();
            }
            else if(button == editPasses){
                letPress = false;
                Minecraft.getMinecraft().displayGuiScreen(new EditDoorPassGUI(editValidator, door, groups));
            }
        }
    }
}
