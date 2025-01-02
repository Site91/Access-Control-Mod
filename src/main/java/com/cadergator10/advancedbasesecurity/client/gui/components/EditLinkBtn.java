package com.cadergator10.advancedbasesecurity.client.gui.components;

import com.cadergator10.advancedbasesecurity.util.ButtonTooltip;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import java.util.List;

public class EditLinkBtn extends ButtonImg{

	public EditLinkBtn(int buttonId, int x, int y) {
		super(buttonId, x, y, ButtonTooltip.EditDoor);
	}

	public void onClick(){
		if(getTooltip() == ButtonTooltip.EditDoor)
			changeTooltip(ButtonTooltip.LinkDoor);
		else
			changeTooltip(ButtonTooltip.EditDoor);
	}

	public boolean isEdit(){
		return getTooltip() == ButtonTooltip.EditDoor;
	}
}
