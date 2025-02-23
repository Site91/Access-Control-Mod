package com.cadergator10.advancedbasesecurity.common.interfaces.addons;

import net.minecraft.nbt.NBTTagCompound;

//Base class for any of the NBT extenders that will be added by any addon mods
public interface IDoorManager {
    public enum type {GLOBAL, DOOR, PASS, USER, SECTOR};
    public type DataType = null;

    public IDoorManager createNew();

    public String addonId();
    public void readFromNBT(NBTTagCompound nbt);
    public NBTTagCompound writeToNBT(NBTTagCompound nbt);

}
