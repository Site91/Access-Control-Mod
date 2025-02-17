package com.cadergator10.advancedbasesecurity.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

//Not used atm. Dunno why I got it XD
public class ContainerSystemManager extends Container {
    private IInventory managerInv;

    //private DoorHandler.Doors.OneDoor door;
    public ContainerSystemManager(IInventory playerInventory){
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, j * 18 + 8, i * 18 + 95));
            }
        }

        for(int i = 0; i < 9; i++)
        {
            this.addSlotToContainer(new Slot(playerInventory, i, i * 18 + 8, 153));
        }
    }
    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.managerInv.isUsableByPlayer(playerIn);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
    }
}
