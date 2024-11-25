package com.cadergator10.advancedbasesecurity.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonToggle extends GuiButton {
    protected ResourceLocation resourceLocation;
    protected boolean stateTriggered;
    protected int xTexStart;
    protected int yTexStart;
    protected int xDiffTex;
    protected int yDiffTex;

    public ButtonToggle(int buttonId, int xIn, int yIn, int widthIn, int heightIn, String buttonText, boolean toggled)
    {
        super(buttonId, xIn, yIn, widthIn, heightIn, buttonText);
        this.stateTriggered = toggled;
    }

    public void initTextureValues(int xTexStartIn, int yTexStartIn, int xDiffTexIn, int yDiffTexIn, ResourceLocation resourceLocationIn)
    {
        this.xTexStart = xTexStartIn;
        this.yTexStart = yTexStartIn;
        this.xDiffTex = xDiffTexIn;
        this.yDiffTex = yDiffTexIn;
        this.resourceLocation = resourceLocationIn;
    }

    public boolean onClick(){
        setStateTriggered(isStateTriggered());
        return isStateTriggered();
    }

    public void setStateTriggered(boolean p_191753_1_)
    {
        this.stateTriggered = p_191753_1_;
    }

    public boolean isStateTriggered()
    {
        return this.stateTriggered;
    }

    public void setPosition(int p_191752_1_, int p_191752_2_)
    {
        this.x = p_191752_1_;
        this.y = p_191752_2_;
    }

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            mc.getTextureManager().bindTexture(this.resourceLocation);
            GlStateManager.disableDepth();
            int i = this.xTexStart;
            int j = this.yTexStart;

            if (this.stateTriggered)
            {
                i += this.xDiffTex;
            }

            if (this.hovered)
            {
                j += this.yDiffTex;
            }

            this.drawTexturedModalRect(this.x, this.y, i, j, this.width, this.height);
            GlStateManager.enableDepth();
        }
    }
}
