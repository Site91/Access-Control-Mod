package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.common.interfaces.ICamo;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityCamoBase extends TileEntityDeviceBase implements ICamo { //once again: pretty much OpenSecurity's implementation cause it works
    MimicBlock mimicBlock = new MimicBlock();

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if(nbt.hasKey("camo"))
            mimicBlock.readFromNBT(nbt.getCompoundTag("camo"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("camo", mimicBlock.writeToNBT(new NBTTagCompound()));
        return super.writeToNBT(nbt);
    }

    @Override
    public IBlockState getCamoBlock() {
        return mimicBlock.get();
    }

    @Deprecated
    @Override
    public void setCamoBlock(Block block, int meta) {
        mimicBlock.set(block, meta);
        markDirtyClient();
    }

    public void markDirtyClient() {
        markDirty();
        if (getWorld() != null) {
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }
    }
}
