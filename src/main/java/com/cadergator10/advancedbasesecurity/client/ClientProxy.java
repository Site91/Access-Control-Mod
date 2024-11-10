package com.cadergator10.advancedbasesecurity.client;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.renderer.RenderCardReader;
import com.cadergator10.advancedbasesecurity.common.CommonProxy;
import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityCardReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {


    @Override
    public World getWorld(int dimId) {
        World world = Minecraft.getMinecraft().world;
        return world.provider.getDimension() == dimId ? world : null;
    }

    @Override
    public void preinit(FMLPreInitializationEvent event) {
        super.preinit(event);
        //Config.clientPreInit();

        MinecraftForge.EVENT_BUS.register(this);

        //ModelLoaderRegistry.registerLoader(new CamouflageBlockModelLoader());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCardReader.class, new RenderCardReader());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        Minecraft mc = Minecraft.getMinecraft();
//        mc.getItemColors().registerItemColorHandler(new CardColorHandler(), ItemRFIDCard.DEFAULTSTACK.getItem());
//        mc.getItemColors().registerItemColorHandler(new CardColorHandler(), ItemMagCard.DEFAULTSTACK.getItem());

//        CamouflageBakedModel.initTextures();

//        Item.getItemFromBlock(BlockEnergyTurret.DEFAULTITEM).setTileEntityItemStackRenderer(new EnergyTurretRenderHelper());
    }

    @Override
    public void registerModels() {
        for(Block block : ContentRegistry.modBlocks)
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName().toString(), "inventory"));

        // BlockNanoFog uses custom texture/model loader for shield blocks
//        for(Block block : ContentRegistry.modCamoBlocks)
//            CamouflageBlockModelLoader.registerBlock((BlockCamouflage) block);

        for(ItemStack itemStack : ContentRegistry.modBlocksWithItem.values())
            ModelLoader.setCustomModelResourceLocation(itemStack.getItem(), 0, new ModelResourceLocation(itemStack.getItem().getRegistryName().toString(), "inventory"));

        for(ItemStack itemStack : ContentRegistry.modItems)
            ModelLoader.setCustomModelResourceLocation(itemStack.getItem(), 0, new ModelResourceLocation(itemStack.getItem().getRegistryName().toString(), "inventory"));

//        ModelLoader.setCustomStateMapper(BlockRolldoorElement.DEFAULTITEM, new StateMap.Builder().ignore(BlockRolldoorElement.PROPERTYOFFSET).build());
//        ModelLoader.setCustomStateMapper(BlockSecureDoor.DEFAULTITEM, new StateMap.Builder().ignore(BlockDoor.POWERED).build());
//        ModelLoader.setCustomStateMapper(BlockSecurePrivateDoor.DEFAULTITEM, new StateMap.Builder().ignore(BlockDoor.POWERED).build());
//        ModelLoader.setCustomStateMapper(BlockSecureMagDoor.DEFAULTITEM, new StateMap.Builder().ignore(BlockDoor.POWERED).build());
    }

    private void listFilesForPath(final File path) {
//        AlarmResource r = new AlarmResource();
//        int i = 1;
//
//        for(File fileEntry : FileUtils.listFilesForPath(path.getPath()))
//            r.addSoundReferenceMapping(i++, fileEntry.getName()); //add map soundlocation -> recordX
//
//        r.registerAsResourceLocation(); //finalise IResourcePack
    }

    @Override
    public void registerSounds() {
//        File alarmSounds = new File("./mods/OpenSecurity/assets/opensecurity/sounds/alarms");
//
//        if (!alarmSounds.exists())
//            return;
//
//        for(File file : alarmSounds.listFiles())
//            if (file.isFile())
//                AdvBaseSecurity.instance.alarmList.add(file.getName());
//
//        listFilesForPath(alarmSounds);
    }
}
