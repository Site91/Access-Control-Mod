package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonSelect;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.RequestPassesPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public class EditDoorPassGUI extends GuiScreen implements GuiPageButtonList.GuiResponder {
	UUID editValidator;
	UUID managerId;
	DoorHandler.Doors.OneDoor door;
	List<ButtonEnum.groupIndex> groups; //just used to preserve it for going back to the other door.
	List<DoorHandler.Doors.PassValue> passes;
	boolean finished = false;
	boolean letPress = true;
	//buttons
	int currentIndex;
	GuiButton backButton;
	ButtonEnum typeButton;
	ButtonEnum passButton;
	ButtonEnum priority;
	ButtonSelect addPassList;
	GuiButton selectAddPass;
	GuiTextField passValueI;
	ButtonEnum passValueG;
	//add/del
	ButtonEnum passListButton;
	GuiButton addPass;
	GuiButton delPass;
	DoorHandler.Doors.PassValue passSelected;
	private DoorHandler.Doors.OneDoor.OnePass doorPass;

	//GuiScrollingList passList;
//	List<GuiButton> passList;
//	List<HashMap<String, GuiButton>> allButtons = new LinkedList<>();


	public EditDoorPassGUI(UUID editValidator, UUID managerId, DoorHandler.Doors.OneDoor door, List<ButtonEnum.groupIndex> groups){
		this.editValidator = editValidator;
		this.managerId = managerId;
		this.door = door;
		this.groups = groups;
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

	@Override
	public void initGui() {
		super.initGui();
		passes = new LinkedList<>();
		int id = -1;
		//gen edit buttons first
		this.buttonList.add(backButton = new GuiButton(id++, this.width / 2 - 45, this.height - (this.height / 4) + 10, 90, 16, "Back"));
		this.buttonList.add(passButton = new ButtonEnum(id++, this.width / 2 - 20, 60, 80, 16, false, new LinkedList<>(),0));
		this.buttonList.add(typeButton = new ButtonEnum(id++, this.width / 2 + 120, 60, 80, 16, false, Arrays.asList(new ButtonEnum.groupIndex("0", "Supreme"),new ButtonEnum.groupIndex("1", "Base"),new ButtonEnum.groupIndex("2", "Reject"),new ButtonEnum.groupIndex("3", "Add")),0));
		this.buttonList.add(priority = new ButtonEnum(id++, this.width / 2 - 20, 80, 80, 16, false, Arrays.asList(new ButtonEnum.groupIndex("1", "1"),new ButtonEnum.groupIndex("2", "2"),new ButtonEnum.groupIndex("3", "3"),new ButtonEnum.groupIndex("4", "4"), new ButtonEnum.groupIndex("5", "5")),0));
		passValueI = new GuiTextField(id++, fontRenderer, this.width / 2 + 120, 80, 80, 16);
		passValueI.setGuiResponder(this);
		this.buttonList.add(passValueG = new ButtonEnum(id++, this.width / 2 + 120, 80, 80, 16, false, new LinkedList<>(),0));
		this.buttonList.add(addPassList = new ButtonSelect(id++, this.width / 2 - 20, 100, 80, 16, new LinkedList<>(),0));
		this.buttonList.add(selectAddPass = new GuiButton(id++, this.width / 2 + 120, 100, 80, 16, "Toggle Add Pass"));
		//pass modify buttons
		this.buttonList.add(passListButton = new ButtonEnum(id++, this.width / 2 - 200, 80, 120, 16, false, new LinkedList<>(),0));
		this.buttonList.add(addPass = new GuiButton(id++, this.width / 2 + 80, 100, 30, 16, "Add Pass"));
		this.buttonList.add(delPass = new GuiButton(id++, this.width / 2 + 120, 100, 30, 16, "Delete Pass"));
		//set default while waiting for finish
		updateWithPasses();
		passListButton.enabled = false;
		addPass.enabled = false;
		//send the packet to request PassValue
		RequestPassesPacket packet = new RequestPassesPacket(editValidator, managerId, false);
		AdvBaseSecurity.instance.network.sendToServer(packet);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
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
		}
		else{
			//global stuff
			delPass.enabled = true;
			//get current pass
			doorPass = getDoorPass(UUID.fromString(passListButton.getUUID()));
			if(doorPass != null){
				passSelected = getPass(doorPass.passID);
				passButton.changeIndex(getPassIndex(doorPass.passID));
				passButton.enabled = true;
				typeButton.changeIndex(doorPass.passType.getInt());
				typeButton.enabled = true;
				priority.changeIndex(doorPass.priority - 1);
				priority.enabled = true;
				DoorHandler.Doors.PassValue pass = getPass(doorPass.passID); //assumed not null
				//handle the requirement to enter stuff.
				if(pass.passType == DoorHandler.Doors.PassValue.type.Group){
					passValueI.setEnabled(false);
					passValueI.setVisible(false);
					passValueG.changeList(processGroup(pass));
					passValueG.changeIndex(doorPass.passValueI);
					passValueG.enabled = true;
					passValueG.visible = true;
				}
				else{
					passValueI.setVisible(true);
					passValueG.enabled = false;
					passValueG.visible = false;
					if(pass.passType == DoorHandler.Doors.PassValue.type.Pass){
						passValueI.setEnabled(false);
						passValueI.setText("no input needed");
					}
					else if(pass.passType == DoorHandler.Doors.PassValue.type.Level){
						passValueI.setEnabled(true);
						passValueI.setText(Integer.toString(doorPass.passValueI));
						passValueI.setValidator((s) -> {
							try{
								if(!s.isEmpty()) {
									int i = Integer.parseInt(s);
									return i >= 0 && i < 100;
								}
								else
									return true;
							}
							catch(Exception e){
								return false;
							}
						});
					}
					else{
						passValueI.setEnabled(true);
						passValueI.setText(doorPass.passValueS);
						passValueI.setValidator((s) -> true);
					}
				}
				//handling add passes
				if(doorPass.passType == DoorHandler.Doors.OneDoor.OnePass.type.Base) {
					if(doorPass.addPasses == null)
						doorPass.addPasses = new LinkedList<>();
					List<ButtonEnum.groupIndex> btns = processAddPasses();
					HashMap<String, Boolean> selected = new HashMap<>();
					for (ButtonEnum.groupIndex btn : btns) {
						DoorHandler.Doors.OneDoor.OnePass passed = getDoorPass(UUID.fromString(btn.id));
						selected.put(btn.id, doorPass.addPasses.contains(passed.id));
					}
					addPassList.changeList(btns, selected);
					addPassList.changeIndex(0);
					addPassList.enabled = true;
					addPassList.visible = true;
					selectAddPass.enabled = true;
					selectAddPass.visible = true;
				}
				else{
					doorPass.addPasses = null;
					addPassList.enabled = false;
					addPassList.visible = false;
					selectAddPass.enabled = false;
					selectAddPass.visible = false;
				}
			}
		}
	}

	public void finishInit(boolean worked, UUID editValidator, List<DoorHandler.Doors.PassValue> passes){
		if(worked){
			this.editValidator = editValidator;
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
				lastMinuteUpdate();
				Minecraft.getMinecraft().displayGuiScreen(new EditDoorGUI(editValidator, managerId, door, groups));
			}
			else if(button == typeButton){
				typeButton.onClick();
				 doorPass.passType = DoorHandler.Doors.OneDoor.OnePass.type.fromInt(Integer.parseInt(typeButton.getUUID()));
				 passListButton.changeCurrentName(processDoorPass(doorPass, false).name);
				 updateWithPasses();
			}
			else if(button == passButton){
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
