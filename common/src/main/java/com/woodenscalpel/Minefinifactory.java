package com.woodenscalpel;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.FunctionType;
import com.mojang.logging.LogUtils;
import com.woodenscalpel.common.blocks.EmitterBlock;
import com.woodenscalpel.common.blocks.multiblockutil.MultiblockBreakEvent;
import com.woodenscalpel.common.blocks.multiblockutil.MultiblockPlaceEvent;
import com.woodenscalpel.common.blocks.pusher.PusherBaseBlock;
import com.woodenscalpel.common.blocks.pusher.PusherHeadBlock;
import com.woodenscalpel.common.init.BlockInit;
import com.woodenscalpel.common.init.ClientInit;
import com.woodenscalpel.common.init.EntityInit;
import com.woodenscalpel.common.mastertick.MasterTick;
import com.woodenscalpel.common.network.EntityDataListSerializer;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.value.IntValue;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
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


    public static void init() {
        // Write common init code here.

        //Register Creative Tab
        TABS.register();

        //Register Blocks
        BlockInit.register();

        //Register items
        ITEMS.register();


        //Register Entities
        EntityInit.register();


        //Register Events
        MasterTick.registerMasterTickEvent();

        BlockEvent.BREAK.register( (Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) ->
                MultiblockBreakEvent.onBreak(level, pos, state, player, xp)
        );

        BlockEvent.PLACE.register((Level level, BlockPos pos, BlockState state, @Nullable Entity placer) ->
                MultiblockPlaceEvent.onPlace(level,pos,state,placer));


        //Networking
        EntityDataSerializers.registerSerializer(EntityDataListSerializer.BLOCKTUPLE);


        //Client renderer
        //todo EnvExecutor?
        if(Platform.getEnv() == EnvType.CLIENT) {
            ClientLifecycleEvent.CLIENT_SETUP.register(ClientInit::register);
        }

    }
}
