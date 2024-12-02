package com.cadergator10.advancedbasesecurity.common.blocks;

import baubles.api.BaublesApi;
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
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class BlockCardReader extends Block implements ITileEntityProvider {
	public static final String NAME = "card_reader";
	public static Block DEFAULTITEM;

	public static final PropertyDirection PROPERTYFACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockCardReader() {
		super(Material.IRON);
		setTranslationKey("advancedbasesecurity." + NAME);
		setRegistryName(AdvBaseSecurity.MODID, NAME);
		setHardness(0.5f);
		setCreativeTab(ContentRegistry.CREATIVETAB);
	}
	public BlockCardReader(String name) {
        super(Material.IRON);
        setTranslationKey("advancedbasesecurity." + name);
		setRegistryName(AdvBaseSecurity.MODID, name);
		setHardness(0.5f);
		setCreativeTab(ContentRegistry.CREATIVETAB);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityCardReader();
	}

	@Override
	@Deprecated
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = EnumFacing.byHorizontalIndex(meta);
		return getDefaultState().withProperty(PROPERTYFACING, facing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int facingbits = getFacing(state).getHorizontalIndex();
		return facingbits;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, PROPERTYFACING);
	}

	@Override
	@Deprecated
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		EnumFacing enumfacing = (placer == null) ? EnumFacing.NORTH : EnumFacing.fromAngle(placer.rotationYaw);
		return getDefaultState().withProperty(PROPERTYFACING, enumfacing);
	}

	public static EnumFacing getFacing(IBlockState state){
		return state.getValue(PROPERTYFACING);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntity te = worldIn.getTileEntity(pos);
		if (!worldIn.isRemote && !AdvBaseSecurity.instance.doorHandler.allReaders.containsKey(((TileEntityCardReader) te).getId())) {
			AdvBaseSecurity.instance.doorHandler.allReaders.put(((TileEntityCardReader) te).getId(), ((IReader) te));
			((IReader) te).updateVisuals(7, new ReaderText("Disconnected", (byte) 4));
		}
	}

	@Override
	@Deprecated
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return side.getOpposite().getHorizontalIndex() != blockState.getBlock().getMetaFromState(blockState);
	}

	@Override
	@Deprecated
	public boolean isBlockNormalCube(IBlockState blockState) {
		return true;
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState blockState) {
		return true;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack heldItem;
		int baubleID = BaublesApi.isBaubleEquipped(player, IDCard.DEFAULTSTACK.getItem());
		if (!player.getHeldItemMainhand().isEmpty() && (player.getHeldItemMainhand().getItem() instanceof IDCard || player.getHeldItemMainhand().getItem() instanceof ItemLinkingCard)) {
			heldItem = player.getHeldItemMainhand();
		} else if (!player.getHeldItemOffhand().isEmpty() && (player.getHeldItemOffhand().getItem() instanceof IDCard || player.getHeldItemMainhand().getItem() instanceof ItemLinkingCard)) {
			heldItem = player.getHeldItemOffhand();
		} else if(baubleID != -1){ //equipped in Baubles slot
			heldItem = BaublesApi.getBaublesHandler(player).getStackInSlot(baubleID);
		} else {
			return false;
		}

		if (!heldItem.isEmpty()) {
			System.out.println(heldItem.getItem().getRegistryName().toString());
			Item equipped = heldItem.getItem();
			TileEntityCardReader tile = (TileEntityCardReader) world.getTileEntity(pos);

			if (!world.isRemote) {
				if(equipped instanceof IDCard)
					tile.readCard(heldItem, player, side);
				else if(equipped instanceof ItemLinkingCard)
					tile.setDoor(heldItem);
			}
			return true;
		}
		return false;
	}


}
