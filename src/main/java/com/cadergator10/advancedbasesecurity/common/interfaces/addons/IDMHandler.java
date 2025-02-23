package com.cadergator10.advancedbasesecurity.common.interfaces.addons;

//Creates the IDoorManagers. Passed on to the DoorHandler by the addons
public interface IDMHandler {
    public IDoorManager createNew(IDoorManager.type type);
}
