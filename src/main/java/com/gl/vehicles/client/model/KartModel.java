package com.gl.vehicles.client.model;

import com.gl.vehicles.GLVehicles;
import com.gl.vehicles.entity.KartEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class KartModel extends GeoModel<KartEntity> {

    @Override
    public Identifier getModelResource(KartEntity animatable) {
        return new Identifier(GLVehicles.MOD_ID, "geo/kart.geo.json");
    }

    @Override
    public Identifier getTextureResource(KartEntity animatable) {
        return new Identifier(GLVehicles.MOD_ID, "textures/entity/kart.png");
    }

    @Override
    public Identifier getAnimationResource(KartEntity animatable) {
        return new Identifier(GLVehicles.MOD_ID, "animations/kart.animation.json");
    }
}
