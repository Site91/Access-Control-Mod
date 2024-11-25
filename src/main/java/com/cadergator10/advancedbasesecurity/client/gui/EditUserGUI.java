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

import javax.jws.soap.SOAPBinding;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class EditUserGUI extends GuiScreen {
    UUID editValidator;
    List<DoorHandler.Doors.Users> users;
    List<DoorHandler.Doors.PassValue> passes;

    DoorHandler.Doors.Users selected;
    //buttons
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

    @Override
    public void initGui() {
        super.initGui();
        int id = -1;
        int xoff = 100;
        int yoff = 60;
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
                restButtons.add(new ButtonToggle(id++, this.width / 2 + xoff, yoff, 80, 16, pass.passName, false));
                restButtonsIDs.add(pass.passId);
            }
            //TODO: finish this
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if(!users.isEmpty())
            drawString(selected.id.toString(),10, 20, 0xFFFFFF);
    }
}
