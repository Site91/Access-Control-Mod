package com.cadergator10.advancedbasesecurity.common.blocks;

import baubles.api.BaublesApi;
import baubles.api.IBauble;
import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import com.cadergator10.advancedbasesecurity.common.interfaces.IReader;
import com.cadergator10.advancedbasesecurity.common.items.IDCard;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityCardReader;
import com.cadergator10.advancedbasesecurity.util.ReaderText;
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

public class BlockCardReader extends Block implements ITileEntityProvider {
	public static final String NAME = "card_reader";
	public static Block DEFAULTITEM;
	public BlockCardReader() {
		super(Material.IRON);
		setTranslationKey("advancedbasesecurity." + NAME);
		setRegistryName(AdvBaseSecurity.MODID, NAME);
		setHardness(0.5f);
		setCreativeTab(ContentRegistry.CREATIVETAB);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityCardReader();
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntity te = worldIn.getTileEntity(pos);
		((IReader) te).updateVisuals(7, new ReaderText("Disconnected", (byte) 4));
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack heldItem;
		int baubleID = BaublesApi.isBaubleEquipped(player, IDCard.DEFAULTSTACK.getItem());
		if(baubleID != -1){ //equipped in Baubles slot
			heldItem = BaublesApi.getBaublesHandler(player).getStackInSlot(baubleID);
		}
		else if (!player.getHeldItemMainhand().isEmpty() && (player.getHeldItemMainhand().getItem() instanceof IDCard || player.getHeldItemMainhand().getItem() instanceof ItemLinkingCard)) {
			heldItem = player.getHeldItemMainhand();
		} else if (!player.getHeldItemOffhand().isEmpty() && (player.getHeldItemOffhand().getItem() instanceof IDCard || player.getHeldItemMainhand().getItem() instanceof ItemLinkingCard)) {
			heldItem = player.getHeldItemOffhand();
		} else {
			return false;
		}

		if (!heldItem.isEmpty()) {
			System.out.println(heldItem.getItem().getRegistryName().toString());
			Item equipped = heldItem.getItem();
			TileEntityCardReader tile = (TileEntityCardReader) world.getTileEntity(pos);

			if (!world.isRemote) {
				if(equipped instanceof IDCard)
					tile.doRead(heldItem, player, side);
				else if(equipped instanceof ItemLinkingCard)
					tile.setDoor(heldItem);
			}
			return true;
		}
		return false;
	}
}
