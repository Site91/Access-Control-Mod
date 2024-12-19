package com.cadergator10.advancedbasesecurity.common.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class ItemLinkingCard extends ItemBase {
	public static final String NAME = "linking_card";
	public static ItemStack DEFAULTSTACK;

	public ItemLinkingCard() {
		super(NAME);
	}

	public static class CardTag { //inspired by OpenSecurity's approach. a lot is as I learn
		public DoorHandler.DoorIdentifier doorId = null;

		public CardTag(ItemStack stack){
			if(stack.getItem() instanceof ItemLinkingCard)
				readFromNBT(stack.getTagCompound());
		}

		public CardTag(NBTTagCompound nbt){
			readFromNBT(nbt);
		}

		public void readFromNBT(NBTTagCompound nbt){
			if(nbt != null) {
				if (nbt.hasKey("identifier")) {
					doorId = new DoorHandler.DoorIdentifier(nbt.getCompoundTag("identifier"));
				}
			}
		}

		public NBTTagCompound writeToNBT(NBTTagCompound nbt){
			if(doorId != null)
				nbt.setTag("identifier", doorId.writeToNBT(new NBTTagCompound()));
			return nbt;
		}
	}
}