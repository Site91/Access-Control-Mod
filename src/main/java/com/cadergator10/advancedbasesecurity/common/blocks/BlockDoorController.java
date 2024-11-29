package com.cadergator10.advancedbasesecurity.common.blocks;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDevice;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityDoorController;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityDoorRedstone;
import net.minecraft.block.Block;
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

public class BlockDoorController extends Block implements ITileEntityProvider {
	public final static String NAME = "door_control";
	public static BlockDoorController DEFAULTITEM;

	public BlockDoorController() {
		super(Material.IRON);
		setTranslationKey("advancedbasesecurity." + NAME);
		setRegistryName(AdvBaseSecurity.MODID, NAME);
		setHardness(0.5f);
		setCreativeTab(ContentRegistry.CREATIVETAB);
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntity te = worldIn.getTileEntity(pos);
		((IDevice) te).newId();
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
		ItemStack heldItem;
		if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemLinkingCard) {
			heldItem = player.getHeldItemMainhand();
		} else if (!player.getHeldItemOffhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemLinkingCard) {
			heldItem = player.getHeldItemOffhand();
		} else {
			return false;
		}

		if (!heldItem.isEmpty()) {
			System.out.println(heldItem.getItem().getRegistryName().toString());
			Item equipped = heldItem.getItem();
			TileEntityDoorController tile = (TileEntityDoorController) world.getTileEntity(pos);

			if (!world.isRemote) {
				if(equipped instanceof ItemLinkingCard)
					tile.setDoor(heldItem);
			}
			return true;
		}
		return false;
	}
}
