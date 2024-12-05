package com.cadergator10.advancedbasesecurity.common.globalsystems;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class CentralDoorNBT extends WorldSavedData {
    public static CentralDoorNBT instance;

    public static final String DATA_NAME = AdvBaseSecurity.MODID + "_doornbt";

    public CentralDoorNBT(String name) {
        super(name);
    }

    public CentralDoorNBT(){
        super(DATA_NAME);
    }

    public static CentralDoorNBT get(World world) {
        MapStorage storage = world.getMapStorage();
        instance = (CentralDoorNBT) storage.getOrLoadData(CentralDoorNBT.class, DATA_NAME);

        if (instance == null) {
            instance = new CentralDoorNBT();
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return null;
    }
}
