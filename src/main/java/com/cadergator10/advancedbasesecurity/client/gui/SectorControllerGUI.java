package com.cadergator10.advancedbasesecurity.client.gui;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import net.minecraft.util.ResourceLocation;

public class SectorControllerGUI extends BaseGUI{

	private static final ResourceLocation background = new ResourceLocation(AdvBaseSecurity.MODID, "textures/gui/basic.png");
	static final int WIDTH = 175;
	static final int HEIGHT = 195;
	public SectorControllerGUI() {
		super(WIDTH, HEIGHT);
	}
}
