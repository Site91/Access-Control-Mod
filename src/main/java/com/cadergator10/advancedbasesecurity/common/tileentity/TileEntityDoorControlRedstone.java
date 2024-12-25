package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.blocks.BlockDoorRedstone;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoorControl;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import javax.annotation.Nonnull;

public class TileEntityDoorControlRedstone extends TileEntityDeviceBase implements IDoorControl, ITickable {

    DoorHandler.Doors door = null;

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
        super.setDoor(heldItem);
        if(door != null){
            door.getDoorState(deviceId);
        }
    }

    @Override
    public void onPlace() {
        //check if in list
            if (AdvBaseSecurity.instance.doorHandler.allDoorControllers.containsKey(this.deviceId))
                AdvBaseSecurity.instance.doorHandler.allDoorControllers.put(this.deviceId, this);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        //get powered
        if(!compound.hasKey("toclient") || !compound.getBoolean("toclient")) {
            if(door != null)
                powered = door.getDoorState(deviceId);
            else
                powered = false;
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
