package com.cadergator10.advancedbasesecurity.common.blocks.doors;

import baubles.api.BaublesApi;
import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import com.cadergator10.advancedbasesecurity.common.globalsystems.CentralDoorNBT;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoor;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoorControl;
import com.cadergator10.advancedbasesecurity.common.items.IDCard;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import com.cadergator10.advancedbasesecurity.common.items.ItemScrewdriver;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityDoor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentScore;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class BlockDoorBase extends BlockDoor implements ITileEntityProvider {
	public final static String NAME = "door_base";

	public BlockDoorBase(){
		this(NAME, Material.IRON);
	}
	BlockDoorBase(String name, Material mat) {
		super(mat);
		setRegistryName(AdvBaseSecurity.MODID, name);
		setTranslationKey("advancedbasesecurity." + name);
		setCreativeTab(ContentRegistry.CREATIVETAB);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		IDoor te;
		if(state.getValue(HALF) == EnumDoorHalf.LOWER) {
			te = (IDoor) worldIn.getTileEntity(pos);
			if (!worldIn.isRemote) {
				te.newId();
				if (!AdvBaseSecurity.instance.doorHandler.allDoors.containsKey(te.getId())) {
					AdvBaseSecurity.instance.doorHandler.allDoors.put(te.getId(), te);
				}
				te.onPlace();
			}
		}
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if(worldIn.isRemote) {
			AdvBaseSecurity.instance.logger.info("Deleting door block now!");
			BlockPos blockpos = state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
			IDoor te = (IDoor) worldIn.getTileEntity(blockpos);
			AdvBaseSecurity.instance.doorHandler.allDoors.remove(te.getId());
			for (CentralDoorNBT.doorHoldr door : AdvBaseSecurity.instance.doorHandler.IndDoors.doors) {
				if (door.deviceId.equals(te.getId())) {
					AdvBaseSecurity.instance.doorHandler.IndDoors.doors.remove(door);
					AdvBaseSecurity.instance.doorHandler.IndDoors.markDirty();
					break;
				}
			}
		}
		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileEntityDoor();
	}

	public static BlockPos getOtherDoorPart(World world, BlockPos thisPos) {
		if (world.getTileEntity(new BlockPos(thisPos.getX(), thisPos.getY() + 1, thisPos.getZ()))  instanceof TileEntityDoor){
			return new BlockPos(thisPos.getX(), thisPos.getY() + 1, thisPos.getZ());
		} else {
			return new BlockPos(thisPos.getX(), thisPos.getY() - 1, thisPos.getZ());
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote && hand == EnumHand.MAIN_HAND) {
			BlockPos blockpos = state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
			IBlockState iblockstate = pos.equals(blockpos) ? state : worldIn.getBlockState(blockpos);
			TileEntityDoor tile = (TileEntityDoor) worldIn.getTileEntity(blockpos);
			ItemStack heldItem = null;
			if (!player.getHeldItemMainhand().isEmpty() && (player.getHeldItemMainhand().getItem() instanceof ItemScrewdriver || player.getHeldItemMainhand().getItem() instanceof ItemLinkingCard)) {
				heldItem = player.getHeldItemMainhand();
			} else if (!player.getHeldItemOffhand().isEmpty() && (player.getHeldItemOffhand().getItem() instanceof ItemScrewdriver || player.getHeldItemMainhand().getItem() instanceof ItemLinkingCard)) {
				heldItem = player.getHeldItemOffhand();
			}
			if (heldItem != null && tile != null) { //change the door type or link
				if(heldItem.getItem() instanceof ItemLinkingCard){
//					tile.setDoor(heldItem); //disabled since it isn't being added like that.
				}
				else {
					tile.pushDoor = !tile.pushDoor;
					tile.markDirty();
					if (tile.pushDoor)
						player.sendMessage(new TextComponentString("Changed door type to push"));
					else
						player.sendMessage(new TextComponentString("Changed door type to automatic"));
				}
				return true;
			}
			if (tile == null || !tile.pushDoor)
				return false;
			if (iblockstate.getValue(OPEN) || (tile.getDoor() != null && tile.getDoor().getDoorStateFromDoor(tile.getId()))) {
				toggleDoor(worldIn, pos, !iblockstate.getValue(OPEN));
				worldIn.playSound(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, ((Boolean) state.getValue(OPEN)).booleanValue() ? SoundEvents.BLOCK_IRON_DOOR_OPEN : SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1F, 1F);
				return true;
			}
		}
		return false;
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos){
		if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
			BlockPos blockpos = pos.down();
			IBlockState iblockstate = worldIn.getBlockState(blockpos).getActualState(worldIn, pos);

			if (iblockstate.getBlock() != this)
				worldIn.setBlockToAir(pos);
			else if (blockIn != this)
				iblockstate.neighborChanged(worldIn, blockpos, blockIn, fromPos);
		}
		else {
			boolean flag1 = false;
			BlockPos blockpos1 = pos.up();
			IBlockState iblockstate1 = worldIn.getBlockState(blockpos1);

			if (iblockstate1.getBlock() != this){
				worldIn.setBlockToAir(pos);
				flag1 = true;
			}

			if (!worldIn.getBlockState(pos.down()).isSideSolid(worldIn,  pos.down(), EnumFacing.UP)) {
				worldIn.setBlockToAir(pos);
				flag1 = true;

				if (iblockstate1.getBlock() == this)
					worldIn.setBlockToAir(blockpos1);
			}

			if (flag1 && !worldIn.isRemote)
				this.dropBlockAsItem(worldIn, pos, state, 0);

		}
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityDoor();
	}
}
