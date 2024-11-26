package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.blocks.doors.BlockDoorBase;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoor;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//Heavy thanks to OpenSecurity. Their code really helped me set this all up and get it working!!!
public class TileEntityDoorController extends TileEntitySimpleBase implements IDoor {
	UUID deviceId = UUID.randomUUID();
	boolean currentState;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		if(nbt.hasUniqueId("deviceId"))
			this.deviceId = nbt.getUniqueId("deviceId");
		else
			this.deviceId = UUID.randomUUID();
		if(nbt.hasKey("currentState"))
			this.currentState = nbt.getBoolean("currentState");
		else
			this.currentState = false;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(deviceId != null)
			nbt.setUniqueId("deviceId", this.deviceId);

		return nbt;
	}

	public HashMap<BlockPos, BlockDoor> getDoors() {
		return getDoors(true);
	}
	//TAKEN STRAIGHT FROM OPENSECURITY!
	// scans the blockposition for any surrounding doors, if it found a door it will start a scan for a neighbourdoor
	// so dont parse the blockposition of any door to this!
	private HashMap<BlockPos, BlockDoor> getDoors(boolean searchMaindoor) {
		HashMap<BlockPos, BlockDoor> doors = new HashMap<>();

		for (EnumFacing direction : EnumFacing.VALUES) {
			if(!searchMaindoor && (direction.equals(EnumFacing.UP) || direction.equals(EnumFacing.DOWN)))
				continue;

			BlockPos position = pos.offset(direction); // Offset the block's position by 1 block in the current direction

			Block block = world.getBlockState(position).getBlock(); // Get the IBlockState's Block
			if (block instanceof BlockDoor) {
				doors.put(position, (BlockDoor) block);

				//if we found a door, we are making another loop with the last parameter set to false
				// otherwise we end in an loop where the doors find each other infinitely
				if(searchMaindoor)
					doors.putAll(getDoors(false));
			}
		}

		return doors;
	}

	@Override
	public void openDoor(boolean toggle) {
		HashMap<BlockPos, BlockDoor> dooreme = getDoors();
		for(Map.Entry<BlockPos, BlockDoor> doorSet : dooreme.entrySet()){
			if(doorSet.getValue() instanceof BlockDoorBase) {
				TileEntityDoor te = (TileEntityDoor) world.getTileEntity(doorSet.getKey());
				doorSet.getValue().toggleDoor(world, doorSet.getKey(), toggle);
			}
			else {
				doorSet.getValue().toggleDoor(world, doorSet.getKey(), toggle);
			}
		}
		if(toggle != currentState){
			currentState = toggle;
			markDirty();
		}
	}

	@Override
	public void newId() {
		deviceId = UUID.randomUUID();
		markDirty();
	}

	@Override
	public UUID getId() {
		return deviceId;
	}

	@Override
	public String getDevType() {
		return "door";
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

	class chunkHolder{

	}
}
