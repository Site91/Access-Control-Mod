package com.cadergator10.advancedbasesecurity.common.blocks.doors;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityDoor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockDoorBase extends BlockDoor {
	protected BlockDoorBase(String name) {
		super(Material.IRON);
		setRegistryName(AdvBaseSecurity.MODID, name);
		setTranslationKey("advancedbasesecurity." + name);
		setCreativeTab(ContentRegistry.CREATIVETAB);
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
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos){
		if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
			BlockPos blockpos = pos.down();
			IBlockState iblockstate = worldIn.getBlockState(blockpos);

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





}
