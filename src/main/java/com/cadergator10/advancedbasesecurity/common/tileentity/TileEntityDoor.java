package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.SoundHandler;
import com.cadergator10.advancedbasesecurity.common.globalsystems.CentralDoorNBT;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoor;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;

import java.util.UUID;

/**
 * Independent Door that is controlled by the TileEntityDoorController
 * @see TileEntityDoorController
 * Lot of mumbo jumbo stuff i'm bored. Not much will be commented as it prolly won't be needed
 */
public class TileEntityDoor extends TileEntityDeviceBase implements IDoor {
//	TileEntityDoorController currentDoor;
	public boolean pushDoor; //if true, door must be right clicked to open/close.

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(nbt.hasKey("pushDoor"))
			pushDoor = nbt.getBoolean("pushDoor");
		else
			pushDoor = false;
		if (nbt.hasKey("toclient") && nbt.getBoolean("toclient")) { //TODO: Open or close based on state
			boolean stated;
			if(nbt.hasKey("devState")) {
				stated = nbt.getBoolean("devState");
				if (!world.getBlockState(pos).getValue(BlockDoor.OPEN).equals(stated))
					((BlockDoor) world.getBlockState(pos).getBlock()).toggleDoor(world, pos, stated);
			}
		}
		else{
			managerId = AdvBaseSecurity.instance.doorHandler.IndDoors.getIndDoorManager(deviceId);
			if(managerId != null)
				door = AdvBaseSecurity.instance.doorHandler.getDoorManager(managerId);
		}
//		if(currentDoor != null && !world.getBlockState(pos).getValue(BlockDoor.OPEN).equals(currentDoor.currentState)){
//			((BlockDoor)world.getBlockState(pos).getBlock()).toggleDoor(world, pos, currentDoor.currentState);
//		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if(!world.isRemote) {
			managerId = AdvBaseSecurity.instance.doorHandler.IndDoors.getIndDoorManager(deviceId);
			if(managerId != null)
				door = AdvBaseSecurity.instance.doorHandler.getDoorManager(managerId);
			if(door != null) {
				IBlockState state = world.getBlockState(pos);
				boolean stated = door.getDoorStateFromDoor(deviceId);
				if (!pushDoor && !state.getValue(BlockDoor.OPEN).equals(stated)) {
					((BlockDoor) world.getBlockState(pos).getBlock()).toggleDoor(world, pos, stated);
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("pushDoor", pushDoor);
		return nbt;
	}

	@Override
	public NBTTagCompound pushMoretoUpdate(NBTTagCompound nbt) {
		if(!pushDoor) {
			nbt.setBoolean("toclient", true);
			if(door != null)
				nbt.setBoolean("devState", door.getDoorStateFromDoor(deviceId));
			else
				nbt.setBoolean("devState", false);
		}
		return nbt;
	}

	public TileEntityDoor(){
		super();
	}

	public boolean isSameID(UUID id){
		return deviceId != null && deviceId.equals(id);
	}

	@Override
	public void openDoor(boolean toggle) {
		if(pushDoor){
			world.playSound(null, getPos().getX() + 0.5F, getPos().getY() + 0.5F, getPos().getZ() + 0.5F, SoundHandler.lockopen, SoundCategory.BLOCKS, 1F, toggle ? 1F : 0.8F);
		}
		else{
			((BlockDoor)world.getBlockState(pos).getBlock()).toggleDoor(world, pos, toggle);
		}
	}

	public void setDoorM(UUID managerId){
		AdvBaseSecurity.LogDebug("Independent Door " + deviceId + " recieved communication with DoorController. Got managerID " + managerId);
		this.managerId = managerId;
		if(managerId != null)
			door = AdvBaseSecurity.instance.doorHandler.getDoorManager(managerId);
	}

	@Override
	public String getDevType() {
		return "door";
	}

	@Override
	public void onPlace() {
//		if (!AdvBaseSecurity.instance.doorHandler.allDoors.containsKey(this.deviceId))
//			AdvBaseSecurity.instance.doorHandler.allDoors.put(this.deviceId, this);
		AdvBaseSecurity.instance.doorHandler.IndDoors.doors.add(new CentralDoorNBT.doorHoldr(getId()));
		AdvBaseSecurity.instance.doorHandler.IndDoors.markDirty();
	}
}
