package com.woodenscalpel.common.init;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.client.render.BaseEntityRenderer;
import com.woodenscalpel.common.multiblockentity.BaseEntity;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import static com.woodenscalpel.Minefinifactory.MOD_ID;

public class EntityInit {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<BaseEntity>> baseEntityType = ENTITIES.register(new ResourceLocation(MOD_ID+"baseentity"), () -> EntityType.Builder.<BaseEntity>of(BaseEntity::new, MobCategory.MISC).sized(1f,1f).updateInterval(1).build(new ResourceLocation(MOD_ID, "baseentity").toString()));

    public static void register(){
        ENTITIES.register();

        if(Platform.getEnv() == EnvType.CLIENT) {
            EntityRendererRegistry.register(baseEntityType, BaseEntityRenderer::new);
        }
    }
}
