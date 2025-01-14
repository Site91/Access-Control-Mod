package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class TileEntitySectorController extends TileEntity { //basic tile entity, so none of the device base stuff needed.
	DoorHandler.DoorIdentifier ids = null;
	List<DoorHandler.Doors.OneDoor.OnePass> overrides = null;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		return nbt;
	}

	public void redstoneSignalRecieved(boolean signal){
		//TODO: actually recieve the signal. yes.
		if(!world.isRemote) {
			if (signal) {

			} else {
				AdvBaseSecurity.instance.doorHandler.getDoorManager(ids);
			}
		}
	}
}
