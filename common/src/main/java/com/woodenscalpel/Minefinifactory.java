package com.woodenscalpel;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import com.woodenscalpel.common.blocks.EmitterBlock;
import com.woodenscalpel.common.blocks.pusher.PusherBaseBlock;
import com.woodenscalpel.common.blocks.pusher.PusherHeadBlock;
import com.woodenscalpel.common.init.BlockInit;
import com.woodenscalpel.common.init.EntityInit;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;

import java.util.function.Supplier;

public final class Minefinifactory {
    public static final String MOD_ID = "minefinifactory";
    public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

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

        //Register items
        Registrar<Item> items = MANAGER.get().get(Registries.ITEM);

        //Register Blocks
        BlockInit.register();
        //Registrar<Block> blocks = MANAGER.get().get(Registries.BLOCK);
        //RegistrySupplier<Block> emitterBlock = blocks.register(new ResourceLocation(MOD_ID, "emitterblock"), () -> new EmitterBlock(BlockBehaviour.Properties.copy(Blocks.STONE)));
        //pusher
        //RegistrySupplier<Block> pusherBaseBlock = blocks.register(new ResourceLocation(MOD_ID, "pusherblock"), () -> new PusherBaseBlock(false,BlockBehaviour.Properties.copy(Blocks.PISTON)));
        //RegistrySupplier<Block> pusherHeadBlock = blocks.register(new ResourceLocation(MOD_ID, "pusherheadblock"), () -> new PusherHeadBlock(BlockBehaviour.Properties.copy(Blocks.PISTON_HEAD)));

        //RegistrySupplier<BlockItem> emitterBlockItem = items.register(new ResourceLocation(MOD_ID +"testblockitem"), () -> new BlockItem(emitterBlock.get(),new Item.Properties().arch$tab(MINEFINIFACTORY_TAB)));
        //RegistrySupplier<BlockItem> pusherBlockItem = items.register(new ResourceLocation(MOD_ID +"pusherblockitem"), () -> new BlockItem(pusherBaseBlock.get(),new Item.Properties().arch$tab(MINEFINIFACTORY_TAB)));

        //Register Entities
        EntityInit.register();

    }
}
