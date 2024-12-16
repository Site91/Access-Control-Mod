package com.cadergator10.advancedbasesecurity.client.model;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.blocks.BlockCamo;
import com.cadergator10.advancedbasesecurity.common.interfaces.camo.CamoBlockId;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class CamoflageBakedModel implements IBakedModel {
    public static final ModelResourceLocation modelFacade = new ModelResourceLocation(AdvBaseSecurity.MODID + ":" + BlockCamo.CAMO);

    private static TextureAtlasSprite particleTexture;

    public CamoflageBakedModel(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {}

    @SideOnly(Side.CLIENT)
    public static void initTextures() {
        if(particleTexture == null)
            particleTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(AdvBaseSecurity.MODID + ":blocks/nanofog");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if(state == null)
            return Collections.emptyList();

        try {
            IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
            CamoBlockId facadeId = extendedBlockState.getValue(BlockCamo.CAMOID);
            if (facadeId == null) {
                return Collections.emptyList();
            }

            IBlockState facadeState = facadeId.getBlockState();
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            if (layer != null && !facadeState.getBlock().canRenderInLayer(facadeState, layer)) { // always render in the null layer or the block-breaking textures don't show up
                return Collections.emptyList();
            }

            if(!(facadeState.getBlock() instanceof BlockCamo) && !(facadeState.getBlock().equals(Blocks.AIR)))
                return getModel(facadeState).getQuads(state, side, rand);
        }catch (Exception e){}

        try {
            ModelResourceLocation loc = new ModelResourceLocation(state.getBlock().getRegistryName().toString(), "inventory");
            IBakedModel modelWhenNotCamouflaged = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(loc);
            return modelWhenNotCamouflaged.getQuads(state, side, rand);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @SideOnly(Side.CLIENT)
    private IBakedModel getModel(@Nonnull IBlockState facadeState) {
        initTextures();
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(facadeState);
        return model;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return particleTexture;
    }

    @Override
    @Deprecated
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
