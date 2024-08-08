package com.woodenscalpel.client.render.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.blockentity.WelderBlockEntity;
import com.woodenscalpel.common.blocks.WelderBlock;
import it.unimi.dsi.fastutil.longs.LongRBTreeSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WelderBER implements BlockEntityRenderer<WelderBlockEntity> {
    private final BlockEntityRendererProvider.Context context;
    public WelderBER(BlockEntityRendererProvider.Context ctx){
        this.context = ctx;
    }

    @Override
    public void render(WelderBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {


        Vec3 campos = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();

        Direction facing = blockEntity.getBlockState().getValue(WelderBlock.FACING);
        BlockPos pos = blockEntity.getBlockPos();


        for(BlockPos c: blockEntity.connections){

            RenderSystem.setShaderColor(1F,0.2F,0.2F,0.5F);
            poseStack.pushPose();
            poseStack.translate(facing.getNormal().getX(),facing.getNormal().getY(),facing.getNormal().getZ());
            context.getBlockRenderDispatcher().renderSingleBlock(Blocks.RED_STAINED_GLASS.defaultBlockState(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

            BlockPos diff = c.subtract(new Vec3i(pos.getX(),pos.getY(),pos.getZ()));
            poseStack.translate(diff.getX(),diff.getY(),diff.getZ());
            context.getBlockRenderDispatcher().renderSingleBlock(Blocks.RED_STAINED_GLASS.defaultBlockState(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();

        }
    }
}
