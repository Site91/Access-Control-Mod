package com.cadergator10.advancedbasesecurity.util;

import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonImg;
import net.minecraft.util.text.translation.I18n;

/**
 * All the preset IconButtons
 * Simply selects the icon to display on the button as well as the tooltip to select
 * @see ButtonImg
 */
public enum ButtonTooltip{ //creates the complete button. Sets tooltip and chooses icontype

    AddDoor(icontype.GreenPlus,"newdoor"),
    AddManager(icontype.GreenPlus, "newmanager"),
    Back(icontype.WhiteReturn, "back"),
    EditPass(icontype.RegLock, "editpasses"),
    EditUser(icontype.BlueUser, "editusers"),
    ClearDevices(icontype.Eraser, "clearlink"),
    DelayUp(icontype.WhiteUp, "delayup"),
    DelayDown(icontype.WhiteDown, "delaydown"),
    SaveDoor(icontype.BlueDisc, "savedoor"),
    UpButton(icontype.WhiteUp, "uplist"),
    DownButton(icontype.WhiteDown, "downlist"),
    AddDoorPass(icontype.WhitePlus, "addpass"),
    DelDoorPass(icontype.WhiteMinus, "delpass"),
    AddUser(icontype.WhitePlus, "adduser"),
    DelUser(icontype.WhiteMinus, "deluser"),
    SelectAddPass(icontype.WhiteSelect, "selectadd"),
    SavePasses(icontype.BlueDisc, "savepasses"),
    WriteCard(icontype.WriteCard, "writecard"),
    ResetUUID(icontype.WhiteReset, "resetuuid"),
    SaveUsers(icontype.BlueDisc, "saveusers"),
    SaveSectors(icontype.BlueDisc, "savesectors"),
    LinkDoor(icontype.Link, "linkdoor"),
    EditDoor(icontype.Pencil, "editdoor"),
    SectorMenu(icontype.SectorOrange, "editsector"),
    AddSector(icontype.WhitePlus, "addsector"),
    DelSector(icontype.WhiteMinus, "delsector"),
    Add(icontype.WhitePlus, "add"),
    Delete(icontype.WhiteMinus, "delete"),
    Edit(icontype.Pencil, "edit");

    public enum icontype { //specifies the actual icon type.
        GreenPlus(0,0),
        RedMinus(1,0),
        Pencil(2,0),
        RedX(3,0),
        BlueDisc(4,0),
        GreenCheck(5,0),
        WhiteList(6,0),
        BlueUser(7,0),
        RegLock(8,0),
        RedLock(9,0),
        RedStopSign(10,0),
        GreenCheckPlus(11,0),
        GreenUnLock(12,0),
        WhiteUp(13,0),
        WhiteDown(14,0),
        WhiteGarbage(15,0),
        WhitePlus(0,1),
        WhiteMinus(1,1),
        WhiteX(2,1),
        WhiteCheck(3,1),
        WhiteReturn(4,1),
        Eraser(5,1),
        WhiteSelect(6,1),
        WriteCard(7,1),
        WhiteReset(8,1),
        Link(9,1),
        SectorOrange(10,1);


        private int x;
        private int y;
        private icontype(int x, int y){ //x, y, is just the location on the texture. x * 16, y * 16
            this.x = x;
            this.y = y;
        }
        public int returnSpot(){
            return (16 * y) + x;
        }
    };

    private final icontype icon;
    private final String tooltip; //the address to the tooltip in the localization
    private final String root; //base of the localization name
    private ButtonTooltip(icontype icon, String tooltip){
        this.root = "gui.tooltips.advancedbasesecurity";
        this.icon = icon;
        this.tooltip = tooltip;
    }

    private ButtonTooltip()
    {
        this.root = "gui.tooltips.advancedbasesecurity";
        this.icon = icontype.RedX;
        this.tooltip = "";
    }

    public int getIcon(){
        return icon.returnSpot();
    }
    public String getTooltip(){
        return tooltip;
    }

    public String getLocal() //returns the translated text from currently selected localization
    {
        return I18n.translateToLocal( this.getUnlocalized() );
    }
    public String getLocal(String[] replace) //same as above, except formatted (replaces [0] and [1] I believe in text with corresponding indexes in replace[])
    {
        String str = String.format(I18n.translateToLocal( this.getUnlocalized()), (Object) replace);

        return "";
    }

    public String getUnlocalized() //just returns the actual address to localization
    {
        return this.root + '.' + this.tooltip;
    }
}
