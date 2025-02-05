package com.cadergator10.advancedbasesecurity.common.blocks;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import com.cadergator10.advancedbasesecurity.common.items.ItemDoorManager;
import com.cadergator10.advancedbasesecurity.common.networking.SectControllerPacket;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntitySectorController;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockSectorController extends Block implements ITileEntityProvider {
	public final static String NAME = "sector_control";
	public static BlockSectorController DEFAULTITEM;

	public BlockSectorController(){
		super(Material.IRON);
		setTranslationKey("advancedbasesecurity." + NAME);
		setRegistryName(AdvBaseSecurity.MODID, NAME);
		setHardness(0.5f);
		setCreativeTab(ContentRegistry.CREATIVETAB);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntitySectorController();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(hand == EnumHand.MAIN_HAND){
			ItemStack heldItem;
			if(!playerIn.getHeldItemMainhand().isEmpty() &&  playerIn.getHeldItemMainhand().getItem() instanceof ItemDoorManager)
				heldItem = playerIn.getHeldItemMainhand();
			else
				return false;
			if(!heldItem.isEmpty() && playerIn.isSneaking()){
				Item equipped = heldItem.getItem();
				TileEntitySectorController te = (TileEntitySectorController) worldIn.getTileEntity(pos);
				if(!worldIn.isRemote && te != null) {
					ItemDoorManager.ManagerTag tag = new ItemDoorManager.ManagerTag(heldItem);
					if(tag.managerID == null)
						return false;
					boolean good = te.setFirstTime(tag.managerID);
					if(good) {
						SectControllerPacket packet = new SectControllerPacket(te);
						AdvBaseSecurity.instance.network.sendTo(packet, (EntityPlayerMP) playerIn);
						return true;
					}
					else
						playerIn.sendMessage(new TextComponentString("Sector Controller is linked to a different manager ID"));
				}
			}
		}
		return false;
		//TODO: Add in functionality
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);

			if (tile instanceof TileEntitySectorController) {
				TileEntitySectorController te = (TileEntitySectorController) tile;
				te.redstoneSignalRecieved(world.isBlockPowered(pos));
//				if (te.isActivatedByRedstone()) {
//					te.setActive(world.isBlockPowered(pos));
//					te.sync();
//				}
			}
		}
	}
}
