package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import net.minecraft.block.BlockDoor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public class TileEntityDoor extends TileEntityDeviceBase {
//	TileEntityDoorController currentDoor;
	UUID clonedID;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(nbt.hasUniqueId("clonedId"))
			clonedID = nbt.getUniqueId("clonedId");
		else
			clonedID = null;
		if(clonedID != null) {
			if (!world.isRemote) {
				boolean stated = AdvBaseSecurity.instance.doorHandler.getDoorState(clonedID);
				if (!world.getBlockState(pos).getValue(BlockDoor.OPEN).equals(stated))
					((BlockDoor) world.getBlockState(pos).getBlock()).toggleDoor(world, pos, stated);
			} else {
				boolean stated;
				if(nbt.hasKey("devState")) {
					stated = nbt.getBoolean("devState");
					if (!world.getBlockState(pos).getValue(BlockDoor.OPEN).equals(stated))
						((BlockDoor) world.getBlockState(pos).getBlock()).toggleDoor(world, pos, stated);
				}
			}
		}
//		if(currentDoor != null && !world.getBlockState(pos).getValue(BlockDoor.OPEN).equals(currentDoor.currentState)){
//			((BlockDoor)world.getBlockState(pos).getBlock()).toggleDoor(world, pos, currentDoor.currentState);
//		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(clonedID != null)
			nbt.setUniqueId("clonedId", clonedID);
		return nbt;
	}

	@Override
	public NBTTagCompound pushMoretoUpdate(NBTTagCompound nbt) {
		nbt.setBoolean("devState", AdvBaseSecurity.instance.doorHandler.getDoorState(clonedID));
		return nbt;
	}

	public TileEntityDoor(){
		super();
	}

	public boolean isSameID(UUID id){
		return clonedID != null && clonedID.equals(id);
	}

	public void setClonedID(UUID id){
		clonedID = id;
		markDirty();
	}
}
