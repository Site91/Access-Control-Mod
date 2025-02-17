package com.cadergator10.advancedbasesecurity.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Slider extends GuiButton { //Slider pulled from SecurityCraft I think? Maybe appliedenergistics. Modified to allow vertical slider action!
    protected static final ResourceLocation BUTTON_TEXTURES2 = new ResourceLocation("advancedbasesecurity", "textures/gui/widgets.png");

    private float sliderPosition = 1.0F;
    public boolean isMouseDown;
    private final float min;
    private final float max;
    private final GuiPageButtonList.GuiResponder responder;
    private net.minecraft.client.gui.GuiSlider.FormatHelper formatHelper;

    public Slider(GuiPageButtonList.GuiResponder guiResponder, int idIn, int x, int y, int width, int height, float minIn, float maxIn, float defaultValue) {
        super(idIn, x, y, width, height, "");
        this.min = minIn;
        this.max = maxIn;
        this.sliderPosition = (defaultValue - minIn) / (maxIn - minIn);
        this.responder = guiResponder;
    }

    /**
     * Gets the value of the slider.
     *
     * @return A value that will under normal circumstances be between the slider's {@link #min} and {@link #max}
     * values, unless it was manually set out of that range.
     */
    public float getSliderValue() {
        return this.min + (this.max - this.min) * this.sliderPosition;
    }

    /**
     * Sets the slider's value, optionally notifying the associated {@linkplain GuiPageButtonList.GuiResponder
     * responder} of the change.
     */
    public void setSliderValue(float value, boolean notifyResponder) {
        this.sliderPosition = (value - this.min) / (this.max - this.min);

        if (notifyResponder) {
            this.responder.setEntryValue(this.id, this.getSliderValue());
        }
    }

    /**
     * Gets the slider's position.
     *
     * @return The position of the slider, which will under normal circumstances be between 0 and 1, unless it was
     * manually set out of that range.
     */
    public float getSliderPosition() {
        return this.sliderPosition;
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    private boolean isWide(){
        return this.width > this.height;
    }

    //taken from super method and modified due to texture issues when vertical
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible)
        {
            FontRenderer fontrenderer = mc.fontRenderer;
            if(isWide())
                mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            else
                mc.getTextureManager().bindTexture(BUTTON_TEXTURES2);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            if(isWide()) {
                this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
                this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
            }
            else{
                this.drawTexturedModalRect(this.x, this.y, i * 20, 0, this.width, this.height / 2);
                this.drawTexturedModalRect(this.x, this.y + this.height / 2, i * 20, 200 - this.height / 2, this.width, this.height / 2);
            }
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            if (isWide()) {
                if (this.isMouseDown) {
                    this.sliderPosition = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);

                    if (this.sliderPosition < 0.0F) {
                        this.sliderPosition = 0.0F;
                    }

                    if (this.sliderPosition > 1.0F) {
                        this.sliderPosition = 1.0F;
                    }

                    this.responder.setEntryValue(this.id, this.getSliderValue());
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.drawTexturedModalRect(this.x + (int) (this.sliderPosition * (float) (this.width - 8)), this.y, 0, 66, 4, height);
                this.drawTexturedModalRect(this.x + (int) (this.sliderPosition * (float) (this.width - 8)) + 4, this.y, 196, 66, 4, height);
            }
            else{
                if (this.isMouseDown) {
                    this.sliderPosition = (float) (mouseY - (this.y + 4)) / (float) (this.height - 8);

                    if (this.sliderPosition < 0.0F) {
                        this.sliderPosition = 0.0F;
                    }

                    if (this.sliderPosition > 1.0F) {
                        this.sliderPosition = 1.0F;
                    }

                    this.responder.setEntryValue(this.id, this.getSliderValue());
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.drawTexturedModalRect(this.x, this.y + (int) (this.sliderPosition * (float) (this.height - 8)), 20, 0, width, 4);
                this.drawTexturedModalRect(this.x, this.y + (int) (this.sliderPosition * (float) (this.height - 8)) + 4, 20, 196, width, 4);
            }
        }
    }

    /**
     * Sets the position of the slider and notifies the associated {@linkplain GuiPageButtonList.GuiResponder responder}
     * of the change
     */
    public void setSliderPosition(float position) {
        this.sliderPosition = position;
        this.responder.setEntryValue(this.id, this.getSliderValue());
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            if(isWide())
                this.sliderPosition = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
            else
                this.sliderPosition = (float) (mouseY - (this.y + 4)) / (float) (this.height - 8);

            if (this.sliderPosition < 0.0F) {
                this.sliderPosition = 0.0F;
            }

            if (this.sliderPosition > 1.0F) {
                this.sliderPosition = 1.0F;
            }

            this.responder.setEntryValue(this.id, this.getSliderValue());
            this.isMouseDown = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY) {
        this.isMouseDown = false;
    }
}