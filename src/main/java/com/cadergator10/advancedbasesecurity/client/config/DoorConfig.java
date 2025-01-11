package com.cadergator10.advancedbasesecurity.client.config;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.Mod;

@Config(modid = AdvBaseSecurity.MODID, category = "door", name = "Door Settings")
@Mod.EventBusSubscriber(modid = AdvBaseSecurity.MODID)
public class DoorConfig {
    @Config.Name("Cache Time")
    @Config.LangKey("advancedbasesecurity.gui.config.door.cache")
    @Config.Comment("How long a user is saved in the cache of a door? 0 disables the cache")
    @Config.RequiresMcRestart
    public static int cachetime = 30;
    @Config.Name("Max Managers")
    @Config.LangKey("advancedbasesecurity.gui.config.door.managerLimit")
    @Config.Comment("How many managers one user may have? Just prevents abuse by users just in case (as they cannot be deleted without editing nbt)")
    public static int managerLimit = 1;
}
