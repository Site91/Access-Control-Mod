package com.cadergator10.advancedbasesecurity.common.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

import java.util.UUID;

public class IDCard extends ItemBase implements IBauble {
	public static final String NAME = "id_card";
	public static ItemStack DEFAULTSTACK;

	public IDCard() {
		super(NAME);
	}

	@Override
	public BaubleType getBaubleType(ItemStack itemStack) {
		return BaubleType.AMULET;
	}

	public static class CardTag { //inspired by OpenSecurity's approach. a lot is as I learn
		public DoorHandler.DoorIdentifier cardId = null;
		public UUID playerId = null;
		public int color = 0xFFFFFF;

		public CardTag(ItemStack stack){
			if(stack.getItem() instanceof IDCard)
				readFromNBT(stack.getTagCompound());
		}

		public CardTag(NBTTagCompound nbt){
			readFromNBT(nbt);
		}

		public void readFromNBT(NBTTagCompound nbt){
			if(nbt != null) {
				if (nbt.hasKey("cardId"))
					cardId = new DoorHandler.DoorIdentifier(nbt.getCompoundTag("cardId"));
				if (nbt.hasUniqueId("playerId"))
					playerId = nbt.getUniqueId("playerId");

				if (nbt.hasKey("display", 10)) {
					NBTTagCompound displayTag = nbt.getCompoundTag("display");
					if (displayTag.hasKey("color", 3)) {
						color = displayTag.getInteger("color");
					}
				}
			}
		}

		public NBTTagCompound writeToNBT(NBTTagCompound nbt){
			if(playerId != null)
				nbt.setUniqueId("playerId", playerId);
			if(cardId != null)
				nbt.setTag("cardId", cardId.writeToNBT(new NBTTagCompound()));

			NBTTagCompound displayTag = new NBTTagCompound();
			displayTag.setInteger("color", color);

			nbt.setTag("display", displayTag);

			return nbt;
		}
	}
}