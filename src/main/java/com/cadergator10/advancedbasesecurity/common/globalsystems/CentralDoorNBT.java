package com.cadergator10.advancedbasesecurity.common.globalsystems;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class CentralDoorNBT extends WorldSavedData { //World Save Data for the list of independent doors in the world. Doesn't do too much, just stores data
    public static CentralDoorNBT instance;

    public static final String DATA_NAME = AdvBaseSecurity.MODID + "_doornbt";

    public List<doorHoldr> doors = new LinkedList<>();

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
        doors = new LinkedList<>();
        if(nbt.hasKey("doors")){
            NBTTagList tags = nbt.getTagList("doors", Constants.NBT.TAG_COMPOUND);
            for(int i=0; i<tags.tagCount(); i++){
                NBTTagCompound tag = tags.getCompoundTagAt(i);
                if(tag.hasUniqueId("id")){
                    doorHoldr door = new doorHoldr();
                    door.deviceId = tag.getUniqueId("id");
                    if(tag.hasUniqueId("cloned"))
                        door.clonedId = tag.getUniqueId("cloned");
                    else
                        door.clonedId = null;
                    if(tag.hasUniqueId("clonedM"))
                        door.clonedManager = tag.getUniqueId("clonedM");
                    else
                        door.clonedManager = null;
                    doors.add(door);
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for(doorHoldr door : doors){
            NBTTagCompound tag = new NBTTagCompound();
            tag.setUniqueId("id", door.deviceId);
            if(door.clonedId != null)
                tag.setUniqueId("cloned", door.clonedId);
            if(door.clonedManager != null)
                tag.setUniqueId("clonedM", door.clonedManager);
            list.appendTag(tag);
        }
        nbt.setTag("doors", list);
        return nbt;
    }

    public UUID getIndDoorManager(UUID deviceID){
        for(doorHoldr door : doors){
            if(door.deviceId.equals(deviceID)){
                return door.clonedManager;
            }
        }
        return null;
    }

    public static class doorHoldr{
        public doorHoldr(){

        }
        public doorHoldr(UUID id){
            deviceId = id;
            clonedId = null;
            clonedManager = null;
        }
        public doorHoldr(UUID id, UUID cloned, UUID clonedManager){
            this.deviceId = id;
            this.clonedId = cloned;
            this.clonedManager = clonedManager;
        }
        public UUID deviceId;
        public UUID clonedId;
        public UUID clonedManager;
    }
}
