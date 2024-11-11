package com.cadergator10.advancedbasesecurity.common.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
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
		public UUID doorId = UUID.randomUUID();

		public CardTag(ItemStack stack){
			if(stack.getItem() instanceof ItemLinkingCard)
				readFromNBT(stack.getTagCompound());
		}

		public CardTag(NBTTagCompound nbt){
			readFromNBT(nbt);
		}

		public void readFromNBT(NBTTagCompound nbt){
			if(nbt != null) {
				if (nbt.hasKey("cardId"))
					doorId = nbt.getUniqueId("cardId");
			}
		}

		public NBTTagCompound writeToNBT(NBTTagCompound nbt){
			nbt.setUniqueId("doorId", doorId);
			return nbt;
		}
	}
}