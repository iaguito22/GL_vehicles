package com.gl.vehicles.client.render;

import com.gl.vehicles.entity.TractorEntity;
import com.gl.vehicles.entity.AbstractVehicleEntity;
import com.gl.vehicles.client.model.TractorEntityModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class TractorEntityRenderer extends GeoEntityRenderer<TractorEntity> {

    public TractorEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new TractorEntityModel());

        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Override
            protected ItemStack getStackForBone(GeoBone bone, TractorEntity animatable) {
                String name = bone.getName();
                // Slot 0: Acc Frontal
                if (name.equals("attach_front")) return animatable.getInventory().getStack(0);
                // Slot 1: Motor
                if (name.equals("engine")) return animatable.getInventory().getStack(1);
                // Slot 2: Ruedas
                if (name.startsWith("wheel_")) return animatable.getInventory().getStack(2);
                // Slot 3: Acc Trasero
                if (name.equals("attach_rear")) return animatable.getInventory().getStack(3);
                return null;
            }
        });
    }

    @Override
    public void render(TractorEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        poseStack.push();
        float yaw = entity.getDataTracker().get(AbstractVehicleEntity.SYNC_YAW);
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
        super.render(entity, 0, partialTick, poseStack, bufferSource, packedLight);
        poseStack.pop();
    }

    @Override
    public void actuallyRender(MatrixStack poseStack, TractorEntity animatable, BakedGeoModel model, net.minecraft.client.render.RenderLayer renderType, VertexConsumerProvider bufferSource, net.minecraft.client.render.VertexConsumer buffer, boolean isRebind, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        int color = animatable.getColor();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        GeoBone front = model.getBone("attach_front").orElse(null);
        if (front != null) {
            front.setHidden(!animatable.getInventory().getStack(0).isOf(com.gl.vehicles.item.ModItems.HARVESTER));
        }

        GeoBone rear = model.getBone("attach_rear").orElse(null);
        if (rear != null) {
            net.minecraft.item.Item rearItem = animatable.getInventory().getStack(3).getItem();
            rear.setHidden(rearItem != com.gl.vehicles.item.ModItems.TRAILER && rearItem != com.gl.vehicles.item.ModItems.SEEDER);
        }

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isRebind, partialTick, packedLight, packedOverlay, r, g, b, alpha);
    }
}
