package com.cadergator10.advancedbasesecurity.common.items;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemScrewdriver extends Item {
    public static final String NAME = "screwdriver";
    public static ItemStack DEFAULTSTACK;
    public ItemScrewdriver() {
        setTranslationKey("advancedbasesecurity." + NAME);
        setRegistryName(AdvBaseSecurity.MODID, NAME);
        setCreativeTab(ContentRegistry.CREATIVETAB);
    }
}
