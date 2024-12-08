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

public class CentralDoorNBT extends WorldSavedData {
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
            tag.setUniqueId("cloned", door.clonedId);
            list.appendTag(tag);
        }
        nbt.setTag("doors", list);
        return nbt;
    }

    public static class doorHoldr{
        public doorHoldr(){

        }
        public doorHoldr(UUID id){
            deviceId = id;
            clonedId = null;
        }
        public doorHoldr(UUID id, UUID cloned){
            this.deviceId = id;
            this.clonedId = cloned;
        }
        public UUID deviceId;
        public UUID clonedId;
    }
}
