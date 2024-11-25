package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import net.minecraft.client.gui.GuiScreen;

import java.util.HashMap;
import java.util.UUID;

public class EditPassGUI extends GuiScreen {
    UUID editValidator;
    HashMap<String, DoorHandler.Doors.PassValue> passes;



    public EditPassGUI(UUID editValidator, HashMap<String, DoorHandler.Doors.PassValue> passes){
        this.editValidator = editValidator;
        this.passes = passes;
    }
}
