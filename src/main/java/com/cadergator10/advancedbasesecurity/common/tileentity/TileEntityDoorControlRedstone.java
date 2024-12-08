package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.blocks.BlockDoorRedstone;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoorControl;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TileEntityDoorControlRedstone extends TileEntityDeviceBase implements IDoorControl, ITickable {
    UUID deviceId = UUID.randomUUID();
    boolean powered = false;

    public TileEntityDoorControlRedstone() {
        super();
    }

    public boolean isPowered(){
        //get door state from doorHandler
//        boolean temp = AdvBaseSecurity.instance.doorHandler.getDoorState(deviceId);
//        boolean update = temp != powered;
//        powered = temp;
//        if(update){
//            this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 2);
//            this.world.scheduleBlockUpdate(this.pos, this.world.getBlockState(this.pos).getBlock(),1,1);
//            getUpdateTag();
//            markDirty();
//        }
        return powered;
    }

    @Override
    public void setDoor(@Nonnull ItemStack heldItem){
        ItemLinkingCard.CardTag cardTag = new ItemLinkingCard.CardTag(heldItem);
        if(cardTag.doorId != null) {
            AdvBaseSecurity.instance.logger.info("Setting DoorRedstone's ID of card id " + cardTag.doorId);
            boolean found = AdvBaseSecurity.instance.doorHandler.SetDevID(deviceId, cardTag.doorId, true);
            if(found){
                AdvBaseSecurity.instance.logger.info("Found door! Linking...");
                openDoor(AdvBaseSecurity.instance.doorHandler.getDoorState(deviceId));
            }
        }
    }

    @Override
    public void onPlace() {
        //check if in list
        if (!AdvBaseSecurity.instance.doorHandler.allDoorControllers.containsKey(this.deviceId))
            AdvBaseSecurity.instance.doorHandler.allDoorControllers.put(this.deviceId, this);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasUniqueId("deviceId"))
            deviceId = compound.getUniqueId("deviceId");
        AdvBaseSecurity.instance.logger.info("Device ID: " + compound);
        //get powered
        if(!compound.hasKey("toclient") || !compound.getBoolean("toclient")) {
            powered = AdvBaseSecurity.instance.doorHandler.getDoorState(deviceId);
            //check if in list
        }
        else{
            if(compound.hasKey("powered"))
                powered = compound.getBoolean("powered");
            else {
                powered = false;
                AdvBaseSecurity.instance.logger.warn("Failed to recieve powered state");
            }
        }
        AdvBaseSecurity.instance.logger.info("Powered: " + powered);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setUniqueId("deviceId", deviceId);
        return compound;
    }

    @Override
    public NBTTagCompound pushMoretoUpdate(NBTTagCompound nbt) {
        nbt.setBoolean("toclient", true);
        nbt.setBoolean("powered", powered);
        return nbt;
    }

    @Override
    public void openDoor(boolean toggle) {
        //change powered state
        AdvBaseSecurity.instance.logger.info("Door ID " + deviceId + " recieved toggle: " + toggle);
        if(toggle != powered){
            powered = toggle;
//            this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 2);
//            this.world.scheduleBlockUpdate(this.pos, this.world.getBlockState(this.pos).getBlock(),1,1);
//            getUpdateTag();
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
        return "doorcontrol";
    }

    @Override
    public void update() {
        boolean blockOn = getWorld().getBlockState(this.getPos()).getValue(BlockDoorRedstone.POWERED);
        if(blockOn != powered){
            world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockDoorRedstone.POWERED, powered));
        }
    }
}
