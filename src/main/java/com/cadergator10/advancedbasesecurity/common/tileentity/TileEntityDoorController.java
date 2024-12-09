package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.SoundHandler;
import com.cadergator10.advancedbasesecurity.common.blocks.doors.BlockDoorBase;
import com.cadergator10.advancedbasesecurity.common.globalsystems.CentralDoorNBT;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoorControl;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

//Heavy thanks to OpenSecurity. Their code really helped me set this all up and get it working!!!
public class TileEntityDoorController extends TileEntityDeviceBase implements IDoorControl {
	UUID deviceId = UUID.randomUUID();
	boolean currentState = false;

	List<UUID> prevPos = new LinkedList<>();

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		if(nbt.hasUniqueId("deviceId"))
			this.deviceId = nbt.getUniqueId("deviceId");
		else
			this.deviceId = UUID.randomUUID();
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
		if(!nbt.hasKey("toclient") || !nbt.getBoolean("toclient"))
			this.currentState = AdvBaseSecurity.instance.doorHandler.getDoorState(deviceId);
		else{
			if(nbt.hasKey("currentState"))
				this.currentState = nbt.getBoolean("currentState");
			else
				this.currentState = false;
		}
		if(current != currentState)
			openDoor(currentState);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(deviceId != null)
			nbt.setUniqueId("deviceId", this.deviceId);
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
		AdvBaseSecurity.instance.doorHandler.toggleIndDoors(deviceId, toggle);
		if(toggle != currentState) {
			currentState = toggle;
			markDirty();
		}
	}

	@Override
	public void newId() {
		deviceId = UUID.randomUUID();
		currentState = false;
		markDirty();
	}

	@Override
	public UUID getId() {
		return deviceId;
	}

	@Override
	public String getDevType() {
		return "doorcontrol";
	}

	@Override
	public void setDoor(ItemStack heldItem) {
		ItemLinkingCard.CardTag cardTag = new ItemLinkingCard.CardTag(heldItem);
		if(cardTag.doorId != null) {
			AdvBaseSecurity.instance.logger.info("Setting DoorController's ID of card id " + cardTag.doorId);
			boolean found = AdvBaseSecurity.instance.doorHandler.SetDevID(deviceId, cardTag.doorId, true);
			if(found){
				AdvBaseSecurity.instance.logger.info("Found door! Linking...");
				openDoor(AdvBaseSecurity.instance.doorHandler.getDoorState(deviceId));
			}
		}
	}

	public void linkDoors() {
		prevPos = new LinkedList<>();
		HashMap<BlockPos, BlockDoor> dooreme = getDoors();
		for(Map.Entry<BlockPos, BlockDoor> doorSet : dooreme.entrySet()){
			if(doorSet.getValue() instanceof BlockDoorBase) {
				TileEntityDoor te = (TileEntityDoor) world.getTileEntity(doorSet.getKey());
				CentralDoorNBT.doorHoldr door = AdvBaseSecurity.instance.doorHandler.indDoorsContains(te.deviceId);
				if(door != null){
					door.clonedId = deviceId;
					prevPos.add(door.deviceId);
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
