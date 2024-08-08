package com.woodenscalpel.client.render.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.woodenscalpel.common.blockentity.DestroyerBlockEntity;
import com.woodenscalpel.common.blockentity.WelderBlockEntity;
import com.woodenscalpel.common.blocks.DestroyerBlock;
import com.woodenscalpel.common.blocks.WelderBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class DestroyerBER implements BlockEntityRenderer<DestroyerBlockEntity> {
    private final BlockEntityRendererProvider.Context context;
    public DestroyerBER(BlockEntityRendererProvider.Context ctx){
        this.context = ctx;
    }

    @Override
    public void render(DestroyerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {


        Vec3 campos = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();

        Direction facing = blockEntity.getBlockState().getValue(DestroyerBlock.FACING);
        BlockPos pos = blockEntity.getBlockPos();



            RenderSystem.setShaderColor(1F,0.2F,0.2F,0.5F);
            poseStack.pushPose();
            poseStack.translate(facing.getNormal().getX(),facing.getNormal().getY(),facing.getNormal().getZ());
            context.getBlockRenderDispatcher().renderSingleBlock(Blocks.RED_STAINED_GLASS.defaultBlockState(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

            poseStack.popPose();

    }
}
