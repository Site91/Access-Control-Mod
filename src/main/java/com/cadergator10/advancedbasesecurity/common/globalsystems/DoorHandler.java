package com.cadergator10.advancedbasesecurity.common.globalsystems;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.config.DoorConfig;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDevice;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoor;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoorControl;
import com.cadergator10.advancedbasesecurity.common.interfaces.IReader;
import com.cadergator10.advancedbasesecurity.util.ReaderText;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.function.BiConsumer;

public class DoorHandler {
    public DoorData doorData;
    public HashMap<UUID, Doors> DoorGroups;
    public CentralDoorNBT IndDoors;
    public boolean loaded = false;

    public HashMap<UUID, IReader> allReaders;
    public HashMap<UUID, IDoorControl> allDoorControllers;
    public HashMap<UUID, IDoor> allDoors;

    public DoorHandler(){
        AdvBaseSecurity.instance.logger.info("Loaded DoorHandler!");
        //world.getMapStorage().getOrLoadData(Doors.class, DATA_NAME);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldLoad(WorldEvent.Load event){
        doorTime = Integer.MIN_VALUE;
        if(!event.getWorld().isRemote && !loaded) {
            doorData = DoorData.get(event.getWorld());
            AdvBaseSecurity.instance.logger.info("Found " + doorData.doors.size() + " doors to load!");
            DoorGroups = new HashMap<>();
            for(UUID id : doorData.doors){
                Doors door = Doors.get(event.getWorld(), id.toString());
                for (Doors.OneDoor doore : door.doors) {
                    if (doore.isDoorOpen == 1) { //any timed doors add to list
                        door.timedDoors.add(doore);
                    }
                }
                DoorGroups.put(id, door);
                AdvBaseSecurity.instance.logger.info("Door Manager with ID " + id.toString() + " and name " + door.name + " loaded!");
            }
            AdvBaseSecurity.instance.logger.info("Successfully loaded");
            IndDoors = CentralDoorNBT.get(event.getWorld());
            loaded = true;
            allReaders = new HashMap<>();
            allDoorControllers = new HashMap<>();
            allDoors = new HashMap<>();
        }
    }

    public void onWorldUnload(FMLServerStoppedEvent event){
        if(loaded) {
            AdvBaseSecurity.instance.logger.info("World unloading. Removing door stuff");
            loaded = false;
            DoorGroups = null;
            DoorData.instance = null;
            IndDoors = null;
            CentralDoorNBT.instance = null;
            editValidator = null;
            allReaders = null;
            allDoorControllers = null;
            allDoors = null;
        }
    }



    public static int doorTime = Integer.MIN_VALUE;

    static class cacheHolder{
        public cacheHolder(){

        }
        public cacheHolder(UUID user, UUID door, int time, int worked){
            this.userID = user;
            this.doorID = door;
            this.time = time;
            this.worked = worked;
        }
        public UUID userID;
        public UUID doorID;
        public int time;
        public int worked;
    }

    public UUID editValidator;

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event){
        if(event.phase == TickEvent.Phase.START) {
            if (DoorConfig.cachetime != 0)
                doorTime++;
            for(Doors door : DoorGroups.values()){
                door.onTick(event, doorTime);
            }
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event){ //when chunks unload, detect if any readers/doors are unloaded, and remove from list accordingly.
        if(!event.getWorld().isRemote) {
            Map<BlockPos, TileEntity> map = event.getChunk().getTileEntityMap();
            BiConsumer<BlockPos, TileEntity> biConsumer = (k, v) -> {
                if (v instanceof IDevice) {
                    IDevice dev = (IDevice) v;
                    if (dev.getDevType().equals("reader"))
                        allReaders.remove(dev.getId());
                    else if (dev.getDevType().equals("doorcontrol"))
                        allDoorControllers.remove(dev.getId());
                    else if (dev.getDevType().equals("door"))
                        allDoors.remove(dev.getId());
                }
            };
            map.forEach(biConsumer);
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event){
        if(!event.getWorld().isRemote) {
            Map<BlockPos, TileEntity> map = event.getChunk().getTileEntityMap();
            BiConsumer<BlockPos, TileEntity> biConsumer = (k, v) -> {
                if (v instanceof IDevice) {
                    IDevice dev = (IDevice) v;
                    if (dev.getDevType().equals("reader"))
                        allReaders.put(dev.getId(), (IReader) dev);
                    else if (dev.getDevType().equals("doorcontrol"))
                        allDoorControllers.put(dev.getId(), (IDoorControl) dev);
                    else if (dev.getDevType().equals("door"))
                        allDoors.put(dev.getId(), (IDoor) dev);
                }
            };
            map.forEach(biConsumer);
        }
    }

//    @SubscribeEvent
//    public void serverSave(WorldEvent.Save event){
//
//    }
    

    public Doors getDoorManager(DoorIdentifier id){
        return getDoorManager(id.ManagerID);
    }
    
    public Doors getDoorManager(UUID id){
        if(id != null && DoorGroups.containsKey(id)){
            return DoorGroups.get(id);
        }
        return null;
    }

    public Doors addDoorManager(EntityPlayer player, String name){
        UUID id = UUID.randomUUID();
        Doors door = Doors.get(player.world, id.toString());
        doorData.doors.add(id);
        doorData.markDirty();
        door.id = id;
        door.creator = player.getUniqueID();
        door.name = name != null ? name : "new";
        door.markDirty();
        DoorGroups.put(id, door);
        return door;
    }


    //region Reader/Door Management
    public boolean SetDevID(UUID devID, DoorIdentifier doorID, boolean isDoor){
        //make sure manager ID exists
        if(!DoorGroups.containsKey(doorID.ManagerID))
            return false;
        Doors manager = DoorGroups.get(doorID.ManagerID);
        int foundRightOne = -1;
        int index = 0;
        if(!isDoor) {

            for (Doors.OneDoor door : manager.doors) {
                if (door.doorId.equals(doorID.DoorID)) {
                    boolean contained = false;
                    for(UUID id : door.Readers)
                        if(id.equals(devID)){
                            contained = true;
                            break;
                        }
                    if(!contained) {
                        door.Readers.add(devID);
                        AdvBaseSecurity.instance.logger.info("Reader of ID " + devID.toString() + " linked to door named " + door.doorName);
                        foundRightOne = index;
                    }
                }
                index++;
            }
            if (foundRightOne != -1) {
                index = 0;
                for (Doors.OneDoor door : manager.doors) { //make sure no other door has this reader.
                    for(int j=0; j<door.Readers.size(); j++){
                        if(foundRightOne != index && door.Readers.get(j).equals(devID)){
                            door.Readers.remove(j);
                            break;
                        }
                    }
                    index++;
                }
                manager.markDirty();
            }
        }
        else{
            for (Doors.OneDoor door : manager.doors) {
                if (door.doorId.equals(doorID.DoorID)) {
                    boolean contained = false;
                    for(UUID id : door.Doors)
                        if(id.equals(devID)){
                            contained = true;
                            break;
                        }
                    if(!contained) {
                        door.Doors.add(devID);
                        AdvBaseSecurity.instance.logger.info("Door of ID " + devID.toString() + " linked to door named " + door.doorName);
                        foundRightOne = index;
                    }
                }
                index++;
            }
            if (foundRightOne != -1) {
                index = 0;
                for (Doors.OneDoor door : manager.doors) { //make sure no other door has this reader.
                    for(int j=0; j<door.Doors.size(); j++){
                        if(foundRightOne != index && door.Doors.get(j).equals(devID)){
                            door.Doors.remove(j);
                            break;
                        }
                    }
                    index++;
                }
                manager.markDirty();
            }
        }
        return foundRightOne != -1;
    }
    //endregion

//    public Doors createManager(World world, EntityPlayer player, String name){
//        UUID id = UUID.randomUUID();
//        Doors door = Doors.get(world, id.toString());
//        door.name = name;
//        if(player != null)
//            door.creator = player.getUniqueID();
//        door.markDirty();
//        DoorGroups.put(id, door);
//        doorData.doors.add(id);
//        doorData.markDirty();
//        return door;
//    }

    public List<Doors> getAllowedManagers(EntityPlayer player){
        List<Doors> doors = new LinkedList<>();
        for(Doors door : DoorGroups.values()){
            if(door.hasPerms(player))
                doors.add(door);
        }
        return doors;
    }

    public int getManagerCount(EntityPlayer player){
        int count = 0;
        for(Doors door : DoorGroups.values()){
            if(door.creator.equals(player.getUniqueID()))
                count++;
        }
        return count;
    }


    //region Door save file
    public static class Doors extends WorldSavedData{
        public int currentDoorVer = 0; //The version of the file. In case on the website it has been updated.

        public static final String DATA_NAME = AdvBaseSecurity.MODID + "_doorid_";

        public String name;
        public UUID id;
        public UUID creator;
        public List<UUID> allowedPlayers = new LinkedList<>();
        public List<OneDoor> doors = new LinkedList<>();
        public HashMap<String, PassValue> passes = new HashMap<>();
        public HashMap<UUID, Groups> groups = new HashMap<>();
        public List<Users> users = new LinkedList<>();

        //in-session stuff (not nbt)
        private List<Doors.OneDoor> timedDoors; //doors that are currently open on a timer. these are what it loops through every tick.



        public List<cacheHolder> userCache;
        
        public ModifierValidation validator;

        void quickPassAdd(){
            PassValue tempPass = new PassValue("staff");
            tempPass.passName = "Staff";
            tempPass.passType = PassValue.type.Pass;
            tempPass.groupNames = null;
            passes.put("staff", tempPass);
        }
        
        void firstTimeSetup(){
            timedDoors = new LinkedList<>();
            userCache = new LinkedList<>();
            validator = new ModifierValidation();
        }

        public Doors(){
            super(DATA_NAME);
            quickPassAdd();
            firstTimeSetup();
        }
        public Doors(String str){
            super(str);
            quickPassAdd();
            firstTimeSetup();
        }

        public static Doors get(World world, String id) {
            MapStorage storage = world.getMapStorage();
            AdvBaseSecurity.instance.logger.info("Reading or Creating door with ID " + id);
            Doors door = (Doors) storage.getOrLoadData(Doors.class, DATA_NAME + id);

            if (door == null) {
                door = new Doors(DATA_NAME + id);
                storage.setData(DATA_NAME + id, door);
            }
            door.id = UUID.fromString(id);
            return door;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            AdvBaseSecurity.instance.logger.info("Reading Door NBT");
            if(nbt.hasKey("versionNum"))
                currentDoorVer = nbt.getInteger("versionNum");
            else
                currentDoorVer = 0;
            if(nbt.hasKey("name"))
                name = nbt.getString("name");
            else
                name = "new";
            if(nbt.hasKey("owner"))
                creator = UUID.fromString(nbt.getString("owner"));
            else
                creator = null;
            allowedPlayers = new LinkedList<>();
            if(nbt.hasKey("whitelist")){
                NBTTagList list = nbt.getTagList("whitelist", Constants.NBT.TAG_STRING);
                for(int i=0; i<list.tagCount(); i++){
                    allowedPlayers.add(UUID.fromString(list.getStringTagAt(i)));
                }
            }

            //read all pass data
            this.passes = new HashMap<>();
            quickPassAdd();
            if(nbt.hasKey("passes")){
                NBTTagList tempPassList = nbt.getTagList("passes", Constants.NBT.TAG_COMPOUND);
                for(int i=0; i<tempPassList.tagCount(); i++){
                    PassValue thisPass = new PassValue(tempPassList.getCompoundTagAt(i));
                    this.passes.put(thisPass.passId, thisPass);
                }
            }

            //read all group data
            this.groups = new HashMap<>();
            if(nbt.hasKey("groups")){
                NBTTagList tempGroupList = nbt.getTagList("groups", Constants.NBT.TAG_COMPOUND);
                for(int i=0; i<tempGroupList.tagCount(); i++){
                    Groups group = new Groups(tempGroupList.getCompoundTagAt(i), passes);
                    this.groups.put(group.id, group);
                }
            }

            //read all door data
            this.doors = new LinkedList<>();
            if(nbt.hasKey("doors")){ //iterate through nbt
                NBTTagList tempDoorList = nbt.getTagList("doors", Constants.NBT.TAG_COMPOUND);
                for(int i=0; i<tempDoorList.tagCount(); i++){
                    OneDoor theDoor = new OneDoor(tempDoorList.getCompoundTagAt(i), passes, groups);
                    this.doors.add(theDoor);
                }
            }

            //read all user data
            this.users = new LinkedList<>();
            if(nbt.hasKey("users")){ //iterate through nbt
                NBTTagList tempUserList = nbt.getTagList("users", Constants.NBT.TAG_COMPOUND);
                for(int i=0; i<tempUserList.tagCount(); i++){
                    Users user = new Users(tempUserList.getCompoundTagAt(i), passes);
                    this.users.add(user);
                }
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            AdvBaseSecurity.instance.logger.info("Writing door NBT");
            nbt.setInteger("versionNum", currentDoorVer);
            if(name != null)
                nbt.setString("name", name);
            if(creator != null)
                nbt.setString("owner", creator.toString());
            NBTTagList whitelist = new NBTTagList();
            for(UUID id : allowedPlayers){
                whitelist.appendTag(new NBTTagString(id.toString()));
            }
            nbt.setTag("whitelist", whitelist);
            //write all pass data
            if(this.passes != null){
                NBTTagList list = new NBTTagList();
                BiConsumer<String, PassValue> biConsumer = (k,v) -> {
                    if(!k.equals("staff"))
                        list.appendTag(v.returnNBT());
                };
                passes.forEach(biConsumer);
                nbt.setTag("passes", list);
            }

            //write all groups
            if(this.groups != null){
                NBTTagList list = new NBTTagList();
                BiConsumer<UUID, Groups> biConsumer = (k,v) -> {
                    list.appendTag(v.returnNBT(passes));
                };
                groups.forEach(biConsumer);
                nbt.setTag("groups", list);
            }

            //write all doors
            if(this.doors != null){
                NBTTagList list = new NBTTagList();
                for(OneDoor door : doors){
                    list.appendTag(door.returnNBT(passes, groups));
                }
                nbt.setTag("doors", list);
            }

            //write all users
            if(this.users != null){
                NBTTagList list = new NBTTagList();
                for(Users user : users){
                    list.appendTag(user.returnNBT(passes));
                }
                nbt.setTag("users", list);
            }

            return nbt;
        }

        public void onTick(TickEvent.ServerTickEvent event, int timer){
            try {
                if (!timedDoors.isEmpty()) {
                    for (int i = 0; i < timedDoors.size(); i++) {
                        Doors.OneDoor door = timedDoors.get(i);
                        if (door.isDoorOpen == 1) {
                            door.currTick--;
                            if (door.currTick <= 0) {
                                door.isDoorOpen = 0;
                                pushDoorUpdate(door);
                                markDirty();
                            }
                        }

                        if (door.isDoorOpen == 0) {
                            timedDoors.remove(door);
                        }
                    }
                }
            }
            catch (Exception e){
                //nothing. Just in case above thing crashes
            }
            //cache
            if (DoorConfig.cachetime != 0) {
                for (int i = 0; i < userCache.size(); i++) {
                    if (userCache.get(i).time <= timer) {
                        userCache.remove(i);
                        i--;
                    }
                }
            }
        }

        public boolean hasPerms(EntityPlayer player){
            return creator.equals(player.getUniqueID()) || allowedPlayers.contains(player.getUniqueID());
        }

        //region Door Controls
        private void updateDoorState(Doors.OneDoor door){ //update door state of all doors that are currently loaded.
            for(UUID dev : door.Doors){
                if(AdvBaseSecurity.instance.doorHandler.allDoorControllers.containsKey(dev)){
                    AdvBaseSecurity.instance.doorHandler.allDoorControllers.get(dev).openDoor(door.isDoorOpen != 0);
                }
            }
        }
        //Check permissions list of a door
        private boolean checkPassList(List<Doors.OneDoor.OnePass> door, Doors.Users user){
            for(int i=1; i<=5; i++){ //priorities
                int isThrough = 0; //-1 = reject pass. 0 = nope. 1 = allowed base.
                for(Doors.OneDoor.OnePass pass : door){
                    if(pass.priority == i && pass.passType != Doors.OneDoor.OnePass.type.Add){
                        boolean gotIt = !pass.passID.equals("staff") ? checkPass(pass, user.passes.get(pass.passID)) : user.staff;
                        if(gotIt) {
                            if (pass.passType == Doors.OneDoor.OnePass.type.Reject) {
                                isThrough = -1;
                            }
                            else if (pass.passType == Doors.OneDoor.OnePass.type.Supreme) {
                                isThrough = 1;
                                break;
                            }
                            else if (pass.passType == Doors.OneDoor.OnePass.type.Base) {
                                //check if either reject or base pass was already evaluated to true.
                                if(isThrough != 0)
                                    continue;
                                //check all add passes
                                isThrough = 1;
                                if(!pass.addPasses.isEmpty()) {
                                    for (Doors.OneDoor.OnePass addPass : door) {
                                        if (addPass.passType == Doors.OneDoor.OnePass.type.Add) {
                                            //check add passes
                                            boolean contained = false;
                                            for(int j=0; j<pass.addPasses.size(); j++)
                                                if(pass.addPasses.get(j).equals(addPass.id)){
                                                    contained = true;
                                                    break;
                                                }
                                            if(contained && ((!pass.passID.equals("staff") && !checkPass(addPass, user.passes.get(addPass.passID))) || (pass.passID.equals("staff") && !user.staff ))){
                                                isThrough = 0;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if(isThrough == 1){
                    return true;
                }
                else if(isThrough == -1){
                    return false;
                }
            }
            return false;
        }

        //region Quick Item Retrieval Functions
        //get the door from a reader ID
        public Doors.OneDoor getDoorFromReader(UUID reader){
            for(Doors.OneDoor tempdoor : doors){
                boolean contained = false;
                for(UUID id : tempdoor.Readers)
                    if(id.equals(reader)){
                        contained = true;
                        break;
                    }
                if(contained) {
                    return tempdoor;
                }
            }
            return null;
        }

        //get the door by its ID
        public Doors.OneDoor getDoorFromID(UUID doorID){
            for(Doors.OneDoor tempdoor : doors){
                if(tempdoor.doorId.equals(doorID)){
                    return tempdoor;
                }
            }
            return null;
        }

        //get the door by its name (first one only)
        public Doors.OneDoor getDoorFromName(String doorName){
            for(Doors.OneDoor tempdoor : doors){
                if(tempdoor.doorName.equalsIgnoreCase(doorName)){
                    return tempdoor;
                }
            }
            return null;
        }

        //get the user by user ID
        public Doors.Users getUser(UUID userID){
            for(Doors.Users tempuser : users){
                if(tempuser.id.equals(userID)) {
                    return tempuser;
                }
            }
            return null;
        }
        public Doors.Users getUserByName(String userID){
            for(Doors.Users tempuser : users){
                if(tempuser.name.equals(userID)) {
                    return tempuser;
                }
            }
            return null;
        }

        //get group by ID
        public Doors.Groups getDoorGroup(UUID groupID){
            if(groups.containsKey(groupID))
                return groups.get(groupID);
            return null;
        }
        //get group by name
        public UUID getDoorGroupID(String group){
            List<UUID> grouped = new LinkedList<>();
            BiConsumer<UUID, Doors.Groups> biConsumer = (k, v) -> {
                if(v.name.equalsIgnoreCase(group))
                    grouped.add(v.id);
            };
            groups.forEach(biConsumer);
            if(!group.isEmpty())
                return grouped.get(0);
            return null;
        }
        //get children groups
        public List<UUID> getDoorGroupChildren(UUID groupID, boolean cascade){ //cascade means all children. false means only direct children
            List<UUID> grouped = new LinkedList<>();
            //Find any that are children
            BiConsumer<UUID, Doors.Groups> biConsumer = (k, v) -> {
                if(v.parentID != null && v.parentID.equals(groupID))
                    grouped.add(k);
            };
            groups.forEach(biConsumer);
            //if children found & cascade=true, call this on all others too
            if(cascade && !grouped.isEmpty()){
                for(UUID id : grouped){
                    List<UUID> newGroups = getDoorGroupChildren(id, true);
                    //delete duplicates
                    newGroups.removeIf(grouped::contains); //u -> groups.contains(u)
                    //add to groups list
                    grouped.addAll(newGroups);
                }
            }
            return grouped;
        }

        //get group hashmap as list with index
        public List<ButtonEnum.groupIndex> getGroupList(){
            List<ButtonEnum.groupIndex> groups = new LinkedList<>();
            BiConsumer<UUID,Doors.Groups> biConsumer = (k, v) -> groups.add(new ButtonEnum.groupIndex(k.toString(), v.name));
            this.groups.forEach(biConsumer);
            return groups;
        }

        //get doors by groupID
        private List<Doors.OneDoor> getDoorsByGroup(UUID groupID){
            List<Doors.OneDoor> doors = new LinkedList<>();
            for(Doors.OneDoor door : doors){
                if(door.groupID.equals(groupID))
                    doors.add(door);
            }
            return doors;
        }
        //get doors by list of groupIDs
        private List<Doors.OneDoor> getDoorsByGroup(List<UUID> groupID){
            List<Doors.OneDoor> doors = new LinkedList<>();
            for(Doors.OneDoor door : doors){
                boolean contained = false;
                for(UUID id : groupID)
                    if(id.equals(door.groupID)){
                        contained = true;
                        break;
                    }
                if(contained)
                    doors.add(door);
            }
            return doors;
        }
        //public ones
        //make new door and return
        public Doors.OneDoor addNewDoor(){
            Doors.OneDoor door = new Doors.OneDoor(true);
            doors.add(door);
            markDirty();
            return door;
        }
        //endregion

        //remove from timedDoors
        private void removeFromTimedDoors(UUID doorID){
            for (int i = 0; i < timedDoors.size(); i++) {
                if (timedDoors.get(i).doorId.equals(doorID)) {
                    timedDoors.remove(i);
                    break;
                }
            }
        }

        //push update to existing tile entities
        private void pushDoorUpdate(Doors.OneDoor door){
            for(UUID id : door.Doors){
                if(AdvBaseSecurity.instance.doorHandler.allDoorControllers.containsKey(id)){
                    AdvBaseSecurity.instance.doorHandler.allDoorControllers.get(id).openDoor(door.isDoorOpen != 0);
                }
            }
            String display;
            byte color;
            int barColor;
            int bar = door.isDoorOpen != 0 ? 4 : (door.doorStatus.getInt() < 0 ? 1 : (door.doorStatus.getInt() > 1 ? 4 : 0));
            if(door.isDoorOpen == 0){ //perform the stuff based on a closed door.
                if(door.doorStatus == Doors.OneDoor.allDoorStatuses.NO_ACCESS) {
                    display = new TextComponentTranslation("advancedbasesecurity.reader.text.noaccess").getUnformattedText();
                    color = 4;
                    barColor = 1;
                }
                else if(door.doorStatus == Doors.OneDoor.allDoorStatuses.LOCKDOWN) {
                    display = new TextComponentTranslation("advancedbasesecurity.reader.text.lockdown").getUnformattedText();
                    color = 12;
                    barColor = 1;
                }
                else {
                    display = new TextComponentTranslation("advancedbasesecurity.reader.text.idle").getUnformattedText();
                    color = (byte)(door.doorStatus == Doors.OneDoor.allDoorStatuses.OVERRIDDEN_ACCESS ? 14 : 6);
                    barColor = 0;
                }
            }
            else{ //perform based on an open door
                if(door.doorStatus == Doors.OneDoor.allDoorStatuses.ALL_ACCESS) {
                    display = new TextComponentTranslation("advancedbasesecurity.reader.text.allaccess").getUnformattedText();
                    color = 10;
                    barColor = 4;
                }
                else {
                    display = new TextComponentTranslation("advancedbasesecurity.reader.text.allowed").getUnformattedText();
                    color = 2;
                    barColor = 4;
                }
            }
            door.readerLabel = display;
            door.readerLabelColor = color;
            door.readerLights = barColor;
            for(UUID id : door.Readers){
                if(AdvBaseSecurity.instance.doorHandler.allReaders.containsKey(id)){
                    AdvBaseSecurity.instance.doorHandler.allReaders.get(id).updateVisuals(bar, new ReaderText(display, color) );
                }
            }
            //timedDoors stuff
            if(door.isDoorOpen == 1 && !timedDoors.contains(door)){
                timedDoors.add(door);
            }
            else{
                timedDoors.remove(door);
            }
            for(int i=0; i<userCache.size(); i++){ //clear cache of these door IDs
                if(userCache.get(i).doorID.equals(door.doorId)){
                    userCache.remove(i);
                    i--;
                }
            }
        }

        //push update to a group of doors
        private void pushDoorUpdateMult(List<Doors.OneDoor> door){
            for(Doors.OneDoor id : door){
                pushDoorUpdate(id);
            }
        }

        //Push state to door (internal because this should only be called by "changeDoorState")
        private void changeDoorStateInternal(Doors.OneDoor door, boolean openState, int ticks){ //if ticks = 0, toggle; if openState == -1, use default
            //check state
            if(!openState && door.isDoorOpen != 0){ //if asking to close door & door state is not already closed.
                //check timed doors list to see if it needs a remove
                if(door.isDoorOpen == 1) {
                    removeFromTimedDoors(door.doorId);
                }
                //change door values
                door.isDoorOpen = 0;
                door.currTick = 0;
                //update tile entites
                markDirty();
                pushDoorUpdate(door);
            }
            else if(openState && door.isDoorOpen == 0){ //opening a closed door
                //change door values
                door.isDoorOpen = (ticks == 0 ? 2 : 1);
                door.currTick = ticks;
                //update tile entities
                markDirty();
                pushDoorUpdate(door);
            }
            else if(openState && door.isDoorOpen != 0){ //trying to open an already open door.
                if(door.isDoorOpen == 1){ //both timed door, so update time to max
                    if(ticks != 0) //is a tick door
                        door.currTick = Math.max(door.currTick, ticks);
                    else{ //convert to being toggled open
                        removeFromTimedDoors(door.doorId);
                        door.isDoorOpen = 2;
                        door.currTick = 0;
                    }
                }
                markDirty();
                //if (door.isDoorOpen == 2) not needed due to it being a toggle.
                //pushDoorUpdate(door) not needed since door isn't needing a new state
            }
        }

        public void changeDoorState(UUID doorID){ //use default value
            Doors.OneDoor door = getDoorFromID(doorID);
            if(door == null)
                return;
            changeDoorStateInternal(door, door.defaultToggle ? door.isDoorOpen == 0 : true, door.defaultToggle ? 0 : door.defaultTick);
        }
        public void changeDoorState(UUID doorID, boolean openState, int ticks){
            Doors.OneDoor door = getDoorFromID(doorID);
            if(door == null)
                return;
            changeDoorStateInternal(door, openState, ticks);
        }

        public void verifyUserPasses(){
            List<String> exists = new LinkedList<>();
            BiConsumer<String, Doors.PassValue> bic = (s, passValue) -> exists.add(s);
            passes.forEach(bic);
            for(Doors.Users user : users){
                List<String> exists2 = new LinkedList<>();
                BiConsumer<String, Doors.Users.UserPass> bic2 = (s, passValue) -> exists2.add(s);
                user.passes.forEach(bic2);
                for (String s : exists2) { //check for deleted passes
                    if (!exists.contains(s)) {
                        user.passes.remove(s);
                    }
                }
                for (String s : exists){ //check for incorrect inputs.
                    Doors.PassValue pass = passes.get(s);
                    if(!exists2.contains(s) || user.passes.get(s).type != pass.passType.getInt() || (pass.passType == Doors.PassValue.type.Group && Integer.parseInt(user.passes.get(s).passValue.get(0)) > pass.groupNames.size())){
                        user.passes.put(s, new Doors.Users.UserPass(pass.passId,pass.passType == Doors.PassValue.type.Level || pass.passType == Doors.PassValue.type.Group ? Arrays.asList("0") : pass.passType == Doors.PassValue.type.Pass ? Arrays.asList("false") : Arrays.asList("none") , pass.passType.getInt()));
                    }
                }
            }
            markDirty();
        }

        public void updateGroups(Doors.Groups group, boolean pushToChildren){ //pushToChildren means if a change was made to a group, if it should update all child groups too (groups with parentID set to this groupID)
            //group at this point has already been updated. this function simply pushes to doors and updates children groups
            List<UUID> groups = new LinkedList<>();
            groups.add(group.id);
            if(pushToChildren) //if all groups that are children will receive this state
                groups.addAll(getDoorGroupChildren(group.id, true));
            List<Doors.OneDoor> pushUpdateDoors = getDoorsByGroup(groups); //Any doors which need a doorState push
            //get all groups and update their values correctly first
            for(UUID tempgroupID : groups){
                if(!tempgroupID.equals(group.id)) {
                    Doors.Groups tempgroup = getDoorGroup(tempgroupID);
                    tempgroup.status = group.status;
                    tempgroup.override = group.override;
                }
            }
            //get all doors and push their values
            for(Doors.OneDoor door : doors){
                if(door.groupID != null && groups.contains(door.groupID)){ //that door is part of the group tree
                    Doors.OneDoor.allDoorStatuses prev = door.doorStatus;
                    door.doorStatus = group.status;
                    door.override = (Math.abs(group.status.getInt()) == 1 ? group.override : null); //because -1 and 1 values are override ones.
                    if(door.doorStatus == Doors.OneDoor.allDoorStatuses.ALL_ACCESS)
                        door.isDoorOpen = 2; //lock open
                    else if(prev == Doors.OneDoor.allDoorStatuses.ALL_ACCESS) //revert to closed state
                        door.isDoorOpen = 0;
                    pushUpdateDoors.add(door);
                }
            }
            markDirty();
            //push update
            pushDoorUpdateMult(pushUpdateDoors);
        }

        public void recievedUpdate(UUID editValidator, Doors.OneDoor door){ //if new door settings are added from outside.
            if(validator.hasPermissions("door:" + door.doorId.toString(), editValidator)) {
                Doors.OneDoor listDoor = null;
                for (Doors.OneDoor door1 : doors) {
                    if (door1.doorId.equals(door.doorId)) {
                        listDoor = door1;
                        break;
                    }
                }
                if (listDoor != null) {
                    listDoor.doorName = door.doorName;
                    listDoor.passes = door.passes;
                    listDoor.defaultToggle = door.defaultToggle;
                    listDoor.defaultTick = door.defaultTick;
                    listDoor.Readers = door.Readers;
                    listDoor.Doors = door.Doors;
                    //check if the group needs an update
                    boolean pushDoor = false;
                    if((listDoor.groupID != null && door.groupID != null && !listDoor.groupID.equals(door.groupID)) || (listDoor.groupID == null && door.groupID != null)) {
                        Doors.Groups group = getDoorGroup(door.groupID);
                        if (group != null) {
                            listDoor.doorStatus = group.status;
                            listDoor.override = group.override;
                            pushDoor = true;
                            //don't bother with isDoorOpen because it'll be done in pushDoorUpdate;
                        }
                    }
                    else if(door.groupID == null && listDoor.groupID != null) { //remove group, so revert to default access
                        listDoor.doorStatus = Doors.OneDoor.allDoorStatuses.ACCESS;
                        listDoor.override = null;
//                        if(listDoor.isDoorOpen != 0){ //will need to push the door update
//                            listDoor.isDoorOpen = 0;
//                            listDoor.currTick = 0;
//							timedDoors.remove(listDoor); //if it exists here
//                            pushDoor = true;
//                        }
                        pushDoor = true; //commented all back stuff out since pushDoorUpdate does it all
                    }
                    listDoor.groupID = door.groupID;
                    markDirty();
                    if(pushDoor)
                        pushDoorUpdate(listDoor);
                }
            }
        }

        public int getReaderLight(UUID id){
            for(Doors.OneDoor door : doors){
                for(int i=0; i<door.Readers.size(); i++){
                    if(door.Readers.get(i).equals(id)){
                        return door.readerLights;
                    }
                }
            }
            return 0;
        }
        public ReaderText getReaderLabel(UUID id){
            for(Doors.OneDoor door : doors){
                for(int i=0; i<door.Readers.size(); i++){
                    if(door.Readers.get(i).equals(id)){
                        AdvBaseSecurity.instance.logger.info("Reader label:" + door.readerLabel + " for the id " + id);
                        return new ReaderText(door.readerLabel, door.readerLabelColor);
                    }
                }
            }
            return new ReaderText("Disconnected", (byte) 4);
        }
        public boolean getDoorState(UUID id){
            for(Doors.OneDoor door : doors){
                for(int i=0; i<door.Doors.size(); i++){
                    if(door.Doors.get(i).equals(id)){
                        return door.isDoorOpen != 0;
                    }
                }
            }
            return false;
        }
        public boolean getDoorStateFromDoor(UUID id){ //based on individual doors on load instead
            for(CentralDoorNBT.doorHoldr door : AdvBaseSecurity.instance.doorHandler.IndDoors.doors){
                if(door.deviceId.equals(id)){
                    if(door.clonedId != null){
                        return getDoorState(door.clonedId);
                    }
                    return false;
                }
            }
            return false;
        }

        public List<String> getUserNames(){
            List<String> names = new LinkedList<>();
            for(Doors.Users user : users){
                names.add(user.name);
            }
            return names;
        }

        public List<String> getDoorNames(){
            List<String> names = new LinkedList<>();
            for(Doors.OneDoor dooor : doors){
                names.add(dooor.doorName);
            }
            return names;
        }

        public List<String> getGroupNames(){
            List<String> names = new LinkedList<>();
            for(Doors.Groups dooor : groups.values()){
                names.add(dooor.name);
            }
            return names;
        }

        public CentralDoorNBT.doorHoldr indDoorsContains(UUID id){
            for(int i=0; i<AdvBaseSecurity.instance.doorHandler.IndDoors.doors.size(); i++){
                if(AdvBaseSecurity.instance.doorHandler.IndDoors.doors.get(i).deviceId.equals(id))
                    return AdvBaseSecurity.instance.doorHandler.IndDoors.doors.get(i);
            }
            return null;
        }

        public List<CentralDoorNBT.doorHoldr> getIndDoors(UUID clonedID){
            List<CentralDoorNBT.doorHoldr> doors = new LinkedList<>();
            for(int i=0; i<AdvBaseSecurity.instance.doorHandler.IndDoors.doors.size(); i++){
                if(AdvBaseSecurity.instance.doorHandler.IndDoors.doors.get(i).clonedId != null && AdvBaseSecurity.instance.doorHandler.IndDoors.doors.get(i).clonedId.equals(clonedID))
                    doors.add(AdvBaseSecurity.instance.doorHandler.IndDoors.doors.get(i));
            }
            return doors;
        }

        public void toggleIndDoors(UUID clonedID, boolean toggle){
            List<UUID> doors = new LinkedList<>();
            for(int i=0; i<AdvBaseSecurity.instance.doorHandler.IndDoors.doors.size(); i++){
                if(AdvBaseSecurity.instance.doorHandler.IndDoors.doors.get(i).clonedId != null && AdvBaseSecurity.instance.doorHandler.IndDoors.doors.get(i).clonedId.equals(clonedID))
                    doors.add(AdvBaseSecurity.instance.doorHandler.IndDoors.doors.get(i).deviceId);
            }
            BiConsumer<UUID, IDoor> bic = (k, v) -> {
                if(doors.contains(k)){
                    v.openDoor(toggle);
                }
            };
            AdvBaseSecurity.instance.doorHandler.allDoors.forEach(bic);
            AdvBaseSecurity.instance.doorHandler.IndDoors.markDirty();
        }

        private boolean checkPass(Doors.OneDoor.OnePass pass, Doors.Users.UserPass user){
            AdvBaseSecurity.instance.logger.debug("Checking pass " + pass.passID);
            if(passes.containsKey(pass.passID)){
                Doors.PassValue passValue = passes.get(pass.passID);
                if(passValue.passType == Doors.PassValue.type.Level){
                    if(pass.passValueI <= Integer.parseInt(user.passValue.get(0)))
                        return true;
                }
                else if(passValue.passType == Doors.PassValue.type.Group){
                    if(pass.passValueI == Integer.parseInt(user.passValue.get(0)) - 1)
                        return true;
                }
                else if(passValue.passType == Doors.PassValue.type.Text){
                    if(pass.passValueS.equals(user.passValue.get(0)))
                        return true;
                }
                else if(passValue.passType == Doors.PassValue.type.MultiText){
                    for(String s : user.passValue){
                        if(s.equals(pass.passValueS))
                            return true;
                    }
                }
                else if(passValue.passType == Doors.PassValue.type.Pass)
                    return user.passValue.get(0).equals("true");
            }
            return false;
        }

        /*
        -400: error
        -4: door doesn't exist
        -3: user doesn't exist
        -2: don't do anything
        -1: blocked user (denied)
        0: access denied
        1: access granted
        2: access granted staff
         */
        public int checkSwipe(UUID userID, UUID readerID, boolean properChange){ //properChange actually updates the door state.
            AdvBaseSecurity.instance.logger.debug("Checking ID " + userID + " in reader ID " + readerID);
            //get the door
            Doors.OneDoor door = getDoorFromReader(readerID);
            if(door == null)
                return -4;
            AdvBaseSecurity.instance.logger.debug("Found the door " + door.doorName);
            //get the user's card
            Doors.Users user = getUser(userID);
            if(user == null)
                return -3;
            AdvBaseSecurity.instance.logger.debug("Found the user " + user.name);
            //check the door
            if(door.doorStatus == Doors.OneDoor.allDoorStatuses.ALL_ACCESS) //if always open, don't do anything.
                return -2;
            AdvBaseSecurity.instance.logger.debug("Door is not AllAccess");
            if(user.blocked)
                return -1;
            else if(door.doorStatus == Doors.OneDoor.allDoorStatuses.NO_ACCESS)
                return user.staff ? 1 : 0;
            AdvBaseSecurity.instance.logger.debug("User is neither blocked or door is no access. Preparing checks. Status: " + door.doorStatus.getInt());
            //check cache
            for(cacheHolder cache : userCache){
                if(cache.userID.equals(userID) && cache.doorID.equals(door.doorId)){
                    //use this return value
                    AdvBaseSecurity.instance.logger.debug("Using cache value: " + cache.worked);
                    if(properChange){
                        if(cache.worked != 0){ //1 or 2
                            changeDoorState(cache.doorID);
                        }
                    }
                    return cache.worked;
                }
            }
            if(door.doorStatus.getInt() >= 0) // # > 0 = can be opened by card swipe
            {
                if (checkPassList(door.passes, user)){
                    AdvBaseSecurity.instance.logger.debug("Succeeded check. Returning 1");
                    if(DoorConfig.cachetime != 0)
                        userCache.add(new cacheHolder(userID, door.doorId, doorTime + Math.abs(DoorConfig.cachetime), 1));
                    if(properChange)
                        changeDoorState(door.doorId);
                    return 1;
                }
            }
            if(door.doorStatus == Doors.OneDoor.allDoorStatuses.OVERRIDDEN_ACCESS || door.doorStatus == Doors.OneDoor.allDoorStatuses.LOCKDOWN){ // |#| == 1 : checks with overridden pass list.
                if (checkPassList(door.override, user)){
                    AdvBaseSecurity.instance.logger.debug("Succeeded override check. Returning 1");
                    if(DoorConfig.cachetime != 0)
                        userCache.add(new cacheHolder(userID, door.doorId, doorTime + Math.abs(DoorConfig.cachetime), 1));
                    if(properChange)
                        changeDoorState(door.doorId);
                    return 1;
                }
            }
            AdvBaseSecurity.instance.logger.debug("Failed others. Is staff: " + user.staff);
            if(DoorConfig.cachetime != 0)
                userCache.add(new cacheHolder(userID, door.doorId, doorTime + Math.abs(DoorConfig.cachetime), user.staff ? 2 : 0));
            if(properChange && user.staff)
                changeDoorState(door.doorId);
            return user.staff ? 2 : 0;

            //return -400;
        }

        //endregion

        public static class OneDoor{
            public UUID doorId; //unique identifier
            public String doorName; //the name of the door

            public enum allDoorStatuses {
                NO_ACCESS(-2),
                LOCKDOWN(-1),
                ACCESS(0),
                OVERRIDDEN_ACCESS(1),
                ALL_ACCESS(2);

                private final int value;
                allDoorStatuses(int value) {
                    this.value = value;
                }

                public int getInt() {
                    return value;
                }
                public static allDoorStatuses fromInt(int val){
                    switch(val){
                        case -2:
                            return NO_ACCESS;
                        case -1:
                            return LOCKDOWN;
                        case 1:
                            return OVERRIDDEN_ACCESS;
                        case 2:
                            return ALL_ACCESS;
                        default:
                            return ACCESS;
                    }
                }
            }
            public allDoorStatuses doorStatus = allDoorStatuses.ACCESS; //current door status

            public List<OnePass> passes; //All the passes that this door is linked to.
            public List<OnePass> override; //All the passes that this door is linked to under override accesses.

            public UUID groupID; //if door is part of group, ID of it
            public int isDoorOpen = 0; //is the door open? 0 = no, 1 = yes (timed), 2 = toggled.
            public int currTick; //the ticks that count down if door is open

            public boolean defaultToggle; //if door is toggle when opened
            public int defaultTick; //time door is open by default

            public List<UUID> Doors = new LinkedList<UUID>(); //the amount of doors linked to it (stuff that will be activated if door is opened)
            public List<UUID> Readers = new LinkedList<UUID>(); //the amount of devices that trigger the doors to open or close.

            public String readerLabel; //label that the readers may display soon.
            public byte readerLabelColor; //color of label
            public int readerLights; //light status on the readers to display access.

            @Override
            public String toString() {
                return String.format("%s | %s : %s T| %d Tck| %d O | %d St", doorName, doorId.toString(), defaultToggle, defaultTick, isDoorOpen, doorStatus.getInt());
            }

            public OneDoor(){

            }
            public OneDoor(boolean newOne){
                if(newOne){ //set default values
                    doorName = "new door";
                    doorId = UUID.randomUUID();
                    isDoorOpen = 0;
                    doorStatus = allDoorStatuses.ACCESS;
                    //prep one default pass
                    passes = new LinkedList<>();
                    OnePass pass = new OnePass();
                    pass.id = UUID.randomUUID();
                    pass.passID = "staff";
                    pass.priority = 1;
                    pass.passType = OnePass.type.Supreme;
                    passes.add(pass);
                    override = new LinkedList<>();
                    currTick = 0;
                    defaultToggle = false;
                    defaultTick = 20 * 5;
                    Doors = new LinkedList<>();
                    Readers = new LinkedList<>();
                    readerLabel = new TextComponentTranslation("advancedbasesecurity.reader.text.idle").getUnformattedText();
                    readerLabelColor = 6;
                    readerLights = 0;
                }
            }
            public OneDoor(NBTTagCompound tag, HashMap<String, PassValue> passMap, HashMap<UUID, Groups> groupMap){
                AdvBaseSecurity.instance.logger.info(tag);
                if(tag.hasKey("doorName"))
                    doorName = tag.getString("doorName");
                else
                    doorName = "new";
                if(tag.hasUniqueId("doorID"))
                    doorId = tag.getUniqueId("doorID");
                else
                    doorId = UUID.randomUUID();
                if(tag.hasKey("readerLabel"))
                    readerLabel = tag.getString("readerLabel");
                else
                    readerLabel = "Normal";
                if(tag.hasKey("readerLabelColor"))
                    readerLabelColor = tag.getByte("readerLabelColor");
                else
                    readerLabelColor = 7;
                if(tag.hasKey("readerLights"))
                    readerLights = tag.getInteger("readerLights");
                else
                    readerLights = 0;
                if(tag.hasKey("toggleDefault"))
                    defaultToggle = tag.getBoolean("toggleDefault");
                else
                    defaultToggle = false;
                if(tag.hasKey("tickDefault"))
                    defaultTick = tag.getInteger("tickDefault");
                else
                    defaultTick = 20 * 5;
                if(tag.hasKey("doorOpen"))
                    isDoorOpen = tag.getInteger("doorOpen");
                else
                    isDoorOpen = 0;
                if(tag.hasKey("openTick"))
                    currTick = tag.getInteger("openTick");
                else
                    currTick = 0;
                Doors = new LinkedList<>();
                if(tag.hasKey("Doors")){
                    NBTTagList inDoorsList = tag.getTagList("Doors", Constants.NBT.TAG_STRING);
                    int thisLength = inDoorsList.tagCount();
                    for(int j=0; j<thisLength; j++){
                        Doors.add(UUID.fromString(inDoorsList.getStringTagAt(j)));
                    }
                }
                Readers = new LinkedList<>();
                if(tag.hasKey("Readers")){
                    NBTTagList inDoorsList = tag.getTagList("Readers", Constants.NBT.TAG_STRING);
                    int thisLength = inDoorsList.tagCount();
                    for(int j=0; j<thisLength; j++){
                        Readers.add(UUID.fromString(inDoorsList.getStringTagAt(j)));
                    }
                }
                passes = new LinkedList<>();
                if(tag.hasKey("passes")){
                    NBTTagList thisList = tag.getTagList("passes", Constants.NBT.TAG_COMPOUND);
                    for(int j=0; j<thisList.tagCount(); j++){
                        passes.add(new OnePass(thisList.getCompoundTagAt(j), passMap));
                    }
                }
                if(tag.hasKey("status"))
                    doorStatus = allDoorStatuses.fromInt(tag.getInteger("status"));
                else
                    doorStatus = allDoorStatuses.ACCESS;
                override = new LinkedList<>();
                if(tag.hasKey("override") && (doorStatus == allDoorStatuses.OVERRIDDEN_ACCESS || doorStatus == allDoorStatuses.LOCKDOWN)){
                    NBTTagList thisList = tag.getTagList("override", Constants.NBT.TAG_COMPOUND);
                    for(int j=0; j<thisList.tagCount(); j++){
                        override.add(new OnePass(thisList.getCompoundTagAt(j), passMap));
                    }
                }
                if(tag.hasUniqueId("group")) {
                    groupID = tag.getUniqueId("group");
                    if(!groupMap.containsKey(groupID))
                        groupID = null;
                }
            }

            public NBTTagCompound returnNBT(HashMap<String, PassValue> passMap, HashMap<UUID, Groups> groupMap){
                NBTTagCompound tag = new NBTTagCompound();
                if(readerLabel != null)
                    tag.setString("readerLabel", readerLabel);
                tag.setUniqueId("doorID", doorId);
                tag.setString("doorName", doorName);
                tag.setByte("readerLabelColor", readerLabelColor);
                tag.setInteger("readerLights", readerLights);
                tag.setBoolean("toggleDefault", defaultToggle);
                tag.setInteger("tickDefault", defaultTick);
                tag.setInteger("doorOpen", isDoorOpen);
                tag.setInteger("openTick", currTick);
                if(Doors != null){
                    NBTTagList tagList = new NBTTagList();
                    for(UUID strong : Doors) {
                        tagList.appendTag(new NBTTagString(strong.toString()));
                    }
                    tag.setTag("Doors", tagList);
                }
                if(Readers != null){
                    NBTTagList tagList = new NBTTagList();
                    for(UUID strong : Readers) {
                        tagList.appendTag(new NBTTagString(strong.toString()));
                    }
                    tag.setTag("Readers", tagList);
                }
                if(passes != null){
                    NBTTagList tagList = new NBTTagList();
                    for(OnePass strong : passes) {
                        tagList.appendTag(strong.returnNBT(passMap));
                    }
                    tag.setTag("passes", tagList);
                }
                if(override != null){
                    NBTTagList tagList = new NBTTagList();
                    for(OnePass strong : override) {
                        tagList.appendTag(strong.returnNBT(passMap));
                    }
                    tag.setTag("override", tagList);
                }
                if(doorStatus != null)
                    tag.setInteger("status", doorStatus.getInt());
                if(groupID != null && groupMap.containsKey(groupID))
                    tag.setUniqueId("group", groupID);
                return tag;
            }

            public static class OnePass{
                public enum type { //has int values too to allow for

                    Supreme(0), Base(1), Reject(2), Add(3);

                    private final int value;
                    private type(int value) {
                        this.value = value;
                    }

                    public int getInt() {
                        return value;
                    }
                    public static type fromInt(int val){
                        switch(val){
                            case 1:
                                return Base;
                            case 2:
                                return Reject;
                            case 3:
                                return Add;
                            default:
                                return Supreme;
                        }
                    }
                    public String toString(){
                        switch(value) {
                            case 1:
                                return "Base";
                            case 2:
                                return "Reject";
                            case 3:
                                return "Add";
                            default:
                                return "Supreme";
                        }
                    }
                }

                public UUID id; //The ID of this specifically
                public String passID; //the ID of the pass it is referencing
                public type passType; //Type of pass that this is (supreme, base, etc.)
                public short priority; //the priority of it from 1 to 5. 1 will be checked first over 5. On top of that reject passes of lower priority than a base pass don't block requests
                public List<UUID> addPasses; //if Base pass, it will also require these.
                public String passValueS; //The value needed (for string and multistring)
                public int passValueI; //The value needed (for int and group)

                public OnePass(){

                }

                public OnePass(NBTTagCompound tag, HashMap<String, PassValue> passMap){
                    if(tag.hasUniqueId("id"))
                        id = tag.getUniqueId("id");
                    else
                        id = UUID.randomUUID();
                    if(tag.hasKey("passId"))
                        passID = tag.getString("passId");
                    else
                        passID = "NAN";
                    if(tag.hasKey("type"))
                        passType = type.fromInt(tag.getShort("type"));
                    else
                        passType = type.Supreme;
                    if(tag.hasKey("priority"))
                        priority = (short)Math.max(1, Math.min(5, tag.getShort("priority"))); //clamp
                    else
                        priority = 1;
                    //stuff that depends on prev values
                    if(passType == type.Base){
                        addPasses = new LinkedList<>();
                        if(tag.hasKey("addPasses")){
                            NBTTagList tlist = tag.getTagList("addPasses", Constants.NBT.TAG_STRING);
                            for(int i=0; i<tlist.tagCount(); i++){
                                addPasses.add(UUID.fromString(tlist.getStringTagAt(i)));
                            }
                        }
                    }
                    if(passMap.containsKey(passID) && tag.hasKey("value")){
                        if((passMap.get(passID).passType == PassValue.type.Text || passMap.get(passID).passType == PassValue.type.MultiText)){
                            passValueS = tag.getString("value");
                        }
                        else if(passMap.get(passID).passType == PassValue.type.Group || passMap.get(passID).passType == PassValue.type.Level){
                            passValueI = tag.getInteger("value");
                        }
                    }
                }

                public OnePass(NBTTagCompound tag, PassValue.type typed){
                    if(tag.hasUniqueId("id"))
                        id = tag.getUniqueId("id");
                    else
                        id = UUID.randomUUID();
                    if(tag.hasKey("passId"))
                        passID = tag.getString("passId");
                    else
                        passID = "NAN";
                    if(tag.hasKey("type"))
                        passType = type.fromInt(tag.getShort("type"));
                    else
                        passType = type.Supreme;
                    if(tag.hasKey("priority"))
                        priority = (short)Math.max(1, Math.min(5, tag.getShort("priority"))); //clamp
                    else
                        priority = 1;
                    //stuff that depends on prev values
                    if(passType == type.Base){
                        addPasses = new LinkedList<>();
                        if(tag.hasKey("addPasses")){
                            NBTTagList tlist = tag.getTagList("addPasses", Constants.NBT.TAG_STRING);
                            for(int i=0; i<tlist.tagCount(); i++){
                                addPasses.add(UUID.fromString(tlist.getStringTagAt(i)));
                            }
                        }
                    }
                    if(tag.hasKey("value")){
                        if(typed == PassValue.type.Text || typed == PassValue.type.MultiText){
                            passValueS = tag.getString("value");
                        }
                        else if(typed == PassValue.type.Group || typed == PassValue.type.Level){
                            passValueI = tag.getInteger("value");
                        }
                    }
                }

                public NBTTagCompound returnNBT(HashMap<String, PassValue> passMap){
                    NBTTagCompound tag = new NBTTagCompound();
                    if(id != null)
                        tag.setUniqueId("id", id);
                    if(passID != null)
                        tag.setString("passId", passID);
                    if(passType != null)
                        tag.setShort("type", (short)passType.getInt());
                    tag.setShort("priority", priority);
                    if(passType == type.Base){
                        NBTTagList list = new NBTTagList();
                        for(UUID id : addPasses){
                            list.appendTag(new NBTTagString(id.toString()));
                        }
                        tag.setTag("addPasses", list);
                    }
                    if(passMap.containsKey(passID)){
                        if(passValueS != null && (passMap.get(passID).passType == PassValue.type.Text || passMap.get(passID).passType == PassValue.type.MultiText)){
                            tag.setString("value", passValueS);
                        }
                        else if(passMap.get(passID).passType == PassValue.type.Group || passMap.get(passID).passType == PassValue.type.Level){
                            tag.setInteger("value", passValueI);
                        }
                    }
                    return tag;
                }
            }
        }

        public static class PassValue {
            public enum type { //has int values too to allow for

                Pass(0), Level(1), Group(2), Text(3), MultiText(4);

                private final int value;
                private type(int value) {
                    this.value = value;
                }

                public int getInt() {
                    return value;
                }
                public static type fromInt(int val){
                    switch(val){
                        case 1:
                            return Level;
                        case 2:
                            return Group;
                        case 3:
                            return Text;
                        case 4:
                            return MultiText;
                        default:
                            return Pass;
                    }
                }
            }
            public String passId;
            public String passName;
            public type passType;
            public List<String> groupNames; //if type Group, what are the group names.

            @Override
            public String toString() {
                return String.format("%s | %s : %d Type", passName, passId, passType.getInt());
            }

            public PassValue(NBTTagCompound nbt){
                if(nbt.hasKey("id"))
                    passId = nbt.getString("id");
                else
                    passId = "NAN";
                if(nbt.hasKey("name"))
                    passName = nbt.getString("name");
                else
                    passName = "new pass";
                if(nbt.hasKey("type"))
                    passType = type.fromInt(nbt.getInteger("type"));
                else
                    passType = type.Pass;
                groupNames = new LinkedList<>();
                if(nbt.hasKey("groups"))
                {
                    NBTTagList list = nbt.getTagList("groups", Constants.NBT.TAG_STRING);
                    for(int j=0; j<list.tagCount(); j++){
                        groupNames.add(list.getStringTagAt(j));
                    }
                }
            }
            public PassValue(String id){
                passId = id;
            }
            public PassValue(){
                passId = "NAN";
            }

            public NBTTagCompound returnNBT(){
                NBTTagCompound tag = new NBTTagCompound();
                if(passId != null)
                    tag.setString("id", passId);
                if(passName != null)
                    tag.setString("name", passName);
                if(passType != null)
                    tag.setInteger("type", passType.getInt());
                if(groupNames != null && !groupNames.isEmpty()){
                    NBTTagList list = new NBTTagList();
                    for (String groupName : groupNames) {
                        list.appendTag(new NBTTagString(groupName));
                    }
                    tag.setTag("groups", list);
                }
                return tag;
            }
        }

        public static class Users{
            public UUID id; //ID used by cards to know who to look for.
            public String name;
            public UUID owner; //in case I link a player ID to the card. Biometric?
            public boolean staff;
            public boolean blocked;
            public HashMap<String, UserPass> passes;
            public Users(){

            }
            public Users(NBTTagCompound tag, HashMap<String, PassValue> passMap){
                if(tag.hasUniqueId("id"))
                    id = tag.getUniqueId("id");
                else
                    id = UUID.randomUUID();
                if(tag.hasKey("name"))
                    name = tag.getString("name");
                else
                    name = "new";
                if(tag.hasUniqueId("owner"))
                    owner = tag.getUniqueId("owner");
                else
                    owner = null;
                if(tag.hasKey("staff"))
                    staff = tag.getBoolean("staff");
                else
                    staff = false;
                if(tag.hasKey("blocked"))
                    blocked = tag.getBoolean("blocked");
                else
                    blocked = false;
                passes = new HashMap<>();
                if(tag.hasKey("passes")){
                    NBTTagList list = tag.getTagList("passes", Constants.NBT.TAG_COMPOUND);
                    for(int i=0; i<list.tagCount(); i++){
                        UserPass passed = new UserPass(list.getCompoundTagAt(i), passMap);
                        passes.put(passed.passId, passed);
                    }
                }
            }

            public NBTTagCompound returnNBT(HashMap<String, PassValue> passMap){
                if(id != null){
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setUniqueId("id", id);
                    if(owner != null)
                        tag.setUniqueId("owner", owner);
                    if(name != null)
                        tag.setString("name", name);
                    tag.setBoolean("staff", staff);
                    tag.setBoolean("blocked", blocked);
                    if(passes != null){
                        NBTTagList list = new NBTTagList();
                        BiConsumer<String, UserPass> biConsumer = (k,v) -> {
                            NBTTagCompound thisPass = v.returnNBT(passMap);
                            if(thisPass != null)
                                list.appendTag(thisPass);
                        };
                        passes.forEach(biConsumer);
                        tag.setTag("passes", list);
                    }
                    return tag;
                }
                return null;
            }

            public static class UserPass{
                public String passId; //ID of PassValue
                public List<String> passValue; //Any values it may be.
                public int type; //type set by PassValue, so when it checks it knows if invalid.\

                @Override
                public String toString() {
                    return "UserPass{" +
                            "passId='" + passId + '\'' +
                            ", passValue=" + passValue +
                            ", type=" + type +
                            '}';
                }

                public UserPass(String passId, int type){
                    this.passId = passId;
                    this.type = type;
                }
                public UserPass(String passId, List<String> passValue, int type){
                    this.passId = passId;
                    this.passValue = passValue;
                    this.type = type;
                }
                public UserPass(NBTTagCompound tag, HashMap<String, PassValue> passMap){
                    //TODO: Do stuff to check if the pass still exists
                    if(tag.hasKey("id")) {
                        passId = tag.getString("id");
                        if(stillExists(passMap, false))
                        {
                            if(tag.hasKey("value")) {
                                PassValue pass = passMap.get(passId);
                                NBTBase thisTag = tag.getTag("value");
                                if (pass.passType == PassValue.type.MultiText && thisTag instanceof NBTTagList) {
                                    passValue = new LinkedList<>();
                                    NBTTagList list = (NBTTagList) thisTag;
                                    for (int i = 0; i < list.tagCount(); i++) {
                                        passValue.add(list.getStringTagAt(i));
                                    }
                                }
                                if (pass.passType == PassValue.type.Text && thisTag instanceof NBTTagString) {
                                    passValue = new LinkedList<>();
                                    passValue.add(((NBTTagString) thisTag).getString());
                                }
                                if ((pass.passType == PassValue.type.Level || pass.passType == PassValue.type.Group) && thisTag instanceof NBTTagInt) {
                                    passValue = new LinkedList<>();
                                    passValue.add(Integer.toString(((NBTTagInt) thisTag).getInt()));
                                }
                                if(pass.passType == PassValue.type.Pass && thisTag instanceof  NBTTagShort){
                                    passValue = new LinkedList<>();
                                    passValue.add(((NBTTagShort) thisTag).getShort() == 1 ? "true" : "false");
                                }
                            }
                            if(tag.hasKey("type"))
                                type = tag.getInteger("type");
                        }
                    }
                }
                public boolean stillExists(HashMap<String, PassValue> passMap, boolean checkValue){
                    return passMap.containsKey(passId) && ((passValue != null && !passValue.isEmpty()) || !checkValue);
                }
                public NBTTagCompound returnNBT(HashMap<String, PassValue> passMap){
                    AdvBaseSecurity.instance.logger.info(this.toString());
                    NBTTagCompound tag = new NBTTagCompound();
                    if(passId != null && stillExists(passMap, true)){
                        tag.setString("id", passId);
                        PassValue.type passType = passMap.get(passId).passType;
                        if(passType == PassValue.type.MultiText){
                            NBTTagList list = new NBTTagList();
                            for (String s : passValue) {
                                list.appendTag(new NBTTagString(s));
                            }
                            tag.setTag("value", list);
                        }
                        else if(passType == PassValue.type.Text){
                            tag.setString("value", passValue.get(0));
                        }
                        else if(passType == PassValue.type.Group || passType == PassValue.type.Level){
                            tag.setInteger("value", Integer.parseInt(passValue.get(0)));
                        }
                        else if(passType == PassValue.type.Pass){
                            tag.setShort("value", (short) (passValue.get(0).equals("true") ? 1 : 0));
                        }
                        tag.setInteger("type", type);
                    }
                    else{
                        return null; //does not exist anymore so remove
                    }
                    return tag;
                }
            }
        }
        public static class Groups{
            public String name;
            public UUID id;
            public UUID parentID;
            public OneDoor.allDoorStatuses status;
            public List<OneDoor.OnePass> override; //override passes that are passed onto doors when group status is pushed
            public Groups(){

            }
            public Groups(NBTTagCompound tag, HashMap<String, PassValue> passMap){
                if(tag.hasKey("name"))
                    name = tag.getString("name");
                else
                    name = "new";
                if(tag.hasUniqueId("id"))
                    id = tag.getUniqueId("id");
                else
                    id = UUID.randomUUID();
                if(tag.hasUniqueId("parentID"))
                    parentID = tag.getUniqueId("parentID");
                if(tag.hasKey("status"))
                    status = OneDoor.allDoorStatuses.fromInt(tag.getShort("status"));
                else
                    status = OneDoor.allDoorStatuses.ACCESS;
                override = new LinkedList<>();
                if(tag.hasKey("override") && (status == OneDoor.allDoorStatuses.OVERRIDDEN_ACCESS || status == OneDoor.allDoorStatuses.LOCKDOWN)){
                    NBTTagList thisList = tag.getTagList("override", Constants.NBT.TAG_COMPOUND);
                    for(int j=0; j<thisList.tagCount(); j++){
                        override.add(new OneDoor.OnePass(thisList.getCompoundTagAt(j), passMap));
                    }
                }

            }
            public NBTTagCompound returnNBT(HashMap<String, PassValue> passMap){
                NBTTagCompound tag = new NBTTagCompound();
                if(id != null) {
                    tag.setUniqueId("id", id);
                    if (name != null)
                        tag.setString("name", name);
                    else
                        tag.setString("name", "new");
                    if (parentID != null)
                        tag.setUniqueId("parentID", parentID);
                    if(status != null)
                        tag.setShort("status", (short)status.getInt());
                    else
                        tag.setShort("status", (short)OneDoor.allDoorStatuses.ACCESS.getInt());
                    if(override != null){
                        NBTTagList tagList = new NBTTagList();
                        for(OneDoor.OnePass strong : override) {
                            tagList.appendTag(strong.returnNBT(passMap));
                        }
                        tag.setTag("override", tagList);
                    }
                    return tag;
                }
                return null;
            }
        }
    }
    //endregion

    public static class DoorData extends WorldSavedData{

        public static final String DATA_NAME = AdvBaseSecurity.MODID + "_doorlist";
        public static DoorData instance;

        List<UUID> doors;

        public DoorData(){
            super(DATA_NAME);
            doors = new LinkedList<>();
        }
        public DoorData(String str){
            super(str);
            doors = new LinkedList<>();
        }

        public static DoorData get(World world) {
            MapStorage storage = world.getMapStorage();
            instance = (DoorData) storage.getOrLoadData(DoorData.class, DATA_NAME);

            if (instance == null) {
                instance = new DoorData();
                storage.setData(DATA_NAME, instance);
            }
            return instance;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            doors = new LinkedList<>();
            if(nbt.hasKey("doors")) {
                NBTTagList list = nbt.getTagList("doors", Constants.NBT.TAG_STRING);
                for(int i=0; i<list.tagCount(); i++){
                    doors.add(UUID.fromString(list.getStringTagAt(i)));
                }
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            NBTTagList list = new NBTTagList();
            for(UUID id : doors){
                list.appendTag(new NBTTagString(id.toString()));
            }
            nbt.setTag("doors", list);
            return nbt;
        }
    }

    public static class DoorIdentifier{
        public UUID ManagerID;
        public UUID DoorID;
        public DoorIdentifier(UUID manager, UUID door){
            ManagerID = manager;
            DoorID = door;
        }
        public DoorIdentifier(){}
        public DoorIdentifier(NBTTagCompound tag){
            readFromNBT(tag);
        }

        public void readFromNBT(NBTTagCompound tag){
            if(tag.hasUniqueId("manager"))
                ManagerID = tag.getUniqueId("manager");
            if(tag.hasUniqueId("door"))
                DoorID = tag.getUniqueId("door");
        }

        public NBTTagCompound writeToNBT(NBTTagCompound tag){
            tag.setUniqueId("manager", ManagerID);
            tag.setUniqueId("door", DoorID);
            return tag;
        }
    }

    public static class ModifierValidation{ //Contains the IDs for people allowed to modify a part of the system (to prevent overwriting)
        public static final int maxTime = 60 * 15;

        private HashMap<String, OneHolder> currentHolders = new HashMap<>();

        public void onSecond(){
            List<String> keys = new LinkedList<>();
            for(OneHolder hol : currentHolders.values()){
                hol.timeLeft--;
                if(hol.timeLeft <= 0){
                    keys.add(hol.key);
                }
            }
            for(String str : keys)
                removePerm(str);
        }

        public boolean hasPermissions(String where, UUID id){
            if(currentHolders.containsKey(where) && currentHolders.get(where).editValidator.equals(id)){
                currentHolders.get(where).timeLeft = maxTime;
                return true;
            }
            return false;
        }

        public boolean addPermissions(String where, UUID id, boolean overwrite){
            if(currentHolders.containsKey(where) && !overwrite)
                return false;
            currentHolders.put(where, new OneHolder(id, where));
            return true;
        }

        public void removePerm(String where){
            currentHolders.remove(where);
        }

        public static class OneHolder{
            public OneHolder(UUID id, String key){
                timeLeft = maxTime;
                editValidator = id;
                this.key = key;
            }
            public int timeLeft;
            public UUID editValidator;
            public String key;
        }
    }
}
