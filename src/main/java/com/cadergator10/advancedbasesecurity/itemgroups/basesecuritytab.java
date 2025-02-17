package com.cadergator10.advancedbasesecurity.itemgroups;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

public class basesecuritytab extends CreativeTabs { //The tab... das it

    public basesecuritytab() {
        super(getNextID(), "tabBaseSecurity");
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(Item.getItemFromBlock(Blocks.IRON_BARS));
    }

    @Override
    public String getTranslationKey() {
        return new TextComponentTranslation("itemGroup.advancedbasesecurity.tabbasesecurity").getUnformattedText();
        //return "itemGroup.advancedbasesecurity.tabbasesecurity";
    }
}
