package com.woodenscalpel.client.render;

import com.woodenscalpel.common.multiblockentity.BaseEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;

public class BaseEntityRenderer extends EntityRenderer<BaseEntity> {
    public BaseEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(BaseEntity entity) {
        return InventoryMenu.BLOCK_ATLAS; //TODO random placeholder
    }

}
