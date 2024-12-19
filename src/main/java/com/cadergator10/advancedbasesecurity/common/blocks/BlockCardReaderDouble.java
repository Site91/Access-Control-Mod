package com.cadergator10.advancedbasesecurity.common.blocks;

import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityCardReader;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityCardReaderDouble;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityCardReaderSmall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockCardReaderDouble extends BlockCardReader {
	public static final String NAME = "card_reader_double";

	public BlockCardReaderDouble() {
		super(NAME);
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		int sided = side.getOpposite().getHorizontalIndex();
		int required = blockState.getBlock().getMetaFromState(blockState);
		int required2 = required - 2;
		if(required2 < 0)
			required2 += 4;
		return sided != required && sided != required2;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityCardReaderDouble();
	}
}
