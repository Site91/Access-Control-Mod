package com.cadergator10.advancedbasesecurity.client.gui.components;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GUIButtonTooltip extends GuiButtonExt implements ITooltip{
    private String replacers;

    public GUIButtonTooltip(int buttonId, int x, int y, String buttonText, String tooltip) {
        super(buttonId, x, y, buttonText);
        this.replacers = tooltip;
    }
    public GUIButtonTooltip(int buttonId, int x, int y, int width, int height, String buttonText, String tooltip) {
        super(buttonId, x, y, width, height, buttonText);
        this.replacers = tooltip;
    }


    @Override
    public String getMessage() {
        if(replacers != null)
            return replacers;
        return null;
    }

    @Override
    public int xPos() {
        return this.x;
    }

    @Override
    public int yPos() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }
}
