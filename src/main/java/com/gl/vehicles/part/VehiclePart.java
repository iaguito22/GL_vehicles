package com.gl.vehicles.part;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class VehiclePart extends Item {
    public VehiclePart(Settings settings) {
        super(settings);
    }

    public abstract float getSpeedMultiplier(ItemStack stack);
    public abstract float getFuelConsumptionRate(ItemStack stack);
    public abstract float getTraction(ItemStack stack);
}
