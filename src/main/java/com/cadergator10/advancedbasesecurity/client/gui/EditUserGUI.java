package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonImg;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonToggle;
import com.cadergator10.advancedbasesecurity.client.gui.components.ContainerGUI;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.inventory.doorManagerContainer;
import com.cadergator10.advancedbasesecurity.common.networking.DoorServerRequest;
import com.cadergator10.advancedbasesecurity.common.networking.UserEditPacket;
import com.cadergator10.advancedbasesecurity.util.ButtonTooltip;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public class EditUserGUI extends ContainerGUI implements GuiPageButtonList.GuiResponder {

    UUID editValidator = null;

    List<DoorHandler.Doors.Users> users;
    List<DoorHandler.Doors.PassValue> passes;
    //inventorySlots is container
    boolean letPress = true;
    boolean finished = false;

    DoorHandler.Doors.Users user;
    //buttons
    ButtonImg saveButton;
    ButtonEnum allUsers;
    ButtonImg addUser;
    ButtonImg delUser;
    ButtonImg resetID;
    ButtonImg writeCard;
    GuiTextField nameField;
    ButtonToggle staffButton;
    ButtonToggle blockedButton;
    List<GuiButton> restButtons;
    List<String> restButtonsIDs;
    List<GuiTextField> fields;
    List<String> fieldIDs;

    int id = -1;
    int xoff = -70;
    int yoff = 60;

    public static final int WIDTH = 175;
    public static final int HEIGHT = 195;

    private static final ResourceLocation background = new ResourceLocation(AdvBaseSecurity.MODID, "textures/gui/writecard.png");

    public EditUserGUI(doorManagerContainer container){
        super(container, WIDTH, HEIGHT);
        this.users = null;
        this.passes = null;
//        this.users = users;
//        this.passes = passes;
//        for(int i=0; i<passes.size(); i++){
//            if(!user.passes.containsKey(passes.get(i).passId)){
//
//            }
//        }
    }

    void drawString(String string, int x, int y, int color, double scale){
        GL11.glScaled(scale, scale, scale);
        FontRenderer fr = mc.fontRenderer;
        double reverse = 1/scale;
        fr.drawString(string, (int) (x * reverse), (int) (y * reverse), color);
        GL11.glScaled(reverse, reverse, reverse);
    }

    void drawCenteredString(String string, int y, int color, double scale){
        drawString(string, this.width/2 - mc.fontRenderer.getStringWidth(string)/2, y, color, scale);
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
        this.buttonList.add(saveButton = new ButtonImg(id++, guiLeft + 116, guiTop + 65, ButtonTooltip.SaveUsers));
        this.buttonList.add(allUsers = new ButtonEnum(id++, guiLeft + 1, guiTop + 1, 80, 16, false, new LinkedList<>(), 0));
        this.buttonList.add(addUser = new ButtonImg(id++, guiLeft + 86, guiTop + 1, ButtonTooltip.AddUser));
        this.buttonList.add(delUser = new ButtonImg(id++, guiLeft + 107, guiTop + 1, ButtonTooltip.DelUser));
        nameField = new GuiTextField(id++, fontRenderer, guiLeft + 128, guiTop + 1, 80, 16);
        nameField.setGuiResponder(this);
        this.buttonList.add(staffButton = new ButtonToggle(id++, guiLeft + 1, guiTop + 41, 80, 16, "Staff", false));
        this.buttonList.add(resetID =  new ButtonImg(id++, guiLeft + WIDTH - 17, guiTop + 21, ButtonTooltip.ResetUUID));
        this.buttonList.add(blockedButton = new ButtonToggle(id++, this.width / 2 + 1, guiTop + + 41, 100, 16, "Blocked", false));
        //set default while waiting to finish
        updateWithPasses();
        allUsers.enabled = false;
        addUser.enabled = false;
        delUser.enabled = false;
        saveButton.enabled = false;
        //no user data here yet so request with packet
        UUID ide = ((doorManagerContainer)inventorySlots).getManager();
        DoorServerRequest packet = new DoorServerRequest(ide, "getuserdata", "");
        AdvBaseSecurity.instance.network.sendToServer(packet);
    }

    private void finishButtons(){
        //add to lists
        restButtons = new LinkedList<>();
        restButtonsIDs = new LinkedList<>();
        fields = new LinkedList<>();
        fieldIDs = new LinkedList<>();
        for(DoorHandler.Doors.PassValue pass : passes){
            if(!pass.passId.equals("staff")) {
                if (pass.passType == DoorHandler.Doors.PassValue.type.Pass) {
                    ButtonToggle button = new ButtonToggle(id++, this.width / 2 - (50 + xoff), yoff, 100, 16, pass.passName, false);
                    restButtons.add(button);
                    buttonList.add(button);
                    restButtonsIDs.add(pass.passId);
                } else if (pass.passType == DoorHandler.Doors.PassValue.type.Group) {
                    ButtonEnum button = new ButtonEnum(id++, this.width / 2 - (50 + xoff), yoff, 100, 16, true, processGroup(pass), 0);
                    restButtons.add(button);
                    buttonList.add(button);
                    restButtonsIDs.add(pass.passId);
                } else {
                    GuiTextField field = new GuiTextField(id++, fontRenderer, this.width / 2 - (50 + xoff), yoff, 100, 16);
                    field.setGuiResponder(this);
                    if (pass.passType == DoorHandler.Doors.PassValue.type.Level) {
                        field.setValidator((s) -> {
                            try {
                                if (!s.isEmpty()) {
                                    int i = Integer.parseInt(s);
                                    return i >= 0 && i <= 99;
                                } else
                                    return true;
                            } catch (Exception e) {
                                return false;
                            }
                        });
                    }
                    fields.add(field);
                    fieldIDs.add(pass.passId);
                }
                //update offsets
                xoff *= -1;
                if (xoff > 0)
                    yoff += 20;
            }
        }
    }

    private void updateWithPasses(){
        if(users == null || users.isEmpty()){
            allUsers.enabled = false;
            delUser.enabled = false;
            nameField.setEnabled(false);
            staffButton.enabled = false;
            resetID.enabled = false;
            blockedButton.enabled = false;
            if(restButtons != null && fields != null) {
                for (GuiButton button : restButtons) {
                    button.enabled = false;
                }
                for (GuiTextField field : fields) {
                    field.setEnabled(false);
                }
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

    public void finishInit(boolean worked, UUID editValidator, List<DoorHandler.Doors.Users> users, List<DoorHandler.Doors.PassValue> passes)
    {
        if(worked){
            this.editValidator = editValidator;
            this.users = users;
            this.passes = passes;
            //finish buttons
            allUsers.changeList(processUsers());
            finishButtons();
            //allow edit of users now
            allUsers.enabled = true;
            addUser.enabled = true;
            delUser.enabled = true;
            saveButton.enabled = true;
            updateWithPasses();
            finished = true;
        }
        else{
            AdvBaseSecurity.instance.logger.warn("Failed to retrieve users: might not be authenticated or already in use?");
        }
    }

    private void finishLastMinute(){
        if(user != null){
            user.name = !nameField.getText().isEmpty() ? nameField.getText() : "new";
            for(int i=0; i<fields.size(); i++){
                DoorHandler.Doors.PassValue pass;
                if((pass = getPass(fieldIDs.get(i))) != null){
                    GuiTextField field = fields.get(i);
                    if(pass.passType == DoorHandler.Doors.PassValue.type.MultiText){
                        user.passes.get(pass.passId).passValue = Arrays.asList(field.getText().split(","));
                    }
                    else{
                        user.passes.get(pass.passId).passValue = new LinkedList<>();
                        user.passes.get(pass.passId).passValue.add(field.getText().isEmpty() && pass.passType == DoorHandler.Doors.PassValue.type.Level ? "0" : field.getText());
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if(users != null && !users.isEmpty())
            drawString(user.id.toString(),guiLeft + 10, guiTop + 25, 0xFFFFFF, 0.5F);
        nameField.drawTextBox();
        if(fields != null) {
            for (GuiTextField field : fields)
                field.drawTextBox();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect( guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode != Keyboard.KEY_ESCAPE) {
            if(nameField.isFocused()) {
                nameField.textboxKeyTyped(typedChar, keyCode);
            }
            else {
                for(GuiTextField field : fields)
                    if(field.isFocused()){
                        field.textboxKeyTyped(typedChar, keyCode);
                        break;
                    }
            }
        }
        else
            super.keyTyped(typedChar, keyCode);
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        if(fields != null) {
            for (GuiTextField field : fields)
                field.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(letPress){
            if(button == saveButton){ //TODO: Finish the User gui
                finishLastMinute();
                UserEditPacket packet = new UserEditPacket(true, editValidator, users, ((doorManagerContainer)inventorySlots).getManager());
                AdvBaseSecurity.instance.network.sendToServer(packet);
                mc.player.closeScreen();
            }
            else if(button == allUsers){
                finishLastMinute();
                allUsers.onClick();
                updateWithPasses();
            }
            else if(button == addUser){
                finishLastMinute();
                DoorHandler.Doors.Users user1 = new DoorHandler.Doors.Users();
                user1.name = "new";
                user1.blocked = false;
                user1.owner = null;
                user1.id = UUID.randomUUID();
                user1.staff = false;
                user1.passes = new HashMap<>();
                for (DoorHandler.Doors.PassValue pass : passes){ //check for incorrect inputs.
                    user1.passes.put(pass.passId, new DoorHandler.Doors.Users.UserPass(pass.passId,pass.passType == DoorHandler.Doors.PassValue.type.Level || pass.passType == DoorHandler.Doors.PassValue.type.Group ? Arrays.asList("0") : pass.passType == DoorHandler.Doors.PassValue.type.Pass ? Arrays.asList("false") : Arrays.asList("") , pass.passType.getInt()));
                }
                users.add(user1);
                ButtonEnum.groupIndex btn = processUser(user1);
                allUsers.insertList(btn);
                allUsers.changeIndex(users.size() - 1);
                updateWithPasses();
            }
            else if(button == delUser){
                int index = allUsers.getIndex();
                users.remove(index);
                allUsers.removeList();
                updateWithPasses();
            }
            else if(button == resetID){
                user.id = UUID.randomUUID();
            }
            else if(button == staffButton){
                user.staff = staffButton.onClick();
            }
            else if(button == blockedButton){
                user.blocked = blockedButton.onClick();
            }
            else if(buttonList != null){ //go through restButtons
                for(int i=0; i<restButtonsIDs.size(); i++){
                    if(button == restButtons.get(i)){
                        if(button instanceof ButtonToggle){ //pass
                            user.passes.get(restButtonsIDs.get(i)).passValue.set(0, Boolean.toString(((ButtonToggle)button).onClick()));
                        }
                        else{ //group
                            ((ButtonEnum)button).onClick();
                            user.passes.get(restButtonsIDs.get(i)).passValue.set(0,Integer.toString(((ButtonEnum)button).getIndex()));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setEntryValue(int id, boolean value) {

    }

    @Override
    public void setEntryValue(int id, float value) {

    }

    @Override
    public void setEntryValue(int id, String value) {
        if(id == nameField.getId()){
            allUsers.changeCurrentName(value);
        }
    }
}
