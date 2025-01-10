package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.client.gui.components.GUITextFieldTooltip;
import com.cadergator10.advancedbasesecurity.client.gui.components.ITooltip;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.List;

public class BaseGUI extends GuiScreen {
    private int wdth;
    private int hght;
    int GUILeft;
    int GUITop;
    public BaseGUI(int WIDTH, int HEIGHT){
        super();
        wdth = WIDTH;
        hght = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();
        GUILeft = (this.width / 2) - (wdth / 2);
        GUITop = (this.height / 2) - (hght / 2);
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {
        super.onResize(mcIn, w, h);
        GUILeft = (this.width / 2) - (wdth / 2);
        GUITop = (this.height / 2) - (hght / 2);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        for( final Object c : this.buttonList )
        {
            if( c instanceof ITooltip)
            {
                this.drawTooltip( (ITooltip) c, mouseX, mouseY );
            }
        }
    }
    public void drawScreen(int mouseX, int mouseY, float partialTicks, List<GuiTextField> fields) {
        drawScreen(mouseX, mouseY, partialTicks);
        for( final Object c : fields )
        {
            if( c instanceof ITooltip)
            {
                this.drawTooltip( (ITooltip) c, mouseX, mouseY );
            }
        }
    }

    public void processField(ITooltip field, int mouseX, int mouseY){
        this.drawTooltip( (ITooltip) field, mouseX, mouseY );
    }

    private void drawTooltip(ITooltip tooltip, int mouseX, int mouseY )
    {
        final int x = tooltip.xPos(); // ((GuiImgButton) c).x;
        int y = tooltip.yPos(); // ((GuiImgButton) c).y;

        if( x < mouseX && x + tooltip.getWidth() > mouseX && tooltip.isVisible() )
        {
            if( y < mouseY && y + tooltip.getHeight() > mouseY )
            {
                if( y < 15 )
                {
                    y = 15;
                }

                final String msg = tooltip.getMessage();
                if( msg != null )
                {
                    this.drawTooltip( x + 11, y + 4, msg );
                }
            }
        }
    }

    protected void drawTooltip( int x, int y, String message )
    {
        String[] lines = message.split( "/n" );
        this.drawTooltip( x, y, Arrays.asList( lines ) );
    }

    protected void drawTooltip( int x, int y, List<String> lines )
    {
        if( lines.isEmpty() )
        {
            return;
        }

        // For an explanation of the formatting codes, see http://minecraft.gamepedia.com/Formatting_codes
        lines = Lists.newArrayList( lines ); // Make a copy

        // Make the first line white
        lines.set( 0, TextFormatting.WHITE + lines.get( 0 ) );

        // All lines after the first are colored gray
        for( int i = 1; i < lines.size(); i++ )
        {
            lines.set( i, TextFormatting.GRAY + lines.get( i ) );
        }

        this.drawHoveringText( lines, x, y, this.fontRenderer );
    }
}
