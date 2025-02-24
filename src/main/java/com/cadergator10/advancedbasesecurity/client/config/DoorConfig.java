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
    @Config.Name("Universal card use")
    @Config.LangKey("advancedbasesecurity.gui.config.door.universalcard")
    @Config.Comment("If a user who does not own a card swipes the card, will it let them swipe it or not? true: anyone can use any card | false: only whoever's ID is linked to the card (or first swiper) can use the card")
    public static boolean universalCard = true;
    @Config.Name("Link User on first swipe")
    @Config.LangKey("advancedbasesecurity.gui.config.door.firstswipe")
    @Config.Comment("On the first swipe of a card if no user is linked, should it link that user?")
    public static boolean firstLink = false;
}
