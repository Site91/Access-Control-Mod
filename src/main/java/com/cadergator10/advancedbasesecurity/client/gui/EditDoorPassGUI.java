package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class EditDoorPassGUI extends GuiScreen {
	UUID editValidator;
	DoorHandler.Doors.OneDoor door;
	List<ButtonEnum.groupIndex> groups; //just used to preserve it for going back to the other door.
	boolean letPress = true;
	//buttons
	GuiButton backButton;
	GuiScrollingList passList;
	List<HashMap<String, GuiButton>> allButtons = new LinkedList<>();


	public EditDoorPassGUI(UUID editValidator, DoorHandler.Doors.OneDoor door, List<ButtonEnum.groupIndex> groups){
		this.editValidator = editValidator;
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

	@Override
	public void initGui() {
		super.initGui();
		int id = -1;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(letPress){
			if(button == backButton){
				letPress = false;
				Minecraft.getMinecraft().displayGuiScreen(new EditDoorGUI(editValidator, door, groups));
			}
		}
	}
}
