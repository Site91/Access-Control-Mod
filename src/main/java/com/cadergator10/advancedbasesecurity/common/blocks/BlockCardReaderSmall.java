package com.cadergator10.advancedbasesecurity.common.blocks;

import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityCardReaderSmall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockCardReaderSmall extends BlockCardReader {
	public static final String NAME = "card_reader_small";

	public BlockCardReaderSmall() {
		super(NAME);
	}

	@Override
	@Deprecated
	public boolean isBlockNormalCube(IBlockState blockState) {
		return false;
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		float px = 1.0F / 16.0F; //one sixteenth of a block
		float fourPX = px * 4;
		float fivePX = px * 5;
		float ySideMin = 0.5F - fourPX; //bottom of block when placed on a block side
		float ySideMax = 0.5F + fourPX; //top of block when placed on a block side
		float hSideMin = 0.5F - fivePX; //the left start for s/w and right start for n/e
		float hSideMax = 0.5F + fivePX; //the left start for n/e and right start for s/w
		switch (state.getValue(PROPERTYFACING).getHorizontalIndex()) { //S-W-N-E
			case 1: //east
				return new AxisAlignedBB(0.0F, ySideMin - px, hSideMin - px, px * 2, ySideMax + px, hSideMax + px);
			case 3: //west
				return new AxisAlignedBB(px * 14, ySideMin - px, hSideMin - px, 1.0F, ySideMax + px, hSideMax + px);
			case 2: //north
				return new AxisAlignedBB(hSideMin - px, ySideMin - px, 0.0F, hSideMax + px, ySideMax + px, px * 2);
			case 0: //south
				return new AxisAlignedBB(hSideMin - px, ySideMin - px, px * 14, hSideMax + px, ySideMax + px, 1.0F);
			default:
				return state.getBoundingBox(source, pos);
		}
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityCardReaderSmall();
	}


}
