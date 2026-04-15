package com.gl.vehicles.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

public class KartEntity extends AbstractVehicleEntity {

    public KartEntity(EntityType<? extends KartEntity> type, World world) {
        super(type, world, 2); // 2 slots: Motor and Ruedas
    }

    @Override
    public ItemStack getEngineStack() {
        return this.inventory.getStack(0); // Slot 0: Motor
    }

    @Override
    public ItemStack getWheelStack() {
        return this.inventory.getStack(1); // Slot 1: Ruedas
    }

    @Override
    public float getBaseWeight() {
        return 40.0f; // Más ligero para mayor agilidad
    }

    @Override
    public void calculateStats() {
        super.calculateStats();
        // El Kart ya es ligero de base, así que solo le damos un toque extra de nervio
        if (!getEngineStack().isEmpty() && !getWheelStack().isEmpty()) {
            this.accelerationStat *= 1.3f; // Un poco más de aceleración extra
            this.maxSpeed *= 1.2f; // Un poco más de punta extra
            this.setAerodynamics(0.8f); 
            this.grip *= 1.05f; 
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (Math.abs(forwardSpeed) > 0.01) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kart.move"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kart.idle"));
        }));
    }

    @Override
    public double getMountedHeightOffset() {
        return 0.15D; // Bajado 3 píxeles respecto al base (0.34 - 0.19)
    }
}
