package com.cadergator10.advancedbasesecurity.client.gui.components;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.translation.I18n;

public class GUITextFieldTooltip extends GuiTextField implements ITooltip {
    String replacers;
    public GUITextFieldTooltip(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height, String tooltip) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
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
        return this.getVisible();
    }
}
