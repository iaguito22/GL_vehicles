package com.gl.vehicles.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class FuelCanItem extends Item {
    private final float capacity;

    public FuelCanItem(Settings settings, float capacity) {
        super(settings);
        this.capacity = capacity;
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        NbtCompound nbt = new NbtCompound();
        nbt.putFloat("Fuel", 0.0f);
        stack.setNbt(nbt);
        return stack;
    }

    public float getFuel(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null ? nbt.getFloat("Fuel") : 0.0f;
    }

    public void setFuel(ItemStack stack, float fuel) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putFloat("Fuel", Math.max(0, Math.min(fuel, capacity)));
    }

    public float getCapacity() {
        return capacity;
    }
}
