package com.cadergator10.advancedbasesecurity.common.items;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import net.minecraft.item.Item;

//base item. Nothing to it.
public abstract class ItemBase extends Item {
	ItemBase(String name) {
		setTranslationKey("advancedbasesecurity." + name);
		setRegistryName(AdvBaseSecurity.MODID, name);
		setCreativeTab(ContentRegistry.CREATIVETAB);
	}
}
