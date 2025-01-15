package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.LinkedList;
import java.util.List;

public class TileEntitySectorController extends TileEntity { //basic tile entity, so none of the device base stuff needed.
	DoorHandler.DoorIdentifier ids = null;
	List<DoorHandler.Doors.OneDoor.OnePass> overrides = null;
	DoorHandler.Doors.OneDoor.allDoorStatuses thisStatus;
	boolean pushToChildren;
	boolean toggle;
	boolean currentPower;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(nbt.hasKey("id"))
			ids = new DoorHandler.DoorIdentifier(nbt.getCompoundTag("id"));
		else
			ids = null;
		if(nbt.hasKey("overrides")){
			//TODO: do the overrides
		}
		else{
			overrides = null;
		}
		if(nbt.hasKey("status"))
			thisStatus = DoorHandler.Doors.OneDoor.allDoorStatuses.fromInt(nbt.getShort("status"));
		else
			thisStatus = DoorHandler.Doors.OneDoor.allDoorStatuses.ACCESS;
		if(nbt.hasKey("push"))
			pushToChildren = nbt.getBoolean("push");
		else
			pushToChildren = true;
		if(nbt.hasKey("toggle"))
			toggle = nbt.getBoolean("toggle");
		else
			toggle = false;
		if(nbt.hasKey("current"))
			currentPower = nbt.getBoolean("current");
		else
			currentPower = false;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		return nbt;
	}

	public void redstoneSignalRecieved(boolean signal){
		//TODO: actually recieve the signal. yes.
		if(!world.isRemote) {
			if(currentPower != signal) {
				DoorHandler.Doors manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(ids);
				if (manager != null) {
					DoorHandler.Doors.Groups group = manager.groups.get(ids.DoorID);
					if (group == null)
						return;
					if (signal) {
						group.status = thisStatus;
						if (Math.abs(thisStatus.getInt()) == 1)
							group.override = overrides;
					} else if (toggle) {
						group.status = DoorHandler.Doors.OneDoor.allDoorStatuses.ACCESS;
						group.override = new LinkedList<>();
					}
					manager.updateGroups(group, pushToChildren);
				}
				currentPower = signal;
				markDirty();
			}
		}
	}
}
