package com.cadergator10.advancedbasesecurity.common.blocks.doors;

import com.cadergator10.advancedbasesecurity.common.items.ItemMetalDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockGlassDoor extends BlockDoorBase{
    public static final String NAME = "glass_door";
    public static BlockGlassDoor DEFAULTITEM;

    public BlockGlassDoor() {
        super(NAME, Material.GLASS);
    }

    @Nonnull
    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(getItem());
    }

    @Nonnull
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(HALF) == EnumDoorHalf.UPPER ? Items.AIR : getItem();
    }

    protected Item getItem() {
        return ItemMetalDoor.DEFAULTSTACK.getItem();
    }
}
