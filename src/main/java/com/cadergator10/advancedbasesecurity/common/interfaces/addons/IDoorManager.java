package com.cadergator10.advancedbasesecurity.common.interfaces.addons;

import net.minecraft.nbt.NBTTagCompound;

//Base class for any of the NBT extenders that will be added by any addon mods
public interface IDoorManager {
    public enum type {GLOBAL, DOOR, PASS, USER, SECTOR};
    public type DataType = null;

    public String addonId();
    public void readFromNBT(NBTTagCompound nbt);
    public NBTTagCompound writeToNBT(NBTTagCompound nbt);
    public String performAction(String action, String param1, String param2);

    public int returnInt(String key);
    public String returnString(String key);
    public boolean returnBool(String key);
    public void setInt(String key, int value);
    public void setString(String key, String value);
    public void setBool(String key, boolean value);
}
