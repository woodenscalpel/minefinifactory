package com.woodenscalpel.common.mastertick;

import com.woodenscalpel.Minefinifactory;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class MasterTick {
   public static int tickcount;
   public static final int TICKSPERBLOCK = 6;

   public static void incTickcount(){
       tickcount++;
   }

   public static int getTickcount(MinecraftServer server){
       return server.getTickCount();
   }

   public static boolean isMasterTick(MinecraftServer server){
       return getTickcount(server) % TICKSPERBLOCK == 0;
   }

   public static boolean isPhase1(MinecraftServer server){ return getTickcount(server) % TICKSPERBLOCK == 1;}
    public static boolean isPhase2(MinecraftServer server){return getTickcount(server) % TICKSPERBLOCK == 2;}


   /*
   public static void registerMasterTickEvent(){
       //TickEvent.ServerLevelTick.SERVER_LEVEL_PRE.register((ServerLevel level) -> {incTickcount();});
       TickEvent.ServerLevelTick.SERVER_PRE.register((MinecraftServer server) -> {incTickcount();});
   }

    */
}
