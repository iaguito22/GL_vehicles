package com.gl.vehicles.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WheelItem extends Item {
    private final float grip;

    public WheelItem(Settings settings, float grip) {
        super(settings.maxCount(1));
        this.grip = grip;
    }

    public float getGrip() {
        return grip;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.gl_vehicles.tooltip.type", Text.translatable("item.gl_vehicles.type.wheels")));
        
        int gripLevel = (int)(grip * 5);
        tooltip.add(Text.translatable("item.gl_vehicles.tooltip.stat", String.valueOf(gripLevel), Text.translatable("item.gl_vehicles.stat.grip")));

        if (this == ModItems.TRACTOR_WHEELS) {
            tooltip.add(Text.translatable("item.gl_vehicles.tooltip.lore_offroad_wheels"));
        } else if (this == ModItems.SPORT_WHEELS) {
            tooltip.add(Text.translatable("item.gl_vehicles.tooltip.lore_racing_wheels"));
        }
        
        if (stack.hasNbt() && stack.getNbt().contains("Wear")) {
            float wear = stack.getNbt().getFloat("Wear") * 100;
            tooltip.add(Text.literal("Desgaste: ").append(Text.literal(String.format("%.1f%%", wear)).formatted(Formatting.RED)));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
