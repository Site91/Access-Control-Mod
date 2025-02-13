package com.cadergator10.advancedbasesecurity.common;

import com.cadergator10.advancedbasesecurity.client.gui.EditUserGUI;
import com.cadergator10.advancedbasesecurity.common.inventory.doorManagerContainer;
import com.cadergator10.advancedbasesecurity.common.items.ItemDoorManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

/**
 * Registers all GUIs that need parity between the client and server.
 * ATM, only the user editing page needs it since that's the only one.
 */

public class GuiHandler implements IGuiHandler {
    private static final int[] itemGUI = {1}; //All the GUIs that relate to an item

    private boolean hasIndex(int index){ //get the index
        for(int i : itemGUI){
            if(i == index)
                return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) { //return the Container to the server
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
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) { //return the GUIContainer to the Client.
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
