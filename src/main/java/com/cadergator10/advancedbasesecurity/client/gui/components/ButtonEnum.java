package com.cadergator10.advancedbasesecurity.client.gui.components;

import net.minecraftforge.fml.client.config.GuiButtonExt;

import java.util.List;
import java.util.UUID;

public class ButtonEnum extends GuiButtonExt{
	int currentIndex = 0;
	int size = 0;
	List<groupIndex> map;

	public ButtonEnum(int buttonId, int x, int y, List<groupIndex> map, int index) {
		super(buttonId, x, y, "none");
		this.size = map.size();
		this.currentIndex = index;
		this.map = map;
	}

	public ButtonEnum(int buttonId, int x, int y, int widthIn, int heightIn, List<groupIndex> map, int index) {
		super(buttonId, x, y, widthIn, heightIn, "none");
		this.size = map.size();
		this.currentIndex = index;
		this.map = map;
	}

	public void onClick(){
		currentIndex++;
		if(currentIndex > size) {
			currentIndex = 0;
			displayString = "none";
		}
		else{
			displayString = map.get(currentIndex - 1).name;
		}
	}

	public String getUUID(){
		if(currentIndex != 0)
			return map.get(currentIndex - 1).id;
		return "none";
	}

	public static class groupIndex{
		public String id;
		public String name;
		public groupIndex(String id, String name){
			this.id = id;
			this.name = name;
		}
	}
}
