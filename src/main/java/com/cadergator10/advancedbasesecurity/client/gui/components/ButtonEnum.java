package com.cadergator10.advancedbasesecurity.client.gui.components;

import net.minecraftforge.fml.client.config.GuiButtonExt;

import java.util.List;

public class ButtonEnum extends GuiButtonExt{
	int currentIndex = 0;
	int size = 0;
	boolean includeNone;
	List<groupIndex> map;

	public ButtonEnum(int buttonId, int x, int y, boolean includeNone, List<groupIndex> map, int index) {
		super(buttonId, x, y, includeNone && !map.isEmpty() ? map.get(index).name : "none");
		this.includeNone = includeNone;
		this.size = includeNone ? map.size() + 1 : map.size();
		this.currentIndex = index;
		this.map = map;
	}

	public ButtonEnum(int buttonId, int x, int y, int widthIn, int heightIn, boolean includeNone, List<groupIndex> map, int index) {
		super(buttonId, x, y, widthIn, heightIn, includeNone && !map.isEmpty() ? map.get(index).name : "none");
		this.includeNone = includeNone;
		this.size = includeNone ? map.size() + 1 : map.size();
		this.currentIndex = index;
		this.map = map;
	}

	protected void updateDisplay(){
		if(includeNone && currentIndex == 0 || map.isEmpty())
			displayString = "none";
		else{
			displayString = map.get(includeNone ? currentIndex - 1 : currentIndex).name;
		}
	}

	public void onClick(){
		currentIndex++;
		if(currentIndex >= size) {
			currentIndex = 0;
		}
		updateDisplay();
	}

	public void changeIndex(int index){
		currentIndex = Math.max(Math.min(index,size - 1),0);
		updateDisplay();
	}

	public void changeList(List<groupIndex> map){
		this.map = map;
		size = includeNone ? map.size() + 1 : map.size();
		changeIndex(currentIndex); //make sure it's still within the proper bounds
	}

	public void changeCurrentName(String newOne){
		if(!includeNone || currentIndex != 0) {
			this.map.get(includeNone ? currentIndex - 1 : currentIndex).name = newOne;
			updateDisplay();
		}
	}

	public void insertList(groupIndex ind){
		this.map.add(ind);
		size = includeNone ? map.size() + 1 : map.size();
	}
	public void removeList(int index){
		if(index < size && index > (includeNone ? 0 : -1)) {
			this.map.remove(includeNone ? index - 1 : index);
			size = includeNone ? map.size() + 1 : map.size();
			changeIndex(currentIndex);
		}
	}
	public void removeList(){
		if(!includeNone || currentIndex != 0) {
			this.map.remove(currentIndex);
			size = includeNone ? map.size() + 1 : map.size();
			changeIndex(currentIndex);
		}
	}

	public String getUUID(){
		if((!includeNone || currentIndex != 0) && !map.isEmpty())
			return map.get(includeNone ? currentIndex - 1 : currentIndex).id;
		return "none";
	}

	public int getIndex(){
		return currentIndex;
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
