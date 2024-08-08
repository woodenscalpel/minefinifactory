package com.woodenscalpel.common.mastertick;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerLevel;

public class MasterTick {
   public static int tickcount;
   public static final int TICKSPERBLOCK = 5;

   public void incTickcount(){
       tickcount++;
   }

   public static boolean isMasterTick(){
       return tickcount % TICKSPERBLOCK == 0;
   }

   public static void registerMasterTickEvent(){
       TickEvent.ServerLevelTick.SERVER_LEVEL_PRE.register((ServerLevel level) -> {tickcount++;});
   }
}
