package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonImg;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.PassEditPacket;
import com.cadergator10.advancedbasesecurity.util.ButtonTooltip;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

public class EditPassGUI extends GuiScreen implements GuiPageButtonList.GuiResponder {
    UUID editValidator;
    UUID managerId;
    HashMap<String, DoorHandler.Doors.PassValue> passes;
    DoorHandler.Doors.PassValue pass;

    ButtonImg saveButton;
    ButtonEnum passList;
    ButtonImg addPass;
    ButtonImg delPass;
    GuiTextField nameInput;
    ButtonEnum typeInput;
    GuiTextField groupInput;

    public EditPassGUI(UUID editValidator, UUID managerId, HashMap<String, DoorHandler.Doors.PassValue> passes){
        this.editValidator = editValidator;
        this.managerId = managerId;
        this.passes = passes;
    }

    void drawString(String string, int x, int y, int color){
        FontRenderer fr = mc.fontRenderer;
        fr.drawString(string, x, y, color);
    }


    void drawCenteredString(String string, int y, int color){
        drawString(string, this.width/2 - mc.fontRenderer.getStringWidth(string)/2, y, color);
    }

    List<ButtonEnum.groupIndex> processGroup(DoorHandler.Doors.PassValue pass){
        List<ButtonEnum.groupIndex> btn = new LinkedList<>();
        if(pass.passType == DoorHandler.Doors.PassValue.type.Group && pass.groupNames != null){
            for(int i=0; i<pass.groupNames.size(); i++){
                btn.add(new ButtonEnum.groupIndex(Integer.toString(i), pass.groupNames.get(i)));
            }
            return btn;
        }
        return null;
    }
    ButtonEnum.groupIndex processPass(DoorHandler.Doors.PassValue pass){
        return new ButtonEnum.groupIndex(pass.passId, pass.passName);
    }
    List<ButtonEnum.groupIndex> processPasses(HashMap<String, DoorHandler.Doors.PassValue> passes){
        List<ButtonEnum.groupIndex> btn = new LinkedList<>();
        BiConsumer<String, DoorHandler.Doors.PassValue> biConsumer = (k, v) -> {
            if (!k.equals("staff"))
                btn.add(processPass(v));
        };
        passes.forEach(biConsumer);
        return btn;
    }

    @Override
    public void initGui() {
        super.initGui();
        int id = -1;
        this.buttonList.add(saveButton = new ButtonImg(id++, this.width / 2 - 45, this.height - (this.height / 4) + 10, ButtonTooltip.SavePasses));
        nameInput = new GuiTextField(id++, fontRenderer, this.width / 2 - 50, 20, 100, 16);
        nameInput.setGuiResponder(this);
        this.buttonList.add(typeInput = new ButtonEnum(id++, this.width / 2 - 50, 40, 100, 16, false, Arrays.asList(new ButtonEnum.groupIndex("0", "Pass"),new ButtonEnum.groupIndex("1", "Level"),new ButtonEnum.groupIndex("2", "Group"),new ButtonEnum.groupIndex("3", "Text"),new ButtonEnum.groupIndex("4", "Multi-Text")),0));
        groupInput = new GuiTextField(id++, fontRenderer, this.width / 2 - 50, 60, 100, 16);
        groupInput.setGuiResponder(this);

        this.buttonList.add(passList = new ButtonEnum(id++, this.width / 2 - 60, 80, 120, 16, false, processPasses(passes),0));
        this.buttonList.add(addPass = new ButtonImg(id++, this.width / 2 - 70, 100, ButtonTooltip.AddDoorPass));
        this.buttonList.add(delPass = new ButtonImg(id++, this.width / 2 + 10, 100, ButtonTooltip.DelDoorPass));
        updateWithPasses(false);
    }

    public void updateWithPasses(boolean safe){
        if(passes.size() - 1 <= 0){
            AdvBaseSecurity.instance.logger.info("Disabled all pass editing");
            pass = null;
            delPass.enabled = false;
            passList.enabled = false;
            nameInput.setEnabled(false);
            typeInput.enabled = false;
            groupInput.setEnabled(false);
            groupInput.setVisible(false);
        }
        else{
            AdvBaseSecurity.instance.logger.info("Enabled all pass editing");
            if(!safe)
                pass = passes.get(passList.getUUID());
            if (pass != null) {
                if(!safe){
                    delPass.enabled = true;
                    passList.enabled = true;
                    nameInput.setEnabled(true);
                    nameInput.setText(pass.passName);
                    typeInput.enabled = true;
                    typeInput.changeIndex(pass.passType.getInt());
                }
                if(pass.passType == DoorHandler.Doors.PassValue.type.Group) {
                    groupInput.setEnabled(true);
                    groupInput.setVisible(true);
                    groupInput.setText(String.join(",", pass.groupNames));
                }
                else{
                    groupInput.setEnabled(false);
                    groupInput.setVisible(false);
                }
            }
        }
    }

    public void lastMinuteUpdate(){
        if(pass != null){
            pass.passName = !nameInput.getText().isEmpty() ? nameInput.getText() : "new pass";
            if(pass.passType == DoorHandler.Doors.PassValue.type.Group){
                pass.groupNames = Arrays.asList(groupInput.getText().split(","));
            }
            else{
                pass.groupNames = null;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        nameInput.drawTextBox();
        groupInput.drawTextBox();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode != Keyboard.KEY_ESCAPE) {
            if(nameInput.isFocused()) {
                nameInput.textboxKeyTyped(typedChar, keyCode);
            }
            else if(groupInput.isFocused()) {
                groupInput.textboxKeyTyped(typedChar, keyCode);
            }
        }
        else
            super.keyTyped(typedChar, keyCode);
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameInput.mouseClicked(mouseX, mouseY, mouseButton);
        groupInput.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button == saveButton){
            lastMinuteUpdate();
            PassEditPacket packet = new PassEditPacket(editValidator, managerId, passes);
            AdvBaseSecurity.instance.network.sendToServer(packet);
            mc.player.closeScreen();
        }
        else if(button == passList){
            lastMinuteUpdate();
            passList.onClick();
            updateWithPasses(false);
        }
        else if(button == addPass){
            lastMinuteUpdate();
            DoorHandler.Doors.PassValue passd = new DoorHandler.Doors.PassValue();
            passd.passId = UUID.randomUUID().toString();
            passd.passType = DoorHandler.Doors.PassValue.type.Pass;
            passd.passName = "new pass";
            passd.groupNames = null;
            passes.put(passd.passId, passd);
            passList.insertList(processPass(passd));
            passList.changeIndex(passes.size() - 1);
            updateWithPasses(false);
        }
        else if(button == delPass){
            passes.remove(pass.passId);
            passList.removeList();
            updateWithPasses(false);
        }
        else if(button == typeInput){
            typeInput.onClick();
            pass.passType = DoorHandler.Doors.PassValue.type.fromInt(typeInput.getIndex());
            lastMinuteUpdate();
            updateWithPasses(true);
        }
        AdvBaseSecurity.instance.logger.info(pass.toString());
    }

    @Override
    public void setEntryValue(int id, boolean value) {

    }

    @Override
    public void setEntryValue(int id, float value) {

    }

    @Override
    public void setEntryValue(int id, String value) {
        if(id == nameInput.getId()){
            passList.changeCurrentName(value);
        }
    }
}
