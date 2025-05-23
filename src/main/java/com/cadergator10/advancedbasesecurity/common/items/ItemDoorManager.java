package com.cadergator10.advancedbasesecurity.common.items;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.blocks.BlockSectorController;
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

/**
 * Used to control ALL functions of the doormanager and therefore all doors.
 * Sneak right click for global stuff.
 * Right click on blocks to perform functions
 * 		SectorController: change its settings
 * 		linking: Reader/DoorController: link a door to the currently selected door.
 * 		(soon) nonlinking: Reader/DoorController: edit a door that the device is linked to
 */
public class ItemDoorManager extends ItemBase {
	public static final String NAME = "door_manager";
	public static ItemStack DEFAULTSTACK;

	public ItemDoorManager() {
		super(NAME);
	}

	private EnumActionResult openMenu(ItemStack heldItem, EntityPlayerMP player){ //Performs checks whether the manager can be opened and is the right hand etc.
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
		DoorNamePacket packet = new DoorNamePacket(door, tag.currentScanMode == 0, door.allowedPlayers);
		AdvBaseSecurity.instance.network.sendTo(packet, player);
		return EnumActionResult.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) { //When block right clicked in air or general.
		ItemStack heldItem = player.getHeldItemMainhand();
		if (!(heldItem.getItem() instanceof ItemDoorManager))
			return super.onItemRightClick(worldIn, player, handIn);
		return new ActionResult<ItemStack>(!worldIn.isRemote && player instanceof EntityPlayerMP && player.isSneaking() ? openMenu(heldItem, (EntityPlayerMP) player) : EnumActionResult.PASS,player.getHeldItem(handIn));
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) { //When block right clicked on block.
		if(!worldIn.isRemote && player instanceof EntityPlayerMP) {
			ItemStack heldItem = player.getHeldItemMainhand();
			if (!(heldItem.getItem() instanceof ItemDoorManager))
				return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
			if (!player.isSneaking() || worldIn.getBlockState(pos).getBlock() instanceof BlockSectorController) { //perform actions on other stuff
				return EnumActionResult.SUCCESS;
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
				AdvBaseSecurity.instance.logger.info(nbt);
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
				inventory = new InventoryDoorHandler(nbt, managerID); //managerID passed to container yeah
			}
		}

		public NBTTagCompound writeToNBT(NBTTagCompound nbt){
			if(managerID != null)
				nbt.setUniqueId("managerID", managerID);
			AdvBaseSecurity.instance.logger.info(inventory.writeToNBT(new NBTTagCompound()));
			nbt = inventory.writeToNBT(nbt);
			nbt.setInteger("scanmode", currentScanMode);
			if(doorIDScan != null)
				nbt.setUniqueId("scanID", doorIDScan);
			AdvBaseSecurity.instance.logger.info(nbt);
			return nbt;
		}
	}
}