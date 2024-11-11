package com.cadergator10.advancedbasesecurity.client.config;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.Mod;

@Config(modid = AdvBaseSecurity.MODID, category = "general")
@Mod.EventBusSubscriber(modid = AdvBaseSecurity.MODID)
public class GeneralConfig {
    @Config.Name("debug")
    @Config.LangKey("advancedbasesecurity.gui.config.general.debug")
    @Config.Comment("Whether to send debug messages to players?")
    @Config.RequiresMcRestart
    public static boolean debug = false;
}
