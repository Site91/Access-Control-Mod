package com.cadergator10.advancedbasesecurity.common.items;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.globalsystems.DoorHandler;
import com.cadergator10.advancedbasesecurity.common.inventory.InventoryDoorHandler;
import com.cadergator10.advancedbasesecurity.common.networking.DoorNamePacket;
import com.cadergator10.advancedbasesecurity.common.networking.ManagerNamePacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.UUID;

public class ItemDoorManager extends ItemBase {
	public static final String NAME = "door_manager";
	public static ItemStack DEFAULTSTACK;

	public ItemDoorManager() {
		super(NAME);
	}

	private EnumActionResult openMenu(ItemStack heldItem, EntityPlayerMP player){
		ManagerTag tag = new ManagerTag(heldItem);
		if(tag.managerID == null){
			ManagerNamePacket packet = new ManagerNamePacket(AdvBaseSecurity.instance.doorHandler.getAllowedManagers(player));
			AdvBaseSecurity.instance.network.sendTo(packet, player);
			return EnumActionResult.SUCCESS;
		}
		DoorHandler.Doors door = AdvBaseSecurity.instance.doorHandler.getDoorManager(tag.managerID);
		if(door == null){
			player.sendMessage(new TextComponentString("Door manager missing. May have been deleted or misconfigured. Please use the item again to set manager."));
			tag.managerID = null;
			heldItem.setTagCompound(tag.writeToNBT(new NBTTagCompound()));
			heldItem.setStackDisplayName(getItemName());
			return EnumActionResult.FAIL;
		}
		//send door names
		DoorNamePacket packet = new DoorNamePacket(door);
		AdvBaseSecurity.instance.network.sendTo(packet, player);
		return EnumActionResult.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) {
		ItemStack heldItem = player.getHeldItemMainhand();
		if (!(heldItem.getItem() instanceof ItemDoorManager))
			return super.onItemRightClick(worldIn, player, handIn);
		return new ActionResult<ItemStack>(!worldIn.isRemote && player instanceof EntityPlayerMP ? openMenu(heldItem, (EntityPlayerMP) player) : EnumActionResult.SUCCESS,player.getHeldItem(handIn));
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!worldIn.isRemote && player instanceof EntityPlayerMP) {
			ItemStack heldItem = player.getHeldItemMainhand();
			if (!(heldItem.getItem() instanceof ItemDoorManager))
				return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
			if (player.isSneaking()) { //perform actions on other stuff
				return EnumActionResult.PASS;
			} else {
				return openMenu(heldItem, (EntityPlayerMP) player);
			}
			//return EnumActionResult.FAIL;
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	public static String getItemName(){ //shorten the return of this.
		return new TextComponentTranslation("item." + AdvBaseSecurity.MODID + "." + NAME).getUnformattedText();
	}

	public static class ManagerTag { //inspired by OpenSecurity's approach. a lot is as I learn
		public UUID managerID;
		public int currentScanMode = 0; //0 = none, 1 = door
		public UUID doorIDScan;
		public InventoryDoorHandler inventory = new InventoryDoorHandler();

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
				if(nbt.hasKey("scanmode"))
					currentScanMode = nbt.getInteger("scanmode");
				else
					currentScanMode = 0;
				if(nbt.hasUniqueId("scanID"))
					doorIDScan = nbt.getUniqueId("scanID");
				else
					doorIDScan = null;
				if(nbt.hasKey("inventory"))
					inventory = new InventoryDoorHandler(nbt.getCompoundTag("inventory"), managerID); //managerID passed to container yeah
				else
					inventory = new InventoryDoorHandler(managerID);
			}
		}

		public NBTTagCompound writeToNBT(NBTTagCompound nbt){
			if(managerID != null)
				nbt.setUniqueId("managerID", managerID);
			nbt.setTag("inventory", inventory.writeToNBT(new NBTTagCompound()));
			nbt.setInteger("scanmode", currentScanMode);
			if(doorIDScan != null)
				nbt.setUniqueId("scanID", doorIDScan);
			return nbt;
		}
	}
}