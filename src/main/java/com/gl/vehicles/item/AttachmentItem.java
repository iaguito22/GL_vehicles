package com.gl.vehicles.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class AttachmentItem extends Item {
    public enum AttachmentType {
        FRONT, REAR
    }

    private final AttachmentType type;

    public AttachmentItem(Settings settings, AttachmentType type) {
        super(settings);
        this.type = type;
    }

    public AttachmentType getAttachmentType() {
        return type;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.gl_vehicles.tooltip.type", Text.translatable("item.gl_vehicles.type.attachment")));
        
        if (this == ModItems.HARVESTER) {
            tooltip.add(Text.translatable("item.gl_vehicles.tooltip.lore_harvester"));
        } else if (this == ModItems.SEEDER) {
            tooltip.add(Text.translatable("item.gl_vehicles.tooltip.lore_seeder"));
        }
        
        super.appendTooltip(stack, world, tooltip, context);
    }
}
