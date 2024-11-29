package com.cadergator10.advancedbasesecurity.client.config;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.Mod;

@Config(modid = AdvBaseSecurity.MODID, category = "door")
@Mod.EventBusSubscriber(modid = AdvBaseSecurity.MODID)
public class DoorConfig {
    @Config.Name("Cache Time")
    @Config.LangKey("advancedbasesecurity.gui.config.door.cache")
    @Config.Comment("How long a user is saved in the cache of a door? 0 disables the cache")
    @Config.RequiresMcRestart
    public static int cachetime = 30;
}
