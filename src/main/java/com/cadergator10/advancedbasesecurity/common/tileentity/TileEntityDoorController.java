package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.blocks.doors.BlockDoorBase;
import com.cadergator10.advancedbasesecurity.common.globalsystems.CentralDoorNBT;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoorControl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

//Heavy thanks to OpenSecurity. Their code really helped me set this all up and get it working!!!
public class TileEntityDoorController extends TileEntityCamoBase implements IDoorControl {
	boolean currentState = false;

	List<UUID> prevPos = new LinkedList<>();

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(nbt.hasKey("allDoors")){
			NBTTagList tagList = nbt.getTagList("allDoors", Constants.NBT.TAG_COMPOUND);
			prevPos = new LinkedList<>();
			for(int i=0; i<tagList.tagCount(); i++){
				prevPos.add(tagList.getCompoundTagAt(i).getUniqueId("id"));
			}
		}
		else{
			prevPos = new LinkedList<>();
		}
		boolean current = this.currentState;
		if(!nbt.hasKey("toclient") || !nbt.getBoolean("toclient")) {
			if(door != null)
				this.currentState = door.getDoorState(deviceId);
			else
				this.currentState = false;
		}
		else{
			if(nbt.hasKey("currentState"))
				this.currentState = nbt.getBoolean("currentState");
			else
				this.currentState = false;
		}
//		if(current != currentState)
//			openDoor(currentState);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagList tagList = new NBTTagList();
		for(int i=0; i<prevPos.size(); i++){
			NBTTagCompound tag = new NBTTagCompound();
			tag.setUniqueId("id", prevPos.get(i));
			tagList.appendTag(tag);
		}
		nbt.setTag("allDoors", tagList);
		return nbt;
	}

	@Override
	public NBTTagCompound pushMoretoUpdate(NBTTagCompound nbt) {
		nbt.setBoolean("toclient", true);
		nbt.setBoolean("currentState", currentState);
		return nbt;
	}

	@Override
	public DoorHandler.Doors getDoor() {
		return door;
	}

	public HashMap<BlockPos, BlockDoor> getDoors() {
		return getDoors(this.pos, true);
	}
	//TAKEN STRAIGHT FROM OPENSECURITY!
	// scans the blockposition for any surrounding doors, if it found a door it will start a scan for a neighbourdoor
	// so dont parse the blockposition of any door to this!
	private HashMap<BlockPos, BlockDoor> getDoors(BlockPos pos, boolean searchMaindoor) {
		HashMap<BlockPos, BlockDoor> doors = new HashMap<>();

		for (EnumFacing direction : EnumFacing.VALUES) {
			if(!searchMaindoor && (direction.equals(EnumFacing.UP) || direction.equals(EnumFacing.DOWN)))
				continue;

			BlockPos position = pos.offset(direction); // Offset the block's position by 1 block in the current direction

			World world1 = world;
			Block block = world.getBlockState(position).getBlock(); // Get the IBlockState's Block
			if (block instanceof BlockDoor) {
				doors.put(position, (BlockDoor) block);

				//if we found a door, we are making another loop with the last parameter set to false
				// otherwise we end in an loop where the doors find each other infinitely
				if(searchMaindoor)
					doors.putAll(getDoors(position, false));
			}
		}

		return doors;
	}

	@Override
	public void openDoor(boolean toggle) {
		if(door != null) {
			door.toggleIndDoors(deviceId, toggle);
			if (toggle != currentState) {
				currentState = toggle;
				markDirty();
			}
		}
	}

	@Override
	public void newId() {
		currentState = false;
		super.newId();
	}

	@Override
	public String getDevType() {
		return "doorcontrol";
	}

	@Override
	public void setDoor(ItemStack heldItem) {
		super.setDoor(heldItem);
		if(door != null)
			openDoor(door.getDoorState(deviceId));
	}

	public void linkDoors() {
		prevPos = new LinkedList<>();
		HashMap<BlockPos, BlockDoor> dooreme = getDoors();
		for(Map.Entry<BlockPos, BlockDoor> doorSet : dooreme.entrySet()){
			if(doorSet.getValue() instanceof BlockDoorBase) {
				TileEntityDoor te = (TileEntityDoor) world.getTileEntity(doorSet.getKey());
				if(door != null) {
					CentralDoorNBT.doorHoldr doore = door.indDoorsContains(te.deviceId);
					if (doore != null) {
						doore.clonedId = deviceId;
						prevPos.add(doore.deviceId);
					}
				}
			}
		}
		markDirty();
	}

	@Override
	public void onPlace() {
		//check if in list
		if (!AdvBaseSecurity.instance.doorHandler.allDoorControllers.containsKey(this.deviceId))
			AdvBaseSecurity.instance.doorHandler.allDoorControllers.put(this.deviceId, this);
	}
}
