package com.gl.vehicles.client.render;

import com.gl.vehicles.entity.TractorEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class TractorRenderer extends EntityRenderer<TractorEntity> {
    private static final Identifier TEXTURE = new Identifier("gl_vehicles", "textures/entity/tractor.png");
    private final BoatEntityModel model;

    public TractorRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new BoatEntityModel(ctx.getPart(EntityModelLayers.createBoat(net.minecraft.entity.vehicle.BoatEntity.Type.OAK)));
    }

    @Override
    public Identifier getTexture(TractorEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(TractorEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0.0, 0.375, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - yaw));
        
        // Basic scaling to make it look a bit more like a tractor size
        matrices.scale(-1.0f, -1.0f, 1.0f);
        
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(this.getTexture(entity)));
        this.model.render(matrices, vertexConsumer, light, net.minecraft.client.render.OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
        
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
