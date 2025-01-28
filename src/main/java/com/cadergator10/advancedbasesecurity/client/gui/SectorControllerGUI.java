package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonImg;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.SectControllerPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SectorControllerGUI extends BaseGUI{

	private static final ResourceLocation background = new ResourceLocation(AdvBaseSecurity.MODID, "textures/gui/basic.png");

	private DoorHandler.DoorIdentifier ids;
	private List<DoorHandler.Doors.OneDoor.OnePass> overrides;
	private boolean pushChildren;
	private boolean toggle;

	private BlockPos pos;
	private HashMap<UUID, DoorHandler.Doors.Groups> sectors;

	boolean finished = false;
	boolean clean = false;
	//buttons
	int currentIndex;
	ButtonImg saveButton;
	ButtonEnum typeButton;
	ButtonEnum passButton;

	static final int WIDTH = 175;
	static final int HEIGHT = 195;
	public SectorControllerGUI(SectControllerPacket packet, BlockPos pos, HashMap<UUID, DoorHandler.Doors.Groups> sectors) {
		super(WIDTH, HEIGHT);
		ids = packet.ids;
		overrides = packet.overrides;
		pushChildren = packet.pushToChildren;
		toggle = packet.toggle;
		this.pos = pos;
		this.sectors = sectors;
	}


}
