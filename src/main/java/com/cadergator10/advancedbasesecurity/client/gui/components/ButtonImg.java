package com.cadergator10.advancedbasesecurity.client.gui.components;

import com.cadergator10.advancedbasesecurity.util.ButtonTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.regex.Pattern;

//Credit to Applied Energistics
public class ButtonImg extends GuiButton implements ITooltip { //Button with a ICON that is displayed
    private static final Pattern COMPILE = Pattern.compile( "%s" );
    private static final Pattern PATTERN_NEW_LINE = Pattern.compile( "\\n", Pattern.LITERAL );
    private boolean halfSize = false;
    private String[] replacers;
    private ButtonTooltip tooltip;

    public ButtonImg(int buttonId, int x, int y, final ButtonTooltip tooltip) {
        super(buttonId, 0, 16, "");
        this.tooltip = tooltip;
        this.x = x;
        this.y = y;
        this.width = 16;
        this.height = 16;
    }
    public ButtonImg(int buttonId, int x, int y, final ButtonTooltip tooltip, String[] replacers) {
        this(buttonId,x,y,tooltip);
        this.replacers = replacers;
    }

    public void setVisibility( final boolean vis){
        this.visible = vis;
        this.enabled = vis;
    }

    @Override
    public void drawButton(final Minecraft par1Minecraft, final int par2, final int par3, float partial )
    {
        if( this.visible )
        {
            final int iconIndex = this.getIconIndex();

            if( this.halfSize )
            {
                this.width = 8;
                this.height = 8;

                GlStateManager.pushMatrix();
                GlStateManager.translate( this.x, this.y, 0.0F );
                GlStateManager.scale( 0.5f, 0.5f, 0.5f );

                if( this.enabled )
                {
                    GlStateManager.color( 1.0f, 1.0f, 1.0f, 1.0f );
                }
                else
                {
                    GlStateManager.color( 0.5f, 0.5f, 0.5f, 1.0f );
                }

                par1Minecraft.renderEngine.bindTexture( new ResourceLocation( "advancedbasesecurity", "textures/gui/buttonicons.png" ) );
                this.hovered = par2 >= this.x && par3 >= this.y && par2 < this.x + this.width && par3 < this.y + this.height;
                int offset = hovered ? 32 : 16;

                final int uv_y = (int) Math.floor( iconIndex / 16 );
                final int uv_x = iconIndex - uv_y * 16;

                this.drawTexturedModalRect( 0, 0, 256 - offset, 256 - 16, 16, 16 );
                this.drawTexturedModalRect( 0, 0, uv_x * 16, uv_y * 16, 16, 16 );
                this.mouseDragged( par1Minecraft, par2, par3 );

                GlStateManager.popMatrix();
            }
            else
            {
                if( this.enabled )
                {
                    GlStateManager.color( 1.0f, 1.0f, 1.0f, 1.0f );
                }
                else
                {
                    GlStateManager.color( 0.5f, 0.5f, 0.5f, 1.0f );
                }

                par1Minecraft.renderEngine.bindTexture( new ResourceLocation( "advancedbasesecurity", "textures/gui/buttonicons.png" ) );
                this.hovered = par2 >= this.x && par3 >= this.y && par2 < this.x + this.width && par3 < this.y + this.height;
                int offset = hovered ? 32 : 16;

                final int uv_y = (int) Math.floor( iconIndex / 16 );
                final int uv_x = iconIndex - uv_y * 16;

                this.drawTexturedModalRect( this.x, this.y, 256 - offset, 256 - 16, 16, 16 );
                this.drawTexturedModalRect( this.x, this.y, uv_x * 16, uv_y * 16, 16, 16 );
                this.mouseDragged( par1Minecraft, par2, par3 );
            }
        }
        GlStateManager.color( 1.0f, 1.0f, 1.0f, 1.0f );
    }

    private int getIconIndex()
    {
        if( this.tooltip != null )
        {
            return tooltip.getIcon();
        }
        return 256 - 1;
    }


    @Override
    public String getMessage() {
        if(tooltip != null)
            if(replacers != null)
                return tooltip.getLocal(replacers);
            else
                return tooltip.getLocal();
        return "none";
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
        return this.halfSize ? 8 : 16;
    }

    @Override
    public int getHeight() {
        return this.halfSize ? 8 : 16;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    public ButtonTooltip getTooltip(){
        return tooltip;
    }

    public void changeTooltip(ButtonTooltip tooltip){
        this.tooltip = tooltip;
    }

    public void setHalfSize( final boolean halfSize )
    {
        this.halfSize = halfSize;
    }
}
