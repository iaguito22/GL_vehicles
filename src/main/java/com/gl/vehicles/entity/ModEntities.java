package com.gl.vehicles.entity;

import com.gl.vehicles.GLVehicles;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<TractorEntity> TRACTOR = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(GLVehicles.MOD_ID, "tractor"),
            FabricEntityTypeBuilder.<TractorEntity>create(SpawnGroup.MISC, TractorEntity::new)
                    .dimensions(EntityDimensions.fixed(1.5f, 1.5f))
                    .trackRangeBlocks(10)
                    .trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<KartEntity> KART = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(GLVehicles.MOD_ID, "kart"),
            FabricEntityTypeBuilder.<KartEntity>create(SpawnGroup.MISC, KartEntity::new)
                    .dimensions(EntityDimensions.fixed(1.2f, 0.8f))
                    .trackRangeBlocks(10)
                    .trackedUpdateRate(10)
                    .build()
    );

    public static void registerEntities() {
        GLVehicles.LOGGER.info("Registering Entities for " + GLVehicles.MOD_ID);
    }
}
