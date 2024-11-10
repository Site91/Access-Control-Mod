package com.cadergator10.advancedbasesecurity.common.globalsystems;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDevice;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoor;
import com.cadergator10.advancedbasesecurity.common.interfaces.IReader;
import com.cadergator10.advancedbasesecurity.util.ReaderText;
import com.mojang.authlib.yggdrasil.response.User;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.function.BiConsumer;

public class DoorHandler {
    public Doors DoorGroups;
    public boolean loaded = false;

    public DoorHandler(){
        AdvBaseSecurity.instance.logger.info("Loaded DoorHandler!");
        //world.getMapStorage().getOrLoadData(Doors.class, DATA_NAME);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldLoad(WorldEvent.Load event){
        AdvBaseSecurity.instance.logger.info("World Loaded! Prepping Doors");
        DoorGroups = Doors.get(event.getWorld());
        AdvBaseSecurity.instance.logger.info("Successfully loaded");
        loaded = true;
    }
    private List<Doors.OneDoor> timedDoors = new LinkedList<>(); //doors that are currently open on a timer. these are what it loops through every tick.

    public HashMap<UUID, IReader> allReaders = new HashMap<>();
    public HashMap<UUID, IDoor> allDoors = new HashMap<>();

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event){
        if(!timedDoors.isEmpty()){
            for(Doors.OneDoor door : timedDoors){
                if(door.isDoorOpen == 1) {
                    door.currTick--;
                    if(door.currTick <= 0)
                        door.isDoorOpen = 0;
                }

                if(door.isDoorOpen == 0){
                    timedDoors.remove(door);
                }
            }
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event){ //when chunks unload, detect if any readers/doors are unloaded, and remove from list accordingly.
         Map<BlockPos, TileEntity> map = event.getChunk().getTileEntityMap();
        BiConsumer<BlockPos, TileEntity> biConsumer = (k, v) -> {
           if(v instanceof IDevice){
               IDevice dev = (IDevice)v;
               if(dev.getDevType().equals("reader"))
                   allReaders.remove(dev.getId());
               else if(dev.getDevType().equals("door"))
                   allDoors.remove(dev.getId());
           }
        };
        map.forEach(biConsumer);
    }

//    @SubscribeEvent
//    public void serverSave(WorldEvent.Save event){
//
//    }

    //region Door Controls
    private void updateDoorState(Doors.OneDoor door){ //update door state of all doors that are currently loaded.
        for(UUID dev : door.Doors){
            if(allDoors.containsKey(dev)){
                allDoors.get(dev).openDoor(door.isDoorOpen != 0);
            }
        }
    }

    //Check permissions list of a door
    private boolean checkPassList(List<Doors.OneDoor.OnePass> door, Doors.Users user){
        for(int i=1; i<=5; i++){ //priorities
            int isThrough = 0; //-1 = reject pass. 0 = nope. 1 = allowed base.
            for(Doors.OneDoor.OnePass pass : door){
                if(pass.priority == i && pass.passType != Doors.OneDoor.OnePass.type.Add){
                    boolean gotIt = checkPass(pass, user.passes.get(pass.passID));
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
                                    if (addPass.passType == Doors.OneDoor.OnePass.type.Add && pass.addPasses.contains(addPass.id)) {
                                        if(!checkPass(addPass, user.passes.get(addPass.passID))){
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
    private Doors.OneDoor getDoorFromReader(UUID reader){
        for(Doors.OneDoor tempdoor : DoorGroups.doors){
            if(tempdoor.Readers.contains(reader)) {
                return tempdoor;
            }
        }
        return null;
    }

    //get the door by its ID
    private Doors.OneDoor getDoorFromID(UUID doorID){
        for(Doors.OneDoor tempdoor : DoorGroups.doors){
            if(tempdoor.doorId == doorID){
                return tempdoor;
            }
        }
        return null;
    }

    //get the user by user ID
    private Doors.Users getUser(UUID userID){
        for(Doors.Users tempuser : DoorGroups.users){
            if(tempuser.id == userID) {
                return tempuser;
            }
        }
        return null;
    }

    //get group by ID
    private Doors.Groups getDoorGroup(UUID groupID){
        if(DoorGroups.groups.containsKey(groupID))
            return DoorGroups.groups.get(groupID);
        return null;
    }
    //get children groups
    private List<UUID> getDoorChildren(UUID groupID, boolean cascade){ //cascade means all children. false means only direct children
        List<UUID> groups = new LinkedList<>();
        //Find any that are children
        BiConsumer<UUID, Doors.Groups> biConsumer = (k, v) -> {
            if(v.parentID == groupID)
                groups.add(k);
        };
        DoorGroups.groups.forEach(biConsumer);
        //if children found & cascade=true, call this on all others too
        if(cascade && !groups.isEmpty()){
            for(UUID id : groups){
                List<UUID> newGroups = getDoorChildren(id, true);
                //delete duplicates
                newGroups.removeIf(groups::contains); //u -> groups.contains(u)
                //add to groups list
                groups.addAll(newGroups);
            }
        }
        return groups;
    }

    //get doors by groupID
    private List<Doors.OneDoor> getDoorsByGroup(UUID groupID){
        List<Doors.OneDoor> doors = new LinkedList<>();
        for(Doors.OneDoor door : DoorGroups.doors){
            if(door.groupID == groupID)
                doors.add(door);
        }
        return doors;
    }
    //get doors by list of groupIDs
    private List<Doors.OneDoor> getDoorsByGroup(List<UUID> groupID){
        List<Doors.OneDoor> doors = new LinkedList<>();
        for(Doors.OneDoor door : DoorGroups.doors){
            if(groupID.contains(door.groupID))
                doors.add(door);
        }
        return doors;
    }
	//endregion

    //remove from timedDoors
    private void removeFromTimedDoors(UUID doorID){
        for (int i = 0; i < timedDoors.size(); i++) {
            if (timedDoors.get(i).doorId == doorID) {
                timedDoors.remove(i);
                break;
            }
        }
    }

    //push update to existing tile entities
    private void pushDoorUpdate(Doors.OneDoor door){
        for(UUID id : door.Doors){
            if(allDoors.containsKey(id)){
                allDoors.get(id).openDoor(door.isDoorOpen != 0);
            }
        }
        for(UUID id : door.Readers){
            if(allReaders.containsKey(id)){
                allReaders.get(id).updateVisuals(door.isDoorOpen != 0 ? 4 : (door.doorStatus.getInt() < 0 ? 1 : (door.doorStatus.getInt() > 1 ? 4 : 0)), new ReaderText(door.isDoorOpen != 0 ? "Open" : "Closed", (byte) 7) );
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
            pushDoorUpdate(door);
        }
        else if(openState && door.isDoorOpen == 0){ //opening a closed door
            //change door values
            door.isDoorOpen = (ticks == 0 ? 2 : 1);
            door.currTick = ticks;
            //update tile entities
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

    public void updateGroups(Doors.Groups group, boolean pushToChildren){ //pushToChildren means if a change was made to a group, if it should update all child groups too (groups with parentID set to this groupID)
        //group at this point has already been updated. this function simply pushes to doors and updates children groups
        List<UUID> groups = new LinkedList<>();
        groups.add(group.id);
        if(pushToChildren) //if all groups that are children will receive this state
            groups.addAll(getDoorChildren(group.id, true));
        List<Doors.OneDoor> pushUpdateDoors = getDoorsByGroup(groups); //Any doors which need a doorState push
        //get all groups and update their values correctly first
        for(UUID tempgroupID : groups){
            if(tempgroupID != group.id) {
                Doors.Groups tempgroup = getDoorGroup(tempgroupID);
                tempgroup.status = group.status;
                tempgroup.override = group.override;
            }
        }
        //get all doors and push their values
        for(Doors.OneDoor door : DoorGroups.doors){
            if(door.groupID != null && groups.contains(door.groupID)){ //that door is part of the group tree
                door.doorStatus = group.status;
                door.override = (Math.abs(group.status.getInt()) == 1 ? group.override : null); //because -1 and 1 values are override ones.
                pushUpdateDoors.add(door);
            }
        }
        //push update
        pushDoorUpdateMult(pushUpdateDoors);
    }

    public void recievedUpdate(UUID id, Doors.OneDoor door){

    }

    public int getReaderLight(UUID id){
        for(Doors.OneDoor door : DoorGroups.doors){
            for(int i=0; i<door.Readers.size(); i++){
                if(door.Readers.get(i).equals(id)){
                    return door.readerLights;
                }
            }
        }
        return -1;
    }
    public ReaderText getReaderLabel(UUID id){
        for(Doors.OneDoor door : DoorGroups.doors){
            for(int i=0; i<door.Readers.size(); i++){
                if(door.Readers.get(i).equals(id)){
                    return new ReaderText(door.readerLabel, door.readerLabelColor);
                }
            }
        }
        return null;
    }

    private boolean checkPass(Doors.OneDoor.OnePass pass, Doors.Users.UserPass user){
        if(DoorGroups.passes.containsKey(pass.passID)){
            Doors.PassValue passValue = DoorGroups.passes.get(pass.passID);
            if(passValue.passType == Doors.PassValue.type.Level){
                if(pass.passValueI >= Integer.getInteger(user.passValue.get(0)))
                    return true;
            }
            else if(passValue.passType == Doors.PassValue.type.Group){
                if(pass.passValueI == Integer.getInteger(user.passValue.get(0)))
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
    -3: door doesn't exist
    -2: user doesn't exist
    -1: don't do anything
    0: access denied
    1: access granted
     */
    public int checkSwipe(UUID userID, UUID readerID){
        //get the door
        Doors.OneDoor door = getDoorFromReader(readerID);
        if(door == null)
            return -3;
        //get the user's card
        Doors.Users user = getUser(userID);
        if(user == null)
            return -2;
        //check the door
        if(door.doorStatus == Doors.OneDoor.allDoorStatuses.ALL_ACCESS) //if always open, don't do anything.
            return -1;
        else if(door.doorStatus == Doors.OneDoor.allDoorStatuses.NO_ACCESS)
            return 0;
        if(door.doorStatus.getInt() >= 0) // # > 0 = can be opened by card swipe
        {
            if (checkPassList(door.passes, user)){
                return 1;
            }
        }
        if(door.doorStatus == Doors.OneDoor.allDoorStatuses.OVERRIDDEN_ACCESS || door.doorStatus == Doors.OneDoor.allDoorStatuses.LOCKDOWN){ // |#| == 1 : checks with overridden pass list.
            if (checkPassList(door.override, user)){
                return 1;
            }
        }
        return 0;

        //return -400;
    }

    //endregion


    //region Door save file
    public static class Doors extends WorldSavedData{
        public int currentDoorVer = 0; //The version of the file. In case on the website it has been updated.

        public static final String DATA_NAME = AdvBaseSecurity.MODID + "_basesecuritydoors";
        public static Doors instance;

        public List<OneDoor> doors;
        public HashMap<UUID, PassValue> passes;
        public HashMap<UUID, Groups> groups;
        public List<Users> users;

        public Doors(){
            super(DATA_NAME);
        }

        public static Doors get(World world) {
            MapStorage storage = world.getMapStorage();
            instance = (Doors) storage.getOrLoadData(Doors.class, DATA_NAME);

            if (instance == null) {
                instance = new Doors();
                storage.setData(DATA_NAME, instance);
            }
            return instance;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            AdvBaseSecurity.instance.logger.info("Reading Door NBT");
            if(nbt.hasKey("versionNum"))
                currentDoorVer = nbt.getInteger("versionNum");
            else
                currentDoorVer = 0;

            //read all pass data
            this.passes = new HashMap<>();
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

            //write all pass data
            if(this.passes != null){
                NBTTagList list = new NBTTagList();
                BiConsumer<UUID, PassValue> biConsumer = (k,v) -> {
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
                    list.appendTag(door.returnNBT(passes));
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
                private allDoorStatuses(int value) {
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

            public OneDoor(){

            }
            public OneDoor(NBTTagCompound tag, HashMap<UUID, PassValue> passMap, HashMap<UUID, Groups> groupMap){
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
                if(tag.hasKey("group")) {
                    groupID = tag.getUniqueId("group");
                    if(!groupMap.containsKey(groupID))
                        groupID = null;
                }
            }

            public NBTTagCompound returnNBT(HashMap<UUID, PassValue> passMap){
                NBTTagCompound tag = new NBTTagCompound();
                if(readerLabel != null)
                    tag.setString("readerLabel", readerLabel);
                tag.setByte("readerLabelColor", readerLabelColor);
                tag.setInteger("readerLights", readerLights);
                tag.setBoolean("toggleDefault", defaultToggle);
                tag.setInteger("tickDefault", defaultTick);
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
                }

                public UUID id; //The ID of this specifically
                public UUID passID; //the ID of the pass it is referencing
                public type passType; //Type of pass that this is (supreme, base, etc.)
                public short priority; //the priority of it from 1 to 5. 1 will be checked first over 5. On top of that reject passes of lower priority than a base pass don't block requests
                public List<UUID> addPasses; //if Base pass, it will also require these.
                public String passValueS; //The value needed (for string and multistring)
                public int passValueI; //The value needed (for int and group)

                public OnePass(){

                }

                public OnePass(NBTTagCompound tag, HashMap<UUID, PassValue> passMap){
                    if(tag.hasKey("id"))
                        id = tag.getUniqueId("id");
                    else
                        id = UUID.randomUUID();
                    if(tag.hasKey("passId"))
                        passID = tag.getUniqueId("passId");
                    else
                        passID = UUID.randomUUID();
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

                public NBTTagCompound returnNBT(HashMap<UUID, PassValue> passMap){
                    NBTTagCompound tag = new NBTTagCompound();
                    if(id != null)
                        tag.setUniqueId("id", id);
                    if(passID != null)
                        tag.setUniqueId("passId", passID);
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
            public UUID passId;
            public String passName;
            public type passType;
            public List<String> groupNames; //if type Group, what are the group names.

            public PassValue(NBTTagCompound nbt){
                if(nbt.hasKey("id"))
                    passId = nbt.getUniqueId("id");
                else
                    passId = UUID.randomUUID();
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
            public PassValue(UUID id){
                passId = id;
            }
            public PassValue(){
                passId = UUID.randomUUID();
            }

            public NBTTagCompound returnNBT(){
                NBTTagCompound tag = new NBTTagCompound();
                if(passId != null)
                    tag.setUniqueId("id", passId);
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
            public UUID owner; //in case I link a player ID to the card. Biometric?
            public boolean staff;
            public boolean blocked;
            public HashMap<UUID, UserPass> passes;
            public Users(NBTTagCompound tag, HashMap<UUID, PassValue> passMap){
                if(tag.hasKey("id"))
                    id = tag.getUniqueId("id");
                else
                    id = UUID.randomUUID();
                if(tag.hasKey("owner"))
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

            public NBTTagCompound returnNBT(HashMap<UUID, PassValue> passMap){
                if(id != null){
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setUniqueId("id", id);
                    if(owner != null)
                        tag.setUniqueId("owner", owner);
                    tag.setBoolean("staff", staff);
                    tag.setBoolean("blocked", blocked);
                    if(passes != null){
                        NBTTagList list = new NBTTagList();
                        BiConsumer<UUID, UserPass> biConsumer = (k,v) -> {
                            NBTTagCompound thisPass = v.returnNBT(passMap);
                            if(thisPass != null)
                                list.appendTag(thisPass);
                        };
                        passes.forEach(biConsumer);
                        tag.setTag("passes", list);
                    }
                }
                return null;
            }

            public static class UserPass{
                public UUID passId; //ID of PassValue
                public List<String> passValue; //Any values it may be.
                public UserPass(NBTTagCompound tag, HashMap<UUID, PassValue> passMap){
                    //TODO: Do stuff to check if the pass still exists
                    if(tag.hasKey("id")) {
                        passId = tag.getUniqueId("id");
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
                        }
                    }
                }
                public boolean stillExists(HashMap<UUID, PassValue> passMap, boolean checkValue){
                    return passMap.containsKey(passId) && ((passValue != null && !passValue.isEmpty()) || !checkValue);
                }
                public NBTTagCompound returnNBT(HashMap<UUID, PassValue> passMap){
                    NBTTagCompound tag = new NBTTagCompound();
                    if(passId != null && stillExists(passMap, true)){
                        tag.setUniqueId("id", passId);
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
                            tag.setInteger("value", Integer.getInteger(passValue.get(0)));
                        }
                        else if(passType == PassValue.type.Pass){
                            tag.setShort("value", (short) (passValue.get(0).equals("true") ? 1 : 0));
                        }
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
            public Groups(NBTTagCompound tag, HashMap<UUID, PassValue> passMap){
                if(tag.hasKey("name"))
                    name = tag.getString("name");
                else
                    name = "new";
                if(tag.hasKey("id"))
                    id = tag.getUniqueId("id");
                else
                    id = UUID.randomUUID();
                if(tag.hasKey("parentID"))
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
            public NBTTagCompound returnNBT(HashMap<UUID, PassValue> passMap){
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
                }
                return null;
            }
        }
    }
    //endregion
}
