package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.PassEditPacket;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

public class EditPassGUI extends GuiScreen {
    UUID editValidator;
    HashMap<String, DoorHandler.Doors.PassValue> passes;
    DoorHandler.Doors.PassValue pass;

    GuiButton saveButton;
    ButtonEnum passList;
    GuiButton addPass;
    GuiButton delPass;
    GuiTextField nameInput;
    ButtonEnum typeInput;
    GuiTextField groupInput;

    public EditPassGUI(UUID editValidator, HashMap<String, DoorHandler.Doors.PassValue> passes){
        this.editValidator = editValidator;
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
        BiConsumer<String, DoorHandler.Doors.PassValue> biConsumer = (k, v) -> btn.add(processPass(v));
        passes.forEach(biConsumer);
        return btn;
    }

    @Override
    public void initGui() {
        super.initGui();
        int id = -1;
        this.buttonList.add(saveButton = new GuiButton(id++, this.width / 2, this.height - (this.height / 4) + 10, 90, 16, "Save"));
        nameInput = new GuiTextField(id++, fontRenderer, this.width / 2 , 20, 100, 16);
        this.buttonList.add(typeInput = new ButtonEnum(id++, this.width / 2 + 100, 40, 100, 16, false, Arrays.asList(new ButtonEnum.groupIndex("0", "Pass"),new ButtonEnum.groupIndex("1", "Level"),new ButtonEnum.groupIndex("2", "Group"),new ButtonEnum.groupIndex("3", "Text"),new ButtonEnum.groupIndex("4", "Multi-Text")),0));
        groupInput = new GuiTextField(id++, fontRenderer, this.width / 2 , 20, 100, 16);

        this.buttonList.add(passList = new ButtonEnum(id++, this.width / 2 - 150, 80, 120, 16, false, processPasses(passes),0));
        this.buttonList.add(addPass = new GuiButton(id++, this.width / 2 + 30, 100, 30, 16, "Add Pass"));
        this.buttonList.add(delPass = new GuiButton(id++, this.width / 2 + 70, 100, 30, 16, "Delete Pass"));
    }

    public void updateWithPasses(){
        if(passes.isEmpty()){
            pass = null;
            delPass.enabled = false;
            passList.enabled = false;
            nameInput.setEnabled(false);
            typeInput.enabled = false;
            groupInput.setEnabled(false);
            groupInput.setVisible(false);
        }
        else{
            pass = passes.get(passList.getUUID());
            if (pass != null) {
                delPass.enabled = true;
                passList.enabled = true;
                nameInput.setEnabled(true);
                nameInput.setText(pass.passName);
                typeInput.enabled = true;
                typeInput.changeIndex(pass.passType.getInt());
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
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button == saveButton){
            lastMinuteUpdate();
            PassEditPacket packet = new PassEditPacket(editValidator, passes);
            AdvBaseSecurity.instance.network.sendToServer(packet);
        }
        else if(button == passList){
            lastMinuteUpdate();
            passList.onClick();
            updateWithPasses();
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
            updateWithPasses();
        }
        else if(button == delPass){
            passes.remove(pass.passId);
            passList.removeList();
            updateWithPasses();
        }
        else if(button == typeInput){
            typeInput.onClick();
            pass.passType = DoorHandler.Doors.PassValue.type.fromInt(typeInput.getIndex());
        }
    }
}
