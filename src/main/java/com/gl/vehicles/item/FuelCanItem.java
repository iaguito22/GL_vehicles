package com.gl.vehicles.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.util.List;

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
        nbt.putFloat("Fuel", 50.0f); // Lleno por defecto (50L)
        stack.setNbt(nbt);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
        if (!world.isClient && !stack.hasNbt()) {
            setFuel(stack, capacity); // Llenar al entrar en el inventario por primera vez
        }
    }

    public float getFuel(ItemStack stack) {
        if (!stack.hasNbt()) return capacity; // Si no tiene NBT, asumimos que está lleno
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

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.gl_vehicles.tooltip.type", Text.translatable("item.gl_vehicles.type.fuel")));
        tooltip.add(Text.translatable("item.gl_vehicles.tooltip.lore_fuel_can"));
        
        float fuel = getFuel(stack);
        tooltip.add(Text.literal("Fuel: ").append(Text.literal(String.format("%.1f", fuel) + "L").formatted(Formatting.AQUA)));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
