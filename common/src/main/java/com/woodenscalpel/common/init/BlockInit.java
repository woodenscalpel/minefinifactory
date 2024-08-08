package com.woodenscalpel.common.init;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.client.render.blockentity.WelderBER;
import com.woodenscalpel.common.blockentity.ConveyorBlockEntity;
import com.woodenscalpel.common.blockentity.DestroyerBlockEntity;
import com.woodenscalpel.common.blockentity.WelderBlockEntity;
import com.woodenscalpel.common.blocks.ConveyorBlock;
import com.woodenscalpel.common.blocks.DestroyerBlock;
import com.woodenscalpel.common.blocks.EmitterBlock;
import com.woodenscalpel.common.blocks.WelderBlock;
import com.woodenscalpel.common.blocks.pusher.PusherBaseBlock;
import com.woodenscalpel.common.blocks.pusher.PusherHeadBlock;
import com.woodenscalpel.common.multiblockentity.BaseEntity;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static com.woodenscalpel.Minefinifactory.*;

public class BlockInit {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITIES = DeferredRegister.create(MOD_ID, Registries.BLOCK_ENTITY_TYPE);


    public static final RegistrySupplier<Block> emitterBlock = BLOCKS.register(new ResourceLocation(MOD_ID, "emitterblock"), () -> new EmitterBlock(BlockBehaviour.Properties.copy(Blocks.STONE)));
    public static final RegistrySupplier<Block> conveyorBlock = BLOCKS.register(new ResourceLocation(MOD_ID, "conveyorblock"), () -> new ConveyorBlock(BlockBehaviour.Properties.copy(Blocks.STONE)));
    public static final RegistrySupplier<Block> welderBlock = BLOCKS.register(new ResourceLocation(MOD_ID, "welderblock"), () -> new WelderBlock(BlockBehaviour.Properties.copy(Blocks.STONE)));
    public static final RegistrySupplier<Block> destroyerBlock = BLOCKS.register(new ResourceLocation(MOD_ID, "destroyerblock"), () -> new DestroyerBlock(BlockBehaviour.Properties.copy(Blocks.STONE)));

    public static final RegistrySupplier<Block> pusherBaseBlock = BLOCKS.register(new ResourceLocation(MOD_ID, "pusherblock"), () -> new PusherBaseBlock(false,BlockBehaviour.Properties.copy(Blocks.PISTON)));
    public static final RegistrySupplier<Block> pusherHeadBlock = BLOCKS.register(new ResourceLocation(MOD_ID, "pusherheadblock"), () -> new PusherHeadBlock(BlockBehaviour.Properties.copy(Blocks.PISTON_HEAD)));

    public static final RegistrySupplier<BlockItem> emitterBlockItem = Minefinifactory.ITEMS.register(new ResourceLocation(MOD_ID ,"emitterblockitem"), () -> new BlockItem(emitterBlock.get(),new Item.Properties().arch$tab(MINEFINIFACTORY_TAB)));
    public static final RegistrySupplier<BlockItem> conveyorBlockItem = Minefinifactory.ITEMS.register(new ResourceLocation(MOD_ID ,"conveyorblockitem"), () -> new BlockItem(conveyorBlock.get(),new Item.Properties().arch$tab(MINEFINIFACTORY_TAB)));
    public static final RegistrySupplier<BlockItem> welderBlockItem = Minefinifactory.ITEMS.register(new ResourceLocation(MOD_ID ,"welderblockitem"), () -> new BlockItem(welderBlock.get(),new Item.Properties().arch$tab(MINEFINIFACTORY_TAB)));
    public static final RegistrySupplier<BlockItem> destroyerBlockItem = Minefinifactory.ITEMS.register(new ResourceLocation(MOD_ID ,"destroyerblockitem"), () -> new BlockItem(destroyerBlock.get(),new Item.Properties().arch$tab(MINEFINIFACTORY_TAB)));
    public static final RegistrySupplier<BlockItem> pusherBlockItem = ITEMS.register(new ResourceLocation(MOD_ID +"pusherblockitem"), () -> new BlockItem(pusherBaseBlock.get(),new Item.Properties().arch$tab(MINEFINIFACTORY_TAB)));

    public static final RegistrySupplier<BlockEntityType<ConveyorBlockEntity>> conveyorBlockEntity = BLOCKENTITIES.register(new ResourceLocation(MOD_ID , "conveyorblockentity"),
            () -> BlockEntityType.Builder.<ConveyorBlockEntity>of(ConveyorBlockEntity::new,conveyorBlock.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<WelderBlockEntity>> welderBlockEntity = BLOCKENTITIES.register(new ResourceLocation(MOD_ID , "welderblockentity"),
            () -> BlockEntityType.Builder.<WelderBlockEntity>of(WelderBlockEntity::new,welderBlock.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<DestroyerBlockEntity>> destroyerBlockEntity = BLOCKENTITIES.register(new ResourceLocation(MOD_ID , "destroyerblockentity"),
            () -> BlockEntityType.Builder.<DestroyerBlockEntity>of(DestroyerBlockEntity::new,destroyerBlock.get()).build(null));


    public static void register(){
        BLOCKS.register();
        //FORGITEMS.register();
        BLOCKENTITIES.register();

    }
}
