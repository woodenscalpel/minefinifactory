package com.woodenscalpel.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(com.woodenscalpel.Minefinifactory.MOD_ID)
public final class Minefinifactory {
    public Minefinifactory() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(com.woodenscalpel.Minefinifactory.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        com.woodenscalpel.Minefinifactory.init();
    }
}
