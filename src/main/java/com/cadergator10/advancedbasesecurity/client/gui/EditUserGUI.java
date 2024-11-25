package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonToggle;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.swing.ToggleButton;

import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class EditUserGUI extends GuiScreen {
    UUID editValidator;
    List<DoorHandler.Doors.Users> users;
    List<DoorHandler.Doors.PassValue> passes;
    boolean letPress = false;

    DoorHandler.Doors.Users user;

    DoorHandler.Doors.Users selected;
    //buttons
    GuiButton saveButton;
    ButtonEnum allUsers;
    GuiButton addUser;
    GuiButton delUser;
    GuiButton resetID;
    GuiTextField nameField;
    ButtonToggle staffButton;
    ButtonToggle blockedButton;
    List<GuiButton> restButtons;
    List<String> restButtonsIDs;
    List<GuiTextField> fields;
    List<String> fieldIDs;

    public EditUserGUI(UUID editValidator, List<DoorHandler.Doors.Users> users, List<DoorHandler.Doors.PassValue> passes){
        this.editValidator = editValidator;
        this.users = users;
        this.passes = passes;
//        for(int i=0; i<passes.size(); i++){
//            if(!user.passes.containsKey(passes.get(i).passId)){
//
//            }
//        }
    }

    void drawString(String string, int x, int y, int color){
        FontRenderer fr = mc.fontRenderer;
        fr.drawString(string, x, y, color);
    }

    void drawCenteredString(String string, int y, int color){
        drawString(string, this.width/2 - mc.fontRenderer.getStringWidth(string)/2, y, color);
    }

    DoorHandler.Doors.PassValue getPass(String id){
        for(DoorHandler.Doors.PassValue pass : passes){
            if(pass.passId.equals(id))
                return pass;
        }
        return null;
    }

    ButtonEnum.groupIndex processUser(DoorHandler.Doors.Users user){
        return new ButtonEnum.groupIndex(user.id.toString(), user.name);
    }

    List<ButtonEnum.groupIndex> processUsers(){
        List<ButtonEnum.groupIndex> btn = new LinkedList<>();
        for(DoorHandler.Doors.Users user : users){
            btn.add(processUser(user));
        }
        return btn;
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

    @Override
    public void initGui() {
        super.initGui();
        int id = -1;
        int xoff = 100;
        int yoff = 60;
        this.buttonList.add(saveButton = new GuiButton(id++, this.width / 2, this.height - (this.height / 4) + 10, 90, 16, "Save & Exit"));
        this.buttonList.add(allUsers = new ButtonEnum(id++, this.width / 2 - 200, 20, 80, 16, false, processUsers(), 0));
        this.buttonList.add(addUser = new GuiButton(id++, this.width / 2 - 240, 40, 30, 16, "Add Pass"));
        this.buttonList.add(delUser = new GuiButton(id++, this.width / 2 - 160, 40, 30, 16, "Delete Pass"));
        nameField = new GuiTextField(id++, fontRenderer, this.width / 2 - 100, 20, 80, 16);
        this.buttonList.add(staffButton = new ButtonToggle(id++, this.width / 2 - 100, 40, 80, 16, "Stay Open", false));
        this.buttonList.add(resetID =  new GuiButton(id++, this.width / 2 + 100, 40, 80, 16, "reset ID"));
        this.buttonList.add(blockedButton = new ButtonToggle(id++, this.width / 2 - 100, 60, 80, 16, "Blocked", false));
        //add to lists
        restButtons = new LinkedList<>();
        restButtonsIDs = new LinkedList<>();
        fields = new LinkedList<>();
        fieldIDs = new LinkedList<>();
        for(DoorHandler.Doors.PassValue pass : passes){
            if(pass.passType == DoorHandler.Doors.PassValue.type.Pass){
                ButtonToggle button = new ButtonToggle(id++, this.width / 2 + xoff, yoff, 80, 16, pass.passName, false);
                restButtons.add(button);
                buttonList.add(button);
                restButtonsIDs.add(pass.passId);
            }
            else if(pass.passType == DoorHandler.Doors.PassValue.type.Group){
                ButtonEnum button = new ButtonEnum(id++, this.width / 2 + xoff, yoff, 80, 16, true, processGroup(pass), 0);
                restButtons.add(button);
                buttonList.add(button);
                restButtonsIDs.add(pass.passId);
            }
            else{
                GuiTextField field = new GuiTextField(id++, fontRenderer, this.width / 2 + xoff, yoff, 80, 16);
                if(pass.passType == DoorHandler.Doors.PassValue.type.Level){
                    field.setValidator((s) -> {
                        try{
                            int i = Integer.parseInt(s);
                            return i >= 0 && i <= 99;
                        }
                        catch(Exception e){
                            return false;
                        }
                    });
                }
                fields.add(field);
                fieldIDs.add(pass.passId);
            }
        }
    }

    private void updateWithPasses(){
        if(passes.isEmpty()){
            allUsers.enabled = false;
            delUser.enabled = false;
            nameField.setEnabled(false);
            staffButton.enabled = false;
            resetID.enabled = false;
            blockedButton.enabled = false;
            for(GuiButton button : restButtons){
                button.enabled = false;
            }
            for(GuiTextField field : fields){
                field.setEnabled(false);
            }
        }
        else{
            allUsers.enabled = true;
            delUser.enabled = true;
            user = users.get(allUsers.getIndex());
            if(user != null){
                nameField.setEnabled(true);
                nameField.setText(user.name);
                staffButton.enabled = true;
                staffButton.setStateTriggered(user.staff);
                resetID.enabled = true;
                blockedButton.enabled = true;
                blockedButton.setStateTriggered(user.blocked);
                DoorHandler.Doors.PassValue pass;
                for(int i=0; i<restButtons.size(); i++){
                    if((pass = getPass(restButtonsIDs.get(i))) != null){
                        GuiButton button = restButtons.get(i);
                        if(pass.passType == DoorHandler.Doors.PassValue.type.Pass){
                            ((ButtonToggle)button).setStateTriggered(user.passes.get(pass.passId).passValue.get(0).equals("true"));
                        }
                        else{ //type==Group
                            ((ButtonEnum)button).changeIndex(Integer.parseInt(user.passes.get(pass.passId).passValue.get(0)));
                        }
                        button.enabled = true;
                    }
                }
                for(int i=0; i<fields.size(); i++){
                    if((pass = getPass(fieldIDs.get(i))) != null){
                        GuiTextField field = fields.get(i);
                        if(pass.passType == DoorHandler.Doors.PassValue.type.MultiText){
                            field.setText(String.join(",", user.passes.get(pass.passId).passValue));
                        }
                        else{
                            field.setText(user.passes.get(pass.passId).passValue.get(0));
                        }
                        field.setEnabled(true);
                    }
                }
            }
        }
    }

    private void finishLastMinute(){
        if(user != null){
            user.name = nameField.getText().isEmpty() ? nameField.getText() : "new";
            for(int i=0; i<fields.size(); i++){
                DoorHandler.Doors.PassValue pass;
                if((pass = getPass(fieldIDs.get(i))) != null){
                    GuiTextField field = fields.get(i);
                    if(pass.passType == DoorHandler.Doors.PassValue.type.MultiText){
                        user.passes.get(pass.passId).passValue = Arrays.asList(field.getText().split(","));
                    }
                    else{
                        user.passes.get(pass.passId).passValue = new LinkedList<>();
                        user.passes.get(pass.passId).passValue.add(field.getText());
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if(!users.isEmpty())
            drawString(selected.id.toString(),10, 20, 0xFFFFFF);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(letPress){
            if(button == saveButton){ //TODO: Finish the User gui

            }
        }
    }
}
