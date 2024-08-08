package com.woodenscalpel.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.woodenscalpel.common.multiblockentity.BaseEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BaseEntityRenderer extends EntityRenderer<BaseEntity> {
    private final EntityRendererProvider.Context context;
    public BaseEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public ResourceLocation getTextureLocation(BaseEntity entity) {
        return InventoryMenu.BLOCK_ATLAS; //TODO random placeholder
    }

    @Override
    public void render(BaseEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        List<Tuple<Vec3i,BlockState>> blocks = entity.getBlocks();
        if(blocks == null || blocks.isEmpty()){return;}
        if(entity.isNotPlaced(blocks)) { //TODO checking isIdle is better (doesnt query level state) but doesnt work due to client server sync issues (i think)
            Level level = entity.level();

            for (int i = 0; i < blocks.size(); i++) {
                BlockPos absPos = new BlockPos(entity.getBasePos().offset(blocks.get(i).getA()));
                Vec3i relpos = blocks.get(i).getA();
                BlockState state = blocks.get(i).getB();
                //renderBlockAt(poseStack,buffer2,state,absPos,packedLight);
                poseStack.pushPose();
                poseStack.translate(relpos.getX(), relpos.getY(), relpos.getZ());


                context.getBlockRenderDispatcher().renderSingleBlock(state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();

            }
        }
        //super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderBlockAt(PoseStack ms, VertexConsumer buffer, BlockState state, BlockPos pos,int packedlight) {
        double renderPosX = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition().x();
        double renderPosY = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition().y();
        double renderPosZ = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition().z();

        ms.pushPose();
        ms.translate(-renderPosX, -renderPosY, -renderPosZ);

        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        ms.translate(pos.getX(), pos.getY(), pos.getZ());
        BakedModel model = brd.getBlockModel(state);
        int color = Minecraft.getInstance().getBlockColors().getColor(state, null, null, 0);
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        // always use entity translucent layer so blending is turned on
        brd.getModelRenderer().renderModel(ms.last(), buffer, state, model, r, g, b, packedlight, OverlayTexture.NO_OVERLAY);

        ms.popPose();
    }

}
