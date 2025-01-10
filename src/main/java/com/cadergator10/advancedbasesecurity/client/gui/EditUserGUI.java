package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.*;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.inventory.doorManagerContainer;
import com.cadergator10.advancedbasesecurity.common.networking.DoorServerRequest;
import com.cadergator10.advancedbasesecurity.common.networking.UserEditPacket;
import com.cadergator10.advancedbasesecurity.util.ButtonTooltip;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.client.GuiScrollingList;
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
    boolean clean = false;

    DoorHandler.Doors.Users user;
    //buttons
    ButtonImg saveButton;
    ButtonEnum allUsers;
    ButtonImg addUser;
    ButtonImg delUser;
    ButtonImg resetID;
    ButtonImg writeCard;
    GUITextFieldTooltip nameField;
    ButtonToggle staffButton;
    ButtonToggle blockedButton;
    Slider slider;
    List<GuiButton> restButtons;
    List<String> restButtonsIDs;
    List<GUITextFieldTooltip> fields;
    List<String> fieldIDs;
    HashMap<String, Integer> level; //level in the list with slider that it is shown

    int id = -1;
    int yoff = 55;
    float yPos; //current slider value
    int buttonCount; //amount of buttons
    int space = 18; //space between each button
    int currOffset; //calculated from yPos & space, the current position used by the buttons.
    int currOffsetNormalized;
    int lastOffsetNormal;
    int shown = 2; //max amount shown.

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
        this.buttonList.add(addUser = new ButtonImg(id++, guiLeft + 86, guiTop + 3, ButtonTooltip.AddUser));
        this.buttonList.add(saveButton = new ButtonImg(id++, guiLeft - 19, guiTop + 23, ButtonTooltip.SaveUsers));
        if(!finished){
            this.buttonList.add(allUsers = new ButtonEnum(id++, guiLeft - 85, guiTop + 3, 80, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.allusers"), false, new LinkedList<>(), 0));
            allUsers.enabled = false;
            addUser.enabled = false;
            saveButton.enabled = false;
        }
        else{
            this.buttonList.add(allUsers = new ButtonEnum(id++, guiLeft - 85, guiTop + 3, 80, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.allusers"), false, processUsers(), 0));
        }
        this.buttonList.add(slider = new Slider(this, id++, this.width / 2 + 3, guiTop + 52, 20, 60, 0, 0.99999f, 0));
        this.buttonList.add(writeCard = new ButtonImg(id++, guiLeft + 116, guiTop + 65, ButtonTooltip.WriteCard));
        this.buttonList.add(allUsers = new ButtonEnum(id++, guiLeft - 85, guiTop + 3, 80, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.allusers"), false, new LinkedList<>(), 0));
        this.buttonList.add(delUser = new ButtonImg(id++, guiLeft + 107, guiTop + 3, ButtonTooltip.DelUser));
        nameField = new GUITextFieldTooltip(id++, fontRenderer, guiLeft + 3, guiTop + 3, 80, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.username"));
        nameField.setGuiResponder(this);
        this.buttonList.add(staffButton = new ButtonToggle(id++, guiLeft + 5, guiTop + 35, 80, 16, "Staff", I18n.translateToLocal("gui.tooltips.advancedbasesecurity.staffbutton"), false));
        this.buttonList.add(resetID =  new ButtonImg(id++, guiLeft + WIDTH - 19, guiTop + 3, ButtonTooltip.ResetUUID));
        this.buttonList.add(blockedButton = new ButtonToggle(id++, this.width / 2 + 5, guiTop + 35, 80, 16, "Blocked", I18n.translateToLocal("gui.tooltips.advancedbasesecurity.blockbutton"), false));
        level = new HashMap<>();
        buttonCount = 0;
        //set default while waiting to finish
        updateWithPasses();
        delUser.enabled = false;
        //no user data here yet so request with packet
        if(!finished) {
            UUID ide = ((doorManagerContainer) inventorySlots).getManager();
            DoorServerRequest packet = new DoorServerRequest(ide, "getuserdata", "");
            AdvBaseSecurity.instance.network.sendToServer(packet);
        }
        else{
            finishButtons();
        }
    }

    @Override
    public void onGuiClosed() { //done to ensure the perm is removed even if esc pressed
        super.onGuiClosed();
        if(clean)
            return;
        DoorServerRequest packet = new DoorServerRequest(editValidator, "useredit", ((doorManagerContainer) inventorySlots).getManager(),"removeperm", "");
        AdvBaseSecurity.instance.network.sendToServer(packet);
    }

    private void finishButtons(){
        //add to lists
        restButtons = new LinkedList<>();
        restButtonsIDs = new LinkedList<>();
        fields = new LinkedList<>();
        fieldIDs = new LinkedList<>();
        int leveler = 0;
        for(DoorHandler.Doors.PassValue pass : passes){
            if(!pass.passId.equals("staff")) {
                buttonCount++;
                if (pass.passType == DoorHandler.Doors.PassValue.type.Pass) {
                    ButtonToggle button = new ButtonToggle(id++, guiLeft + 5, yoff, 80, 16, pass.passName, null, false);
                    restButtons.add(button);
                    buttonList.add(button);
                    restButtonsIDs.add(pass.passId);
                } else if (pass.passType == DoorHandler.Doors.PassValue.type.Group) {
                    ButtonEnum button = new ButtonEnum(id++, guiLeft + 5, yoff, 80, 16, pass.passName, true, processGroup(pass), 0);
                    restButtons.add(button);
                    buttonList.add(button);
                    restButtonsIDs.add(pass.passId);
                } else {
                    GUITextFieldTooltip field = new GUITextFieldTooltip(id++, fontRenderer, guiLeft + 5, yoff, 80, 16, pass.passName);
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
                level.put(pass.passId, leveler);
                //update offsets
                yoff += space;
                leveler++;
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
                checkSlider();
            }
        }
    }

    public void checkSlider(){
        for(int i=0; i<restButtonsIDs.size(); i++){
            int leveler = level.get(restButtonsIDs.get(i));
            GuiButton button = restButtons.get(i);
            if(currOffsetNormalized + shown >= leveler && leveler >= currOffsetNormalized){ //show
                button.enabled = true;
                button.visible = true;
                button.y = guiTop + 55 + ((leveler * 20) - currOffset);
            }
            else{
                button.enabled = false;
                button.visible = false;
            }
        }
        for(int i=0; i<fields.size(); i++){
            int leveler = level.get(fieldIDs.get(i));
            GuiTextField field = fields.get(i);
            if(currOffsetNormalized + shown >= leveler && leveler >= currOffsetNormalized){ //show
                field.setEnabled(true);
                field.setVisible(true);
                field.y = guiTop + 55 + ((leveler * 20) - currOffset);
            }
            else{
                field.setEnabled(false);
                field.setVisible(false);
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
        if(users != null && !users.isEmpty())
            drawString("UUID: " + user.id.toString(),guiLeft + 10, guiTop + 24, 0xFFFFFF, 0.5F);
        nameField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
        if(fields != null) {
            for (GuiTextField field : fields)
                field.drawTextBox();
        }
        for( final Object c : this.buttonList )
        {
            if( c instanceof ITooltip)
            {
                this.drawTooltip( (ITooltip) c, mouseX, mouseY );
            }
        }
        if(fields != null) {
            for (final Object c : this.fields) {
                if (c instanceof ITooltip) {
                    this.drawTooltip((ITooltip) c, mouseX, mouseY);
                }
            }
        }
        processField(nameField, mouseX, mouseY);
    }

    private void drawTooltip(ITooltip tooltip, int mouseX, int mouseY )
    {
        final int x = tooltip.xPos(); // ((GuiImgButton) c).x;
        int y = tooltip.yPos(); // ((GuiImgButton) c).y;

        if( x < mouseX && x + tooltip.getWidth() > mouseX && tooltip.isVisible() )
        {
            if( y < mouseY && y + tooltip.getHeight() > mouseY )
            {
                if( y < 15 )
                {
                    y = 15;
                }

                final String msg = tooltip.getMessage();
                if( msg != null )
                {
                    this.drawTooltip( x + 11, y + 4, msg );
                }
            }
        }
    }

    protected void drawTooltip( int x, int y, String message )
    {
        String[] lines = message.split( "/n" );
        this.drawTooltip( x, y, Arrays.asList( lines ) );
    }

    protected void drawTooltip( int x, int y, List<String> lines )
    {
        if( lines.isEmpty() )
        {
            return;
        }

        // For an explanation of the formatting codes, see http://minecraft.gamepedia.com/Formatting_codes
        lines = Lists.newArrayList( lines ); // Make a copy

        // Make the first line white
        lines.set( 0, TextFormatting.WHITE + lines.get( 0 ) );

        // All lines after the first are colored gray
        for( int i = 1; i < lines.size(); i++ )
        {
            lines.set( i, TextFormatting.GRAY + lines.get( i ) );
        }

        this.drawHoveringText( lines, x, y, this.fontRenderer );
    }

    public void processField(ITooltip field, int mouseX, int mouseY){
        this.drawTooltip( (ITooltip) field, mouseX, mouseY );
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
                clean = true;
                finishLastMinute();
                UserEditPacket packet = new UserEditPacket(true, editValidator, users, ((doorManagerContainer)inventorySlots).getManager());
                AdvBaseSecurity.instance.network.sendToServer(packet);
                mc.player.closeScreen();
            }
            else if(button == writeCard){
                DoorServerRequest packet = new DoorServerRequest( editValidator, "useredit", ((doorManagerContainer)inventorySlots).getManager(), "producecard", user.id.toString());
                AdvBaseSecurity.instance.network.sendToServer(packet);
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
        if(id == slider.id){
            yPos = value;
            currOffsetNormalized = (int)Math.floor(value * Math.max(1, buttonCount - shown));
            currOffset = currOffsetNormalized * space;
            if(lastOffsetNormal != currOffsetNormalized){
                lastOffsetNormal = currOffsetNormalized;
                checkSlider();
            }
        }
    }

    @Override
    public void setEntryValue(int id, String value) {
        if(id == nameField.getId()){
            allUsers.changeCurrentName(value);
        }
    }
}
