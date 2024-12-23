package com.cadergator10.advancedbasesecurity.common.items;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.ManagerNamePacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class ItemDoorManager extends ItemBase {
	public static final String NAME = "door_manager";
	public static ItemStack DEFAULTSTACK;

	public ItemDoorManager() {
		super(NAME);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!worldIn.isRemote && player instanceof EntityPlayerMP) {
			ItemStack heldItem = player.getHeldItemMainhand();
			if (!(heldItem.getItem() instanceof ItemDoorManager))
				return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
			ManagerTag tag = new ManagerTag(heldItem);
			if (player.isSneaking()) { //perform actions on other stuff

			} else {
				if(tag.managerID == null){
					ManagerNamePacket packet = new ManagerNamePacket(AdvBaseSecurity.instance.doorHandler.getAllowedManagers(player));
					AdvBaseSecurity.instance.network.sendTo(packet, (EntityPlayerMP) player);
				}
			}
			return EnumActionResult.FAIL;
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	public static class ManagerTag { //inspired by OpenSecurity's approach. a lot is as I learn
		public UUID managerID;
		public ItemStack unwrittenCard;
		public ItemStack writtenCard;

		public ManagerTag(ItemStack stack){
			if(stack.getItem() instanceof ItemDoorManager)
				readFromNBT(stack.getTagCompound());
		}

		public ManagerTag(NBTTagCompound nbt){
			readFromNBT(nbt);
		}

		public void readFromNBT(NBTTagCompound nbt){
			if(nbt != null) {
				if (nbt.hasUniqueId("managerID")) {
					managerID = nbt.getUniqueId("managerID");
				}
				if(nbt.hasKey("slotunwritten"))
					unwrittenCard = new ItemStack(nbt.getCompoundTag("slotunwritten"));
				else
					unwrittenCard = ItemStack.EMPTY;
				if(nbt.hasKey("slotwritten"))
					writtenCard = new ItemStack(nbt.getCompoundTag("slotwritten"));
				else
					writtenCard = ItemStack.EMPTY;
			}
		}

		public NBTTagCompound writeToNBT(NBTTagCompound nbt){
			if(managerID != null)
				nbt.setUniqueId("managerID", managerID);
			nbt.setTag("slotunwritten", unwrittenCard.writeToNBT(new NBTTagCompound()));
			nbt.setTag("slotwritten", writtenCard.writeToNBT(new NBTTagCompound()));
			return nbt;
		}
	}
}