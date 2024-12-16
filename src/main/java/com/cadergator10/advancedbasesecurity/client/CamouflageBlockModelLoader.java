package com.cadergator10.advancedbasesecurity.client;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.model.CamoModel;
import com.cadergator10.advancedbasesecurity.client.model.CamoflageBakedModel;
import com.cadergator10.advancedbasesecurity.common.blocks.BlockCamo;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;

import java.util.HashMap;

public class CamouflageBlockModelLoader implements ICustomModelLoader {
    public static HashMap<String, CamoModel> camoModelBlocks = new HashMap<>();


    static {
        camoModelBlocks.put(BlockCamo.CAMO, new CamoModel());
    }

    public static void registerBlock(BlockCamo block){
        ModelResourceLocation modelLocationDefault = new ModelResourceLocation(block.getRegistryName().toString(), "inventory");
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, modelLocationDefault);

        ModelResourceLocation modelLocation = new ModelResourceLocation(block.getRegistryName().toString());
        camoModelBlocks.put(modelLocation.getPath(), new CamoModel());
        block.initModel(CamoflageBakedModel.modelFacade);
        //use 'invalid' metaindex to register another model for the same block
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 1, modelLocation);
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (!modelLocation.getNamespace().equals(AdvBaseSecurity.MODID)) {
            return false;
        }
        if (modelLocation instanceof ModelResourceLocation && ((ModelResourceLocation)modelLocation).getVariant().equals("inventory")) {
            return false;
        }

        return camoModelBlocks.containsKey(modelLocation.getPath());
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        return camoModelBlocks.containsKey(modelLocation.getPath()) ? camoModelBlocks.get(modelLocation.getPath()) : null;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {}
}
