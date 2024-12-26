package com.cadergator10.advancedbasesecurity.common.inventory;

import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.items.IDCard;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

import java.util.UUID;

public class InventoryDoorHandler implements IInventory {
    public final NonNullList<ItemStack> inv = NonNullList.withSize(2, ItemStack.EMPTY);
    public final UUID managerID;
    /** The stack currently held by the mouse cursor */

    public InventoryDoorHandler(){
        managerID = null;
    }
    public InventoryDoorHandler(UUID id){
        managerID = id;
    }
    public InventoryDoorHandler(NBTTagCompound tag){
        this.readFromNBT(tag);
        managerID = null;
    }
    public InventoryDoorHandler(NBTTagCompound tag, UUID id){
        this.readFromNBT(tag);
        managerID = id;
    }

    public UUID getManager(){
        return managerID;
    }


    @Override
    public int getSizeInventory() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack item : inv)
            if(!item.isEmpty())
                return false;
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < inv.size() ? inv.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return index >= 0 && index < inv.size() && !inv.get(index).isEmpty() ? ItemStackHelper.getAndSplit(inv, index, count) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if(index >= 0 && index < inv.size() && !inv.get(index).isEmpty()){
            ItemStack item = inv.get(index);
            inv.set(index, ItemStack.EMPTY);
            return item;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if(index >= 0 && index < inv.size()){
            inv.set(index, stack);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        //auto generated stub
    }

    public void markDirty(ItemStack item){

    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public String getName() {
        return "container.doorhandler";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    public void readFromNBT(NBTTagCompound nbt){
        if(nbt.hasKey("unwritten"))
            inv.set(0, new ItemStack(nbt.getCompoundTag("unwritten")));
        if(nbt.hasKey("written"))
            inv.set(1, new ItemStack(nbt.getCompoundTag("written")));
    }
    public NBTTagCompound writeToNBT(NBTTagCompound nbt){
        if(!inv.get(0).isEmpty())
            nbt.setTag("unwritten", inv.get(0).writeToNBT(new NBTTagCompound()));
        if(!inv.get(1).isEmpty())
            nbt.setTag("written", inv.get(1).writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    public boolean writeCard(DoorHandler.DoorIdentifier id, String displayName){
        if(inv.get(0).isEmpty()) //perform all checks to ensure that stuff isn't blocking the creation, whether missing a card or the output slot is blocked.
            return false; //check if no cards exist
        ItemStack item = inv.get(1);
        if(!item.isEmpty() && item.getTagCompound().hasKey("cardId")){ //check if card in output slot and make sure that it is the same card to continue or if different to block
            DoorHandler.DoorIdentifier tag = new DoorHandler.DoorIdentifier(item.getTagCompound().getCompoundTag("cardId"));
            if(!tag.DoorID.equals(id.DoorID) || !tag.ManagerID.equals(id.ManagerID) || !item.getDisplayName().equals(displayName))
                return false;
        }
        if(!item.isEmpty() && item.getCount() >= item.getMaxStackSize()) //too many in slot
            return false;
        //begin creation
        ItemStack newCard = ItemStackHelper.getAndSplit(inv, 0, 1);
        if(!item.isEmpty()) //just increase stack size
            item.grow(1);
        else { //add card with data to output
            IDCard.CardTag tag = new IDCard.CardTag(newCard);
            tag.cardId = id;
            newCard.setTagCompound(tag.writeToNBT(new NBTTagCompound()));
            newCard.setStackDisplayName(displayName);
            inv.set(1, newCard);
        }
        return true;
    }
}
