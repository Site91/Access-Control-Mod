package com.cadergator10.advancedbasesecurity.common;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.blocks.BlockCardReader;
import com.cadergator10.advancedbasesecurity.common.blocks.BlockCardReaderSmall;
import com.cadergator10.advancedbasesecurity.common.blocks.BlockDoorController;
import com.cadergator10.advancedbasesecurity.common.blocks.BlockDoorRedstone;
import com.cadergator10.advancedbasesecurity.common.blocks.doors.BlockDoorBase;
import com.cadergator10.advancedbasesecurity.common.blocks.doors.BlockSCPDoor;
import com.cadergator10.advancedbasesecurity.common.items.IDCard;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import com.cadergator10.advancedbasesecurity.common.items.ItemSCPDoor;
import com.cadergator10.advancedbasesecurity.common.tileentity.*;
import com.cadergator10.advancedbasesecurity.itemgroups.basesecuritytab;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.gui.ForgeGuiFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Mod.EventBusSubscriber
public class ContentRegistry { //where all new items will be added

    // holds a list of normal mod blocks
    public static final HashSet<Block> modBlocks = new HashSet<>();

    // holds a list of mod blocks that have a specific custom Item like the doors
    public static final HashMap<Block, ItemStack> modBlocksWithItem = new HashMap<>();

    // holds a list of normal mod items
    public static final HashSet<ItemStack> modItems = new HashSet<>();

    public static final CreativeTabs CREATIVETAB = new basesecuritytab();

    static {
//        modBlocks.add(BlockAlarm.DEFAULTITEM = new BlockAlarm());
//        modBlocks.add(BlockSecurityTerminal.DEFAULTITEM = new BlockSecurityTerminal());
        modBlocks.add(BlockCardReader.DEFAULTITEM = new BlockCardReader());
        modBlocks.add(BlockCardReaderSmall.DEFAULTITEM = new BlockCardReaderSmall());
        modBlocks.add(BlockDoorRedstone.DEFAULTITEM = new BlockDoorRedstone());
        modBlocks.add(BlockDoorController.DEFAULTITEM = new BlockDoorController());
//
//        modBlocksWithItem.put(BlockSecureDoor.DEFAULTITEM = new BlockSecureDoor(), ItemSecureDoor.DEFAULTSTACK = new ItemStack(new ItemSecureDoor()));
//        modBlocksWithItem.put(BlockSecurePrivateDoor.DEFAULTITEM = new BlockSecurePrivateDoor(), ItemSecurePrivateDoor.DEFAULTSTACK = new ItemStack(new ItemSecurePrivateDoor()));
        modBlocksWithItem.put(BlockSCPDoor.DEFAULTITEM = new BlockSCPDoor(), ItemSCPDoor.DEFAULTSTACK = new ItemStack(new ItemSCPDoor()));
//
//        modItems.add(ItemRFIDCard.DEFAULTSTACK = new ItemStack(new ItemRFIDCard()));
//        modItems.add(ItemMagCard.DEFAULTSTACK = new ItemStack(new ItemMagCard()));
        modItems.add(IDCard.DEFAULTSTACK = new ItemStack(new IDCard()));
        modItems.add(ItemLinkingCard.DEFAULTSTACK = new ItemStack(new ItemLinkingCard()));
    }


    @SubscribeEvent
    public static void addBlocks(RegistryEvent.Register<Block> event) {
        for(Block block : modBlocks)
            event.getRegistry().register(block);

        for(Block block : modBlocksWithItem.keySet())
            event.getRegistry().register(block);

        registerTileEntity(TileEntityCardReader.class, BlockCardReader.NAME);
        registerTileEntity(TileEntityCardReaderSmall.class, BlockCardReaderSmall.NAME);
        registerTileEntity(TileEntityDoorRedstone.class, BlockDoorRedstone.NAME);
        registerTileEntity(TileEntityDoorController.class, BlockDoorController.NAME);
        registerTileEntity(TileEntityDoor.class, BlockDoorBase.NAME);
    }

    private static void registerTileEntity(Class<? extends TileEntity> tileEntityClass, String key) {
        // For better readability
        GameRegistry.registerTileEntity(tileEntityClass, new ResourceLocation(AdvBaseSecurity.MODID, key));
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void addItems(RegistryEvent.Register<Item> event) {

        for(Block block : modBlocks)
            event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));

        for(Map.Entry<Block, ItemStack> entry : modBlocksWithItem.entrySet())
            event.getRegistry().register(entry.getValue().getItem());

        for(ItemStack itemStack : modItems)
            event.getRegistry().register(itemStack.getItem());
    }
}
