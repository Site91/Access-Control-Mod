package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonImg;
import com.cadergator10.advancedbasesecurity.client.gui.components.GUITextFieldTooltip;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.DoorServerRequest;
import com.cadergator10.advancedbasesecurity.common.networking.SectorEditPacket;
import com.cadergator10.advancedbasesecurity.util.ButtonTooltip;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

public class EditSectorGUI extends BaseGUI implements GuiPageButtonList.GuiResponder {
    UUID editValidator;
    UUID managerId;
    HashMap<UUID, DoorHandler.Doors.Groups> sectors;
    List<ButtonEnum.groupIndex> sectorsProcessed;
    DoorHandler.Doors.Groups sector;
    boolean clean = false;
    ButtonImg saveButton;
    ButtonEnum sectorList;
    ButtonImg addSector;
    ButtonImg delSector;
    GUITextFieldTooltip nameInput;
    ButtonEnum parentInput;

    private static final ResourceLocation background = new ResourceLocation(AdvBaseSecurity.MODID, "textures/gui/basic.png");
    static final int WIDTH = 175;
    static final int HEIGHT = 195;

    public EditSectorGUI(UUID editValidator, UUID managerId, HashMap<UUID, DoorHandler.Doors.Groups> sectors){
        super(WIDTH, HEIGHT);
        this.editValidator = editValidator;
        this.managerId = managerId;
        this.sectors = sectors;
    }

    void drawString(String string, int x, int y, int color){
        FontRenderer fr = mc.fontRenderer;
        fr.drawString(string, x, y, color);
    }


    void drawCenteredString(String string, int y, int color){
        drawString(string, this.width/2 - mc.fontRenderer.getStringWidth(string)/2, y, color);
    }

    int sectorIndex(UUID id){
        if(id != null){
            for(int i=0; i<sectorsProcessed.size(); i++){
                if(sectorsProcessed.get(i).id.equals(id.toString())){
                    return i + 1;
                }
            }
        }
        return 0;
    }

    ButtonEnum.groupIndex processSector(DoorHandler.Doors.Groups v){
        return new ButtonEnum.groupIndex(v.id.toString(), v.name);
    }

    void processSectors(HashMap<UUID, DoorHandler.Doors.Groups> sectors){
        List<ButtonEnum.groupIndex> btn = new LinkedList<>();
        BiConsumer<UUID, DoorHandler.Doors.Groups> biConsumer = (k, v) -> {
            btn.add(processSector(v));
        };
        sectors.forEach(biConsumer);
        sectorsProcessed = btn;
    }

    public static boolean infiniteLoopCheck(HashMap<UUID, DoorHandler.Doors.Groups> map, List<UUID> currentList){ //Check if the parents loop at all and return true if alright and false if looping
        boolean cascade = true;
        if(currentList == null) {
            currentList = new LinkedList<>();
            cascade = false;
        }
        if(!cascade)
            for (UUID id : map.keySet()) {
                DoorHandler.Doors.Groups currentOne = map.get(id);
                if(currentList.contains(currentOne.id))
                    return false;
                List<UUID> newList = currentList.subList(0, currentList.size());
                if (currentOne.parentID != null) {
                    newList.add(currentOne.id);
                    if (!infiniteLoopCheck(map, newList))
                        return false;
                }
            }
        else{
            DoorHandler.Doors.Groups currentOne = map.get(currentList.get(currentList.size() - 1));
            if(currentOne.parentID == null)
                return true;
            if(currentList.contains(currentOne.parentID))
                return false;
            else {
                currentList.add(currentOne.parentID);
                return infiniteLoopCheck(map, currentList.subList(0, currentList.size()));
            }
        }
        return true;
    }

    @Override
    public void initGui() {
        super.initGui();
        int id = -1;
        int botm = GUITop + HEIGHT - 19;
        this.buttonList.add(saveButton = new ButtonImg(id++, GUILeft + WIDTH - 19, botm, ButtonTooltip.SaveSectors));
        nameInput = new GUITextFieldTooltip(id++, fontRenderer, GUILeft + 3, GUITop + 3, WIDTH - 6, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.sectorname"));
        nameInput.setGuiResponder(this);
        this.buttonList.add(addSector = new ButtonImg(id++, GUILeft + WIDTH - 39, GUITop + 63, ButtonTooltip.AddSector));
        this.buttonList.add(delSector = new ButtonImg(id++, GUILeft + WIDTH - 19, GUITop + 63, ButtonTooltip.DelSector));
        processSectors(sectors);
        this.buttonList.add(sectorList = new ButtonEnum(id++, GUILeft + 3, GUITop + 63, WIDTH - 46, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.allsectors"), false, sectorsProcessed, 0));
        this.buttonList.add(parentInput = new ButtonEnum(id++, GUILeft + 3, GUITop + 23, WIDTH - 46, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.parentsector"), true, sectorsProcessed, 0));
        updateWithPasses(false);
    }

    public void updateWithPasses(boolean safe){
        if(sectors.size() <= 0){
            AdvBaseSecurity.instance.logger.info("Disabled all sector editing");
            sector = null;
            delSector.enabled = false;
            sectorList.enabled = false;
            nameInput.setEnabled(false);
            parentInput.enabled = false;
        }
        else{
            AdvBaseSecurity.instance.logger.info("Enabled all sector editing");
            if(!safe)
                sector = sectors.get(!Objects.equals(sectorList.getUUID(), "none") ? UUID.fromString(sectorList.getUUID()) : null);
            if (sector != null) {
                if(!safe){
                    delSector.enabled = true;
                    sectorList.enabled = true;
                    nameInput.setEnabled(true);
                    nameInput.setText(sector.name);
                    parentInput.enabled = true;
                    parentInput.changeIndex(sectorIndex(sector.parentID));
                }
            }
        }
    }

    public void lastMinuteUpdate(){
        if(sector != null){
            sector.name = !nameInput.getText().isEmpty() ? nameInput.getText() : "new sector";
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(GUILeft, GUITop, 0, 0, WIDTH, HEIGHT);
        nameInput.drawTextBox();
        drawHorizontalLine(GUILeft + 2, GUILeft + WIDTH - 2, GUITop + 61, 14408667); //5c5c5c
        super.drawScreen(mouseX, mouseY, partialTicks);
        processField(nameInput, mouseX, mouseY);
    }

    @Override
    public void onGuiClosed() { //done to ensure the perm is removed even if esc pressed
        super.onGuiClosed();
        if(clean)
            return;
        DoorServerRequest packet = new DoorServerRequest(editValidator, "sectors", managerId,"removeperm", "");
        AdvBaseSecurity.instance.network.sendToServer(packet);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode != Keyboard.KEY_ESCAPE) {
            if(nameInput.isFocused()) {
                nameInput.textboxKeyTyped(typedChar, keyCode);
            }
        }
        else
            super.keyTyped(typedChar, keyCode);
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameInput.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button == saveButton){
            clean = infiniteLoopCheck(sectors, null);
            if(!clean){
                AdvBaseSecurity.instance.logger.info("There is an infinite loop among the sectors. Ensure no sectors loop on each other through the Parent Sectors!");
                return;
            }
            lastMinuteUpdate();
            SectorEditPacket packet = new SectorEditPacket(editValidator, managerId, sectors);
            AdvBaseSecurity.instance.network.sendToServer(packet);
            mc.player.closeScreen();
        }
        else if(button == sectorList){
            lastMinuteUpdate();
            sectorList.onClick();
            updateWithPasses(false);
        }
        else if(button == addSector){
            lastMinuteUpdate();
            DoorHandler.Doors.Groups passd = new DoorHandler.Doors.Groups();
            passd.id = UUID.randomUUID();
            passd.parentID = null;
            passd.name = "new sector";
            passd.override = null;
            passd.status = DoorHandler.Doors.OneDoor.allDoorStatuses.ACCESS;
            sectors.put(passd.id, passd);
            processSectors(sectors);
            sectorList.changeList(sectorsProcessed);
            sectorList.changeIndex(sectorIndex(passd.id));
            parentInput.changeList(sectorsProcessed);
            parentInput.changeIndex(0);
            updateWithPasses(false);
        }
        else if(button == delSector){
            sectors.remove(sector.id);
            sectorList.removeList();
            parentInput.removeList(sectorIndex(sector.id));
            processSectors(sectors);
            updateWithPasses(false);
        }
        else if(button == parentInput){
            parentInput.onClick();
            UUID id = !Objects.equals(parentInput.getUUID(), "none") ? UUID.fromString(parentInput.getUUID()) : null;
            if(id != null && UUID.fromString(sectorList.getUUID()).equals(id)){
                parentInput.onClick();
                id = !Objects.equals(parentInput.getUUID(), "none") ? UUID.fromString(parentInput.getUUID()) : null;
            }
            sector.parentID = id;
            updateWithPasses(true);
        }
        AdvBaseSecurity.instance.logger.info(sector.toString());
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
            sectorList.changeCurrentName(value);
        }
    }
}
