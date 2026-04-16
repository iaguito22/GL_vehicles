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
    private final float wearMultiplier;

    public EngineItem(Settings settings, float speedMultiplier, float fuelConsumption, float basePitch, float maxPitch, float wearMultiplier) {
        super(settings.maxCount(1));
        this.speedMultiplier = speedMultiplier;
        this.fuelConsumption = fuelConsumption;
        this.basePitch = basePitch;
        this.maxPitch = maxPitch;
        this.wearMultiplier = wearMultiplier;
    }

    public EngineItem(Settings settings, float speedMultiplier, float fuelConsumption, float basePitch, float maxPitch) {
        this(settings, speedMultiplier, fuelConsumption, basePitch, maxPitch, 1.0f);
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
