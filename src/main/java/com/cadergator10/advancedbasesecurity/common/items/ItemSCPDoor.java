package com.cadergator10.advancedbasesecurity.common.items;

import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import com.cadergator10.advancedbasesecurity.common.blocks.doors.BlockMetalDoor;
import net.minecraft.block.Block;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;

public class ItemSCPDoor extends ItemDoor {
    public static ItemStack DEFAULTSTACK;

    public ItemSCPDoor(){
        this(BlockMetalDoor.DEFAULTITEM);
    }

    @SuppressWarnings("ConstantConditions")
    ItemSCPDoor(Block block) {
        super(block);
        setTranslationKey(block.getTranslationKey());
        setRegistryName(block.getRegistryName());
        setCreativeTab(ContentRegistry.CREATIVETAB);
    }
}
