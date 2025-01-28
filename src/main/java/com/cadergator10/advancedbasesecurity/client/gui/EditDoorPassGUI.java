package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonImg;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonSelect;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonToggle;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.DoorServerRequest;
import com.cadergator10.advancedbasesecurity.common.networking.RequestPassesPacket;
import com.cadergator10.advancedbasesecurity.common.networking.SectControllerPacket;
import com.cadergator10.advancedbasesecurity.util.ButtonTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

@SideOnly(Side.CLIENT)
public class EditDoorPassGUI extends BaseGUI implements GuiPageButtonList.GuiResponder {
	UUID editValidator;
	DoorHandler.DoorIdentifier managerId;
	//if door
	DoorHandler.Doors.OneDoor door;
	//if sector
	BlockPos pos;
	boolean pushChildren;
	boolean toggle;
	List<DoorHandler.Doors.OneDoor.OnePass> overrides;
	DoorHandler.Doors.OneDoor.allDoorStatuses thisStatus;
	//both
	List<ButtonEnum.groupIndex> groups; //just used to preserve it for going back to the other door.
	List<DoorHandler.Doors.PassValue> passes;

	boolean isDoor; //true: door/ false: sectorcontroller

	boolean finished = false;
	boolean letPress = true;
	boolean clean = false;
	//buttons
	int currentIndex;
	//all buttons
	ButtonImg backButton;
	ButtonEnum typeButton;
	ButtonEnum passButton;
	ButtonEnum priority;
	ButtonSelect addPassList;
	ButtonImg selectAddPass;
	GuiTextField passValueI;
	ButtonEnum passValueG;
	//add/del
	ButtonEnum passListButton;
	ButtonImg addPass;
	ButtonImg delPass;
	//sector buttons
	ButtonImg saveButton;
	ButtonToggle pushChildButton;
	ButtonToggle toggleButton;
	ButtonEnum sectorButton;
	ButtonEnum statusButton;

	DoorHandler.Doors.PassValue passSelected;
	private DoorHandler.Doors.OneDoor.OnePass doorPass;

	private static final ResourceLocation background = new ResourceLocation(AdvBaseSecurity.MODID, "textures/gui/basic.png");
	static final int WIDTH = 175;
	static final int HEIGHT = 195;

	//GuiScrollingList passList;
//	List<GuiButton> passList;
//	List<HashMap<String, GuiButton>> allButtons = new LinkedList<>();

	private void formatGroups(HashMap<UUID, DoorHandler.Doors.Groups> sect){
		//use groups from doorhandler. DO NOT CHECK IF NOT SERVER
		groups = new LinkedList<>();
		BiConsumer<UUID, DoorHandler.Doors.Groups> grouper = (s, v) -> groups.add(new ButtonEnum.groupIndex(s.toString(), v.name));
		sect.forEach(grouper);
	}

	public EditDoorPassGUI(UUID editValidator, UUID managerId, DoorHandler.Doors.OneDoor door, List<ButtonEnum.groupIndex> groups){
		super(WIDTH, HEIGHT);
		this.isDoor = true;
		this.editValidator = editValidator;
		this.managerId = new DoorHandler.DoorIdentifier(managerId, door.doorId);
		this.door = door;
		this.groups = groups;
		overrides = null;
		pushChildren = false;
		toggle = false;
		this.pos = null;
		this.thisStatus = null;
	}
	public EditDoorPassGUI(SectControllerPacket packet, BlockPos pos, HashMap<UUID, DoorHandler.Doors.Groups> sectors){
		super(WIDTH, HEIGHT);
		this.isDoor = false;
		this.editValidator = null;
		this.managerId = packet.ids;
		this.door = null;
		formatGroups(sectors); //sets groups list.
		overrides = packet.overrides;
		pushChildren = packet.pushToChildren;
		toggle = packet.toggle;
		this.pos = pos;
		this.thisStatus = packet.thisStatus;
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
	int getPassIndex(String id){
		int index = 0;
		for(DoorHandler.Doors.PassValue pass : passes){
			if(pass.passId.equals(id))
				return index;
			index++;
		}
		return -1;
	}
	DoorHandler.Doors.OneDoor.OnePass getDoorPass(UUID id){
		for(DoorHandler.Doors.OneDoor.OnePass pass : door.passes){
			if(pass.id.equals(id))
				return pass;
		}
		return null;
	}
	int getDoorPassIndex(UUID id){
		int index = 0;
		for(DoorHandler.Doors.OneDoor.OnePass pass : door.passes){
			if(pass.id.equals(id))
				return index;
			index++;
		}
		return -1;
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
	List<ButtonEnum.groupIndex> processPass(List<DoorHandler.Doors.PassValue> passes){
		List<ButtonEnum.groupIndex> btn = new LinkedList<>();
		for(DoorHandler.Doors.PassValue pass : passes){
			btn.add(new ButtonEnum.groupIndex(pass.passId, pass.passName));
		}
		return btn;
	}
	ButtonEnum.groupIndex processDoorPass(DoorHandler.Doors.OneDoor.OnePass pass, boolean add){
		if(add)
			return new ButtonEnum.groupIndex(pass.id.toString(), String.format("%s | %s", getPass(pass.passID).passName, pass.passType.toString()));
		else
			return new ButtonEnum.groupIndex(pass.id.toString(), String.format("%s | %s | %d", getPass(pass.passID).passName, pass.passType.toString(), pass.priority));
	}
	List<ButtonEnum.groupIndex> processDoorPasses(List<DoorHandler.Doors.OneDoor.OnePass> passes){
		List<ButtonEnum.groupIndex> btn = new LinkedList<>();
		for(DoorHandler.Doors.OneDoor.OnePass pass : passes){
			btn.add(processDoorPass(pass, false));
		}
		return btn;
	}
	List<ButtonEnum.groupIndex> processAddPasses(){
		List<ButtonEnum.groupIndex> btn = new LinkedList<>();
		for(DoorHandler.Doors.OneDoor.OnePass pass : door.passes){
			if(pass.passType == DoorHandler.Doors.OneDoor.OnePass.type.Add)
				btn.add(processDoorPass(pass, true));
		}
		return btn;
	}
	List<ButtonEnum.groupIndex> processSectors(){
		return groups;
	}

	@Override
	public void initGui() {
		super.initGui();
		if(!finished)
			passes = new LinkedList<>();
		int id = -1;
		//gen edit buttons first
		int botm = GUITop + HEIGHT - 19;
		this.buttonList.add(backButton = new ButtonImg(id++, GUILeft + 3, botm, ButtonTooltip.Back));
		this.buttonList.add(typeButton = new ButtonEnum(id++, GUILeft + WIDTH - 83, GUITop + 3, 80, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.selectpasstype"), false, Arrays.asList(new ButtonEnum.groupIndex("0", "Supreme"),new ButtonEnum.groupIndex("1", "Base"),new ButtonEnum.groupIndex("2", "Reject"),new ButtonEnum.groupIndex("3", "Add")),0));
		this.buttonList.add(priority = new ButtonEnum(id++, GUILeft + 3, GUITop + 23, 80, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.passpriority"), false, Arrays.asList(new ButtonEnum.groupIndex("1", "1"),new ButtonEnum.groupIndex("2", "2"),new ButtonEnum.groupIndex("3", "3"),new ButtonEnum.groupIndex("4", "4"), new ButtonEnum.groupIndex("5", "5")),0));
		passValueI = new GuiTextField(id++, fontRenderer, GUILeft + WIDTH - 83, GUITop + 23, 80, 16);
		passValueI.setGuiResponder(this);
		//pass modify buttons
		this.buttonList.add(addPass = new ButtonImg(id++, GUILeft + WIDTH - 39, isDoor ? botm : GUITop + 73, ButtonTooltip.AddDoorPass));
		this.buttonList.add(delPass = new ButtonImg(id++, GUILeft + WIDTH - 19, isDoor ? botm : GUITop + 73, ButtonTooltip.DelDoorPass));


		if(!finished) {
			this.buttonList.add(passListButton = new ButtonEnum(id++, GUILeft + WIDTH - 152, botm, 109, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.doorpasseditselect"), false, new LinkedList<>(), 0));
			this.buttonList.add(passButton = new ButtonEnum(id++, GUILeft + 3, GUITop + 3, 80, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.selectpass"), false, new LinkedList<>(),0));
			passListButton.enabled = false;
			addPass.enabled = false;
		}
		else{
			this.buttonList.add(passListButton = new ButtonEnum(id++, GUILeft + WIDTH - 152, isDoor ? botm : GUITop + 73, 109, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.doorpasseditselect"), false, processDoorPasses(door.passes), 0));
			this.buttonList.add(passButton = new ButtonEnum(id++, GUILeft + 3, GUITop + 3, 80, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.selectpass"), false, processPass(passes),0));
		}
		this.buttonList.add(passValueG = new ButtonEnum(id++, GUILeft + WIDTH - 83, GUITop + 23, 80, 16, null, false, new LinkedList<>(),0));
		this.buttonList.add(addPassList = new ButtonSelect(id++, GUILeft + 3, GUITop + 53, WIDTH - 26, 16, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.addpassselect"), new LinkedList<>(),0));
		this.buttonList.add(selectAddPass = new ButtonImg(id++, GUILeft + WIDTH - 19, GUITop + 53, ButtonTooltip.SelectAddPass));
		// if isDoor false, then its sector.
		if(isDoor){
			clean = true;
			this.buttonList.add(saveButton = new ButtonImg(id++, GUILeft + 23, botm, ButtonTooltip.SaveSectors));
			this.buttonList.add(pushChildButton = new ButtonToggle(id++, GUILeft + 3, GUITop + 103, 80, 16, "push updates to all", I18n.translateToLocal("gui.tooltips.advancedbasesecurity.sectorpushchild"), pushChildren));
			this.buttonList.add(toggleButton = new ButtonToggle(id++, GUILeft + WIDTH - 83, GUITop + 103, 80, 16, "toggle", I18n.translateToLocal("gui.tooltips.advancedbasesecurity.sectorconttoggle"), toggle));
			this.buttonList.add(sectorButton = new ButtonEnum(id++, GUILeft + 3, GUITop + 123, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.sectorchoose"), false, processSectors(), 0));
			this.buttonList.add(statusButton = new ButtonEnum(id++, GUILeft + WIDTH - 83, GUITop + 123, I18n.translateToLocal("gui.tooltips.advancedbasesecurity.sectorstatusbtn"), false, Arrays.asList(new ButtonEnum.groupIndex("-2", "No Access"), new ButtonEnum.groupIndex("-1", "Lockdown"), new ButtonEnum.groupIndex("0", "Access"), new ButtonEnum.groupIndex("1", "Overridden Access"), new ButtonEnum.groupIndex("2", "All Access")), 0));
		}

		//set default while waiting for finish
		updateWithPasses();
		//send the packet to request PassValue
		if(!finished) {
			RequestPassesPacket packet = new RequestPassesPacket(managerId.ManagerID, false);
			AdvBaseSecurity.instance.network.sendToServer(packet);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(GUILeft, GUITop, 0, 0, WIDTH, HEIGHT);
		if(finished){
			drawCenteredString("Waiting for pass list from Server", 40, 0xFFFFFF);
		}
		else if(passes.isEmpty() || passSelected == null){
			drawCenteredString("No Pass Selected", 40, 0xFFFFFF);
		}
		else{
			drawCenteredString(passSelected.passName, 40, 0xFFFFFF);
		}
		passValueI.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawHorizontalLine(GUILeft + 2, GUILeft + WIDTH - 2, GUITop + HEIGHT - 22, 6052956); //5c5c5c

	}

	@Override
	public void onGuiClosed() { //done to ensure the perm is removed even if esc pressed
		super.onGuiClosed();
		if(clean)
			return;
		DoorServerRequest packet = new DoorServerRequest(editValidator, "door:" + door.doorId.toString(), managerId.ManagerID,"removeperm", "");
		AdvBaseSecurity.instance.network.sendToServer(packet);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode != Keyboard.KEY_ESCAPE) {
			if(passValueI.isFocused()) {
				passValueI.textboxKeyTyped(typedChar, keyCode);
			}
		}
		else
			super.keyTyped(typedChar, keyCode);
	}
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		passValueI.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void updateWithPasses(){
		if(passes.isEmpty()){
			doorPass = null;
			passButton.enabled = false;
			typeButton.enabled = false;
			priority.enabled = false;
			passValueI.setEnabled(false);
			passValueI.setVisible(true);
			passValueG.enabled = false;
			passValueG.visible = false;
			delPass.enabled = false;
			addPassList.enabled = false;
			addPassList.visible = false;
			selectAddPass.enabled = false;
			selectAddPass.visible = false;
			if(isDoor) {
				saveButton.enabled = false;
				pushChildButton.enabled = false;
				toggleButton.enabled = false;
				sectorButton.enabled = false;
				statusButton.enabled = false;
			}
		}
		else{
			boolean currStatus = isDoor ? true : Math.abs(thisStatus.getInt()) == 1;
			//global stuff
			delPass.enabled = currStatus;
			//get current pass
			doorPass = getDoorPass(UUID.fromString(passListButton.getUUID()));
			if (doorPass != null) {
				passSelected = getPass(doorPass.passID);
				passButton.changeIndex(getPassIndex(doorPass.passID));
				passButton.enabled = currStatus;
				typeButton.changeIndex(doorPass.passType.getInt());
				typeButton.enabled = currStatus;
				priority.changeIndex(doorPass.priority - 1);
				priority.enabled = currStatus;
				DoorHandler.Doors.PassValue pass = getPass(doorPass.passID); //assumed not null
				//handle the requirement to enter stuff.
				if (pass.passType == DoorHandler.Doors.PassValue.type.Group) {
					passValueI.setEnabled(false);
					passValueI.setVisible(false);
					passValueG.changeList(processGroup(pass));
					passValueG.changeIndex(doorPass.passValueI);
					passValueG.enabled = currStatus;
					passValueG.visible = currStatus;
				} else {
					passValueI.setVisible(currStatus);
					passValueG.enabled = false;
					passValueG.visible = false;
					if (pass.passType == DoorHandler.Doors.PassValue.type.Pass) {
						passValueI.setEnabled(false);
						passValueI.setText("no input needed");
					} else if (pass.passType == DoorHandler.Doors.PassValue.type.Level) {
						passValueI.setEnabled(currStatus);
						passValueI.setText(Integer.toString(doorPass.passValueI));
						passValueI.setValidator((s) -> {
							try {
								if (!s.isEmpty()) {
									int i = Integer.parseInt(s);
									return i >= 0 && i < 100;
								} else
									return true;
							} catch (Exception e) {
								return false;
							}
						});
					} else {
						passValueI.setEnabled(currStatus);
						passValueI.setText(doorPass.passValueS);
						passValueI.setValidator((s) -> true);
					}
				}
				//handling add passes
				if (doorPass.passType == DoorHandler.Doors.OneDoor.OnePass.type.Base) {
					if (doorPass.addPasses == null)
						doorPass.addPasses = new LinkedList<>();
					List<ButtonEnum.groupIndex> btns = processAddPasses();
					HashMap<String, Boolean> selected = new HashMap<>();
					for (ButtonEnum.groupIndex btn : btns) {
						DoorHandler.Doors.OneDoor.OnePass passed = getDoorPass(UUID.fromString(btn.id));
						selected.put(btn.id, doorPass.addPasses.contains(passed.id));
					}
					addPassList.changeList(btns, selected);
					addPassList.changeIndex(0);
					addPassList.enabled = currStatus;
					addPassList.visible = currStatus;
					selectAddPass.enabled = currStatus;
					selectAddPass.visible = currStatus;
				} else {
					doorPass.addPasses = null;
					addPassList.enabled = false;
					addPassList.visible = false;
					selectAddPass.enabled = false;
					selectAddPass.visible = false;
				}
			}
		}
		if(isDoor){
			if(managerId.DoorID == null)
				managerId.DoorID = UUID.fromString(sectorButton.getUUID());

		}
	}

	public void finishInit(boolean worked, List<DoorHandler.Doors.PassValue> passes){
		if(worked){
			this.passes = passes;
			//allow editing of all stuff now
			passListButton.changeList(processDoorPasses(door.passes));
			passButton.changeList(processPass(passes));
			//change enabled/disabled
			passListButton.enabled = true;
			addPass.enabled = true;
			updateWithPasses();
			finished = true;
		}
		else{
			AdvBaseSecurity.instance.logger.warn("Failed to retrieve passes: edit validator likely incorrect");
		}
	}

	private void lastMinuteUpdate(){ //updates values with textFieldInputs when leaving a screen
		DoorHandler.Doors.PassValue pass = getPass(doorPass.passID);
		if(pass.passType == DoorHandler.Doors.PassValue.type.Text || pass.passType == DoorHandler.Doors.PassValue.type.MultiText){
			doorPass.passValueS = passValueI.getText();
		}
		else if(pass.passType == DoorHandler.Doors.PassValue.type.Level){
			doorPass.passValueI = !passValueI.getText().isEmpty() ? Integer.parseInt(passValueI.getText()) : 0;
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(letPress){
			if(button == backButton){
				letPress = false;
				clean = true;
				if(isDoor) {
					lastMinuteUpdate();
					Minecraft.getMinecraft().displayGuiScreen(new EditDoorGUI(editValidator, managerId.ManagerID, door, groups));
				}
				else
					mc.player.closeScreen();
			}
			else if(saveButton != null && button == saveButton){
				letPress = false;
				clean = true;
				lastMinuteUpdate();
				SectControllerPacket packet = new SectControllerPacket(managerId, pos, overrides, pushChildren, toggle, thisStatus);
				AdvBaseSecurity.instance.network.sendToServer(packet);
				mc.player.closeScreen();
			}
			else if(pushChildButton != null && button == pushChildButton){
				pushChildren = pushChildButton.onClick();
			}
			else if(toggleButton != null && button == toggleButton){
				toggle = toggleButton.onClick();
			}
			else if(sectorButton != null && button == sectorButton){
				sectorButton.onClick();
				managerId.DoorID = UUID.fromString(sectorButton.getUUID());
			}
			else if(statusButton != null && button == statusButton){
				statusButton.onClick();
				thisStatus = DoorHandler.Doors.OneDoor.allDoorStatuses.fromInt(Integer.parseInt(statusButton.getUUID()));
				updateWithPasses();
			}
			else if(button == typeButton){
				typeButton.onClick();
				 doorPass.passType = DoorHandler.Doors.OneDoor.OnePass.type.fromInt(Integer.parseInt(typeButton.getUUID()));
				 passListButton.changeCurrentName(processDoorPass(doorPass, false).name);
				 updateWithPasses();
			}
			else if(button == passButton){
				clean = true;
				passButton.onClick();
				doorPass.passID = passButton.getUUID();
				//get pass
				DoorHandler.Doors.PassValue pass = getPass(doorPass.passID);
				if(pass.passType == DoorHandler.Doors.PassValue.type.Level) {
					doorPass.passValueI = 1;
					doorPass.passValueS = null;
				}
				else if(pass.passType == DoorHandler.Doors.PassValue.type.Group){
					doorPass.passValueI = 0;
					doorPass.passValueS = null;
				}
				else if(pass.passType == DoorHandler.Doors.PassValue.type.Text || pass.passType == DoorHandler.Doors.PassValue.type.MultiText){
					doorPass.passValueI = -1;
					doorPass.passValueS = "";
				}
				else{
					doorPass.passValueI = -1;
					doorPass.passValueS = null;
				}
				passListButton.changeCurrentName(processDoorPass(doorPass, false).name);
				updateWithPasses();
			}
			else if(button == priority){
				priority.onClick();
				doorPass.priority = Short.parseShort(priority.getUUID());
				passListButton.changeCurrentName(processDoorPass(doorPass, false).name);
			}
			else if(button == passValueG){
				passValueG.onClick();
				doorPass.passValueI = Integer.parseInt(passValueG.getUUID());
			}
			else if(button == addPassList){
				addPassList.onClick();
			}
			else if(button == selectAddPass){
				addPassList.toggleValue();
				if(addPassList.getToggle()){
					doorPass.addPasses.add(UUID.fromString(addPassList.getUUID()));
				}
				else{
					UUID per = UUID.fromString(addPassList.getUUID());
					doorPass.addPasses.removeIf((s) -> s.equals(per));
				}
			}
			else if(button == passListButton){
				lastMinuteUpdate();
				passListButton.onClick();
				updateWithPasses();
			}
			else if(button == addPass){
				lastMinuteUpdate();
				DoorHandler.Doors.OneDoor.OnePass pass = new DoorHandler.Doors.OneDoor.OnePass();
				pass.id = UUID.randomUUID();
				pass.passID = "staff";
				pass.passType = DoorHandler.Doors.OneDoor.OnePass.type.Supreme;
				pass.priority = 3;
				pass.addPasses = null;
				pass.passValueI = -1;
				pass.passValueS = null;
				door.passes.add(pass);
				passListButton.insertList(processDoorPass(pass, false));
				passListButton.changeIndex(door.passes.size() - 1);
				updateWithPasses();
			}
			else if(button == delPass){
				int index = getDoorPassIndex(UUID.fromString(passListButton.getUUID()));
				AdvBaseSecurity.instance.logger.info("id: " + passListButton.getUUID());
				door.passes.remove(index);
				passListButton.removeList();
				updateWithPasses();
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

	}
}
