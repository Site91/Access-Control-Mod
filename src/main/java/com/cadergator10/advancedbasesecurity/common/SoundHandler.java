package com.cadergator10.advancedbasesecurity.common;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SoundHandler {
    public static SoundEvent lockopen;
    public static SoundEvent card_swipe;

    public static void registerSounds(){
        lockopen = registerSound("lockopen");
        card_swipe = registerSound("card-swipe");
    }

    public static SoundEvent registerSound(String soundName){
        final ResourceLocation soundID = new ResourceLocation(AdvBaseSecurity.MODID, soundName);
        return new SoundEvent(soundID).setRegistryName(soundID);
    }

    @Mod.EventBusSubscriber
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerSoundEvents(RegistryEvent.Register<SoundEvent> event) {
            event.getRegistry().registerAll(
                    lockopen,
                    card_swipe
            );
        }
    }
}
