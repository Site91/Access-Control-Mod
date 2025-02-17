package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.SectControllerPacket;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class TileEntitySectorController extends TileEntity { //basic tile entity, so none of the device base stuff needed.
	DoorHandler.DoorIdentifier ids = null; //managerId: manager, duh | doorId: the ID of the group itself.
	List<DoorHandler.Doors.OneDoor.OnePass> overrides = null; //Passes to add to the door to allow overrides. Only on LOCKDOWN and OVERRIDDEN ACCESS.
	public DoorHandler.Doors.OneDoor.allDoorStatuses thisStatus; //Status to change to when redstone output is true. If false, switches to ACCESS if toggle = true
	public boolean pushToChildren; //If true, any sectors that are parented to the sector set in ids#doorId or children of children etc. are updated to the same status too.
	public boolean toggle; //If true: turning redstone = false will set status to ACCESS. If false, does nothing when redstone = false.
	boolean currentPower; //Current power detected. In case a neighbor is changed that is not redstone.

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(nbt.hasKey("ids"))
			ids = new DoorHandler.DoorIdentifier(nbt.getCompoundTag("ids"));
		else
			ids = null;
		if(nbt.hasKey("overrides")){
			//TODO: do the overrides
			NBTTagList list = nbt.getTagList("overrides", Constants.NBT.TAG_COMPOUND);
			overrides = new LinkedList<>();
			boolean isClient = (nbt.hasKey("toclient") && nbt.getBoolean("toclient"));
			DoorHandler.Doors manager = null;
			if(!isClient)
				manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(ids);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound tag = list.getCompoundTagAt(i);
				if(isClient) {
					DoorHandler.Doors.OneDoor.OnePass pass = new DoorHandler.Doors.OneDoor.OnePass(tag, DoorHandler.Doors.PassValue.type.fromInt(tag.getShort("passvaltype")));
					overrides.add(pass);
				}
				else{
					DoorHandler.Doors.OneDoor.OnePass pass = new DoorHandler.Doors.OneDoor.OnePass(tag,  manager.passes);
					overrides.add(pass);
				}
			}
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
		if(ids != null)
			nbt.setTag("ids", ids.writeToNBT(new NBTTagCompound()));
		if(overrides != null && !overrides.isEmpty() && !nbt.hasKey("overrides")) { //make sure overrides not already done as well
			//TODO: setup overrides in nbt
			NBTTagList list = new NBTTagList();
			DoorHandler.Doors manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(ids);
			for(DoorHandler.Doors.OneDoor.OnePass pass : overrides){
				NBTTagCompound tag = pass.returnNBT(manager.passes);
				list.appendTag(tag);
			}
			nbt.setTag("overrides", list);
		}
		if(thisStatus != null)
			nbt.setShort("status", (short)thisStatus.getInt());
		else
			nbt.setShort("status", (short)0);
		nbt.setBoolean("push", pushToChildren);
		nbt.setBoolean("toggle", toggle);
		nbt.setBoolean("current", currentPower);
		return nbt;
	}

	public void first(){ //When the block is first placed, initiate TE
		thisStatus = DoorHandler.Doors.OneDoor.allDoorStatuses.ACCESS;
		overrides = null;
		ids = null;
		markDirty();
	}

	public void newUpdate(SectControllerPacket packet){ //A new update recieved by the DoorManager. Change settings.
		ids = packet.ids;
		pushToChildren = packet.pushToChildren;
		toggle = packet.toggle;
		overrides = packet.overrides;
		thisStatus = packet.thisStatus;
		redstoneSignalRecieved(currentPower, true); //marks dirty in function
	}

	/**
	 * When right clicked by doorManager, this is called.
	 * @param id The ID of the manager (in the doorManager)
	 * @return whether the doorManager has access to the manager or not.
	 */
	public boolean setFirstTime(UUID id){
		if(this.ids != null && this.ids.ManagerID != null){
			if(this.ids.ManagerID.equals(id))
				return true;
			else
				return false;
		}
		this.ids = new DoorHandler.DoorIdentifier();
		this.ids.ManagerID = id;
		markDirty();
		return true;
	}

	//called whenever sending NBT data to client. For stuff the Client should also get.
	public NBTTagCompound pushMoreToUpdate(NBTTagCompound nbt){
		nbt.setBoolean("toclient", true);
		if(nbt.hasKey("overrides")){
			DoorHandler.Doors manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(ids);
			NBTTagList list = nbt.getTagList("overrides", Constants.NBT.TAG_COMPOUND);
			for(int i=0; i<list.tagCount(); i++){
				NBTTagCompound tag = list.getCompoundTagAt(i);
				tag.setShort("passvaltype", (short)manager.passes.get(tag.getString("passId")).passType.getInt());
				list.set(i, tag);
			}
			nbt.setTag("overrides", list);
		}
		return nbt;
	}

	public DoorHandler.DoorIdentifier getIds(){
		return ids;
	}
	public List<DoorHandler.Doors.OneDoor.OnePass> getOverrides(){
		return overrides;
	}

	//when a client requests NBT data for update.
	@Override
	public NBTTagCompound getUpdateTag() {
		return pushMoreToUpdate(writeToNBT(super.getUpdateTag()));
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		readFromNBT(tag);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
	{
		return (oldState.getBlock() != newState.getBlock());
	}

	//Easier to use. Sets the override to false by default
	public void redstoneSignalRecieved(boolean signal){
		redstoneSignalRecieved(signal, false);
	}

	//If redstone signal recieved and is different from currentPower, then change group status & all doors.
	public void redstoneSignalRecieved(boolean signal, boolean override){
		//TODO: actually recieve the signal. yes.
		if(!world.isRemote) {
			if(currentPower != signal || override) { //unless being overridden, check if currentPower is different from recieved signal. If true continue
				DoorHandler.Doors manager = AdvBaseSecurity.instance.doorHandler.getDoorManager(ids);
				if (manager != null) { //if managerID is correct (or manager exists)
					DoorHandler.Doors.Groups group = manager.groups.get(ids.DoorID);
					if (group == null)
						return;
					if (signal) { //if true, change the status of the group to thisStatus var. Whatever the sectorcontroller is set to.
						group.status = thisStatus;
						if (Math.abs(thisStatus.getInt()) == 1) //-1 and 1 are LOCKDOWN and OVERRIDDEN ACCESS
							group.override = overrides;
					} else if (toggle) { //if toggle = true, when signal = false, set status to ACCESS.
						group.status = DoorHandler.Doors.OneDoor.allDoorStatuses.ACCESS;
						group.override = new LinkedList<>();
					}
					manager.updateGroups(group, pushToChildren); //update the group to the DoorHandler, then change all Doors.
				}
				currentPower = signal;
				markDirty();
			}
		}
	}
}
