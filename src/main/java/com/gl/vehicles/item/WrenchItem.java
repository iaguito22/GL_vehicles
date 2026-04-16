package com.gl.vehicles.item;

import com.gl.vehicles.entity.AbstractVehicleEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class WrenchItem extends Item {
    public WrenchItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.gl_vehicles.tooltip.type", Text.translatable("item.gl_vehicles.type.tool")));
        tooltip.add(Text.translatable("item.gl_vehicles.tooltip.lore_wrench"));
        super.appendTooltip(stack, world, tooltip, context);
    }

}
