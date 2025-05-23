package com.cadergator10.advancedbasesecurity.client.gui.components;

import net.minecraft.util.ResourceLocation;

public class ButtonToggle extends GUIButtonTooltip { //A toggleable button (true/false)
    String buttonName;
    protected ResourceLocation resourceLocation;
    protected boolean stateTriggered;
    protected int xTexStart;
    protected int yTexStart;
    protected int xDiffTex;
    protected int yDiffTex;

    public ButtonToggle(int buttonId, int xIn, int yIn, int widthIn, int heightIn, String buttonText, String tooltip, boolean toggled)
    {
        super(buttonId, xIn, yIn, widthIn, heightIn, buttonText + (toggled ? " (true)" : " (false)"), tooltip);
        buttonName = buttonText;
        this.stateTriggered = toggled;
    }

    public boolean onClick(){
        setStateTriggered(!isStateTriggered());
        return isStateTriggered();
    }

    public void setStateTriggered(boolean toggle)
    {
        this.stateTriggered = toggle;
        displayString = buttonName + (toggle ? " (true)" : " (false)");
    }

    public boolean isStateTriggered()
    {
        return this.stateTriggered;
    }
}
