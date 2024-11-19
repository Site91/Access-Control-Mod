package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.blocks.BlockDoorRedstone;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoor;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TileEntityDoorRedstone extends TileEntitySimpleBase implements IDoor, ITickable {
    UUID deviceId = UUID.randomUUID();
    boolean powered = false;

    public TileEntityDoorRedstone() {
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
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasUniqueId("deviceId"))
            deviceId = compound.getUniqueId("deviceId");
        AdvBaseSecurity.instance.logger.info("Device ID: " + compound);
        //get powered
        powered = AdvBaseSecurity.instance.doorHandler.getDoorState(deviceId);
        AdvBaseSecurity.instance.logger.info("Powered: " + powered);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setUniqueId("deviceId", deviceId);
        return compound;
    }

    @Override
    public void openDoor(boolean toggle) {
        //change powered state
        if(toggle != powered){
            powered = toggle;
//            this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos).withProperty(BlockDoorRedstone.POWERED, powered), this.world.getBlockState(this.pos).withProperty(BlockDoorRedstone.POWERED, powered), 2);
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
        return "door";
    }

    @Override
    public void update() {
        boolean blockOn = getWorld().getBlockState(this.getPos()).getValue(BlockDoorRedstone.POWERED);
        if(blockOn != powered){
            world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockDoorRedstone.POWERED, powered));
        }
    }
}
