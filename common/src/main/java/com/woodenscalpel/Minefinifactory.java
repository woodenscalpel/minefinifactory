package com.woodenscalpel;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.FunctionType;
import com.mojang.logging.LogUtils;
import com.woodenscalpel.common.blocks.EmitterBlock;
import com.woodenscalpel.common.blocks.pusher.PusherBaseBlock;
import com.woodenscalpel.common.blocks.pusher.PusherHeadBlock;
import com.woodenscalpel.common.init.BlockInit;
import com.woodenscalpel.common.init.EntityInit;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;

import java.util.function.Supplier;

public final class Minefinifactory {
    public static final String MOD_ID = "minefinifactory";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> MINEFINIFACTORY_TAB = TABS.register(
            "minefinifactory_tab", // Tab ID
            () -> CreativeTabRegistry.create(
                    Component.translatable("category.minefinifactory"), // Tab Name
                    Items.STONE::getDefaultInstance  // Icon
            )
    );

    public static final Logger LOGGER = LogUtils.getLogger();

    public static int mastertickcount = 0;
    public static final int TICKSPERBLOCK = 5;


    public static void init() {
        // Write common init code here.

        //Register Creative Tab
        TABS.register();

        //Register Blocks
        BlockInit.register();

        //Register items
        //Registrar<Item> items = MANAGER.get().get(Registries.ITEM);
        ITEMS.register();


        //Register Entities
        EntityInit.register();


        //Register Events
        mastertickcount = 0;
        TickEvent.ServerLevelTick.SERVER_LEVEL_PRE.register((ServerLevel level) -> {mastertickcount++;
        });

    }
}
