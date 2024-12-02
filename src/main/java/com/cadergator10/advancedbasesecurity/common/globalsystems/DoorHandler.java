package com.cadergator10.advancedbasesecurity.common.globalsystems;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.client.config.DoorConfig;
import com.cadergator10.advancedbasesecurity.client.gui.components.ButtonEnum;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDevice;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoor;
import com.cadergator10.advancedbasesecurity.common.interfaces.IReader;
import com.cadergator10.advancedbasesecurity.util.ReaderText;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
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
    public Doors DoorGroups;
    public boolean loaded = false;

    public DoorHandler(){
        AdvBaseSecurity.instance.logger.info("Loaded DoorHandler!");
        //world.getMapStorage().getOrLoadData(Doors.class, DATA_NAME);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldLoad(WorldEvent.Load event){
        AdvBaseSecurity.instance.logger.info("got event");
        if(!event.getWorld().isRemote && !loaded) {
            allReaders = new HashMap<>();
            allDoors = new HashMap<>();
            timedDoors = new LinkedList<>();
            AdvBaseSecurity.instance.logger.info("World Loaded! Prepping Doors");
            DoorGroups = Doors.get(event.getWorld());
            AdvBaseSecurity.instance.logger.info("Successfully loaded");
            for (Doors.OneDoor door : DoorGroups.doors) {
                if (door.isDoorOpen == 1) { //any timed doors add to list
                    timedDoors.add(door);
                }
            }
            userCache = new LinkedList<>();
            loaded = true;
        }
    }

    public void onWorldUnload(FMLServerStoppedEvent event){
        if(loaded) {
            AdvBaseSecurity.instance.logger.info("World unloading. Removing door stuff");
            loaded = false;
            DoorGroups = null;
            timedDoors = null;
            allReaders = null;
            allDoors = null;
            editValidator = null;
            userCache = null;
        }
    }

    private List<Doors.OneDoor> timedDoors; //doors that are currently open on a timer. these are what it loops through every tick.

    public HashMap<UUID, IReader> allReaders;
    public HashMap<UUID, IDoor> allDoors;

    public int doorTime = Integer.MIN_VALUE;
    public List<cacheHolder> userCache;

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
            if (!timedDoors.isEmpty()) {
                for (Doors.OneDoor door : timedDoors) {
                    if (door.isDoorOpen == 1) {
                        door.currTick--;
                        if (door.currTick <= 0) {
                            door.isDoorOpen = 0;
                            pushDoorUpdate(door);
                        }
                    }

                    if (door.isDoorOpen == 0) {
                        timedDoors.remove(door);
                    }
                }
            }
            //cache
            if (DoorConfig.cachetime != 0) {
                doorTime++;
                for (int i = 0; i < userCache.size(); i++) {
                    if (userCache.get(i).time <= doorTime) {
                        userCache.remove(i);
                        i--;
                    }
                }
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

    public UUID getEditValidator(){
        editValidator = UUID.randomUUID();
        return editValidator;
    }

    public boolean checkValidator(UUID editValidator){
        return this.editValidator.equals(editValidator);
    }

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
        for(Doors.OneDoor tempdoor : DoorGroups.doors){
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
        for(Doors.OneDoor tempdoor : DoorGroups.doors){
            if(tempdoor.doorId.equals(doorID)){
                return tempdoor;
            }
        }
        return null;
    }

    //get the door by its name (first one only)
    public Doors.OneDoor getDoorFromName(String doorName){
        for(Doors.OneDoor tempdoor : DoorGroups.doors){
            if(tempdoor.doorName.equalsIgnoreCase(doorName)){
                return tempdoor;
            }
        }
        return null;
    }

    //get the user by user ID
    public Doors.Users getUser(UUID userID){
        for(Doors.Users tempuser : DoorGroups.users){
            if(tempuser.id.equals(userID)) {
                return tempuser;
            }
        }
        return null;
    }
    public Doors.Users getUserByName(String userID){
        for(Doors.Users tempuser : DoorGroups.users){
            if(tempuser.name.equals(userID)) {
                return tempuser;
            }
        }
        return null;
    }

    //get group by ID
    public Doors.Groups getDoorGroup(UUID groupID){
        if(DoorGroups.groups.containsKey(groupID))
            return DoorGroups.groups.get(groupID);
        return null;
    }
    //get group by name
    public UUID getDoorGroupID(String group){
        List<UUID> groups = new LinkedList<>();
        BiConsumer<UUID, Doors.Groups> biConsumer = (k, v) -> {
            if(v.name.equalsIgnoreCase(group))
                groups.add(v.id);
        };
        DoorGroups.groups.forEach(biConsumer);
        if(!group.isEmpty())
            return groups.get(0);
        return null;
    }
    //get children groups
    public List<UUID> getDoorGroupChildren(UUID groupID, boolean cascade){ //cascade means all children. false means only direct children
        List<UUID> groups = new LinkedList<>();
        //Find any that are children
        BiConsumer<UUID, Doors.Groups> biConsumer = (k, v) -> {
            if(v.parentID.equals(groupID))
                groups.add(k);
        };
        DoorGroups.groups.forEach(biConsumer);
        //if children found & cascade=true, call this on all others too
        if(cascade && !groups.isEmpty()){
            for(UUID id : groups){
                List<UUID> newGroups = getDoorGroupChildren(id, true);
                //delete duplicates
                newGroups.removeIf(groups::contains); //u -> groups.contains(u)
                //add to groups list
                groups.addAll(newGroups);
            }
        }
        return groups;
    }

    //get group hashmap as list with index
    public List<ButtonEnum.groupIndex> getGroupList(){
        List<ButtonEnum.groupIndex> groups = new LinkedList<>();
        BiConsumer<UUID,Doors.Groups> biConsumer = (k, v) -> groups.add(new ButtonEnum.groupIndex(k.toString(), v.name));
        this.DoorGroups.groups.forEach(biConsumer);
        return groups;
    }

    //get doors by groupID
    private List<Doors.OneDoor> getDoorsByGroup(UUID groupID){
        List<Doors.OneDoor> doors = new LinkedList<>();
        for(Doors.OneDoor door : DoorGroups.doors){
            if(door.groupID.equals(groupID))
                doors.add(door);
        }
        return doors;
    }
    //get doors by list of groupIDs
    private List<Doors.OneDoor> getDoorsByGroup(List<UUID> groupID){
        List<Doors.OneDoor> doors = new LinkedList<>();
        for(Doors.OneDoor door : DoorGroups.doors){
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
        DoorGroups.doors.add(door);
        DoorGroups.markDirty();
        return door;
    }
	//endregion

    //region Reader/Door Management
    public boolean SetDevID(UUID devID, UUID doorID, boolean isDoor){
        int foundRightOne = -1;
        int index = 0;
        if(!isDoor) {
            for (Doors.OneDoor door : DoorGroups.doors) {
                if (door.doorId.equals(doorID)) {
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
                for (Doors.OneDoor door : DoorGroups.doors) { //make sure no other door has this reader.
                    for(int j=0; j<door.Readers.size(); j++){
                        if(foundRightOne != index && door.Readers.get(j).equals(devID)){
                            door.Readers.remove(j);
                            break;
                        }
                    }
                    index++;
                }
                DoorGroups.markDirty();
            }
        }
        else{
            for (Doors.OneDoor door : DoorGroups.doors) {
                if (door.doorId.equals(doorID)) {
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
                for (Doors.OneDoor door : DoorGroups.doors) { //make sure no other door has this reader.
                    for(int j=0; j<door.Doors.size(); j++){
                        if(foundRightOne != index && door.Doors.get(j).equals(devID)){
                            door.Doors.remove(j);
                            break;
                        }
                    }
                    index++;
                }
                DoorGroups.markDirty();
            }
        }
        return foundRightOne != -1;
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
            if(allDoors.containsKey(id)){
                allDoors.get(id).openDoor(door.isDoorOpen != 0);
            }
        }
        String display;
        byte color;
        int bar = door.isDoorOpen != 0 ? 4 : (door.doorStatus.getInt() < 0 ? 1 : (door.doorStatus.getInt() > 1 ? 4 : 0));
        if(door.isDoorOpen == 0){ //perform the stuff based on a closed door.
            if(door.doorStatus == Doors.OneDoor.allDoorStatuses.NO_ACCESS) {
                display = new TextComponentTranslation("advancedbasesecurity.reader.text.nodoor").getUnformattedText();
                color = 4;
            }
            else if(door.doorStatus == Doors.OneDoor.allDoorStatuses.LOCKDOWN) {
                display = new TextComponentTranslation("advancedbasesecurity.reader.text.lockdown").getUnformattedText();
                color = 12;
            }
            else {
                display = new TextComponentTranslation("advancedbasesecurity.reader.text.idle").getUnformattedText();
                color = (byte)(door.doorStatus == Doors.OneDoor.allDoorStatuses.OVERRIDDEN_ACCESS ? 14 : 6);
            }
        }
        else{ //perform based on an open door
            if(door.doorStatus == Doors.OneDoor.allDoorStatuses.ALL_ACCESS) {
                display = new TextComponentTranslation("advancedbasesecurity.reader.text.allaccess").getUnformattedText();
                color = 10;
            }
            else {
                display = new TextComponentTranslation("advancedbasesecurity.reader.text.allowed").getUnformattedText();
                color = 2;
            }
        }
        door.readerLabel = display;
        door.readerLabelColor = color;
        for(UUID id : door.Readers){
            if(allReaders.containsKey(id)){
                allReaders.get(id).updateVisuals(bar, new ReaderText(display, color) );
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
            DoorGroups.markDirty();
            pushDoorUpdate(door);
        }
        else if(openState && door.isDoorOpen == 0){ //opening a closed door
            //change door values
            door.isDoorOpen = (ticks == 0 ? 2 : 1);
            door.currTick = ticks;
            //update tile entities
            DoorGroups.markDirty();
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
            DoorGroups.markDirty();
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
        DoorGroups.passes.forEach(bic);
        for(Doors.Users user : DoorGroups.users){
            List<String> exists2 = new LinkedList<>();
            BiConsumer<String, Doors.Users.UserPass> bic2 = (s, passValue) -> exists2.add(s);
            user.passes.forEach(bic2);
            for (String s : exists2) { //check for deleted passes
                if (!exists.contains(s)) {
                    user.passes.remove(s);
                }
            }
            for (String s : exists){ //check for incorrect inputs.
                Doors.PassValue pass = DoorGroups.passes.get(s);
                if(!exists2.contains(s) || user.passes.get(s).type != pass.passType.getInt() || (pass.passType == Doors.PassValue.type.Group && Integer.parseInt(user.passes.get(s).passValue.get(0)) > pass.groupNames.size())){
                    user.passes.put(s, new Doors.Users.UserPass(pass.passId,pass.passType == Doors.PassValue.type.Level || pass.passType == Doors.PassValue.type.Group ? Arrays.asList("0") : pass.passType == Doors.PassValue.type.Pass ? null : Arrays.asList("none") , pass.passType.getInt()));
                }
            }
        }
        DoorGroups.markDirty();
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
        for(Doors.OneDoor door : DoorGroups.doors){
            if(door.groupID != null && groups.contains(door.groupID)){ //that door is part of the group tree
                door.doorStatus = group.status;
                door.override = (Math.abs(group.status.getInt()) == 1 ? group.override : null); //because -1 and 1 values are override ones.
                pushUpdateDoors.add(door);
            }
        }
        DoorGroups.markDirty();
        //push update
        pushDoorUpdateMult(pushUpdateDoors);
    }

    public void recievedUpdate(UUID validator, Doors.OneDoor door){ //if new door settings are added from outside.
        if(checkValidator(validator)) {
            Doors.OneDoor listDoor = null;
            for (Doors.OneDoor door1 : DoorGroups.doors) {
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
                DoorGroups.markDirty();
                if(pushDoor)
                    pushDoorUpdate(listDoor);
            }
        }
    }

    public int getReaderLight(UUID id){
        for(Doors.OneDoor door : DoorGroups.doors){
            for(int i=0; i<door.Readers.size(); i++){
                if(door.Readers.get(i).equals(id)){
                    return door.readerLights;
                }
            }
        }
        return 0;
    }
    public ReaderText getReaderLabel(UUID id){
        for(Doors.OneDoor door : DoorGroups.doors){
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
        for(Doors.OneDoor door : DoorGroups.doors){
            for(int i=0; i<door.Doors.size(); i++){
                if(door.Doors.get(i).equals(id)){
                    return door.isDoorOpen != 0;
                }
            }
        }
        return false;
    }

    private boolean checkPass(Doors.OneDoor.OnePass pass, Doors.Users.UserPass user){
        AdvBaseSecurity.instance.logger.debug("Checking pass " + pass.passID);
        if(DoorGroups.passes.containsKey(pass.passID)){
            Doors.PassValue passValue = DoorGroups.passes.get(pass.passID);
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


    //region Door save file
    public static class Doors extends WorldSavedData{
        public int currentDoorVer = 0; //The version of the file. In case on the website it has been updated.

        public static final String DATA_NAME = AdvBaseSecurity.MODID + "_basesecuritydoors";
        public static Doors instance;

        public List<OneDoor> doors = new LinkedList<>();
        public HashMap<String, PassValue> passes = new HashMap<>();
        public HashMap<UUID, Groups> groups = new HashMap<>();
        public List<Users> users = new LinkedList<>();

        public Doors(){
            super(DATA_NAME);
        }
        public Doors(String str){
            super(str);
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
            PassValue tempPass = new PassValue("staff");
            tempPass.passName = "Staff";
            tempPass.passType = PassValue.type.Pass;
            tempPass.groupNames = null;
            passes.put("staff", tempPass);
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
}
