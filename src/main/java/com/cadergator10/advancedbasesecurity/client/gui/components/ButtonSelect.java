package com.cadergator10.advancedbasesecurity.client.gui.components;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ButtonSelect extends ButtonEnum{
    List<Boolean> isToggled;

    public ButtonSelect(int buttonId, int x, int y, String tooltip, List<groupIndex> map, int index) {
        super(buttonId, x, y, tooltip,false, map, index);
        isToggled = new LinkedList<>();
        for(int i=0; i<size; i++)
            isToggled.add(false);
    }
    public ButtonSelect(int buttonId, int x, int y, int widthIn, int heightIn, String tooltip, List<groupIndex> map, int index) {
        super(buttonId, x, y, widthIn, heightIn, tooltip, false, map, index);
        isToggled = new LinkedList<>();
        for(int i=0; i<size; i++)
            isToggled.add(false);
    }

    @Override
    protected void updateDisplay() {
        if(!map.isEmpty())
            displayString = " (" + (isToggled.get(currentIndex) ? " )" : "X)") + map.get(currentIndex).name;
        else
            displayString = "none";
    }

    @Override
    public void changeList(List<groupIndex> map){
        isToggled = new LinkedList<>();
        for(int i=0; i<map.size(); i++)
            isToggled.add(false);
        super.changeList(map);
    }
    public void changeList(List<groupIndex> map, HashMap<String, Boolean> toggles){
        isToggled = new LinkedList<>();
        for(int i=0; i<map.size(); i++)
            isToggled.add(toggles.getOrDefault(map.get(i).id, false));
        super.changeList(map);
    }

    public void toggleValue(){ //flip the true/false
        isToggled.set(currentIndex, !isToggled.get(currentIndex));
        updateDisplay();
    }
    public void toggleValue(boolean val){ //flip the true/false
        isToggled.set(currentIndex, val);
        updateDisplay();
    }

    public void changeToggles(HashMap<String, Boolean> toggles){
        for(int i=0; i<map.size(); i++){
            if(toggles.containsKey(map.get(i).id)){
                isToggled.set(i, toggles.get(map.get(i).id));
            }
        }
        updateDisplay();
    }

    @Override
    public void insertList(groupIndex ind) {
        this.isToggled.add(false);
        super.insertList(ind);
    }
    public void insertList(groupIndex ind, boolean val){
        this.isToggled.add(val);
        super.insertList(ind);
    }
    public boolean getToggle(){
        return isToggled.get(currentIndex);
    }

    @Override
    public void removeList() {
        this.isToggled.remove(currentIndex);
        super.removeList();
    }

    public List<String> getSelections(){
        List<String> strs= new LinkedList<>();
        for(int i=0; i<isToggled.size(); i++){
            if(isToggled.get(i))
                strs.add(map.get(i).id);
        }
        return strs;
    }
}
