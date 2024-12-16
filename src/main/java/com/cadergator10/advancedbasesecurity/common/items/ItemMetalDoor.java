package com.cadergator10.advancedbasesecurity.common.items;

import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import com.cadergator10.advancedbasesecurity.common.blocks.doors.BlockMetalDoor;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemMetalDoor extends ItemDoorFix {
    public static ItemStack DEFAULTSTACK;

    public ItemMetalDoor(){
        this(BlockMetalDoor.DEFAULTITEM);
    }

    @SuppressWarnings("ConstantConditions")
    ItemMetalDoor(Block block) {
        super(block);
        setTranslationKey(block.getTranslationKey());
        setRegistryName(block.getRegistryName());
        setCreativeTab(ContentRegistry.CREATIVETAB);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        EnumActionResult result = super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        if(result == EnumActionResult.SUCCESS){

        }
        return result;
    }
}
