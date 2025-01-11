package com.cadergator10.advancedbasesecurity.common.inventory.slot;

import com.cadergator10.advancedbasesecurity.common.items.SwipeCard;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardInputSlot extends Slot implements ISlotTooltip{
    public CardInputSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof SwipeCard;
    }

    @Override
    public List<String> getTooltip() {
        return new ArrayList<>(Collections.singletonList("Accepts ID cards"));
    }
}
