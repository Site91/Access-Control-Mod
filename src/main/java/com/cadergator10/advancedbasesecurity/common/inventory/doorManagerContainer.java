package com.cadergator10.advancedbasesecurity.common.inventory;

import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.inventory.slot.CardInputSlot;
import com.cadergator10.advancedbasesecurity.common.inventory.slot.CardOutputSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class doorManagerContainer extends Container {
    private InventoryDoorHandler doorManager;
    private ItemStack managerItem;

    public doorManagerContainer(InventoryPlayer playerInventory, InventoryDoorHandler item, ItemStack managerItem){
        doorManager = item;
        this.managerItem = managerItem;
        addOwnSlots();
        addPlayerSlots(playerInventory);
    }

    public UUID getManager(){
        return doorManager.managerID;
    }

    private void addPlayerSlots(IInventory playerInventory) {
        // Slots for the main inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = row * 18 + 114;
                this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, x, y));
            }
        }

        // Slots for the hotbar
        for (int row = 0; row < 9; ++row) {
            int x = 8 + row * 18;
            int y = 102 + 70;
            this.addSlotToContainer(new Slot(playerInventory, row, x, y));
        }
    }

    private void addOwnSlots() {
        this.addSlotToContainer(new CardInputSlot(doorManager, 0, 116, 87));
        this.addSlotToContainer(new CardOutputSlot(doorManager, 1, 151, 87));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public void detectAndSendChanges() {//copy/pasted from super method
        boolean mainInvChange = false;
        for (int i = 0; i < this.inventorySlots.size(); ++i)
        {
            Slot currSlot = this.inventorySlots.get(i);
            ItemStack itemstack = currSlot.getStack();
            ItemStack itemstack1 = this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack))
            {
                boolean clientStackChanged = !ItemStack.areItemStacksEqualUsingNBTShareTag(itemstack1, itemstack);
                itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);
                if(currSlot.getSlotIndex() == 0 || currSlot.getSlotIndex() == 1)
                    mainInvChange = true;

                if (clientStackChanged)
                    for (int j = 0; j < this.listeners.size(); ++j)
                    {
                        ((IContainerListener)this.listeners.get(j)).sendSlotContents(this, i, itemstack1);
                    }
            }
        }
        if(mainInvChange)
            managerItem.setTagCompound(doorManager.writeToNBT(managerItem.getTagCompound()));
    }

    public boolean writeCard(EntityPlayer player, DoorHandler.DoorIdentifier id, String displayName){
        boolean work = doorManager.writeCard(id, displayName);
        if(work && player instanceof EntityPlayerMP)
            ((EntityPlayerMP)player).sendContainerToPlayer(this);
        return work;
    }
}
