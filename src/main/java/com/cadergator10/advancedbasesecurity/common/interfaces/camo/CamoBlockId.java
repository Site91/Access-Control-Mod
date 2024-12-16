package com.cadergator10.advancedbasesecurity.common.interfaces.camo;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

//Credit all to OpenSecurity
public class CamoBlockId {
    private final String registryName;
    private final int meta;

    public CamoBlockId(IBlockState mimicBlock) {
        Block block = mimicBlock.getBlock();
        this.registryName = block.getRegistryName().toString();
        this.meta = block.getMetaFromState(mimicBlock);
    }

    public IBlockState getBlockState() {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(registryName)).getStateFromMeta(meta);
    }

    @Override
    public String toString() {
        return registryName + '@' + meta;
    }
}
