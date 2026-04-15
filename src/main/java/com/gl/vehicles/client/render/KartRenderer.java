package com.gl.vehicles.client.render;

import com.gl.vehicles.client.model.KartModel;
import com.gl.vehicles.entity.AbstractVehicleEntity;
import com.gl.vehicles.entity.KartEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KartRenderer extends GeoEntityRenderer<KartEntity> {

    public KartRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new KartModel());
    }

    @Override
    public void render(KartEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        poseStack.push();
        float yaw = entity.getDataTracker().get(AbstractVehicleEntity.SYNC_YAW);
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
        super.render(entity, 0, partialTick, poseStack, bufferSource, packedLight);
        poseStack.pop();
    }
}
