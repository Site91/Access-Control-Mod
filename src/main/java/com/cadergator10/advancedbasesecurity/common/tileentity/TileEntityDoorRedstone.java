package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.blocks.BlockDoorRedstone;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoor;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TileEntityDoorRedstone extends TileEntity implements IDoor {
    UUID deviceId = UUID.randomUUID();

    public boolean isPowered(){
        //get door state from doorHandler
        return AdvBaseSecurity.instance.doorHandler.getDoorState(deviceId);
    }

    @Override
    public void setDoor(@Nonnull ItemStack heldItem){
        ItemLinkingCard.CardTag cardTag = new ItemLinkingCard.CardTag(heldItem);
        if(cardTag.doorId != null) {
            boolean found = AdvBaseSecurity.instance.doorHandler.SetDevID(deviceId, cardTag.doorId, true);
            if(found){
                openDoor(AdvBaseSecurity.instance.doorHandler.getDoorState(deviceId));
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("deviceId"))
            deviceId = compound.getUniqueId("deviceId");
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
        world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockDoorRedstone.POWERED, toggle));
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
}
