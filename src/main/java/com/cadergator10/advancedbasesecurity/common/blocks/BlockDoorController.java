package com.cadergator10.advancedbasesecurity.common.blocks;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoorControl;
import com.cadergator10.advancedbasesecurity.common.items.ItemDoorManager;
import com.cadergator10.advancedbasesecurity.common.items.ItemScrewdriver;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityDoorController;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockDoorController extends BlockCamo implements ITileEntityProvider {
	public final static String NAME = "door_control";
	public static BlockDoorController DEFAULTITEM;

	public BlockDoorController() {
		super(Material.IRON);
		setTranslationKey("advancedbasesecurity." + NAME);
		setRegistryName(AdvBaseSecurity.MODID, NAME);
		setHardness(0.5f);
		setCreativeTab(ContentRegistry.CREATIVETAB);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		IDoorControl te = (IDoorControl) worldIn.getTileEntity(pos);
		if (!worldIn.isRemote) {
			te.newId();
			if (!AdvBaseSecurity.instance.doorHandler.allDoorControllers.containsKey(te.getId()))
				AdvBaseSecurity.instance.doorHandler.allDoorControllers.put(te.getId(), te);
		}
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		super.onBlockHarvested(worldIn, pos, state, player);
		if(!worldIn.isRemote) {
			IDoorControl te = (IDoorControl) worldIn.getTileEntity(pos);
			AdvBaseSecurity.instance.doorHandler.allDoorControllers.remove(te.getId());
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}


	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityDoorController();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		boolean rightClick = super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
		if(rightClick)
			return true;
		ItemStack heldItem;
		if (!player.getHeldItemMainhand().isEmpty() && (player.getHeldItemMainhand().getItem() instanceof ItemDoorManager || player.getHeldItemMainhand().getItem() instanceof ItemScrewdriver)) {
			heldItem = player.getHeldItemMainhand();
		} else if (!player.getHeldItemOffhand().isEmpty() && (player.getHeldItemMainhand().getItem() instanceof ItemDoorManager || player.getHeldItemMainhand().getItem() instanceof ItemScrewdriver)) {
			heldItem = player.getHeldItemOffhand();
		} else {
			return false;
		}

		if (!heldItem.isEmpty()) {
			Item equipped = heldItem.getItem();
			TileEntityDoorController tile = (TileEntityDoorController) world.getTileEntity(pos);

			if (!world.isRemote) {
				if(equipped instanceof ItemDoorManager)
					tile.setDoor(heldItem);
				else
					tile.linkDoors();
			}
			return true;
		}
		return false;
	}
}
