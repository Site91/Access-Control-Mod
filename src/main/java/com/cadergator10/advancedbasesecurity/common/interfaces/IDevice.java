package com.cadergator10.advancedbasesecurity.common.interfaces;

import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface IDevice {
	public void newId();
	public UUID getId();
	public String getDevType(); //get type of device, if door or not door
	public void setDoor(ItemStack heldItem);
	public void onPlace();
}
