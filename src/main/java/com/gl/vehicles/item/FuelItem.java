package com.gl.vehicles.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class FuelItem extends Item {
    private final float fuelValue;

    public FuelItem(Settings settings, float fuelValue) {
        super(settings);
        this.fuelValue = fuelValue;
    }

    public float getFuelValue() {
        return fuelValue;
    }
}
