package com.cadergator10.advancedbasesecurity.common.items;

import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import com.cadergator10.advancedbasesecurity.common.blocks.doors.BlockDoorBase;
import com.cadergator10.advancedbasesecurity.common.blocks.doors.BlockSCPDoor;
import net.minecraft.block.Block;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;

public class ItemSCPDoor extends ItemDoor {
    public static ItemStack DEFAULTSTACK;

    public ItemSCPDoor(){
        this(BlockSCPDoor.DEFAULTITEM);
    }

    @SuppressWarnings("ConstantConditions")
    ItemSCPDoor(Block block) {
        super(block);
        setTranslationKey(block.getTranslationKey());
        setRegistryName(block.getRegistryName());
        setCreativeTab(ContentRegistry.CREATIVETAB);
    }
}
