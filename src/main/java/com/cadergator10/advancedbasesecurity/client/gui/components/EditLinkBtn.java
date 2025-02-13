package com.cadergator10.advancedbasesecurity.client.gui.components;

import com.cadergator10.advancedbasesecurity.util.ButtonTooltip;

public class EditLinkBtn extends ButtonImg{ //A very specific ButtonImg that switches its icon when clicked.

	public EditLinkBtn(int buttonId, int x, int y, boolean isEdit) {
		super(buttonId, x, y, isEdit ? ButtonTooltip.EditDoor : ButtonTooltip.LinkDoor);
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
