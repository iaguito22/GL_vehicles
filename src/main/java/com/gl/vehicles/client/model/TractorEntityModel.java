package com.gl.vehicles.client.model;

import com.gl.vehicles.GLVehicles;
import com.gl.vehicles.entity.TractorEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class TractorEntityModel extends GeoModel<TractorEntity> {

    @Override
    public Identifier getModelResource(TractorEntity animatable) {
        return new Identifier(GLVehicles.MOD_ID, "geo/tractor.geo.json");
    }

    @Override
    public Identifier getTextureResource(TractorEntity animatable) {
        return new Identifier(GLVehicles.MOD_ID, "textures/entity/tractor.png");
    }

    @Override
    public Identifier getAnimationResource(TractorEntity animatable) {
        return new Identifier(GLVehicles.MOD_ID, "animations/tractor.animation.json");
    }
}
