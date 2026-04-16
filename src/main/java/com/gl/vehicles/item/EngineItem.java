package com.gl.vehicles.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EngineItem extends Item {
    private final float speedMultiplier;
    private final float fuelConsumption;
    private final float basePitch;
    private final float maxPitch;
    private final float baseVolume;
    private final float wearMultiplier;
    private final net.minecraft.sound.SoundEvent engineSound;

    public EngineItem(Settings settings, float speedMultiplier, float fuelConsumption, float basePitch, float maxPitch, float baseVolume, float wearMultiplier, net.minecraft.sound.SoundEvent engineSound) {
        super(settings.maxCount(1));
        this.speedMultiplier = speedMultiplier;
        this.fuelConsumption = fuelConsumption;
        this.basePitch = basePitch;
        this.maxPitch = maxPitch;
        this.baseVolume = baseVolume;
        this.wearMultiplier = wearMultiplier;
        this.engineSound = engineSound;
    }

    public EngineItem(Settings settings, float speedMultiplier, float fuelConsumption, float basePitch, float maxPitch, float baseVolume, net.minecraft.sound.SoundEvent engineSound) {
        this(settings, speedMultiplier, fuelConsumption, basePitch, maxPitch, baseVolume, 1.0f, engineSound);
    }

    public float getBaseVolume() {
        return baseVolume;
    }

    public net.minecraft.sound.SoundEvent getEngineSound() {
        return engineSound;
    }

    public float getWearMultiplier() {
        return wearMultiplier;
    }

    public float getBasePitch() {
        return basePitch;
    }

    public float getMaxPitch() {
        return maxPitch;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public float getFuelConsumption() {
        return fuelConsumption;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.gl_vehicles.tooltip.type", Text.translatable("item.gl_vehicles.type.engine")));
        
        int displayPower = (int)(speedMultiplier * 110);
        tooltip.add(Text.translatable("item.gl_vehicles.tooltip.stat", String.valueOf(displayPower), Text.translatable("item.gl_vehicles.stat.power")));
        
        if (this == ModItems.ENGINE_1_9TDI) {
            tooltip.add(Text.translatable("item.gl_vehicles.tooltip.lore_engine_tdi"));
        } else if (this == ModItems.ENGINE_V6 || this == ModItems.ENGINE_V12) {
            tooltip.add(Text.translatable("item.gl_vehicles.tooltip.lore_engine_sport"));
        }

        if (stack.hasNbt() && stack.getNbt().contains("Wear")) {
            float wear = stack.getNbt().getFloat("Wear") * 100;
            tooltip.add(Text.literal("Desgaste: ").append(Text.literal(String.format("%.1f%%", wear)).formatted(Formatting.RED)));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
