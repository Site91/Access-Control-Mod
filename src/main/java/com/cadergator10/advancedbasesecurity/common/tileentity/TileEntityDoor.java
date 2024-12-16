package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.SoundHandler;
import com.cadergator10.advancedbasesecurity.common.globalsystems.CentralDoorNBT;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoor;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import scala.collection.parallel.ParIterableLike;

import java.util.UUID;

public class TileEntityDoor extends TileEntityDeviceBase implements IDoor {
//	TileEntityDoorController currentDoor;
	public UUID deviceId;
	public boolean pushDoor; //if true, door must be right clicked to open/close.

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(nbt.hasUniqueId("deviceId"))
			deviceId = nbt.getUniqueId("deviceId");
		else
			deviceId = null;
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
//		if(currentDoor != null && !world.getBlockState(pos).getValue(BlockDoor.OPEN).equals(currentDoor.currentState)){
//			((BlockDoor)world.getBlockState(pos).getBlock()).toggleDoor(world, pos, currentDoor.currentState);
//		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		IBlockState state = world.getBlockState(pos);
		boolean stated = AdvBaseSecurity.instance.doorHandler.getDoorStateFromDoor(deviceId);
		if (!pushDoor && !state.getValue(BlockDoor.OPEN).equals(stated)) {
			((BlockDoor) world.getBlockState(pos).getBlock()).toggleDoor(world, pos, stated);

		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(deviceId != null)
			nbt.setUniqueId("deviceId", deviceId);
		nbt.setBoolean("pushDoor", pushDoor);
		return nbt;
	}

	@Override
	public NBTTagCompound pushMoretoUpdate(NBTTagCompound nbt) {
		if(!pushDoor) {
			nbt.setBoolean("toclient", true);
			nbt.setBoolean("devState", AdvBaseSecurity.instance.doorHandler.getDoorStateFromDoor(deviceId));
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
			AdvBaseSecurity.instance.logger.info("Setting Door's ID of card id " + cardTag.doorId);
			boolean found = AdvBaseSecurity.instance.doorHandler.SetDevID(deviceId, cardTag.doorId, true);
			if(found){
				AdvBaseSecurity.instance.logger.info("Found door! Linking...");
				openDoor(AdvBaseSecurity.instance.doorHandler.getDoorState(deviceId));
			}
		}
	}

	@Override
	public void onPlace() {
//		if (!AdvBaseSecurity.instance.doorHandler.allDoors.containsKey(this.deviceId))
//			AdvBaseSecurity.instance.doorHandler.allDoors.put(this.deviceId, this);
		AdvBaseSecurity.instance.doorHandler.IndDoors.doors.add(new CentralDoorNBT.doorHoldr(getId()));
		AdvBaseSecurity.instance.doorHandler.IndDoors.markDirty();
	}
}
