package com.cadergator10.advancedbasesecurity.common.tileentity;

import net.minecraft.block.BlockDoor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityDoor extends TileEntity {
	TileEntityDoorController currentDoor;

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if(currentDoor != null && !world.getBlockState(pos).getValue(BlockDoor.OPEN).equals(currentDoor.currentState)){
			((BlockDoor)world.getBlockState(pos).getBlock()).toggleDoor(world, pos, currentDoor.currentState);
		}
	}

	public TileEntityDoor(){
		super();
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return super.getUpdateTag();
	}
}
