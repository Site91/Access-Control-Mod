package com.cadergator10.advancedbasesecurity.common;

import com.cadergator10.advancedbasesecurity.client.gui.EditUserGUI;
import com.cadergator10.advancedbasesecurity.common.inventory.doorManagerContainer;
import com.cadergator10.advancedbasesecurity.common.items.ItemDoorManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {
    private static final int[] itemGUI = {1};

    private boolean hasIndex(int index){
        for(int i : itemGUI){
            if(i == index)
                return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if(hasIndex(ID)){ //go off of item held instead
            ItemStack item = null;
            if(player.getHeldItemMainhand().getItem() instanceof ItemDoorManager)
                item = player.getHeldItemMainhand();
            else if(player.getHeldItemOffhand().getItem() instanceof ItemDoorManager)
                item = player.getHeldItemOffhand();
            if(item != null){
                if(item.getItem() instanceof ItemDoorManager)
                    return new doorManagerContainer(player.inventory, new ItemDoorManager.ManagerTag(item).inventory, item);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if(hasIndex(ID)){ //go off of item held instead
            ItemStack item = null;
            if(player.getHeldItemMainhand().getItem() instanceof ItemDoorManager)
                item = player.getHeldItemMainhand();
            else if(player.getHeldItemOffhand().getItem() instanceof ItemDoorManager)
                item = player.getHeldItemOffhand();
            if(item != null){
                if(item.getItem() instanceof ItemDoorManager)
                    return new EditUserGUI(new doorManagerContainer(player.inventory, new ItemDoorManager.ManagerTag(item).inventory, item));
            }
        }
        return null;
    }
}
